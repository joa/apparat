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
 * Date: 23 juin 2010
 * Time: 16:47:11
 */
package apparat.graph

import collection.mutable.ListBuffer
import apparat.bytecode.operations._
import apparat.bytecode.{BytecodeExceptionHandler, Marker, Bytecode, MarkerManager}
import mutable.{MutableAbstractOpBlockVertex, MutableBytecodeControlFlowGraph}

object ControlFlowGraphBytecodeBuilder {
	private def toBytecode[V <: BlockVertex[AbstractOp], G <: ControlFlowGraphLike[V]](cfg: G) = {
		val newGraph = cfg.optimized

		val vertices = GraphVerticesSorter(newGraph).vertices
		val verticesWithIndex = vertices.zipWithIndex

		val verticesIndexMap = {
			var map = Map.empty[V, Int]
			verticesWithIndex.foreach {p => map = map updated (p._1, p._2)}
			map
		}

		var elms = List.empty[ListBuffer[AbstractOp]]

		var vertexBlockMap = Map.empty[V, ListBuffer[AbstractOp]]

		val markers: MarkerManager = new MarkerManager()

		def addBlockFor(vertex: V) = {
			val lb = ListBuffer.empty[AbstractOp]
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
					case Some(op) => if (!op.isInstanceOf[Label]) {
						val label = Label()
						lb.insert(0, label)
						markers.forwardMarker(op, label)
					}
					case _ => lb.insert(0, Label())
				}
			}
			lb
		}
		vertices map addBlockFor _

		sealed class TryBlock(val startIdx: Int, val catchIdx: Int, val endIdx: Int) {
			def needMarker(blockIdx: Int) = blockIdx == startIdx || blockIdx == endIdx || blockIdx == catchIdx

			override def toString() = "TryBlock(" + startIdx + "," + endIdx + "," + catchIdx + ")"
		}

		// get all catch and also finally blocks
		var catchBlocksWithIndex = {
			@inline def f(l: Iterable[Edge[V]]) = {
				l.filter(_.kind == EdgeKind.Throw).size == l.size
			}
			@inline def sortCatchBlock(x: (V, Int), y: (V, Int)) = x._2 > y._2

			// all catch block have only incoming throw edge
			// n.b. a finally is also a catch block
			verticesWithIndex.filter(vi => f(newGraph.incomingOf(vi._1))).sortWith(sortCatchBlock)
		}

		var tryCatchList = List.empty[TryBlock]

		for (catchBlock <- catchBlocksWithIndex) {
			val throws = newGraph.incomingOf(catchBlock._1).map(e => verticesIndexMap(e.startVertex)).toList.sortWith((i1: Int, i2: Int) => i1 < i2)
			tryCatchList = new TryBlock(throws.head, catchBlock._2, throws.last) :: tryCatchList
		}

		// check if a block immediately follow the other one
		@inline def inSequence(v1: V, v2: V) = {
			verticesIndexMap.get(v2) match {
				case Some(i) => (i - verticesIndexMap(v1)) == 1
				case _ => false
			}
		}

		def getMarkerFor(vertex: V) = {
			var lst = vertexBlockMap(vertex)
			if (lst.isEmpty) lst += Nop()
			markers.mark(lst.head)
		}

		def addLabelIfNeeded(from: V, to: V) {
			if (verticesIndexMap(from) > verticesIndexMap(to)) {
				val toBlock = vertexBlockMap(to)
				toBlock.headOption match {
					case Some(op) => if (!op.isInstanceOf[Label]) {
						val label = Label()
						toBlock.insert(0, label)
						markers.forwardMarker(op, label)
					}
					case _ => toBlock.insert(0, Label())
				}
			}
		}

		def patchJump(from: V, to: V, invertCondition: Boolean = false) = {
			addLabelIfNeeded(from, to)

			val marker = getMarkerFor(to)

			val target = vertexBlockMap(from)
			if (target.isEmpty)
				target += Nop()

			val oldOp = target.lastOption
			target.last match {
				case op: AbstractConditionalOp => {
					target(target.length - 1) = if (invertCondition)
						Op.invertCopyConditionalOp(op, marker)
					else
						Op.copyConditionalOp(op, marker)
				}
				case op: Jump => target(target.length - 1) = Jump(marker)
				case _ => target += Jump(marker)
			}
			oldOp.map(markers.forwardMarker(_, target.last))
		}

		def appendJump(from: V, to: V) = {
			addLabelIfNeeded(from, to)

			val marker = getMarkerFor(to)

			val target = vertexBlockMap(from)

			target += Jump(marker)
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
				lookupSwitchBuildMap = lookupSwitchBuildMap updated (startVertex, new LookupSwitchContainer(newGraph.outdegreeOf(startVertex) - 1))

			val lsContainer = lookupSwitchBuildMap(startVertex)

			if (lsContainer.addCase(startVertex, endVertex, isDefault, index)) {
				val target = vertexBlockMap(startVertex)
				val lastOp = target.lastOption
				lastOp match {
					case Some(op) if (op.opCode == Op.lookupswitch) => target(target.length - 1) = lsContainer()
					case _ => target += lsContainer()
				}
				lookupSwitchBuildMap = lookupSwitchBuildMap - startVertex
				lastOp.map(markers.forwardMarker(_, target.last))
			}
		}

		var edgesVisited = Set.empty[Edge[V]]

		vertices.foreach(
			vertex => {
				for (edge <- newGraph.outgoingOf(vertex) if (!edgesVisited.contains(edge))) {
					edgesVisited += edge
					edge match {
						case TrueEdge(start, end) => {
							if (inSequence(start, end)) {
								// need to invert the condition
								// so we can avoid a jump
								// have to found the false edge
								// and mark the target op
								newGraph.outgoingOf(start).find(_.kind == EdgeKind.False) match {
									case Some(falseEdge) => patchJump(start, falseEdge.endVertex, true)
									case _ => error("Missing false edge : " + edge)
								}
							} else {
								patchJump(start, end)
								// check if the false edge follow the block if not add a jump
								newGraph.outgoingOf(start).find(_.kind == EdgeKind.False) match {
									case Some(falseEdge) if (!inSequence(start, falseEdge.endVertex)) => {
										appendJump(start, falseEdge.endVertex)
									}
									case _ =>
								}
							}
						}
						case FalseEdge(start, end) =>
						case ThrowEdge(start, end) =>
						case DefaultCaseEdge(start, end) => patchLookupSwitch(start, end, true)
						case NumberedCaseEdge(start, end, n) => patchLookupSwitch(start, end, false, n)
						case ReturnEdge(start, end) =>
						case _ => if (!inSequence(edge.startVertex, edge.endVertex)) appendJump(edge.startVertex, edge.endVertex)
					}
				}
			})

		// mark and patch exception
		var exceptionHandlers = List.empty[BytecodeExceptionHandler]

		for (tryCatch <- tryCatchList) {
			val catchBlock = vertexBlockMap(vertices(tryCatch.catchIdx))
			val tryStartBlock = vertexBlockMap(vertices(tryCatch.startIdx))
			val tryEndBlock = vertexBlockMap(vertices(tryCatch.endIdx))
			val markerStart = markers.mark(tryStartBlock.head)

			val markerEnd = if (tryCatch.endIdx < (tryCatch.catchIdx - 1)) {
				markers.mark(tryEndBlock.last)
			} else {
				tryEndBlock.last match {
					case branch: OpWithMarker => {
						markers.mark(branch)
					}
					case _ => {
						var m = markers.mark(Label())
						val jmp = Jump(m)
						markers.forwardMarker(m.op.get, jmp)
						tryEndBlock += jmp
						markers.mark(jmp)
					}
				}
			}

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
				error("Internal error : Missing NewCatch operation")
		}

		// N.B beware of the order of the exceptionHandlers
		// most inner first
		new Bytecode(elms.reverse.flatMap(ops => ops), markers, exceptionHandlers.reverse.toArray, None)
	}

	def apply(cfg: MutableBytecodeControlFlowGraph) = {
		toBytecode[MutableAbstractOpBlockVertex, MutableBytecodeControlFlowGraph](cfg)
	}

	def apply[V <: BlockVertex[AbstractOp]](cfg: BytecodeControlFlowGraph[V]) = {
		toBytecode[V, BytecodeControlFlowGraph[V]](cfg)
	}
}