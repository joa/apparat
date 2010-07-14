package apparat.graph

import annotation.tailrec

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


final class GraphVerticesSorter[V](graph: GraphLike[V]) {
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

	lazy val vertices = getVertices(entry, graph.verticesIterator.toList).filterNot(p => p == entry || p == exit)

	private def getVertices(firstVertex: V, verticesToConsider: List[V]): List[V] = {
		// Sort the vertices using a topological sort and when finding a SCC reapply the algorithm on the SCC
		// greatly inspired from http://www.woodmann.com/forum/entry.php?95-Control-Flow-Deobfuscation-Part-3
		// the challenging part is to correctly ordered exception block

		var order = List.empty[V]
		var stack = List.empty[V]
		var currIndex = 0
		var indexMap = Map.empty[V, Int]
		var lowMap = Map.empty[V, Int]
		var visitedMap = Map.empty[V, Boolean]

		def sortIncomingVertices(l: List[V]) = {
			@inline def sort(v1:V, v2:V)={
				if (graph.outgoingOf(v1).view.exists(_.kind==EdgeKind.Throw))
					true
				else if (graph.outgoingOf(v2).view.exists(_.kind==EdgeKind.Throw))
					false
				else if (graph.incomingOf(v1).view.exists(_.kind==EdgeKind.Throw))
					true
				else
					false
			}
			l.sortWith(sort)
		}

		def sortOutgoingVertices(e1: graph.E, e2: graph.E) = {
			if (e1.kind == EdgeKind.Throw) {
				if (e2.kind == EdgeKind.Throw)
					true
				else {
					!graph.successorsOf(e2.endVertex).view.exists(_ == e1.endVertex)
				}
			} else if (e2.kind != EdgeKind.Throw) {
				true
			} else {
				graph.successorsOf(e1.endVertex).view.exists(_ == e2.endVertex)
			}
		}

		def visit(vertex: V): Unit = {
			stack = vertex :: stack

			lowMap = lowMap updated (vertex, currIndex)
			indexMap = indexMap updated (vertex, currIndex)

			currIndex += 1

			for (child <- graph.outgoingOf(vertex).toList.sortWith(sortOutgoingVertices).map(e => e.endVertex) if (verticesToConsider.contains(child))) {
				if (visitedMap.contains(child)) {
					if (!visitedMap(child)) {
						visitedMap -= child
						visit(child)
						lowMap = lowMap updated (vertex, math.min(lowMap(vertex), lowMap(child)))
					}
				} else {
					lowMap = lowMap updated (vertex, math.min(lowMap(vertex), indexMap(child)))
				}
			}

			if (lowMap(vertex) == indexMap(vertex)) {
				/* we found an SCC */
				var scc = List.empty[V]
				@tailrec def sccLoop(): Unit = {
					val popped = stack.head
					stack = stack.tail
					scc = popped :: scc
					visitedMap = visitedMap updated (popped, true)
					if (popped != vertex)
						sccLoop()
				}
				sccLoop()

				if (scc.size == 1)
					order = vertex :: order
				else {
					val sccSorted = sortIncomingVertices(scc)
					order = getVertices(sccSorted.head, sccSorted) ::: order
				}
			}
		}

		for (v <- verticesToConsider) visitedMap = visitedMap updated (v, false)

		visitedMap = visitedMap updated (firstVertex, true)

		for (child <- graph.outgoingOf(firstVertex).toList.sortWith(sortOutgoingVertices).map(e => e.endVertex) if (verticesToConsider.contains(child))) {
			if (!visitedMap.getOrElse(child, true))
				visit(child)
		}
		firstVertex :: order
	}
}

object GraphVerticesSorter {
	def apply[V](graph: GraphLike[V]) = new GraphVerticesSorter(graph)
}