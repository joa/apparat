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
 * Time: 14:15:41
 */

class ControlFlowGraph[T, V <: BlockVertex[T]](val graph: GraphLike[V], val entryVertex: V, val exitVertex: V) extends GraphLike[V] with ControlFlow[V] {
	def predecessorsOf(vertex: V) = graph.predecessorsOf(vertex)

	def successorsOf(vertex: V) = graph.successorsOf(vertex)

	def incomingOf(vertex: V) = graph.incomingOf(vertex)

	def verticesIterator = graph.verticesIterator

	def edgesIterator = graph.edgesIterator

	def indegreeOf(vertex: V) = graph.indegreeOf(vertex)

	def outdegreeOf(vertex: V) = graph.outdegreeOf(vertex)

	def contains(edge: Edge[V]) = graph.contains(edge)

	def outgoingOf(vertex: V) = graph.outgoingOf(vertex)

	def contains(vertex: V) = graph.contains(vertex)
}