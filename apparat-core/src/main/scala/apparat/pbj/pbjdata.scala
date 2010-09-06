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

/**
 * @author Joa Ebert
 */
object pbjdata {
	object implicits {
		@inline implicit def intToType(value: Int): PType = value match {
			case 0x01 => PFloatType
			case 0x02 => PFloat2Type
			case 0x03 => PFloat3Type
			case 0x04 => PFloat4Type
			case 0x05 => PFloat2x2Type
			case 0x06 => PFloat3x3Type
			case 0x07 => PFloat4x4Type
			case 0x08 => PIntType
			case 0x09 => PInt2Type
			case 0x0a => PInt3Type
			case 0x0b => PInt4Type
			case 0x0c => PStringType
			case 0x0d => PBoolType
			case 0x0e => PBool2Type
			case 0x0f => PBool3Type
			case 0x10 => PBool4Type
			case _ => error("Invalid type "+value)
		}
		
		@inline implicit def typeToInt(value: PType): Int  = value match {
			case PFloatType => 0x01
			case PFloat2Type => 0x02
			case PFloat3Type => 0x03
			case PFloat4Type => 0x04
			case PFloat2x2Type => 0x05
			case PFloat3x3Type => 0x06
			case PFloat4x4Type => 0x07
			case PIntType => 0x08
			case PInt2Type => 0x09
			case PInt3Type => 0x0a
			case PInt4Type => 0x0b
			case PStringType => 0x0c
			case PBoolType => 0x0d
			case PBool2Type => 0x0e
			case PBool3Type => 0x0f
			case PBool4Type => 0x10
		}	
	}
	
	sealed trait PChannel
	case object PChannelR extends PChannel
	case object PChannelG extends PChannel
	case object PChannelB extends PChannel
	case object PChannelA extends PChannel
	case object PChannelM2x2 extends PChannel
	case object PChannelM3x3 extends PChannel
	case object PChannelM4x4 extends PChannel

	sealed abstract class PType(val code: Int)
	sealed abstract class PNumeric(code: Int) extends PType(code)
	sealed abstract class PMatrix(code: Int) extends PNumeric(code)

	sealed trait PTyped { def `type`: PType }

	sealed trait PConst extends PTyped

	sealed trait PParam extends PTyped {
		def name: String
		def register: PReg
	}

	sealed trait PReg {
		def index: Int
		def swizzle: List[PChannel]
	}

	case class PIntReg(index: Int, swizzle: List[PChannel]) extends PReg
	case class PFloatReg(index: Int, swizzle: List[PChannel]) extends PReg

	case object PFloatType extends PNumeric(0x01)
	case object PFloat2Type extends PNumeric(0x02)
	case object PFloat3Type extends PNumeric(0x03)
	case object PFloat4Type extends PNumeric(0x04)
	case object PFloat2x2Type extends PMatrix(0x05)
	case object PFloat3x3Type extends PMatrix(0x06)
	case object PFloat4x4Type extends PMatrix(0x07)
	case object PIntType extends PNumeric(0x08)
	case object PInt2Type extends PNumeric(0x09)
	case object PInt3Type extends PNumeric(0x0a)
	case object PInt4Type extends PNumeric(0x0b)
	case object PStringType extends PType(0x0c)
	case object PBoolType extends PNumeric(0x0d)
	case object PBool2Type extends PNumeric(0x0e)
	case object PBool3Type extends PNumeric(0x0f)
	case object PBool4Type extends PNumeric(0x10)

	case class PFloat(x: Float) extends PConst { override def `type` = PFloatType }
	case class PFloat2(x: Float, y: Float) extends PConst { override def `type` = PFloat2Type }
	case class PFloat3(x: Float, y: Float, z: Float) extends PConst { override def `type` = PFloat3Type }
	case class PFloat4(x: Float, y: Float, z: Float, w: Float) extends PConst { override def `type` = PFloat4Type }
	case class PInt(x: Int) extends PConst { override def `type` = PIntType }
	case class PInt2(x: Int, y: Int) extends PConst { override def `type` = PInt2Type }
	case class PInt3(x: Int, y: Int, z: Int) extends PConst { override def `type` = PInt3Type }
	case class PInt4(x: Int, y: Int, z: Int, w: Int) extends PConst { override def `type` = PInt4Type }
	case class PString(value: String) extends PConst { override def `type` = PStringType }
	case class PBool(x: Boolean) extends PConst { override def `type` = PBoolType }
	case class PBool2(x: Boolean, y: Boolean) extends PConst { override def `type` = PBool2Type }
	case class PBool3(x: Boolean, y: Boolean, z: Boolean) extends PConst { override def `type` = PBool3Type }
	case class PBool4(x: Boolean, y: Boolean, z: Boolean, w: Boolean) extends PConst { override def `type` = PBool4Type }

	case class PFloat2x2(
			v00: Float, v01: Float,
			v10: Float, v11: Float) extends PConst {
		override def `type` = PFloat2x2Type
		def apply(index: Int) = index match {
			case 0 => v00
			case 1 => v01
			case 2 => v10
			case 3 => v11
			case _ => error("Index "+index+" is out of bounds.")
		}
	}

	case class PFloat3x3(
				v00: Float, v01: Float, v02: Float,
				v10: Float, v11: Float, v12: Float,
				v20: Float, v21: Float, v22: Float) extends PConst {
		override def `type` = PFloat3x3Type
		def apply(index: Int) = index match {
			case 0 => v00
			case 1 => v01
			case 2 => v02
			case 3 => v10
			case 4 => v11
			case 5 => v12
			case 6 => v20
			case 7 => v21
			case 8 => v22
			case _ => error("Index "+index+" is out of bounds.")
		}
	}

	case class PFloat4x4(
				v00: Float, v01: Float, v02: Float, v03: Float,
				v10: Float, v11: Float, v12: Float, v13: Float,
				v20: Float, v21: Float, v22: Float, v23: Float,
				v30: Float, v31: Float, v32: Float, v33: Float) extends PConst {
		override def `type` = PFloat4x4Type
		def apply(index: Int) = index match {
			case  0 => v00
			case  1 => v01
			case  2 => v02
			case  3 => v03
			case  4 => v10
			case  5 => v11
			case  6 => v12
			case  7 => v13
			case  8 => v20
			case  9 => v21
			case 10 => v22
			case 11 => v23
			case 12 => v30
			case 13 => v31
			case 14 => v32
			case 15 => v33
			case _ => error("Index "+index+" is out of bounds.")
		}
	}

	case class PMeta(key: String, value: PConst)

	case class PInParameter(name: String, `type`: PNumeric, register: PReg) extends PParam
	case class POutParameter(name: String, `type`: PNumeric, register: PReg) extends PParam

	case class PTexture(name: String, index: Int, channels: Int) extends PTyped {
		override def `type` = channels match {
			case 1 => PFloatType
			case 2 => PFloat2Type
			case 3 => PFloat3Type
			case 4 => PFloat4Type
			case _ => error("Invalid channel selector.")
		}
	}

	object POp {
		val Nop =  0x00
		val Add = 0x01
		val Subtract = 0x02
		val Multiply = 0x03
		val Reciprocal = 0x04
		val Divide = 0x05
		val Atan2 = 0x06
		val Pow = 0x07
		val Mod = 0x08
		val Min = 0x09
		val Max = 0x0A
		val Step = 0x0B
		val Sin = 0x0C
		val Cos = 0x0D
		val Tan = 0x0E
		val ASin = 0x0F
		val ACos = 0x10
		val ATan = 0x11
		val Exp = 0x12
		val Exp2 = 0x13
		val Log = 0x14
		val Log2 = 0x15
		val Sqrt = 0x16
		val RSqrt = 0x17
		val Abs = 0x18
		val Sign = 0x19
		val Floor = 0x1A
		val Ceil = 0x1B
		val Fract = 0x1C
		val Copy = 0x1D
		val FloatToInt = 0x1E
		val IntToFloat = 0x1F
		val MatrixMatrixMultiply = 0x20
		val VectorMatrixMultiply = 0x21
		val MatrixVectorMultiply = 0x22
		val Normalize = 0x23
		val Length = 0x24
		val Distance = 0x25
		val DotProduct = 0x26
		val CrossProduct = 0x27
		val Equal = 0x28
		val NotEqual = 0x29
		val LessThan = 0x2A
		val LessThanEqual = 0x2B
		val LogicalNot = 0x2C
		val LogicalAnd = 0x2D
		val LogicalOr = 0x2E
		val LogicalXor = 0x2F
		val SampleNearest = 0x30
		val SampleBilinear = 0x31
		val LoadConstant = 0x32
		val Select = 0x33
		val If = 0x34
		val Else = 0x35
		val Endif = 0x36
		val FloatToBool = 0x37
		val BoolToFloat = 0x38
		val IntToBool = 0x39
		val BoolToInt = 0x3A
		val VectorEqual = 0x3B
		val VectorNotEqual = 0x3C
		val Any = 0x3D
		val All = 0x3E
		val KernelMetaData = 0xa0
		val ParameterData = 0xa1
		val ParameterMetaData = 0xa2
		val TextureData = 0xa3
		val KernelName = 0xa4
		val VersionData = 0xa5
	}

	sealed trait POpCode { def opCode: Int }
	sealed abstract class POp(val opCode: Int) extends Product with POpCode {
		override def equals(that: Any) = that match {
			case op: POp => op eq this
			case _ => false
		}

		def ~==(that: POp): Boolean = {
			if (opCode == that.opCode && productArity == that.productArity) {
				var i = 0
				val n = productArity

				while(i < n) {
					if(this.productElement(i) != that.productElement(i)) {
						return false
					}

					i += 1
				}

				true
			} else {
				false
			}
		}
	}

	sealed trait PDst { def dst: PReg }
	sealed trait PSrc { def src: PReg }
	sealed trait PDstAndSrc extends PSrc with PDst

	case class PNop() extends POp(POp.Nop)
	case class PAdd(dst: PReg, src: PReg) extends POp(POp.Add) with PDstAndSrc
	case class PSubtract(dst: PReg, src: PReg) extends POp(POp.Subtract) with PDstAndSrc
	case class PMultiply(dst: PReg, src: PReg) extends POp(POp.Multiply) with PDstAndSrc
	case class PReciprocal(dst: PReg, src: PReg) extends POp(POp.Reciprocal) with PDstAndSrc
	case class PDivide(dst: PReg, src: PReg) extends POp(POp.Divide) with PDstAndSrc
	case class PAtan2(dst: PReg, src: PReg) extends POp(POp.Atan2) with PDstAndSrc
	case class PPow(dst: PReg, src: PReg) extends POp(POp.Pow) with PDstAndSrc
	case class PMod(dst: PReg, src: PReg) extends POp(POp.Mod) with PDstAndSrc
	case class PMin(dst: PReg, src: PReg) extends POp(POp.Min) with PDstAndSrc
	case class PMax(dst: PReg, src: PReg) extends POp(POp.Max) with PDstAndSrc
	case class PStep(dst: PReg, src: PReg) extends POp(POp.Step) with PDstAndSrc
	case class PSin(dst: PReg, src: PReg) extends POp(POp.Sin) with PDstAndSrc
	case class PCos(dst: PReg, src: PReg) extends POp(POp.Cos) with PDstAndSrc
	case class PTan(dst: PReg, src: PReg) extends POp(POp.Tan) with PDstAndSrc
	case class PASin(dst: PReg, src: PReg) extends POp(POp.ASin) with PDstAndSrc
	case class PACos(dst: PReg, src: PReg) extends POp(POp.ACos) with PDstAndSrc
	case class PATan(dst: PReg, src: PReg) extends POp(POp.ATan) with PDstAndSrc
	case class PExp(dst: PReg, src: PReg) extends POp(POp.Exp) with PDstAndSrc
	case class PExp2(dst: PReg, src: PReg) extends POp(POp.Exp2) with PDstAndSrc
	case class PLog(dst: PReg, src: PReg) extends POp(POp.Log) with PDstAndSrc
	case class PLog2(dst: PReg, src: PReg) extends POp(POp.Log2) with PDstAndSrc
	case class PSqrt(dst: PReg, src: PReg) extends POp(POp.Sqrt) with PDstAndSrc
	case class PRSqrt(dst: PReg, src: PReg) extends POp(POp.RSqrt) with PDstAndSrc
	case class PAbs(dst: PReg, src: PReg) extends POp(POp.Abs) with PDstAndSrc
	case class PSign(dst: PReg, src: PReg) extends POp(POp.Sign) with PDstAndSrc
	case class PFloor(dst: PReg, src: PReg) extends POp(POp.Floor) with PDstAndSrc
	case class PCeil(dst: PReg, src: PReg) extends POp(POp.Ceil) with PDstAndSrc
	case class PFract(dst: PReg, src: PReg) extends POp(POp.Fract) with PDstAndSrc
	case class PCopy(dst: PReg, src: PReg) extends POp(POp.Copy) with PDstAndSrc
	case class PFloatToInt(dst: PReg, src: PReg) extends POp(POp.FloatToInt) with PDstAndSrc
	case class PIntToFloat(dst: PReg, src: PReg) extends POp(POp.IntToFloat) with PDstAndSrc
	case class PMatrixMatrixMultiply(dst: PReg, src: PReg) extends POp(POp.MatrixMatrixMultiply) with PDstAndSrc
	case class PVectorMatrixMultiply(dst: PReg, src: PReg) extends POp(POp.VectorMatrixMultiply) with PDstAndSrc
	case class PMatrixVectorMultiply(dst: PReg, src: PReg) extends POp(POp.MatrixVectorMultiply) with PDstAndSrc
	case class PNormalize(dst: PReg, src: PReg) extends POp(POp.Normalize) with PDstAndSrc
	case class PLength(dst: PReg, src: PReg) extends POp(POp.Length) with PDstAndSrc
	case class PDistance(dst: PReg, src: PReg) extends POp(POp.Distance) with PDstAndSrc
	case class PDotProduct(dst: PReg, src: PReg) extends POp(POp.DotProduct) with PDstAndSrc
	case class PCrossProduct(dst: PReg, src: PReg) extends POp(POp.CrossProduct) with PDstAndSrc
	case class PEqual(dst: PReg, src: PReg) extends POp(POp.Equal) with PDstAndSrc
	case class PNotEqual(dst: PReg, src: PReg) extends POp(POp.NotEqual) with PDstAndSrc
	case class PLessThan(dst: PReg, src: PReg) extends POp(POp.LessThan) with PDstAndSrc
	case class PLessThanEqual(dst: PReg, src: PReg) extends POp(POp.LessThanEqual) with PDstAndSrc
	case class PLogicalNot(dst: PReg, src: PReg) extends POp(POp.LogicalNot) with PDstAndSrc
	case class PLogicalAnd(dst: PReg, src: PReg) extends POp(POp.LogicalAnd) with PDstAndSrc
	case class PLogicalOr(dst: PReg, src: PReg) extends POp(POp.LogicalOr) with PDstAndSrc
	case class PLogicalXor(dst: PReg, src: PReg) extends POp(POp.LogicalXor) with PDstAndSrc
	case class PSampleNearest(dst: PReg, src: PReg, texture: Int) extends POp(POp.SampleNearest) with PDstAndSrc
	case class PSampleBilinear(dst: PReg, src: PReg, texture: Int) extends POp(POp.SampleBilinear) with PDstAndSrc
	case class PLoadInt(dst: PReg, value: Int) extends POp(POp.LoadConstant) with PDst
	case class PLoadFloat(dst: PReg, value: Float) extends POp(POp.LoadConstant) with PDst
	//case class PSelect(dst: PReg, src: PReg, src0: PReg, src1: PReg) extends POp(POp.Select) with PDstAndSrc
	case class PIf(condition: PReg) extends POp(POp.If)
	case class PElse() extends POp(POp.Else)
	case class PEndif() extends POp(POp.Endif)
	case class PFloatToBool(dst: PReg, src: PReg) extends POp(POp.FloatToBool) with PDstAndSrc
	case class PBoolToFloat(dst: PReg, src: PReg) extends POp(POp.BoolToFloat) with PDstAndSrc
	case class PIntToBool(dst: PReg, src: PReg) extends POp(POp.IntToBool) with PDstAndSrc
	case class PBoolToInt(dst: PReg, src: PReg) extends POp(POp.BoolToInt) with PDstAndSrc
	case class PVectorEqual(dst: PReg, src: PReg) extends POp(POp.VectorEqual) with PDstAndSrc
	case class PVectorNotEqual(dst: PReg, src: PReg) extends POp(POp.VectorNotEqual) with PDstAndSrc
	case class PAny(dst: PReg, src: PReg) extends POp(POp.Any) with PDstAndSrc
	case class PAll(dst: PReg, src: PReg) extends POp(POp.All) with PDstAndSrc
	case class PKernelMetaData(meta: PMeta) extends POp(POp.KernelMetaData) {
		require(meta.value.`type` == PIntType || meta.value.`type` == PStringType,
			"Kernel metadata must be either of type integer or String.")
	}
	case class PParameterData(param: PParam) extends POp(POp.ParameterData)
	case class PParameterMetaData(meta: PMeta) extends POp(POp.ParameterMetaData)
	case class PTextureData(texture: PTexture) extends POp(POp.TextureData)
	case class PKernelName(name: String) extends POp(POp.KernelName)
	case class PVersionData(version: Int) extends POp(POp.VersionData) {
		require(1 == version, "Only PixelBender kernel version \"1\" is supported, got "+version+".")
	}
}