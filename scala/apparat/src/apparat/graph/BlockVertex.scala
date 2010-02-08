package apparat.graph

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
 * Time: 14:58:13
 */

trait BlockVertex[T] {
	def block: List[T]

	def add(elms: List[T]): BlockVertex[T]

	def removeFirst(): BlockVertex[T]

	def removeLast(): BlockVertex[T]

	def contains(elm: T): Boolean = block contains elm

	def indexOf(elm: T): Int = block indexOf elm

	def length: Int = block.length

	def apply(index: Int): T = block(index)

	def last = block.last

	def lastOption = block.lastOption

	def head = block.head

	def headOption = block.headOption

	def clear(): BlockVertex[T]

	def isEmpty = block.isEmpty
}