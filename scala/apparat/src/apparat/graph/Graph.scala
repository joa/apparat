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
package apparat.graph

import apparat.utils.{IndentingPrintWriter, Dumpable}

class Graph[V <: VertexLike] extends GraphLikeWithAdjacencyMatrix[V] with Dumpable with DOTExportAvailable[V] {
	override def toString = "[Graph]"
	
	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Graph:"
		writer withIndent {
			for(vertex <- verticesIterator) {
				writer <= vertex.toString
				writer withIndent {
					writer.println(outgoingOf(vertex)) {
						edge => (if(edge.kind != EdgeKind.Default) edge.kind.toString else "") + " -> " + edge.endVertex.toString
					}
				}
			}
		}
	}
	
	override def dotExport = {
		def edgeToString(edge: E) = edge match {
			case DefaultEdge(x, y) => ""
			case JumpEdge(x, y) => "jump"
			case TrueEdge(x, y) => "true"
			case FalseEdge(x, y) => "false"
			case DefaultCaseEdge(x, y) => "default"
			case CaseEdge(x, y) => "case"
			case ThrowEdge(x, y) => "throw"
			case ReturnEdge(x, y) => "return"
		}
		new DOTExport(this, (_: V).toString, edgeToString)
	}
}