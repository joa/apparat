/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.pbj

import java.io.{OutputStream => JOutputStream}
import apparat.pbj.pbjdata.implicits._
import apparat.pbj.pbjdata._

/**
 * @author Joa Ebert
 */
class PbjOutputStream(val output: JOutputStream) extends JOutputStream {
	def writeFloat(value: Float): Unit = {
		val bits = java.lang.Float.floatToRawIntBits(value)
		write((bits & 0xff000000) >> 0x18)
		write((bits & 0x00ff0000) >> 0x10)
		write((bits & 0x0000ff00) >> 0x08)
		write( bits & 0x000000ff)
	}

	def writeString(value: String): Unit = {
		value.getBytes("UTF8") foreach writeUI08
		writeUI08(0x00)
	}

	def writeUI08(value: Byte): Unit = write(value)

	def writeUI08(value: Int): Unit = write(value & 0xff)

	def writeUI16(value: Int): Unit = {
		write(value & 0xff)
		write((value & 0xff00) >> 0x08)
	}

	def writeUI24(value: Int): Unit = {
		write(value & 0xff)
		write((value & 0xff00) >> 0x08)
		write((value & 0xff0000) >> 0x10)
	}

	def writeUI32(value: Long): Unit = writeSI32((value & 0xffffffffL).asInstanceOf[Int])

	def writeSI32(value: Int): Unit =  {
		write(value & 0xff)
		write((value & 0xff00) >> 0x08)
		write((value & 0xff0000) >> 0x10)
		write((value & 0xff000000) >> 0x18)
	}

	def writeConst(value: PConst): Unit = value match {
		case PFloat(v0) => writeFloat(v0)
		case PFloat2(v0, v1)=> writeFloat(v0); writeFloat(v1)
		case PFloat3(v0, v1, v2) => writeFloat(v0); writeFloat(v1); writeFloat(v2)
		case PFloat4(v0, v1, v2, v3) => writeFloat(v0); writeFloat(v1); writeFloat(v2); writeFloat(v3)
		case value: PFloat2x2 => for(i <- 0 until  4) writeFloat(value(i))
		case value: PFloat3x3 => for(i <- 0 until  9) writeFloat(value(i))
		case value: PFloat4x4 => for(i <- 0 until 16) writeFloat(value(i))
		case PInt(v0) => writeUI16(v0)
		case PInt2(v0, v1)=> writeUI16(v0); writeUI16(v1)
		case PInt3(v0, v1, v2) => writeUI16(v0); writeUI16(v1); writeUI16(v2)
		case PInt4(v0, v1, v2, v3) => writeUI16(v0); writeUI16(v1); writeUI16(v2); writeUI16(v3)
		case PString(value) => writeString(value)
		case PBool(v0) => writeUI16(if(v0) 1 else 0)
		case PBool2(v0, v1)=> writeUI16(if(v0) 1 else 0); writeUI16(if(v1) 1 else 0)
		case PBool3(v0, v1, v2) => writeUI16(if(v0) 1 else 0); writeUI16(if(v1) 1 else 0); writeUI16(if(v2) 1 else 0)
		case PBool4(v0, v1, v2, v3) => writeUI16(if(v0) 1 else 0); writeUI16(if(v1) 1 else 0); writeUI16(if(v2) 1 else 0); writeUI16(if(v3) 1 else 0)
	}

	def writeType(value: PType): Unit = writeUI08(value)

	def writeMeta(value: PMeta): Unit = {
		writeType(value.value.`type`)
		writeString(value.key)
		writeConst(value.value)
	}

	def writeParam(value: PParam): Unit = {
		import PbjRegisterMagic._

		val swizzle = value.register.swizzle

		writeUI08(value match {
			case PInParameter(_, _, _) => 1
			case POutParameter(_, _, _) => 2
		})

		writeType(value.`type`)
		writeUI16(registerCode(value.register))

		value.`type` match {
			case PFloat2x2Type => assert(Nil == swizzle); writeUI08(2)
			case PFloat3x3Type => assert(Nil == swizzle); writeUI08(3)
			case PFloat4x4Type => assert(Nil == swizzle); writeUI08(4)
			case _ => writeUI08(dstMask(swizzle))
		}

		writeString(value.name)
	}

	def writeTexture(value: PTexture): Unit = {
		writeUI08(value.index)
		writeUI08(value.channels)
		writeString(value.name)
	}

	def writeOp(value: POp): Unit = {
		import PbjRegisterMagic._

		@inline def writeDst(register: PReg, size: Int) = {
			var mask = dstMask(register.swizzle)
			writeUI16(registerCode(register))
			writeUI08(mask << 4 | size)
		}

		@inline def writeSrc(register: PReg, size: Int) = {
			writeUI16(registerCode(register))
			writeUI08(srcSwizzle(register.swizzle, size))
		}

		@inline def writeDstAndSrc(dst: PReg, src: PReg): Unit = {
			val swizzle = src.swizzle
			var maskBits = dstMask(dst.swizzle)
			var sizeBits = (if(Nil == swizzle) 4 else swizzle.length) - 1

			if(Nil != swizzle && swizzle.length == 1) swizzle.head match {
				case PChannelM2x2 => sizeBits = 4
				case PChannelM3x3 => sizeBits = 8
				case PChannelM4x4 => sizeBits = 12
				case _ =>
			}

			writeUI16(registerCode(dst))
			writeUI08(maskBits << 4 | sizeBits)
			writeUI16(registerCode(src))
			writeUI08(srcSwizzle(swizzle, if(Nil == swizzle) 4 else swizzle.length))
			writeUI08(0)
		}

		@inline def sample(dst: PReg, src: PReg, texture: Int) = {
			writeDst(dst, 1)
			writeSrc(src, 2)
			writeUI08(texture)
		}

		@inline def skipBytes() = {
			writeUI24(0)
			writeUI32(0)
		}

		writeUI08(value.opCode)

		value match {
			case PNop() => skipBytes()
			case PAdd(dst, src) => writeDstAndSrc(dst, src)
			case PSubtract(dst, src) => writeDstAndSrc(dst, src)
			case PMultiply(dst, src) => writeDstAndSrc(dst, src)
			case PReciprocal(dst, src) => writeDstAndSrc(dst, src)
			case PDivide(dst, src) => writeDstAndSrc(dst, src)
			case PAtan2(dst, src) => writeDstAndSrc(dst, src)
			case PPow(dst, src) => writeDstAndSrc(dst, src)
			case PMod(dst, src) => writeDstAndSrc(dst, src)
			case PMin(dst, src) => writeDstAndSrc(dst, src)
			case PMax(dst, src) => writeDstAndSrc(dst, src)
			case PStep(dst, src) => writeDstAndSrc(dst, src)
			case PSin(dst, src) => writeDstAndSrc(dst, src)
			case PCos(dst, src) => writeDstAndSrc(dst, src)
			case PTan(dst, src) => writeDstAndSrc(dst, src)
			case PASin(dst, src) => writeDstAndSrc(dst, src)
			case PACos(dst, src) => writeDstAndSrc(dst, src)
			case PATan(dst, src) => writeDstAndSrc(dst, src)
			case PExp(dst, src) => writeDstAndSrc(dst, src)
			case PExp2(dst, src) => writeDstAndSrc(dst, src)
			case PLog(dst, src) => writeDstAndSrc(dst, src)
			case PLog2(dst, src) => writeDstAndSrc(dst, src)
			case PSqrt(dst, src) => writeDstAndSrc(dst, src)
			case PRSqrt(dst, src) => writeDstAndSrc(dst, src)
			case PAbs(dst, src) => writeDstAndSrc(dst, src)
			case PSign(dst, src) => writeDstAndSrc(dst, src)
			case PFloor(dst, src) => writeDstAndSrc(dst, src)
			case PCeil(dst, src) => writeDstAndSrc(dst, src)
			case PFract(dst, src) => writeDstAndSrc(dst, src)
			case PCopy(dst, src) => writeDstAndSrc(dst, src)
			case PFloatToInt(dst, src) => writeDstAndSrc(dst, src)
			case PIntToFloat(dst, src) => writeDstAndSrc(dst, src)
			case PMatrixMatrixMultiply(dst, src) => writeDstAndSrc(dst, src)
			case PVectorMatrixMultiply(dst, src) => writeDstAndSrc(dst, src)
			case PMatrixVectorMultiply(dst, src) => writeDstAndSrc(dst, src)
			case PNormalize(dst, src) => writeDstAndSrc(dst, src)
			case PLength(dst, src) => writeDstAndSrc(dst, src)
			case PDistance(dst, src) => writeDstAndSrc(dst, src)
			case PDotProduct(dst, src) => writeDstAndSrc(dst, src)
			case PCrossProduct(dst, src) => writeDstAndSrc(dst, src)
			case PEqual(dst, src) => writeDstAndSrc(dst, src)
			case PNotEqual(dst, src) => writeDstAndSrc(dst, src)
			case PLessThan(dst, src) => writeDstAndSrc(dst, src)
			case PLessThanEqual(dst, src) => writeDstAndSrc(dst, src)
			case PLogicalNot(dst, src) => writeDstAndSrc(dst, src)
			case PLogicalAnd(dst, src) => writeDstAndSrc(dst, src)
			case PLogicalOr(dst, src) => writeDstAndSrc(dst, src)
			case PLogicalXor(dst, src) => writeDstAndSrc(dst, src)
			case PSampleNearest(dst, src, texture: Int) => sample(dst, src, texture)
			case PSampleBilinear(dst, src, texture: Int) => sample(dst, src, texture)
			case PLoadInt(dst: PReg, value: Int) => {
				writeDst(dst, 0)
				writeSI32(value)
			}
			case PLoadFloat(dst: PReg, value: Float) => {
				writeDst(dst, 0)
				writeFloat(value)
			}
			case PSelect(dst, src, src0, src1) =>
				writeUI08(dst.code)
				writeUI08(src.code)
				writeUI08(src0.code)
				writeUI08(src1.code)
			case PIf(condition) => {
				writeUI24(0)
				writeSrc(condition, 1)
				writeUI08(0)
			}
			case PElse() => skipBytes()
			case PEndif() => skipBytes()
			case PFloatToBool(dst, src) => writeDstAndSrc(dst, src)
			case PBoolToFloat(dst, src) => writeDstAndSrc(dst, src)
			case PIntToBool(dst, src) => writeDstAndSrc(dst, src)
			case PBoolToInt(dst, src) => writeDstAndSrc(dst, src)
			case PVectorEqual(dst, src) => writeDstAndSrc(dst, src)
			case PVectorNotEqual(dst, src) => writeDstAndSrc(dst, src)
			case PAny(dst, src) => writeDstAndSrc(dst, src)
			case PAll(dst, src) => writeDstAndSrc(dst, src)
			case PKernelMetaData(meta) =>
				require(meta.value.`type` == PIntType || meta.value.`type` == PStringType,
					"Kernel metadata must be either of type integer or String.")
				writeMeta(meta)
			case PParameterData(param) => writeParam(param)
			case PParameterMetaData(meta) =>  writeMeta(meta)
			case PTextureData(texture) => writeTexture(texture)
			case PKernelName(name) => {
				val bytes = name.getBytes("UTF8")
				writeUI16(bytes.length)
				bytes foreach writeUI08
			}
			case PVersionData(version) =>
				require(1 == version, "Only PixelBender kernel version \"1\" is supported, got "+version+".")
				writeUI32(version)
		}
	}

	override def close() = output.close()

	override def flush() = output.flush()

	override def write(value: Array[Byte]) = output.write(value)

	override def write(value: Array[Byte], offset: Int, length: Int) = output.write(value, offset, length)

	override def write(value: Int) = output.write(value)
}
