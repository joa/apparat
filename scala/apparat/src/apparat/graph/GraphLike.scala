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

import collection.mutable.{HashSet, ListBuffer}

trait GraphLike[V <: VertexLike] {
	protected type E = Edge[V]

	def +=(that: (V, V)) = {
		if (!contains(that._1)) add(that._1)
		if (!contains(that._2)) add(that._2)
		add(new DefaultEdge[V](that._1, that._2))
	}

	def -=(that: (V, V)) = {
		if (contains(that._1) && contains(that._2))
			outgoingOf(that._1) filter (_.endVertex == that._2) foreach remove _
	}

	def +=(that: E) = add(that)

	def +=(that: V) = add(that)

	def -=(that: E) = remove(that)

	def -=(that: V) = remove(that)

	def indegreeOf(vertex: V) = incomingOf(vertex).length

	def outdegreeOf(vertex: V) = outgoingOf(vertex).length

	def predecessorsOf(vertex: V) = incomingOf(vertex) map (_.startVertex)

	def successorsOf(vertex: V) = outgoingOf(vertex) map (_.endVertex)

	def add(edge: E): Unit

	def contains(edge: E): Boolean

	def remove(edge: E): Unit

	def add(vertex: V): Unit

	def contains(vertex: V): Boolean

	def remove(vertex: V): Unit

	def incomingOf(vertex: V): List[E]

	def outgoingOf(vertex: V): List[E]

	def edgesIterator: Iterator[E]

	def verticesIterator: Iterator[V]

	//use a depth first search so sort are in reverse order
	def reverseTopologicalSort: Seq[V] = {
		val visited: HashSet[V] = new HashSet()
		val list: ListBuffer[V] = new ListBuffer()
		def visit(vertex: V): Unit = {
			if (!visited.contains(vertex)) {
				visited += vertex
				for (edge <- outgoingOf(vertex)) visit(edge.endVertex)
				list += vertex
			}
		}
		for (block <- verticesIterator) visit(block)
		list.toSeq
	}

	def topologicalSort = reverseTopologicalSort reverse
}