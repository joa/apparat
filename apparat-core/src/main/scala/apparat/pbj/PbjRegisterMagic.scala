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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.pbj

import apparat.pbj.pbjdata._
import collection.mutable.ListBuffer

/**
 * @author Joa Ebert
 */
protected[pbj] object PbjRegisterMagic {
	val channels = Array(PChannelR, PChannelG, PChannelB, PChannelA, PChannelM2x2, PChannelM3x3, PChannelM4x4)

	// Input oriented:

	def createSrcRegister(indexAndSwizzle: Int, size: Int) = {
		val sw = indexAndSwizzle >> 16
		val swizzle = ListBuffer.empty[PChannel]

		if(sw != 0x1B) {
			var i = 0
			while(i < size) {
				swizzle += channels((sw >> (6 - (i << 1))) & 3)
				i += 1
			}
		}

		createRegister(indexAndSwizzle & 0xffff, swizzle.toList)
	}

	def createDstRegister(index: Int, mask: Int) = {
		var swizzle = ListBuffer.empty[PChannel]
		
		if(mask != 0xf) {
			if((mask & 8) != 0) swizzle += PChannelR
			if((mask & 4) != 0) swizzle += PChannelG
			if((mask & 2) != 0) swizzle += PChannelB
			if((mask & 1) != 0) swizzle += PChannelA
		}

		createRegister(index, swizzle.toList)
	}

	def createMatrixRegister(index: Int, matrix: Int) = createRegister(index & 0xffff, channels(matrix + 3) :: Nil)

	def createRegister(index: Int, swizzle: List[PChannel]) = index match {
		case x if 0 != (x & 0x8000) => PIntReg(index - 0x8000, swizzle)
		case y => PFloatReg(index, swizzle)
	}

	// Output oriented:

	def registerCode(register: PReg) = register match {
		case PIntReg(index, _) => index + 0x8000
		case PFloatReg(index, _) => index
	}

	def matrixBits(`type`: PMatrix) = `type` match {
		case PFloat2x2Type => 1
		case PFloat3x3Type => 2
		case PFloat4x4Type => 3
	}

	def sizeBits(`type`: PNumeric) = `type` match {
		case PFloatType  | PIntType  | PBoolType  => 0
		case PFloat2Type | PInt2Type | PBool2Type => 1
		case PFloat3Type | PInt3Type | PBool3Type => 2
		case PFloat4Type | PInt4Type | PBool4Type => 3
		case PFloat2x2Type | PFloat3x3Type | PFloat4x4Type => 0
	}

	def dstMask(swizzle: List[PChannel]): Int = {
		if(Nil == swizzle) return 0xf
		var mask = 0

		for(channel <- swizzle) channel match {
			case PChannelR =>
				if(0 != mask) error("Cannot swizzle destination register.")
				mask |= 8
			case PChannelG =>
				if(0 != (mask & 7)) error("Cannot swizzle destination register.")
				mask |= 4
			case PChannelB =>
				if(0 != (mask & 3)) error("Cannot swizzle destination register.")
				mask |= 2
			case PChannelA =>
				if(0 != (mask & 1)) error("Cannot swizzle destination register.")
				mask |= 1
			case PChannelM4x4 | PChannelM3x3 | PChannelM2x2 => return 0
		}

		mask
	}

	def srcSwizzle(swizzle: List[PChannel], size: Int): Int = {
		if(Nil == swizzle) 0x1b
		else {
			var mask = 0

			for(channel <- swizzle) {
				mask <<= 2
				channel match {
					case PChannelR =>
					case PChannelG => mask |= 1
					case PChannelB => mask |= 2
					case PChannelA => mask |= 3
					case PChannelM4x4 | PChannelM3x3 | PChannelM2x2 => return 0
				}
			}

			mask << ((4 - size) << 1)
		}
	}
}