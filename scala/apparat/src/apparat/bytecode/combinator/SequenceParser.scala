package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

class SequenceParser[+A, +B](l: => Parser[A],
							 r: => Parser[B]) extends Parser[(A, B)] {
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