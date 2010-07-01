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
package apparat.graph.analysis

import apparat.graph.GraphLike
import collection.immutable.Stack
import scala.math.min

/**
 * @author Joa Ebert
 */
class StronglyConnectedComponentFinder[V](val graph: GraphLike[V]) {
	type SCC = StronglyConnectedComponent[V]

	private lazy val sccs = {
		var index = 0
		var S = Stack.empty[V]
		var vi = graph vertexMap { v => -1 }
		var vl = graph vertexMap { v => -1 }
		var result = List.empty[SCC]

		def tarjan(vertex: V): Unit = {
			vi = vi updated (vertex, index)
			vl = vl updated (vertex, index)
			index += 1

			S = S push vertex

			for(e <- (graph outgoingOf vertex)) {
				val next = e.endVertex
				val nextIndex = vi(next)

				if(-1 == nextIndex) {
					tarjan(next)
					vl = vl updated (vertex, min(vl(vertex), vl(next)))
				} else if(S contains next) {
					vl = vl updated (vertex, min(vl(vertex), nextIndex))
				}
			}

			if(vi(vertex) == vl(vertex) && S.nonEmpty) {
				def loop(list: List[V]): List[V] = {
					val next = S.top
					S = S.pop
					if(next == vertex) next :: list
					else loop(next :: list)
				}

				loop(Nil) match {
					case x :: Nil =>
					case Nil =>
					case scc => result = new StronglyConnectedComponent(scc, graph) :: result
				}
			}
		}

		for(vertex <- graph.verticesIterator) {
			if(-1 == vi(vertex)) {
				tarjan(vertex)
			}
		}

		result
	}

	def foreach(body: SCC => Unit) = sccs foreach body

	def map[T](f: SCC => T) = sccs map f

	def flatMap[T](f: SCC => Traversable[T]) = sccs flatMap f

	def filter(f: SCC => Boolean) = sccs filter f

	def toList = sccs
}