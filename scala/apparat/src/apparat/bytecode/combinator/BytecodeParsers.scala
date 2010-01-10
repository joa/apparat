package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

object BytecodeParsers {
	implicit def operation(op: AbstractOp) = new Parser[AbstractOp] {
		def apply(stream: Stream[AbstractOp]) = {
			val head = stream.head
			lazy val errorMessage = "Expected '%s' got '%s'".format(op, head)

			if(head =~= op) {
				Success(head, stream drop 1)
			} else {
				Failure(errorMessage)
			}
		}
	}
}