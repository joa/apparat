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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.abc.analysis

import apparat.graph.mutable.Graph
import apparat.abc.{AbcName, Abc, AbcNominalType}
import scala.collection.mutable.HashMap
import apparat.graph.{DefaultEdge, GraphLike}

/**
 * @author Joa Ebert
 */
object AbcDependencyGraphBuilder extends (List[Abc] => GraphLike[AbcNominalType]) {
	def apply(abcs: List[Abc]): GraphLike[AbcNominalType] = {
		var graph: Graph[AbcNominalType] = new Graph()
		var map: HashMap[AbcName, AbcNominalType] = HashMap.empty

		for(abc <- abcs; types = abc.types) {
			graph ++= types
			map ++= types.map { `type` => `type`.inst.name -> `type` }
		}

		for((qname, vertex) <- map.elements) {
			vertex.inst.base match {
				case Some(base) => map get base match {
					case Some(baseType) => graph += DefaultEdge(baseType, vertex)
					case None =>
				}
				case None =>
			}

			for(interface <- vertex.inst.interfaces) {
				map get interface match {
					case Some(interface) => graph += DefaultEdge(interface, vertex)
					case None =>
				}
			}
		}

		graph
	}
}