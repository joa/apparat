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

import analysis.{StronglyConnectedComponentFinder, Dominance}
import apparat.utils.{IndentingPrintWriter, Dumpable}

/**
 * @author Joa Ebert
 */
trait GraphLike[V] extends Dumpable {
	type E = Edge[V]

	def topsort: GraphTraversal[V]

	def dominance: Dominance[V]

	def sccs: StronglyConnectedComponentFinder[V]

	def contains(vertex: V): Boolean

	def contains(edge: E): Boolean

	def outgoingOf(vertex: V): Iterable[E]

	def incomingOf(vertex: V): Iterable[E]

	def predecessorsOf(vertex: V): Iterable[V]

	def successorsOf(vertex: V): Iterable[V]

	def outdegreeOf(vertex: V): Int

	def indegreeOf(vertex: V): Int

	def verticesIterator: Iterator[V]

	def edgesIterator: Iterator[E]

	def dft(vertex: V): GraphTraversal[V] = new DepthFirstTraversal(this, vertex)

	def vertexExists(p: V => Boolean) = verticesIterator exists p

	def edgeExists(p: E => Boolean) = edgesIterator exists p

	def foreachVertex(f: V => Unit) = verticesIterator foreach f

	def foreachEdge(f: E => Unit) = edgesIterator foreach f

	def vertexMap[T](f: V => T): Map[V, T] = Map(verticesIterator map {v => v -> f(v)} toSeq: _*)

	def edgeMap[T](f: E => T): Map[E, T] = Map(edgesIterator map {e => e -> f(e)} toSeq: _*)

	def -(edge: E): this.type

	def +(edge: E): this.type

	def -(vertex: V): this.type

	def +(vertex: V): this.type

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Graph:"
		writer withIndent {
			for (vertex <- verticesIterator) {
				writer <= vertex.toString
				writer withIndent {
					writer.println(outgoingOf(vertex)) {
						edge => (if (edge.kind != EdgeKind.Default) edge.kind.toString else "") + " -> " + edge.endVertex.toString
					}
				}
			}
		}
	}
}