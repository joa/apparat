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
		override def apply(list: List[AbstractOp]) = {
			val head = list.head
			lazy val errorMessage = "Expected '%s' got '%s'.".format(op, head)
			if(head ~== op) Success(head, list drop 1)
			else Failure(errorMessage)
		}
	}

	implicit def partial[A](f: PartialFunction[AbstractOp, A]) = new BytecodeChain[A] {
		override def apply(list: List[AbstractOp]) = {
			val head = list.head
			lazy val errorMessage = "Expected '%s' got '%s'.".format(f, head)
			if(f.isDefinedAt(head)) Success(f(head), list drop 1)
			else Failure(errorMessage)
		}
	}

	implicit def filter(f: PartialFunction[AbstractOp, Boolean]) = new BytecodeChain[AbstractOp] {
		override def apply(list: List[AbstractOp]) = {
			val head = list.head
			lazy val errorMessage = "Expected '%s' got '%s'.".format(f, head)
			if(head != Nil && f.isDefinedAt(head) && f(head)) {
				Success(head, list drop 1)
			}
			else Failure(errorMessage)
		}
	}

	def opt[A](chain: BytecodeChain[A]) = new BytecodeChain[Option[A]] {
		override def apply(list: List[AbstractOp]) = {
			chain(list) match {
				case Success(value, remaining) => Success(Some(value), remaining)
				case f: Failure => Success(None, list)
			}
		}
	}

	def rep[A](chain: BytecodeChain[A]) = new BytecodeChain[List[A]] {
		override def apply(list: List[AbstractOp]) = {
			def repeat(result: List[A], list: List[AbstractOp]): Success[List[A]] = {
				chain(list) match {
					case Success(value, remaining) => repeat(value :: result, remaining)
					case f: Failure => Success(result.reverse, list)
				}
			}

			repeat(Nil, list)
		}
	}
}