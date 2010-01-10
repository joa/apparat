package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

class DisjunctiveParser[+A](left: Parser[A], right: Parser[A]) extends Parser[A] {
  def apply(s: Stream[AbstractOp]) = left(s) match {
    case failure: Failure => right(s)
    case res => res
  }
}