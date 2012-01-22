/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.graph

import annotation.tailrec
import immutable.ImmutableAbstractOpBlockVertex
import mutable.MutableAbstractOpBlockVertex
import apparat.bytecode.operations.{NewCatch, Op}
import apparat.abc.{AbcNamespace, AbcQName}

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

	private lazy val globalCatchQName = AbcQName(Symbol(null), AbcNamespace(0, Symbol(null)))

	private def isGlobalCatch(v: V) = v match {
		case iaobv: ImmutableAbstractOpBlockVertex => {
			iaobv.block.find(op => op.opCode == Op.newcatch) match {
				case Some(op: NewCatch) if (op.exceptionHandler.typeName == globalCatchQName) => true
				case _ => false
			}
		}
		case maobv: MutableAbstractOpBlockVertex => {
			maobv.block.find(op => op.opCode == Op.newcatch) match {
				case Some(op: NewCatch) if (op.exceptionHandler.typeName == globalCatchQName) => true
				case _ => false
			}
		}
		case _ => false
	}

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
			@inline def sort(v1: V, v2: V) = {
				if (graph.outgoingOf(v1).view.exists(_.kind == EdgeKind.Throw))
					true
				else if (graph.outgoingOf(v2).view.exists(_.kind == EdgeKind.Throw))
					false
				else if (graph.incomingOf(v1).view.exists(_.kind == EdgeKind.Throw))
					true
				else
					false
			}
			l.sortWith(sort)
		}

		def sortOutgoingVertices(e1: graph.E, e2: graph.E) = {
			if (e1.kind == EdgeKind.Throw) {
				if (e2.kind == EdgeKind.Throw) {
					isGlobalCatch(e1.endVertex)
				} else {
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
