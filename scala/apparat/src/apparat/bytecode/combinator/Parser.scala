package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

trait Parser[+A] extends (Stream[AbstractOp] => Result[A]) {
	def ~[B](that: => Parser[B]) = new SequenceParser(this, that)
	def |[B >: A](that: Parser[B]) = new DisjunctiveParser(this, that)
}