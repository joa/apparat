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
 * Date: 31 janv. 2010
 * Time: 19:59:02
 */
package apparat.graph.immutable

import apparat.graph.BlockVertex
import apparat.bytecode.operations.AbstractOp

class ImmutableBlockVertex[T](val block: List[T] = Nil) extends BlockVertex[T] with Immutable {
	override def ++(elms: List[T]) = new ImmutableBlockVertex(block ::: elms)

	override def removeFirst = new ImmutableBlockVertex(block.tail)

	override def removeLast = new ImmutableBlockVertex(block dropRight 1)

	override def clear = new ImmutableBlockVertex()
}

class ImmutableAbstractOpBlockVertex(block: List[AbstractOp] = Nil) extends ImmutableBlockVertex[AbstractOp](block) {
	override def ++(elms: List[AbstractOp]) = new ImmutableAbstractOpBlockVertex(block ::: elms)

	override def toString = block.mkString("[[", "\\n", "]]")
}