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

import BytecodeChains._

trait BytecodeChain[+A] extends (List[AbstractOp] => Result[A]) { outer =>

	def ? = opt(this)

	def * = rep(this)

	def ~[B](that: => BytecodeChain[B]) = new BytecodeChainSequence(this, that)

	def |[B >: A](that: BytecodeChain[B]) = new DisjunctiveBytecodeChain(this, that)

	def ^^[B](f: A => B) = new BytecodeChain[B] {
		override def apply(list: List[AbstractOp]) = outer(list) match {
			case Success(value, remaining) => Success(f(value), remaining)
			case f: Failure => f
		}
	}

	def ^^^[B](value: => B) = new BytecodeChain[B] {
		override def apply(list: List[AbstractOp]) = outer(list) match {
			case Success(_, remaining) => Success(value, remaining)
			case f: Failure => f
		}
	}
}
