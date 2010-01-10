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

import BytecodeChains._

trait BytecodeChain[+A] extends (Stream[AbstractOp] => Result[A]) { outer =>

	def ? = opt(this)

	def * = rep(this)
	
	def ~[B](that: => BytecodeChain[B]) = new BytecodeChainSequence(this, that)

	def |[B >: A](that: BytecodeChain[B]) = new DisjunctiveBytecodeChain(this, that)

	def ^^[B](f: A => B) = new BytecodeChain[B] {
		override def apply(stream: Stream[AbstractOp]) = outer(stream) match {
			case Success(value, remaining) => Success(f(value), remaining)
			case f: Failure => f
		}
	}

	def ^^^[B](value: => B) = new BytecodeChain[B] {
		override def apply(stream: Stream[AbstractOp]) = outer(stream) match {
			case Success(_, remaining) => Success(value, remaining)
			case f: Failure => f
		}
	}
}