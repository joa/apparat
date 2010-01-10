package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

trait BytecodeChain[+A] extends (Stream[AbstractOp] => Result[A]) {
	def ~[B](that: => BytecodeChain[B]) = new BytecodeChainSequence(this, that)
	def |[B >: A](that: BytecodeChain[B]) = new DisjunctiveBytecodeChain(this, that)
}