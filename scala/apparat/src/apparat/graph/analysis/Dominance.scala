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

import java.util.ArrayList
import apparat.graph.{ControlFlow, GraphLike}

/**
 * @author Joa Ebert
 */
class Dominance[V](val graph: GraphLike[V]) {
	private lazy val postorder = {
		def findEntry(): V = graph match {
			case controlFlow: ControlFlow[_] => controlFlow.entryVertex.asInstanceOf[V]//ugly :(
			case _ => {
				for(vertex <- graph.verticesIterator if (graph indegreeOf vertex) == 0) {
					return vertex
				}
				
				error("No entry vertex found.")
			}
		}

		val entry = findEntry
	}

	private lazy val frontiers = {
		Map.empty[V, List[V]]
	}

	private lazy val doms = {
		Map.empty[V,V]
	}
}