package apparat.bytecode

import collection.mutable.ListBuffer
import operations._

object Bytecode {
	def fromByteArray(value: Array[Byte]) = new Bytecode
}

class Bytecode {
	private val code = new ListBuffer[AbstractOp]()
	def toByteArray = new Array[Byte](0)
}