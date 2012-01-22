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
package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

object BytecodeChains {
	implicit def operation(op: AbstractOp) = new BytecodeChain[AbstractOp] {
		override def apply(list: List[AbstractOp]) = {
			list headOption match {
				case Some(firstOp) => {
					lazy val errorMessage = "Expected '%s' got '%s'.".format(op, firstOp)
					if(firstOp ~== op) Success(firstOp, list drop 1)
					else Failure(errorMessage)
				}
				case _ => Failure("Expected '%s' got Nil.".format(op))
			}
		}
	}

	implicit def partial[A](f: PartialFunction[AbstractOp, A]) = new BytecodeChain[A] {
		override def apply(list: List[AbstractOp]) = {
			list headOption match {
				case Some(op) => {
					lazy val errorMessage = "Expected '%s' got '%s'.".format(f, op)
					if(f.isDefinedAt(op)) Success(f(op), list drop 1)
					else Failure(errorMessage)
				}
				case _ => Failure("Expected '%s' got Nil.".format(f))
			}
		}
	}

	implicit def filter(f: PartialFunction[AbstractOp, Boolean]) = new BytecodeChain[AbstractOp] {
		override def apply(list: List[AbstractOp]) = {
			list headOption match {
				case Some(op) => {
					lazy val errorMessage = "Expected '%s' got '%s'.".format(f, op)
					if(f.isDefinedAt(op) && f(op)) Success(op, list drop 1)
					else Failure(errorMessage)
				}
				case _ => Failure("Expected '%s' got Nil.".format(f))
			}
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
