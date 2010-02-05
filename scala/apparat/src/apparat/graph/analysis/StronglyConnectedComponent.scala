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
import apparat.graph.immutable.Graph

/**
 * @author Joa Ebert
 */
class StronglyConnectedComponent[V](val vertices: List[V], graph: GraphLike[V]) {
	lazy val entry = {
		vertices filter { vertex => (graph incomingOf vertex) exists (e => !(vertices contains e.startVertex)) } match {
			case x :: Nil => Some(x)
			case _ => None
		}
	}

	lazy val canSearch = entry.isDefined

	lazy val subcomponents = entry match {
		case Some(entry) => {
			def putEdges(list: List[V], g: Graph[V]): Graph[V] = list match {
				case x :: xs => putEdges(xs, g +> ((graph outgoingOf x) filter (e => e.endVertex != entry && (vertices contains e.startVertex) && (vertices contains e.endVertex))))
				case Nil => g
			}
			new StronglyConnectedComponentFinder(putEdges(vertices, Graph.empty[V] ++ vertices))
		}
		case None => error("Can not build subcomponents.")
	}

	override def toString = "[StronglyConnectedComponent " + vertices + "]"
}