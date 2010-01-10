package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

object BytecodeChains {
	implicit def operation(op: AbstractOp) = new BytecodeChain[AbstractOp] {
		def apply(stream: Stream[AbstractOp]) = {
			val head = stream.head
			lazy val errorMessage = "Expected '%s' got '%s'".format(op, head)
			lazy val tail = stream drop 1
			if(head ~== op) {
				Success(head, tail)
			} else {
				Failure(errorMessage, tail)
			}
		}
	}
}