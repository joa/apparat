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
package apparat.graph.mutable

import apparat.graph.BlockVertex

class MutableBlockVertex[T](var block: List[T] = Nil) extends BlockVertex[T] {
	override def ++(elms: List[T]) = {
		block = block ++ elms
		this
	}

	override def removeFirst() = {
		block = block.tail
		this
	}

	override def removeLast() = {
		block = block dropRight 1
		this
	}

	override def clear() = {
		block = Nil
		this
	}

	// FIXME tmp for the dot graph export
	override def toString = {
		block.mkString("[[", "\\n", "]]")
	}
}
