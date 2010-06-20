/*
 * This file is part of Apparat.
 *
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * User: Patrick Le Clec'h
 * Date: 31 janv. 2010
 * Time: 21:34:43
 */
package apparat.graph

import apparat.bytecode.operations._
import annotation.tailrec
import collection.mutable.ListBuffer
import apparat.bytecode.{BytecodeExceptionHandler, MarkerManager, Marker, Bytecode}
import immutable.BytecodeControlFlowGraphBuilder

class BytecodeControlFlowGraph[V <: BlockVertex[AbstractOp]](graph: GraphLike[V], entryVertex: V, exitVertex: V) extends ControlFlowGraph[AbstractOp, V](graph, entryVertex, exitVertex) {
	def this(graph: ControlFlowGraph[AbstractOp, V]) = this (graph, graph.entryVertex, graph.exitVertex)

	override def toString = "[BytecodeControlFlowGraph]"

	override def optimized = simplified

	private lazy val simplified = {
		var g = super.optimized
		var g2 = g

		def reducible(edge: E) = (edge.startVertex != entryVertex) &&
				(edge.endVertex != exitVertex) &&
				(g.outdegreeOf(edge.startVertex) == 1) &&
				(g.indegreeOf(edge.endVertex) == 1) &&
				(!g.outgoingOf(edge.endVertex).exists(_.isInstanceOf[ThrowEdge[_]]))

		// coalesce successive block A->B->C into ABC
		@tailrec def coalesce() {
			g.edgesIterator.find(reducible(_)) match {
				case Some(branch) => {
					val newVertex = (branch.startVertex ++ branch.endVertex.block).asInstanceOf[V]
					val edges = g2.outgoingOf(branch.endVertex)
					g2 = g2 - branch
					g2 = g2.replace(branch.startVertex, newVertex)
					for (edge <- edges) {
						g2 = g2 + Edge.copy(edge, Some(newVertex))
					}
					g2 = g2 - branch.endVertex
				}
				case _ =>
			}
			if (g != g2) {
				g = g2
				coalesce()
			}
		}

		coalesce()

		new BytecodeControlFlowGraph(g, entryVertex, exitVertex)
	}

	def fromBytecode(bytecode: Bytecode) = BytecodeControlFlowGraphBuilder(_)

	lazy val bytecode = {
		val newGraph = optimized

		// reordered vertices
		val vertices = DepthFirstWithOrder(newGraph).vertices.map(iv => iv.vertex)

		// map vertices to their index
		def mapList[T](l: List[T]) = {
			var map = Map.empty[T, Int]
			l.zipWithIndex.foreach {p => map = map updated (p._1, p._2)}
			map
		}
		val verticesIndexMap = mapList(vertices)

		var elms = List.empty[ListBuffer[ControlFlowElm]]

		var vertexBlockMap = Map.empty[V, ListBuffer[ControlFlowElm]]

		def addBlockFor(vertex: V) = {
			val lb = ListBuffer.empty[ControlFlowElm]
			lb ++= vertex.block
			elms = lb :: elms
			vertexBlockMap = vertexBlockMap updated (vertex, lb)

			val index = verticesIndexMap(vertex)
			val backEdge = newGraph.incomingOf(vertex).exists(e => {
				verticesIndexMap.getOrElse(e.startVertex, -1) >= index
			})
			if (backEdge) {
				// this block have at least a back edge
				// so add a Label as a first op if none exists
				lb.headOption match {
					case Some(op) => if (!op.isInstanceOf[Label]) lb.insert(0, Label())
					case _ => lb.insert(0, Label())
				}
			}
			lb
		}
		vertices map addBlockFor _

		sealed class TryBlock(val startIdx: Int, val catchIdx: Int) {
			var endIdx = startIdx

			def needMarker(blockIdx: Int) = blockIdx == startIdx || blockIdx == endIdx || blockIdx == catchIdx;
			override def toString() = "TryBlock(" + startIdx + "," + endIdx + "," + catchIdx + ")"
		}

		// get all catch and also finally blocks
		val catchBlocks = {
			def f[T](l: Iterable[T]) = {
				l.filter(_.isInstanceOf[ThrowEdge[_]]).size == l.size
			}
			// all catch block have only incoming throw edge
			// n.b. a finally is also a catch block
			vertices.filter(v => f(newGraph.incomingOf(v)))
		}

		var workList = List.empty[TryBlock]
		var tryCatchList = List.empty[TryBlock]

		def sortThrowBlock(x: (Int, Edge[V]), y: (Int, Edge[V])) = x._1 > y._1

		// adjust end block index when terminating a try/catch/finaly block
		@tailrec def terminateTryBlock(from: Int, to: Int) {
			workList.headOption match {
				case Some(head) if (head.catchIdx < to) => {
					if (from > head.catchIdx) {
						head.endIdx = to
					} else {
						head.endIdx = from
					}
					tryCatchList = head :: tryCatchList
					workList = workList.tail
					terminateTryBlock(from, to)
				}
				case _ =>
			}
		}

		for (v <- vertices) {
			val throws = newGraph.outgoingOf(v).filter(e => catchBlocks.contains(e.endVertex)).map(e => (verticesIndexMap(e.endVertex), e)).toList.sortWith(sortThrowBlock)
			if (throws.nonEmpty) {
				for (block <- throws) {
					workList.find(_.catchIdx == block._1) match {
						case Some(tb) =>
						case _ => {
							if (!tryCatchList.exists(_.catchIdx == block._1))
								workList = new TryBlock(verticesIndexMap(v), block._1) :: workList
						}
					}
				}
			} else {
				// check if we have a jump outside of a catch block
				// if so this is the end of try block
				newGraph.outgoingOf(v).map(_ match {
					case JumpEdge(startV, endV) => terminateTryBlock(verticesIndexMap(startV), verticesIndexMap(endV))
					case TrueEdge(startV, endV) => terminateTryBlock(verticesIndexMap(startV), verticesIndexMap(endV))
					case FalseEdge(startV, endV) => terminateTryBlock(verticesIndexMap(startV), verticesIndexMap(endV))
					case ReturnEdge(startV, endV) => terminateTryBlock(verticesIndexMap(startV), vertices.length)
					case _ =>
				})
			}
		}

		if (workList.nonEmpty) {
			error("Internal error : A try block have no catch")
		}

		// check if a block immediately follow the other one
		def inSequence(v1: V, v2: V) = {
			verticesIndexMap.get(v2) match {
				case Some(i) => (i - verticesIndexMap(v1)) == 1
				case _ => true
			}
		}

		val markers: MarkerManager = new MarkerManager()

		def getMarkerFor(vertex: V) = {
			markers.mark(
				vertexBlockMap(vertex).head
				)
		}

		def addLabelIfNeeded(from: V, to: V) {
			if (verticesIndexMap(from) > verticesIndexMap(to)) {
				val toBlock = vertexBlockMap(to)
				toBlock.headOption match {
					case Some(op) => if (!op.isInstanceOf[Label]) toBlock.insert(0, Label())
					case _ => toBlock.insert(0, Label())
				}
			}
		}

		def patchJump(from: V, to: V, invertCondition: Boolean = false) = {
			addLabelIfNeeded(from, to)

			val marker = getMarkerFor(to)

			val target = vertexBlockMap(from)
			target.last match {
				case op: AbstractConditionalOp => target(target.length - 1) = if (invertCondition) Op.invertCopyConditionalOp(op, marker) else Op.copyConditionalOp(op, marker)
				case op: Jump => target(target.length - 1) = Jump(marker)
				case _ => target += Jump(marker)
			}
		}

		sealed class LookupSwitchContainer(val size: Int) {
			private val cases: Array[Marker] = new Array(size)
			private var default: Option[Marker] = None
			private var caseLeft = size + 1

			def addCase(startVertex: V, endVertex: V, isDefault: Boolean = false, index: Int = -1): Boolean = {
				if (isDefault) {
					if (default == None) {
						caseLeft -= 1
						default = Some(getMarkerFor(endVertex))
					} else
						error("A LookupSwitch can't have multiple default branch")
				} else {
					caseLeft -= 1
					if (caseLeft < 0)
						error("Too many case into LookupSwitch for " + startVertex)
					else {
						if (index < 0)
							error("Case index can't be < 0")
						else if (index >= cases.length) {
							error("Case index out of bounds : " + index)
						} else
							cases(index) = getMarkerFor(endVertex)
					}
				}
				isDone
			}

			def isDone = caseLeft == 0

			def apply(): LookupSwitch = if (!isDone) error("Incomplete LookupSwitch") else LookupSwitch(default.get, cases)
		}

		// hold a LookupSwitchContainer while building it
		var lookupSwitchBuildMap: Map[V, LookupSwitchContainer] = Map.empty

		def patchLookupSwitch(startVertex: V, endVertex: V, isDefault: Boolean = false, index: Int = -1) = {
			if (!lookupSwitchBuildMap.contains(startVertex))
				lookupSwitchBuildMap = lookupSwitchBuildMap updated (startVertex, new LookupSwitchContainer(outdegreeOf(startVertex) - 1))

			val lsContainer = lookupSwitchBuildMap(startVertex)

			if (lsContainer.addCase(startVertex, endVertex, isDefault, index)) {
				val target = vertexBlockMap(startVertex)
				target.last match {
					case op: LookupSwitch => target(target.length - 1) = lsContainer()
					case _ => target += lsContainer()
				}
				lookupSwitchBuildMap = lookupSwitchBuildMap - startVertex
			}
		}

		vertices.foreach(
			vertex => {
				newGraph.outgoingOf(vertex).foreach({
					edge => edge match {
						case TrueEdge(start, end) => {
							if (inSequence(start, end)) {
								// need to invert the condition
								// so we can avoid a jump
								// have to found the false edge
								// and mark the target op
								newGraph.outgoingOf(start).find(_.kind == EdgeKind.False) match {
									case Some(falseEdge) => {
										patchJump(start, falseEdge.endVertex, true)
									}
									case _ => error("Missing false edge : " + edge)
								}
							} else {
								patchJump(start, end)
							}
						}
						case FalseEdge(start, end) => if (!inSequence(start, end)) patchJump(start, end)
						case ThrowEdge(start, end) =>
						case DefaultCaseEdge(start, end) => patchLookupSwitch(start, end, true)
						case NumberedCaseEdge(start, end, n) => patchLookupSwitch(start, end, false, n)
						case _ => if (!inSequence(edge.startVertex, edge.endVertex)) patchJump(edge.startVertex, edge.endVertex)
					}
				})
			})


		// mark and patch exception
		var exceptionHandlers = List.empty[BytecodeExceptionHandler]

		for (tryCatch <- tryCatchList) {
			val catchBlock = vertexBlockMap(vertices(tryCatch.catchIdx))
			val markerStart = markers.mark(vertexBlockMap(vertices(tryCatch.startIdx)).head)
			val markerEnd = markers.mark(vertexBlockMap(vertices(tryCatch.endIdx)).last)
			val markerCatch = markers.mark(catchBlock.head)

			//patch newCatch
			val opIndex = catchBlock.indexWhere(_.isInstanceOf[NewCatch])
			if (opIndex >= 0) {
				val op = catchBlock(opIndex)
				val exceptionHandler = op.asInstanceOf[NewCatch].exceptionHandler
				val newExceptionHandler = new BytecodeExceptionHandler(markerStart, markerEnd, markerCatch, exceptionHandler.typeName, exceptionHandler.varName)
				catchBlock(opIndex) = NewCatch(newExceptionHandler)
				exceptionHandlers = newExceptionHandler :: exceptionHandlers
			} else
				error("Internal error : Missing new catch op")
		}
		new Bytecode(elms.reverse.flatMap(ops => ops), markers, exceptionHandlers.toArray, None)
	}

	override def edgeToString(edge: E) = {
		def headLabel(vertex: V) = " headlabel=\"" + ({
			if (vertex.length == 0)
				""
			else {
				vertex.last match {
					case op: OpWithMarker => op.marker.toString
					case _ => ""
				}
			}
		}) + "\""

		"[" + (edge match {
			case DefaultEdge(x, y) => label("")
			case JumpEdge(x, y) => label("  jump  ")
			case TrueEdge(x, y) => label("  true  ")
			case FalseEdge(x, y) => label("  false  ")
			case DefaultCaseEdge(x, y) => label("  default  ")
			case CaseEdge(x, y) => label("  case  ")
			case NumberedCaseEdge(x, y, n) => label("  case " + n)
			case ThrowEdge(x, y) => label("  throw  ") + " style=\"dashed\""
			case ReturnEdge(x, y) => label("  return  ")
		}) + "]"
	}
}

