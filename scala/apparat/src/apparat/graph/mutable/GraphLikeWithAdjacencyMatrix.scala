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
package apparat.graph.mutable

import collection.mutable.HashMap

trait GraphLikeWithAdjacencyMatrix[V] extends MutableGraphLike[V]
{
	private val adjacencyMatrix = new HashMap[V, List[E]]()
	private var edges: List[E] = Nil
	private var vertices: List[V] = Nil

	override def verticesIterator = vertices.iterator

	override def edgesIterator = edges.iterator
	
	override def add(edge: E): Unit = {
		assert(!contains(edge))
		assert(contains(edge.startVertex))
		assert(contains(edge.endVertex))
		adjacencyMatrix(edge.startVertex) = edge :: adjacencyMatrix(edge.startVertex)
		edges = edge :: edges
	}

	override def contains(edge: E): Boolean = edges contains edge

	override def remove(edge: E): Unit = {
		assert(contains(edge))
		assert(contains(edge.startVertex))
		assert(contains(edge.endVertex))
		adjacencyMatrix(edge.startVertex) = adjacencyMatrix(edge.startVertex) filterNot (_ == edge)
		edges = edges filterNot (_ == edge)
	}

	override def add(vertex: V): Unit = {
		assert(!contains(vertex))
		adjacencyMatrix(vertex) = Nil
		vertices = vertex :: vertices
	}

	override def contains(vertex: V): Boolean = vertices contains vertex

	override def remove(vertex: V): Unit = {
		assert(contains(vertex))
		outgoingOf(vertex) foreach remove _
		incomingOf(vertex) foreach remove _
		adjacencyMatrix -= vertex
	}

	override def incomingOf(vertex: V) = edges filter (_.endVertex == vertex)

	override def outgoingOf(vertex: V) = adjacencyMatrix(vertex)
}