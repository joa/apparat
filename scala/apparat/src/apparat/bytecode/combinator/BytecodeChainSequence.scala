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