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
package apparat.pbj

import java.io.{InputStream => JInputStream}
import apparat.pbj.pbjdata.implicits._
import apparat.pbj.pbjdata._
import annotation.tailrec

/**
 * @author Joa Ebert
 */
class PbjInputStream(input: JInputStream) extends JInputStream {
	def readFloat() = {
		java.lang.Float.intBitsToFloat((read() << 0x18) | (read() << 0x10) | (read() << 0x08) | read())
	}

	def readString() = {
		@tailrec def loop(seq: List[Byte]): List[Byte] = readUI08() match {
			case 0 => seq
			case y => loop(y.asInstanceOf[Byte] :: seq)
		}

		new String(loop(Nil).reverse.toArray, "UTF8")
	}

	def readUI08(): Int = read()
	
	def readUI16(): Int = (read() << 0x08) | read()

	def readUI32(): Long = (read() << 0x18) | (read() << 0x10) | (read() << 0x08) | read()

	def readConst(`type`: PType): PConst = `type` match {
		case PFloatType => PFloat(readFloat())
		case PFloat2Type => PFloat2(readFloat(), readFloat())
		case PFloat3Type => PFloat3(readFloat(), readFloat(), readFloat())
		case PFloat4Type => PFloat4(readFloat(), readFloat(), readFloat(), readFloat())
		case PFloat2x2Type => PFloat2x2(readFloat(), readFloat(), readFloat(), readFloat())
		case PFloat3x3Type => PFloat3x3(
			readFloat(), readFloat(), readFloat(),
			readFloat(), readFloat(), readFloat(),
			readFloat(), readFloat(), readFloat())
		case PFloat4x4Type => PFloat4x4(
			readFloat(), readFloat(), readFloat(), readFloat(),
			readFloat(), readFloat(), readFloat(), readFloat(),
			readFloat(), readFloat(), readFloat(), readFloat(),
			readFloat(), readFloat(), readFloat(), readFloat())
		case PIntType => PInt(readUI16())
		case PInt2Type => PInt2(readUI16(), readUI16()) 
		case PInt3Type => PInt3(readUI16(), readUI16(), readUI16())
		case PInt4Type => PInt4(readUI16(), readUI16(), readUI16(), readUI16())
		case PStringType => PString(readString())
		case PBoolType => PBool(readUI16() == 1)
		case PBool2Type => PBool2(readUI16() == 1, readUI16() == 1)
		case PBool3Type => PBool3(readUI16() == 1, readUI16() == 1, readUI16() == 1)
		case PBool4Type => PBool4(readUI16() == 1, readUI16() == 1, readUI16() == 1, readUI16() == 1)
	}

	def readType(): PType = readUI08()

	def readMeta(): PMeta = {
		val `type` = readType()
		PMeta(readString(), readConst(`type`))
	}

	def readParam(): PParam = {
		val qualifier = readUI08()
		val `type` = readType()

		assert(`type` != PStringType, "Parameter must not be of type String.")

		val register = readUI16()
		val mask = readUI08()
		val name = readString()

		`type` match {
			case PFloat2x2Type => assert(mask == 2); mask = 0xf
			case PFloat3x3Type => assert(mask == 3); mask = 0xf
			case PFloat4x4Type => assert(mask == 4); mask = 0xf
			case _ => assert((mask >> 4) == 0)
		}

		qualifier match {
			case 1 => PInParameter(name, `type`, dstReg(register, mask))
			case 2 => POutParameter(name, `type`, dstReg(register, mask))
			case _ => error("Qualifier must be either one or \"1\" or \"2\".")
		}
	}
	
	def readOp(): POp = {
		import POp._

		val opCode = readUI08()

		opCode match {
			case Select => error("Loops are not supported.")
			case KernelMetaData => PKernelMetaData(readMeta())
			case ParameterData => PParameterData(readParam())
			case ParameterMetaData => PParameterMetaData(readMeta())
			//case TextureData => PTextureData(null)
			case KernelName => PKernelName(readString())
			case VersionData => PVersionData(readUI32())
			case _ => error("Unknown opcode "+opCode+".")
		}
	}

	override def available() = input.available()

	override def close() = input.close()

	override def read() = input.read()

	override def read(b: Array[Byte]) = input.read(b)

	override def read(b: Array[Byte], off: Int, len: Int) = input.read(b, off, len)

	override def reset() = input.reset()

	override def skip(n: Long) = input.skip(n)
}