/*
 * This file is part of Apparat.
 *
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
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