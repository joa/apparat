/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.taas.graph

import collection.mutable.ListBuffer
import apparat.taas.ast.{TJump, TIf1, TIf2, TExpr}
import apparat.graph.{JumpEdge, DefaultEdge, FalseEdge, TrueEdge}
import apparat.utils.{IndentingPrintWriter, Dumpable}

/**
 * @author Joa Ebert
 */
class TaasGraphLinearizer(graph: TaasGraph) extends Dumpable {
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

	private def sources(element: TExpr): List[TExpr] = {
		var r = List.empty[TExpr]

		for((key, value) <- map) {
			if(value contains element) {
				r = key :: r
			}
		}

		r
	}

	def dump(writer: IndentingPrintWriter): Unit = {
		val keyList = map.keySet.toList

		writer.println(list) {
			element => {
				val elementString = element.toString
				val builder = new StringBuilder(elementString.length + 6)

				for(source <- sources(element)) {
					builder append "L"+(keyList indexOf source)+" "
				}

				while(builder.length < 6) builder append ' '

				builder append elementString

				(keyList indexOf element) match {
					case -1 =>
					case n => builder append (" -> L"+n)
				}

				builder.toString
			}
		}
	}
}
