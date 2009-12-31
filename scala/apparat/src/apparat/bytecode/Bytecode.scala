package apparat.bytecode

import collection.mutable.ListBuffer
import operations._
import java.io.ByteArrayInputStream
import apparat.abc.{AbcMethodBody, AbcMethod, AbcInputStream, AbcConstantPool}

object Bytecode {
	def fromMethod(method: AbcMethod) = method.body match {
		case Some(body) => fromBody(body)
		case None => None
	}

	def fromBody(body: AbcMethodBody) = {
		val input = new AbcInputStream(new ByteArrayInputStream(body.code))
		val cpool = null.asInstanceOf[AbcConstantPool]

		def u30 = input.readU30()
		def name = cpool.names(u30)
		def string = cpool.strings(u30)

		try {
			val ops = new ListBuffer[AbstractOp]()
			
			while(input.available > 0) {//TODO replace with functional approach
				val op = input.readU08() match {
					case 0x01 => Breakpoint()
					case 0x02 => Nop()
					case 0x03 => Throw()
					case 0x04 => GetSuper(name)
					case 0x05 => SetSuper(name)
					case 0x06 => DefaultXMLNamespace(string)
					case 0x07 => DefaultXMLNamespaceLate()
					case 0x08 => Kill(u30)
					case 0x09 => Label()
					//control transfer
					case 0x1c => PushWith()
					case 0x1d => PopScope()
					case x => error("Unknown opcode " + x + ".")
				}

				ops += op
			}

		}
		finally {
			try { input.close() } catch { case _ => {} }
		}
	}
}

class Bytecode {
	private val code = new ListBuffer[AbstractOp]()
}