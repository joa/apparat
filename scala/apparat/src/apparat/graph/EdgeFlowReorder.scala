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
 * Date: 5 févr. 2010
 * Time: 22:30:05
 */

/**
 * reorder edges of a graph using a DFS preferring false edge in first position
 * and return edge in last
 * this reorder also insure the last edge will be a return
 */
class EdgeFlowReorder[V](val graph: GraphLike[V]) {
	lazy val entry: V = graph match {
		case controlFlow: ControlFlow[_] => controlFlow.entryVertex.asInstanceOf[V]
		case _ => graph.verticesIterator find (vertex => (graph indegreeOf vertex) == 0) match {
			case Some(vertex) => vertex
			case None => error("No vertex with indegree(v) == 0 found.")
		}
	}

	def compare(a: Edge[V], b: Edge[V]) = (a.kind == EdgeKind.Return) || (b.kind == EdgeKind.False)

	// body must accept a tuple with an Edge and Map counting backreference to a Vertex
	def foreach(body: (Edge[V], Map[V, Int]) => Unit) = {
		var S = List(graph.outgoingOf(entry).head)

		var visitedEdge = Set[Edge[V]](S(0))

		var list: List[Edge[V]] = Nil

		// counter of back reference to a Vertex
		var backRefCnt: Map[V, Int] = Map.empty

		//memoize the last return edge
		var returnIndex = -1

		while (S.nonEmpty) {
			val e = S.head

			list = e :: list

			backRefCnt = backRefCnt.updated(e.endVertex, backRefCnt.getOrElse(e.endVertex, -1) + 1)

			val outgoing = graph.outgoingOf(e.endVertex)

			// keep the index of the last return
			// so we can later put it at the end
			if (outgoing.head.kind == EdgeKind.Return)
				returnIndex = list.length

			S = S.tail

			outgoing match {
				case outList: List[_] => {
					for (e <- outList.sortWith(compare _) if !visitedEdge.contains(e)) {
						visitedEdge = visitedEdge + e
						if (e.kind != EdgeKind.Return)
							S = e :: S
					}
				}
				case _ => error("Not implemented")
			}
		}

		if (returnIndex < 0)
			error("No return edge found into the graph")

		//put at least a ReturnEdge at the end (in fact since the list is in reverse order we put it at the start)
		((list.length - returnIndex) match {
			// return edge is yet at the end so nothing to do
			case 0 => list
			// put it at the start
			case n => {
				val (head, tail) = list.splitAt(n)
				List(tail.head) ::: head ::: (tail drop 1)
			}
		}).reverse.foreach(body(_, backRefCnt))
	}
}
