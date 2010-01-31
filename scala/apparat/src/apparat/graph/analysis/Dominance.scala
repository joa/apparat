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

/**
 * @author Joa Ebert
 */
class Dominance[V](val graph: GraphLike[V]) {
	lazy val entry: V = {
		def search(): V = graph match {
			case controlFlow: ControlFlow[_] => controlFlow.entryVertex.asInstanceOf[V]//ugly :(
			case _ => {
				for(vertex <- graph.verticesIterator if (graph indegreeOf vertex) == 0) {
					return vertex
				}

				error("No entry vertex found.")
			}
		}
		
		search()
	}

	private lazy val postorder = graph dft entry toList

	private lazy val reversePostorder = postorder.reverse

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

		def pick(predecessors: Iterable[V]): V = {
			for(vertex <- predecessors) {
				if(result contains vertex) {
					return vertex
				}
			}

			error("Unreachable by definition.")
		}

		def intersect(b1: V, b2: V) = {
			var finger1 = b1
			var finger2 = b2

			while(finger1 != finger2) {
				while(postorder.indexOf(finger1) < postorder.indexOf(finger2))
					finger1 = result(finger1)

				while(postorder.indexOf(finger2) < postorder.indexOf(finger1))
					finger2 = result(finger2)
			}

			finger1
		}

		def loop(): Unit = {
			var changed = false

			for(b <- rp) {
				val predecessorsTmp = graph predecessorsOf b
				var newIDom = pick(predecessorsTmp)
				val predecessors = predecessorsTmp filterNot (_ == newIDom)

				for(p <- predecessors) {
					result get p match {
						case Some(vertex) => {
							newIDom = intersect(vertex, newIDom)
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