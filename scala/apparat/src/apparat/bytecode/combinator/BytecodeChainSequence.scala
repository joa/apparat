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

class BytecodeChainSequence[+A, +B](l: => BytecodeChain[A],
							 r: => BytecodeChain[B]) extends BytecodeChain[(A, B)] {
	lazy val left = l
	lazy val right = r

	def apply(s: Stream[AbstractOp]) = left(s) match {
		case Success(a, rem) => right(rem) match {
			case Success(b, rem) => Success((a, b), rem)
			case f: Failure => f
		}
		case f: Failure => f
	}
}