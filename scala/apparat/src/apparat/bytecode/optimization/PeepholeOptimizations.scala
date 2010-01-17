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
package apparat.bytecode

import combinator.BytecodeChains._
import combinator._
import operations._

object PeepholeOptimizations extends (Bytecode => Bytecode) {
	def apply(bytecode: Bytecode) = {
		bytecode rewrite whileLoop
		bytecode rewrite getLex
		bytecode rewrite unnecessaryIntCast
		bytecode
	}
	
	lazy val whileLoop = (
		partial { case getLocal: GetLocal => getLocal } ~
		(IncrementInt() | DecrementInt()) ~
		Dup() ~
		ConvertInt() ~
		partial { case setLocal: SetLocal => setLocal }
	) ^^ {
		case GetLocal(x) ~ op ~ Dup() ~ ConvertInt() ~ SetLocal(y) if x == y => {
			(op match {
				case DecrementInt() => DecLocalInt(x)
				case IncrementInt() => IncLocalInt(x)
				case _ => error("Unreachable code.")
			}) :: GetLocal(x) :: Nil
		}
		case GetLocal(x) ~ op ~ Dup() ~ ConvertInt() ~ SetLocal(y) => {
			GetLocal(x) :: op :: Dup() :: ConvertInt() :: SetLocal(y) :: Nil
		}
		case _ => error("Unreachable code.")
	}

	lazy val getLex = (
		partial { case findPropStrict: FindPropStrict => findPropStrict } ~
		partial { case getProperty: GetProperty => getProperty }
	) ^^ {
		case FindPropStrict(x) ~ GetProperty(y) if x == y => GetLex(x) :: Nil
		case FindPropStrict(x) ~ GetProperty(y) => FindPropStrict(x) :: GetProperty(y) :: Nil
		case _ => error("Unreachable code.")
	}

	lazy val unnecessaryIntCast = ((AddInt() | SubtractInt() | MultiplyInt()) ~ ConvertInt()) ^^ {
		case x ~ ConvertInt() => x :: Nil
		case _ => error("Unreachable code.")
	}
}