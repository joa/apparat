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
package apparat.graph

class BasicBlockVertex[T](var block: Seq[T] = Nil) extends VertexLike {
	def ++(elms: Seq[T]) = {
		block = block ++ elms
		this
	}

	def removeLast() = {block = block take block.length - 1}

	def contains(elm: T): Boolean = block contains elm

	def indexOf(elm: T): Int = block indexOf elm

	def length: Int = block.length

	def apply(index: Int): T = block(index)

	def clear() {block = Nil}

	def isEmpty = block.isEmpty

	// FIXME tmp for the dot graph export
	override def toString = {
		block.mkString("", "\\n", "")
	}
}