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
import apparat.bytecode.{Bytecode, MarkerManager}

class BytecodeControlFlowGraph[V <: BlockVertex[AbstractOp]](graph: GraphLike[V], entryVertex: V, exitVertex: V) extends ControlFlowGraph[AbstractOp, V](graph, entryVertex, exitVertex) {
	// TODO lookup switch
	// TODO exception
	lazy val bytecode = {
		import collection.mutable.ListBuffer

		val markers: MarkerManager = new MarkerManager()

		var vertexBlockMap: Map[V, ListBuffer[ControlFlowElm]] = Map.empty

		var elms: List[ListBuffer[ControlFlowElm]] = Nil

		var prevVertex: V = entryVertex

		def getMarkerFor(vertex: V) = {
			markers.mark(
				if (vertexBlockMap.contains(vertex))
					vertexBlockMap(vertex).head
				else
					vertex.block.head
				)
		}

		def patchJump(startVertex: V, endVertex: V) = {
			val marker = getMarkerFor(endVertex)
			val target = vertexBlockMap(startVertex)
			target(target.length - 1) match {
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

		new EdgeFlowReorder(graph).foreach {
			case (edge, backRefCnt) => {
				val end = edge.endVertex

				if (!vertexBlockMap.contains(end)) {
					val currentBlock = addBlockFor(end)

					// this block have at least a back edge so add a Label as first op
					if ((backRefCnt(end) > 0) && !currentBlock(0).isInstanceOf[Label])
						currentBlock.insert(0, Label())

					if (prevVertex == entryVertex)
						prevVertex = end
					else {
						edge.kind match {
							case EdgeKind.True => {
								if (prevVertex == edge.startVertex) {
									// need to invert the condition
									// so we can avoid a jump
									// have to found the false edge
									// and mark the target op
									graph.outgoingOf(prevVertex).find(_.kind == EdgeKind.False) match {
										case Some(falseEdge) => {
											val marker = getMarkerFor(falseEdge.endVertex)
											val target = vertexBlockMap(edge.startVertex)
											target(target.length - 1) = Op.invertCopyConditionalOp(target.last, marker)
										}
										case _ => error("missing false edge : " + edge)
									}
								} else {
									patchJump(edge.startVertex, end)
								}
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
					patchJump(edge.startVertex, end)
				}
			}
		}

		var l: List[ControlFlowElm] = Nil
		elms.reverse.foreach(n => l = l ++ n)
		new Bytecode(l, markers, new Array(0))
	}
}