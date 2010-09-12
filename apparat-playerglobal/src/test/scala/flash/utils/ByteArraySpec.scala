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
package flash.utils

import org.specs.SpecificationWithJUnit

/**
 * @author Joa Ebert
 */
class ByteArraySpec extends SpecificationWithJUnit {
	"ByteArray" can {
		"be created" in {
			new ByteArray() must notBeNull
		}

		"write and read one floating point value" in {
			val byteArray = new ByteArray()
			val input = 1.0
			byteArray.writeFloat(input)
			byteArray.position(0)
			val result = byteArray.readFloat()
			result mustEqual input
		}

		"write and read two floating point values" in {
			val byteArray = new ByteArray()
			val input = 1.0
			byteArray.writeFloat(input)
			byteArray.writeFloat(input)
			byteArray.position(0)
			byteArray.readFloat() mustEqual input
			byteArray.readFloat() mustEqual input
		}

		"write N floats" in {
			val byteArray = new ByteArray()
			val N = 8192
			for(j <- 0 to 2) {
				byteArray.position(0)
				for(i <- 1 to N) byteArray.writeFloat(i)
				var sum = 0.0
				byteArray.position(0)
				for(i <- 1 to N) sum += byteArray.readFloat()
				sum mustEqual (N * (N + 1.0) / 2.0)
			}
		}
	}

	"ByteArray" should {
		"be empty after creation" in {
			val byteArray = new ByteArray()
			byteArray.length mustEqual 0L
			byteArray.position mustEqual 0L
			byteArray.bytesAvailable mustEqual 0L
		}

		"be initialized with little endian" in {
			new ByteArray().endian mustEqual Endian.LITTLE_ENDIAN
		}

		"be empty after clear" in {
			val byteArray = new ByteArray()
			byteArray.clear()
			byteArray.length mustEqual 0L
			byteArray.position mustEqual 0L
			byteArray.bytesAvailable mustEqual 0L
		}

		"extend its size automatically" in {
			val byteArray = new ByteArray()
			byteArray.writeFloat(0.0)
			byteArray.length mustEqual 4L
			byteArray.position mustEqual 4L
			byteArray.bytesAvailable mustEqual 0L
		}
	}
}