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
 */
package apparat.taas.graph

import collection.mutable.ListBuffer
import apparat.taas.ast.{TJump, TIf1, TIf2, TExpr}
import apparat.graph.{JumpEdge, DefaultEdge, FalseEdge, TrueEdge}

/**
 * @author Joa Ebert
 */
class TaasGraphLinearizer(graph: TaasGraph) {
	var list = ListBuffer.empty[TExpr]
	var map = Map.empty[TExpr, ListBuffer[TExpr]]
	private var visited = graph vertexMap { v => false }

	compute(graph.entryVertex)

	private def mark(from: TExpr, to: TExpr) = {
		map = map updated (from, (map get from getOrElse ListBuffer.empty) += to)
	}

	private def continue(vertex: TaasBlock) = {
		if(visited(vertex)) {
			val jump = TJump()
			mark(jump, vertex.block.head)
			list += jump
		} else {
			compute(vertex)
		}
	}
	
	private def compute(vertex: TaasBlock): Unit = vertex match {
		case TaasEntry => {
			val outgoing = (graph outgoingOf vertex).toList
			assert(1 == outgoing.length, "1 == outgoing.length")
			compute(outgoing.head.endVertex)
		}
		case TaasExit =>
		case blockVertex: TaasBlock => {
			val block = blockVertex.block
			val outgoing = (graph outgoingOf vertex).toList
			val last = block.last
			visited = visited updated (vertex, true)
			list ++= block
			last match {
				case _: TIf1 | _: TIf2 => {
					assert(2 == outgoing.length, "2 == outgoing.length")
					val falseEdges: List[FalseEdge[TaasBlock]] = outgoing collect { case f: FalseEdge[TaasBlock] => f }
					val trueEdges: List[TrueEdge[TaasBlock]] = outgoing collect { case t: TrueEdge[TaasBlock] => t }
					assert(1 == falseEdges.length, "1 == falseEdges.length")
					assert(1 == trueEdges.length, "1 == trueEdges.length")
					val falseEdge = falseEdges.head
					val trueEdge = trueEdges.head

					continue(falseEdge.endVertex)

					val trueVertex = trueEdge.endVertex

					mark(last, trueVertex.block.head)

					if(!visited(trueVertex)) {
						continue(trueVertex)
					}
				}
				case _ => {
					assert(1 == outgoing.length, "1 == outgoing.length")
					outgoing(0) match {
						case JumpEdge(_, end: TaasBlock) => continue(end)
						case other => "Expected jump edge, got "+other+"."
					}
				}
			}
		}
	}
}