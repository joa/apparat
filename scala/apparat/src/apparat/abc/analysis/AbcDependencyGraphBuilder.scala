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

import apparat.abc.Abc
import apparat.abc.AbcNominalType
import apparat.graph.GraphLike
import apparat.graph.immutable.Graph

/**
 * @author Joa Ebert
 */
object AbcDependencyGraphBuilder extends (List[Abc] => GraphLike[AbcNominalType]) {
	def apply(abcs: List[Abc]): GraphLike[AbcNominalType] = {
		def createGraph(abc: List[Abc], G: Graph[AbcNominalType]): Graph[AbcNominalType] = abc match {
			case Nil => G
			case x :: xs => createGraph(xs, G ++ x.types.toList)
		}

		createGraph(abcs, Graph.empty[AbcNominalType]).dump()
	}
}