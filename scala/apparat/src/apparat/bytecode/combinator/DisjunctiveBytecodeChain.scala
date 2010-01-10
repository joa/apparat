package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

class DisjunctiveBytecodeChain[+A](left: BytecodeChain[A], right: BytecodeChain[A]) extends BytecodeChain[A] {
  def apply(s: Stream[AbstractOp]) = left(s) match {
    case failure: Failure => right(s)
    case res => res
  }
}