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

package apparat.graph.immutable

import apparat.graph.{GraphTraversal, VertexLike}

class DepthFirstTraversal[V <: VertexLike](graph: Graph[V], startVertex: V) extends GraphTraversal[V] {
	private lazy val vertexList = {
		var list: List[V] = Nil
		var visited = graph.adjacency map { _._1 -> false } updated (startVertex, true)
		var S = List(startVertex)

		while(S.nonEmpty) {
			val v = S.head
			
			S = S.tail

			list = v :: list

			for(e <- graph.outgoingOf(v) if !visited(e.endVertex)) {
				visited = visited updated (e.endVertex, true)
				S = e.endVertex :: S
			}
		}

		list
	}

	def foreach(body: V => Unit) = vertexList foreach body

	def map[T](f: V => T) = vertexList map f

	def flatMap[T](f: V => Traversable[T]) = vertexList flatMap f

	def toList = vertexList
}