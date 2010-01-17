package apparat.bytecode.analysis

import apparat.bytecode.Bytecode
import apparat.utils.{IndentingPrintWriter, Dumpable}
import apparat.bytecode.operations.OpNames

class FrequencyDistribution extends Dumpable {
	private val d = new Array[Int](0x100)

	def analyze(bytecode: Bytecode) = bytecode.ops foreach { op => d(op.opCode) = d(op.opCode) + 1 }

	def frequencyOf(opCode: Int) = d(opCode)

	def toCSV() = {
		val builder = new StringBuilder("Op,Frequency\n")
		for(i <- 0 until 0x100) {
			builder append OpNames(i)
			builder append ','
			builder append d(i).toString
			builder append '\n'
		}
		builder.toString
	}
	
	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Frequency distribution:"
		writer withIndent {
			for(i <- 0 until 0x100) {
				val builder = new StringBuilder(OpNames(i))

				while(builder.length < 0x10) {
					builder append ' '
				}

				builder append d(i).toString

				writer <= builder.toString
			}
		}
	}
}