package apparat.bytecode

import combinator.BytecodeChains._
import combinator._
import operations._

object PeepholeOptimizations {
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