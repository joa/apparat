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
	sealed trait PChannel
	case object PChannelR
	case object PChannelG
	case object PChannelB
	case object PChannelA
	case object PChannelM2x2
	case object PChannelM3x3
	case object PChannelM4x4

	sealed abstract class PType(val code: Int)

	sealed trait PTyped { def `type`: PType }

	sealed trait PConst extends PTyped

	sealed trait PParam extends PTyped {
		def name: String
		def meta: Array[PMeta]
	}

	case class PReg(index: Int, swizzle: Int, `type`: PType) extends PTyped

	case object PFloatType extends PType(0x01)
	case object PFloat2Type extends PType(0x02)
	case object PFloat3Type extends PType(0x03)
	case object PFloat4Type extends PType(0x04)
	case object PFloat2x2Type extends PType(0x05)
	case object PFloat3x3Type extends PType(0x06)
	case object PFloat4x4Type extends PType(0x07)
	case object PIntType extends PType(0x08)
	case object PInt2Type extends PType(0x09)
	case object PInt3Type extends PType(0x0a)
	case object PInt4Type extends PType(0x0b)
	case object PStringType extends PType(0x0c)
	case object PBoolType extends PType(0x0d)
	case object PBool2Type extends PType(0x0e)
	case object PBool3Type extends PType(0x0f)
	case object PBool4Type extends PType(0x10)

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

	case class PParameter(name: String, meta: Array[PMeta], `type`: PType, out: Boolean, register: PReg) extends PParam

	case class PTexture(name: String, meta: Array[PMeta], channels: Array[PChannel], index: Int) extends PParam {
		override def `type` = PFloat4Type
	}

	sealed abstract class POp(val opCode: Int) extends Product {
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

	case class PNop() extends POp(0x00)
	case class PAdd(dst: PReg, src: PReg) extends POp(0x01) with PDstAndSrc
	case class PSubtract(dst: PReg, src: PReg) extends POp(0x02) with PDstAndSrc
	case class PMultiply(dst: PReg, src: PReg) extends POp(0x03) with PDstAndSrc
	case class PReciprocal(dst: PReg, src: PReg) extends POp(0x04) with PDstAndSrc
	case class PDivide(dst: PReg, src: PReg) extends POp(0x05) with PDstAndSrc
	case class PAtan2(dst: PReg, src: PReg) extends POp(0x06) with PDstAndSrc
	case class PPow(dst: PReg, src: PReg) extends POp(0x07) with PDstAndSrc
	case class PMod(dst: PReg, src: PReg) extends POp(0x08) with PDstAndSrc
	case class PMin(dst: PReg, src: PReg) extends POp(0x09) with PDstAndSrc
	case class PMax(dst: PReg, src: PReg) extends POp(0x0A) with PDstAndSrc
	case class PStep(dst: PReg, src: PReg) extends POp(0x0B) with PDstAndSrc
	case class PSin(dst: PReg, src: PReg) extends POp(0x0C) with PDstAndSrc
	case class PCos(dst: PReg, src: PReg) extends POp(0x0D) with PDstAndSrc
	case class PTan(dst: PReg, src: PReg) extends POp(0x0E) with PDstAndSrc
	case class PASin(dst: PReg, src: PReg) extends POp(0x0F) with PDstAndSrc
	case class PACos(dst: PReg, src: PReg) extends POp(0x10) with PDstAndSrc
	case class PATan(dst: PReg, src: PReg) extends POp(0x11) with PDstAndSrc
	case class PExp(dst: PReg, src: PReg) extends POp(0x12) with PDstAndSrc
	case class PExp2(dst: PReg, src: PReg) extends POp(0x13) with PDstAndSrc
	case class PLog(dst: PReg, src: PReg) extends POp(0x14) with PDstAndSrc
	case class PLog2(dst: PReg, src: PReg) extends POp(0x15) with PDstAndSrc
	case class PSqrt(dst: PReg, src: PReg) extends POp(0x16) with PDstAndSrc
	case class PRSqrt(dst: PReg, src: PReg) extends POp(0x17) with PDstAndSrc
	case class PAbs(dst: PReg, src: PReg) extends POp(0x18) with PDstAndSrc
	case class PSign(dst: PReg, src: PReg) extends POp(0x19) with PDstAndSrc
	case class PFloor(dst: PReg, src: PReg) extends POp(0x1A) with PDstAndSrc
	case class PCeil(dst: PReg, src: PReg) extends POp(0x1B) with PDstAndSrc
	case class PFract(dst: PReg, src: PReg) extends POp(0x1C) with PDstAndSrc
	case class PCopy(dst: PReg, src: PReg) extends POp(0x1D) with PDstAndSrc
	case class PFloatToInt(dst: PReg, src: PReg) extends POp(0x1E) with PDstAndSrc
	case class PIntToFloat(dst: PReg, src: PReg) extends POp(0x1F) with PDstAndSrc
	case class PMatrixMatrixMultiply(dst: PReg, src: PReg) extends POp(0x20) with PDstAndSrc
	case class PVectorMatrixMultiply(dst: PReg, src: PReg) extends POp(0x21) with PDstAndSrc
	case class PMatrixVectorMultiply(dst: PReg, src: PReg) extends POp(0x22) with PDstAndSrc
	case class PNormalize(dst: PReg, src: PReg) extends POp(0x23) with PDstAndSrc
	case class PLength(dst: PReg, src: PReg) extends POp(0x24) with PDstAndSrc
	case class PDistance(dst: PReg, src: PReg) extends POp(0x25) with PDstAndSrc
	case class PDotProduct(dst: PReg, src: PReg) extends POp(0x26) with PDstAndSrc
	case class PCrossProduct(dst: PReg, src: PReg) extends POp(0x27) with PDstAndSrc
	case class PEqual(dst: PReg, src: PReg) extends POp(0x28) with PDstAndSrc
	case class PNotEqual(dst: PReg, src: PReg) extends POp(0x29) with PDstAndSrc
	case class PLessThan(dst: PReg, src: PReg) extends POp(0x2A) with PDstAndSrc
	case class PLessThanEqual(dst: PReg, src: PReg) extends POp(0x2B) with PDstAndSrc
	case class PLogicalNot(dst: PReg, src: PReg) extends POp(0x2C) with PDstAndSrc
	case class PLogicalAnd(dst: PReg, src: PReg) extends POp(0x2D) with PDstAndSrc
	case class PLogicalOr(dst: PReg, src: PReg) extends POp(0x2E) with PDstAndSrc
	case class PLogicalXor(dst: PReg, src: PReg) extends POp(0x2F) with PDstAndSrc
	case class PSampleNearest(dst: PReg, src: PReg, texture: Int) extends POp(0x30) with PDstAndSrc
	case class PSampleBilinear(dst: PReg, src: PReg, texture: Int) extends POp(0x31) with PDstAndSrc
	case class PLoadInt(dst: PReg, value: Int) extends POp(0x32) with PDst
	case class PLoadFloat(dst: PReg, value: Float) extends POp(0x32) with PDst
	case class PSelect(dst: PReg, src: PReg, src0: PReg, src1: PReg) extends POp(0x33) with PDstAndSrc
	case class PIf(condition: PReg) extends POp(0x34)
	case class PElse() extends POp(0x35)
	case class PEndif() extends POp(0x36)
	case class PFloatToBool(dst: PReg, src: PReg) extends POp(0x37) with PDstAndSrc
	case class PBoolToFloat(dst: PReg, src: PReg) extends POp(0x38) with PDstAndSrc
	case class PIntToBool(dst: PReg, src: PReg) extends POp(0x39) with PDstAndSrc
	case class PBoolToInt(dst: PReg, src: PReg) extends POp(0x3A) with PDstAndSrc
	case class PVectorEqual(dst: PReg, src: PReg) extends POp(0x3B) with PDstAndSrc
	case class PVectorNotEqual(dst: PReg, src: PReg) extends POp(0x3C) with PDstAndSrc
	case class PAny(dst: PReg, src: PReg) extends POp(0x3D) with PDstAndSrc
	case class PAll(dst: PReg, src: PReg) extends POp(0x3E) with PDstAndSrc
	/*
	case class PKernelMetaData extends POp(0xa0)
	case class PParameterData extends POp(0xa1)
	case class PParameterMetaData extends POp(0xa2)
	case class PTextureData extends POp(0xa3)
	case class PKernelName extends POp(0xa4)
	case class PVersionData extends POp(0xa5)*/
}