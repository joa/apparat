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

import apparat.graph.{ControlFlow, GraphLike}
import annotation.tailrec

/**
 * @author Joa Ebert
 */
class Dominance[V](val graph: GraphLike[V]) {
	lazy val entry: V = graph match {
		case controlFlow: ControlFlow[_] => controlFlow.entryVertex.asInstanceOf[V]
		case _ => graph.verticesIterator find (vertex => (graph indegreeOf vertex) == 0) match {
			case Some(vertex) => vertex
			case None => error("No vertex with indegree(v) == 0 found.")
		}
	}

	private lazy val postorder = graph dft entry toList

	private lazy val reversePostorder = postorder.reverse

	@tailrec private def advanceIntersection(map: Map[V, V], a: V, b: V): V = {
		if((postorder indexOf a) < (postorder indexOf b)) advanceIntersection(map, map(a), b)
		else a
	}

	@tailrec private def intersect(map: Map[V, V], b1: V, b2: V): V = {
		if(b1 != b2) {
			val f = advanceIntersection(map, b1, b2)
			intersect(map, f, advanceIntersection(map, b2, f))
		} else {
			b1
		}
	}

	private def pickPredecessor(map: Map[V, V], predecessors: Iterable[V]): V = {
		predecessors.find (map contains _) match {
			case Some(vertex) => vertex
			case None => error("Unreachable by definition.")
		}
	}

	private lazy val doms = {
		//
		// "A Simple, Fast Dominance Algorithm"
		//
		// Keith D. Cooper et al.
		// Rice University, Houston, TX
		//
		// http://www.cs.rice.edu/~keith/EMBED/dom.pdf
		// Page 13
		//

		var result = Map(entry -> entry)
		val rp = reversePostorder filterNot (_ == entry)

		def loop(): Unit = {
			var changed = false

			for(b <- rp) {
				val predecessorsTmp = graph predecessorsOf b
				var newIDom = pickPredecessor(result, predecessorsTmp)
				val predecessors = predecessorsTmp filterNot (_ == newIDom)

				for(p <- predecessors) {
					result get p match {
						case Some(vertex) => {
							newIDom = intersect(result, vertex, newIDom)
						}
						case None =>
					}
				}
				
				result get b match {
					case Some(old) => if(old != newIDom) {
						result = result updated (b, newIDom)
						changed = true
					}
					case None => {
						result = result + (b -> newIDom)
						changed = true
					}
				}
			}

			if(changed) {
				loop()
			}
		}

		loop()
		
		result
	}

	private lazy val frontiers = {
		//
		// "A Simple, Fast Dominance Algorithm"
		//
		// Keith D. Cooper et al.
		// Rice University, Houston, TX
		//
		// http://www.cs.rice.edu/~keith/EMBED/dom.pdf
		// Page 18
		//

		var result = graph vertexMap (v => List.empty[V])

		for(b <- graph.verticesIterator) {
			val predecessors = graph predecessorsOf b

			if(predecessors.size > 1) {
				for(p <- predecessors) {
					var runner = p

					while(runner != doms(b)) {
						result = result updated (runner, b :: result(runner))
						runner = doms(runner)
					}
				}
			}
		}

		result
	}

	def frontiersOf(vertex: V) = frontiers get vertex
}