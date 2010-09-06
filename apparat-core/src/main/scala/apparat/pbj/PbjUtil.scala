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
object PbjUtil {
	val channels = Array(PChannelR, PChannelG, PChannelB, PChannelA, PChannelM2x2, PChannelM3x3, PChannelM4x4)
	
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

		createRegister(indexAndSwizzle & 0xFFFF, swizzle.toList)
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

	def createMatrixRegister(index: Int, matrix: Int) = createRegister(index & 0xFFFF, channels(matrix + 3) :: Nil)

	def createRegister(index: Int, swizzle: List[PChannel]) = index match {
		case x if 0 != (x & 0x8000) => PIntReg(index - 0x8000, swizzle)
		case y => PFloatReg(index, swizzle)
	}
}