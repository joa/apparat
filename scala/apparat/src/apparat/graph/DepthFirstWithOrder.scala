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
 * Date: 5 mai 2010
 * Time: 16:32:32
 */

case class IndexedVertex[V](var maxOrder: Int, order: Int, vertex: V)

final class DepthFirstWithOrder[V](graph: GraphLike[V]) {
	private lazy val entry: V = graph match {
		case controlFlow: ControlFlow[_] => controlFlow.entryVertex.asInstanceOf[V]
		case _ => graph.verticesIterator find (vertex => (graph indegreeOf vertex) == 0) match {
			case Some(vertex) => vertex
			case None => error("No vertex with indegree(v) == 0 found.")
		}
	}

	private lazy val exit: V = graph match {
		case controlFlow: ControlFlow[_] => controlFlow.exitVertex.asInstanceOf[V]
		case _ => graph.verticesIterator find (vertex => (graph outdegreeOf vertex) == 0) match {
			case Some(vertex) => vertex
			case None => error("No vertex with outdegree(v) == 0 found.")
		}
	}

	lazy val vertices = {
		var list = List.empty[IndexedVertex[V]]
		var visited = Map.empty[V, IndexedVertex[V]]
		var index: Int = 0
		def dfs(x: V): Unit = {
			if (!visited.contains(x)) {
				val iv = new IndexedVertex(index, index, x)
				index += 1
				visited = visited updated (x, iv)
				def compare(a: V, b: V) = (math.random < 0.5)
				for (v <- graph.successorsOf(x).toList.sortWith(compare); if (!visited.contains(v))) {
					dfs(v)
					visited(v).maxOrder = index
				}
				if (iv.vertex != entry && iv.vertex != exit)
					list = iv :: list
			}
		}
		dfs(entry)

		def s[V](a: IndexedVertex[V], b: IndexedVertex[V]) = {
			if (a.maxOrder == b.maxOrder) {
				a.order < b.order
			} else {
				a.maxOrder > b.maxOrder
			}
		}

		list.sortWith(s)
	}
}

object DepthFirstWithOrder {
	def apply[V](graph: GraphLike[V]) = new DepthFirstWithOrder(graph)
}