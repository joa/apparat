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

import java.io.{InputStream => JInputStream}
import apparat.pbj.pbjdata.implicits._
import apparat.pbj.pbjdata._
import annotation.tailrec
import apparat.utils.IO

/**
 * @author Joa Ebert
 */
class PbjInputStream(input: JInputStream) extends JInputStream {
	def foreach(body: POp => Unit) = while(available() > 0) { body(readOp()) }

	@inline private def signed(mask: Long, r: Long): Int = {
		if (0 != (r & mask)) ((r & (mask - 1L)) - mask).asInstanceOf[Int]
		else r.asInstanceOf[Int]
	}

	def readFloat() = java.lang.Float.intBitsToFloat((read() << 0x18) | (read() << 0x10) | (read() << 0x08) | read())

	def readString() = {
		@tailrec def loop(seq: List[Byte]): List[Byte] = readUI08() match {
			case 0 => seq
			case y => loop(y.asInstanceOf[Byte] :: seq)
		}

		new String(loop(Nil).reverse.toArray, "UTF8")
	}

	def readUI08(): Int = read()

	def readUI16(): Int = read() | (read() << 0x08)

	def readUI24(): Int = read() | (read() << 0x08) | (read() << 0x10)

	def readUI32(): Long = read() | (read() << 0x08) | (read() << 0x10) | (read() << 0x18)

	def readSI32(): Int = signed(0x80000000L, readUI32())

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
		import PbjRegisterMagic._
		val qualifier = readUI08()
		val `type` = readType() match {
			case PStringType => error("Parameter must not be of type String.")
			case x: PNumeric => x
		}
		val register = readUI16()
		val mask = `type` match {
			case PFloat2x2Type => assert(readUI08() == 2); 0xf
			case PFloat3x3Type => assert(readUI08() == 3); 0xf
			case PFloat4x4Type => assert(readUI08() == 4); 0xf
			case _ => val b = readUI08(); assert((b >> 4) == 0); b
		}
		val name = readString()

		qualifier match {
			case 1 => PInParameter(name, `type`, createDstRegister(register, mask))
			case 2 => POutParameter(name, `type`, createDstRegister(register, mask))
			case _ => error("Qualifier must be either \"1\" or \"2\".")
		}
	}

	def readTexture() = {
		val index = readUI08()
		val channels = readUI08()
		val name = readString()
		PTexture(name, index, channels)
	}

	def readOp(): POp = {
		import POp._
		import PbjRegisterMagic._

		@inline def create(f: (PReg, PReg) => POp with PDstAndSrc): POp with PDstAndSrc = {
			val dstIndex = readUI16()
			val mask = readUI08()
			val swizzle = mask >> 4
			val size = (mask & 3) + 1
			val matrix = (mask >> 2) & 3
			val srcIndex = readUI24()

			assert(0 == readUI08())

			if(0 != matrix) {
				assert(0 == (srcIndex >> 16))
				assert(1 == size)
				f(	if(mask == 0) {
						createMatrixRegister(dstIndex, matrix)
					} else {
						createDstRegister(dstIndex, swizzle)
					}, createMatrixRegister(srcIndex, matrix))
			} else {
				f(createDstRegister(dstIndex, swizzle), createSrcRegister(srcIndex, size))
			}
		}

		@inline def sample(f: (PReg, PReg, Int) => POp with PDstAndSrc): POp with PDstAndSrc = {
			val dstIndex = readUI16()
			val mask = readUI08()
			val srcIndex = readUI24()
			val texture = readUI08()
			assert(1 == (mask & 0xf))
			f(createDstRegister(dstIndex, mask >> 4), createSrcRegister(srcIndex, 2), texture)
		}

		@inline def skipBytesAndReturn[@specialized A](x: => A): A = {
			assert(0 == readUI24())
			assert(0 == readUI32())
			x
		}

		readUI08() match {
			case Nop => skipBytesAndReturn { PNop() }
			case Add => create(PAdd(_, _))
			case Subtract => create(PSubtract(_, _))
			case Multiply => create(PMultiply(_, _))
			case Reciprocal => create(PReciprocal(_, _))
			case Divide => create(PDivide(_, _))
			case Atan2 => create(PAtan2(_, _))
			case Pow => create(PPow(_, _))
			case Mod => create(PMod(_, _))
			case Min => create(PMin(_, _))
			case Max => create(PMax(_, _))
			case Step => create(PStep(_, _))
			case Sin => create(PSin(_, _))
			case Cos => create(PCos(_, _))
			case Tan => create(PTan(_, _))
			case ASin => create(PASin(_, _))
			case ACos => create(PACos(_, _))
			case ATan => create(PATan(_, _))
			case Exp => create(PExp(_, _))
			case Exp2 => create(PExp2(_, _))
			case Log => create(PLog(_, _))
			case Log2 => create(PLog2(_, _))
			case Sqrt => create(PSqrt(_, _))
			case RSqrt => create(PRSqrt(_, _))
			case Abs => create(PAbs(_, _))
			case Sign => create(PSign(_, _))
			case Floor => create(PFloor(_, _))
			case Ceil => create(PCeil(_, _))
			case Fract => create(PFract(_, _))
			case Copy => create(PCopy(_, _))
			case FloatToInt => create(PFloatToInt(_, _))
			case IntToFloat => create(PIntToFloat(_, _))
			case MatrixMatrixMultiply => create(PMatrixMatrixMultiply(_, _))
			case VectorMatrixMultiply => create(PVectorMatrixMultiply(_, _))
			case MatrixVectorMultiply => create(PMatrixVectorMultiply(_, _))
			case Normalize => create(PNormalize(_, _))
			case Length => create(PLength(_, _))
			case Distance => create(PDistance(_, _))
			case DotProduct => create(PDotProduct(_, _))
			case CrossProduct => create(PCrossProduct(_, _))
			case Equal => create(PEqual(_, _))
			case NotEqual => create(PNotEqual(_, _))
			case LessThan => create(PLessThan(_, _))
			case LessThanEqual => create(PLessThanEqual(_, _))
			case LogicalNot => create(PLogicalNot(_, _))
			case LogicalAnd => create(PLogicalAnd(_, _))
			case LogicalOr => create(PLogicalOr(_, _))
			case LogicalXor => create(PLogicalXor(_, _))
			case SampleNearest => sample(PSampleNearest(_, _, _))
			case SampleBilinear => sample(PSampleBilinear(_, _, _))
			case LoadConstant => {
				val dstIndex = readUI16()
				val mask = readUI08()
				assert(0 == (mask & 0xf))
				createDstRegister(dstIndex, mask >> 4) match {
					case r @ PIntReg(_, _) => PLoadInt(r, readSI32())
					case r @ PFloatReg(_, _) => PLoadFloat(r, readFloat())
				}
			}
			case Select =>
				val dstIndex = readUI16()
				val mask = readUI08()
				val swizzle = mask >> 4
				val size = (mask & 3) + 1
				val matrix = (mask >> 2) & 3
				val srcIndex = readUI24()
				assert(0 == readUI08())

				val src0Index = readUI24()
				assert(0 == readUI08())

				val src1Index = readUI24()
				assert(0 == readUI08())

				if(0 != matrix) {
					error("Matrix not expected.")
				}

				PSelect(createDstRegister(dstIndex, swizzle),
					createSrcRegister(srcIndex, size),
					createSrcRegister(src0Index, size),
					createSrcRegister(src1Index, size))
			case If => {
				assert(0 == readUI24())
				val src = readUI24()
				assert(0 == readUI08())
				PIf(createSrcRegister(src,1))
			}
			case Else => skipBytesAndReturn { PElse() }
			case Endif => skipBytesAndReturn { PEndif() }
			case FloatToBool => create(PFloatToBool(_, _))
			case BoolToFloat => create(PBoolToFloat(_, _))
			case IntToBool => create(PIntToBool(_, _))
			case BoolToInt => create(PBoolToInt(_, _))
			case VectorEqual => create(PVectorEqual(_, _))
			case VectorNotEqual => create(PVectorNotEqual(_, _))
			case Any => create(PAny(_, _))
			case All => create(PAll(_, _))
			case KernelMetaData => PKernelMetaData(readMeta())
			case ParameterData => PParameterData(readParam())
			case ParameterMetaData => PParameterMetaData(readMeta())
			case TextureData => PTextureData(readTexture())
			case KernelName => PKernelName(new String(IO.read(readUI16())(this), "UTF8"))
			case VersionData => PVersionData(readUI32().asInstanceOf[Int])
			case other => error("Unknown opcode "+other+".")
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
