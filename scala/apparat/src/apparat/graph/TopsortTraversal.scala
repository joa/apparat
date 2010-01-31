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

/**
 * @author Joa Ebert
 */
final protected[graph] class TopsortTraversal[V](graph: GraphLike[V]) extends GraphTraversal[V]
		with ListBasedTraversal[V] {
	protected lazy val vertexList = {
		var visited = graph vertexMap (v => false)
		var result = List.empty[V]

		def visit(vertex: V): Unit = {
			if(!visited(vertex)) {
				visited = visited updated (vertex, true)
				for(edge <- graph.outgoingOf(vertex)) {
					visit(edge.endVertex)
				}
				result = vertex :: result
			}
		}

		graph.verticesIterator foreach visit
		result.reverse
	}
}