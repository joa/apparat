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
package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

object BytecodeChains {
	implicit def operation(op: AbstractOp) = new BytecodeChain[AbstractOp] {
		override def apply(stream: Stream[AbstractOp]) = {
			val head = stream.head
			lazy val errorMessage = "Expected '%s' got '%s'".format(op, head)
			lazy val tail = stream drop 1
			if(head ~== op) {
				Success(head, tail)
			} else {
				Failure(errorMessage, tail)
			}
		}
	}

	def opt[A](chain: BytecodeChain[A]) = new BytecodeChain[Option[A]] {
		override def apply(stream: Stream[AbstractOp]) = {
			chain(stream) match {
				case Success(value, remaining) => Success(Some(value), remaining)
				case f: Failure => Success(None, stream)
			}
		}
	}

	def rep[A](chain: BytecodeChain[A]) = new BytecodeChain[List[A]] {
		override def apply(stream: Stream[AbstractOp]) = {
			def repeat(list: List[A], stream: Stream[AbstractOp]): Success[List[A]] = {
				chain(stream) match {
					case Success(value, remaining) => repeat(value :: list, remaining)
					case f: Failure => Success(list, stream)
				}
			}

			repeat(Nil, stream)
		}
	}
}