package apparat.graph

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

import apparat.bytecode.operations._
import apparat.bytecode.{Marker, Bytecode, MarkerManager}

class BytecodeControlFlowGraph[V <: BlockVertex[AbstractOp]](graph: GraphLike[V], entryVertex: V, exitVertex: V) extends ControlFlowGraph[AbstractOp, V](graph, entryVertex, exitVertex) {
	override def toString = "[BytecodeControlFlowGraph]"

	// TODO exception
	lazy val bytecode = {
		import collection.mutable.ListBuffer

		val markers: MarkerManager = new MarkerManager()

		var vertexBlockMap: Map[V, ListBuffer[ControlFlowElm]] = Map.empty

		var elms: List[ListBuffer[ControlFlowElm]] = Nil

		var prevVertex: V = entryVertex

		val emptyVertex = null.asInstanceOf[V]

		def getMarkerFor(vertex: V) = {
			markers.mark(
				if (vertexBlockMap.contains(vertex))
					vertexBlockMap(vertex).head
				else
					vertex.head
				)
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

		sealed case class ExceptionMarker(from: Marker, to: Marker)

		var vertexExceptionMap: Map[V, ExceptionMarker] = Map.empty

		def patchJump(startVertex: V, endVertex: V) = {
			val marker = getMarkerFor(endVertex)
			val target = vertexBlockMap(startVertex)
			target.last match {
				case op: AbstractConditionalOp => target(target.length - 1) = Op.copyConditionalOp(op, marker)
				case op: Jump => target(target.length - 1) = Jump(marker)
				case _ => target += Jump(marker)
			}
		}

		def addBlockFor(vertex: V) = {
			val lb: ListBuffer[ControlFlowElm] = new ListBuffer()
			lb ++= vertex.block
			elms = lb :: elms
			vertexBlockMap = vertexBlockMap updated (vertex, lb)
			lb
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

		new EdgeFlowReorder(withNoEmptyJump).foreach {
			case (edge, backRefCnt) => {
				val end: V = edge.endVertex

				if (!vertexBlockMap.contains(end)) {
					val currentBlock = addBlockFor(end)

					if (backRefCnt(end) > 0) {
						// this block have at least a back edge
						// so add a Label as a first op if none exists
						currentBlock.headOption match {
							case Some(op) if (!op.isInstanceOf[Label]) => currentBlock.insert(0, Label())
							case _ => currentBlock.insert(0, Label())
						}
					}

					if (prevVertex == entryVertex)
						prevVertex = end
					else {
						edge match {
							case TrueEdge(start, end) => {
								if (prevVertex == start) {
									// need to invert the condition
									// so we can avoid a jump
									// have to found the false edge
									// and mark the target op
									outgoingOf(prevVertex).find(_.kind == EdgeKind.False) match {
										case Some(falseEdge) => {
											val marker = getMarkerFor(falseEdge.endVertex)
											val target = vertexBlockMap(start)
											target(target.length - 1) = Op.invertCopyConditionalOp(target.last, marker)
										}
										case _ => error("Missing false edge : " + edge)
									}
								} else {
									patchJump(start, end)
								}
							}
							case DefaultCaseEdge(start, end) => patchLookupSwitch(start, end, true)
							case NumberedCaseEdge(start, end, n) => patchLookupSwitch(start, end, false, n)
							case ThrowEdge(start, end) => {
								println(start, end)
								if (!vertexExceptionMap.contains(start) && isTryVertex(start)) {
									vertexExceptionMap = vertexExceptionMap.updated(start, ExceptionMarker(markers.mark(start.head), markers.mark(start.last)))
								}
								println("try:", isTryVertex(start))
								println("catch:", isCatchVertex(end))
							}
							case _ => {
								if (prevVertex != edge.startVertex) {
									patchJump(edge.startVertex, end)
								}
							}
						}
						prevVertex = end
					}
				} else {
					prevVertex = emptyVertex
					edge match {
						case DefaultCaseEdge(start, end) => patchLookupSwitch(start, end, true)
						case NumberedCaseEdge(start, end, n) => patchLookupSwitch(start, end, false, n)
						case _ => patchJump(edge.startVertex, end)
					}
				}
			}
		}

		var ops: List[ControlFlowElm] = Nil
		elms.reverse.foreach(op => ops = ops ++ op)
		new Bytecode(ops, markers, new Array(0), None)
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