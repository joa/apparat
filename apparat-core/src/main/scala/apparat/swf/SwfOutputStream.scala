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
package apparat.swf

import java.io.{
OutputStream => JOutputStream,
ByteArrayOutputStream => JByteArrayOutputStream
}

import scala.math.{max, min}

class SwfOutputStream(val output: JOutputStream) extends JOutputStream {
	private var bitBuffer: Int = 0
	private var bitIndex: Int = 0

	@inline private def aligned(body: => Unit): Unit = {
		if (0 != bitIndex) {
			write(bitBuffer);
			bitIndex = 0;
			bitBuffer = 0;
		}
		body
	}

	private def numBits(x: (Int, Int, Int, Int)) = max(max(countBits(x._1), countBits(x._2)), max(countBits(x._3), countBits(x._4)))

	private def countBits(value: Int) = value match {
		case 0 => 0
		case x if x < 0 => 33 - Integer.numberOfLeadingZeros(-x)
		case y if y > 0 => 33 - Integer.numberOfLeadingZeros(y)
	}

	private def nextBit() = {
		bitIndex += 1

		if (8 == bitIndex) {
			write(bitBuffer)

			bitIndex = 0
			bitBuffer = 0
		}
	}

	private def markBit() = bitBuffer |= 1 << (7 - bitIndex)

	def writeFIXED(value: Float) = {
		val b = value.asInstanceOf[Int]
		writeUI16(((value - b) * 65535.0f).asInstanceOf[Int])
		writeUI16(b)
	}

	def writeFIXED8(value: Float) = {
		val b = value.asInstanceOf[Int]
		writeUI08(((value - b) * 255.0f).asInstanceOf[Int])
		writeUI08(b)
	}

	def writeRECORDHEADER(value: Recordheader) = {
		val writeLength = (value.length >= 0x3f || SwfTags.isLongTag(value kind))
		writeUI16((if (writeLength) 0x3f else value.length) | value.kind << 6)
		if(writeLength) {
			writeSI32(value.length)
		}
	}

	def writeRECT(value: Rect) = {
		val n = numBits(value)
		writeUB(n, 5)
		writeSB(value.minX, n)
		writeSB(value.maxX, n)
		writeSB(value.minY, n)
		writeSB(value.maxY, n)
	}

	def writeRGB(value: RGB) = {
		writeUI08(value red)
		writeUI08(value green)
		writeUI08(value blue)
	}

	def writeSTRING(value: String) = {
		value.getBytes("UTF8") foreach writeUI08
		writeUI08(0x00)
	}

	def writeTAG(value: SwfTag) = {
		value match {
			case x: NoDataTag => writeRECORDHEADER(new Recordheader(value.kind, 0))
			case x: KnownLengthTag => {
				writeRECORDHEADER(new Recordheader(value.kind, x.length))
				value write this
			}
			case _ => {
				val byteArrayOutputStream = new JByteArrayOutputStream()
				val buffer = new SwfOutputStream(byteArrayOutputStream)

				try {
					value write buffer
					buffer.flush()

					val bytes = byteArrayOutputStream.toByteArray;
					buffer.close()

					writeRECORDHEADER(new Recordheader(value.kind, bytes.length))
					write(bytes)
				} finally {
					try {
						buffer.close()
					} catch {
						case _ =>
					}
				}
			}
		}
	}

	def writeSB(value: Int): Unit = writeSB(value, countBits(value));
	def writeSB(value: Int, n: Int): Unit = for (i <- (n - 1) to 0 by -1) {
		if (0 != (value & (1 << i))) {
			markBit()
		}
		nextBit()
	}

	def writeUB(value: Int): Unit = writeSB(value, 0x20 - Integer.numberOfLeadingZeros(value));
	def writeUB(value: Int, n: Int): Unit = writeSB(value, n)

	def writeSI08(value: Int) = aligned { write(value & 0xff) }

	def writeSI16(value: Int) = aligned {
		write(value & 0xff)
		write((value & 0xff00) >> 0x08)
	}

	def writeSI32(value: Int) = aligned {
		write(value & 0xff)
		write((value & 0xff00) >> 0x08)
		write((value & 0xff0000) >> 0x10)
		write((value & 0xff000000) >> 0x18)
	}

	def writeUI08(value: Int): Unit = writeSI08(value)

	def writeUI08(value: Byte): Unit = writeSI08(value)

	def writeUI16(value: Int) = writeSI16(value)

	def writeUI24(value: Int) = aligned {
		write(value & 0xff)
		write((value & 0xff00) >> 0x08)
		write((value & 0xff0000) >> 0x10)
	}

	def writeUI32(value: Long) = writeSI32((value & 0xffffffffL).asInstanceOf[Int])

	def writeUI64(value: Int): Unit = {
		writeUI32(value)
		writeUI32(0)
	}

	def writeUI64(value: Long): Unit = {
		writeUI32(value)
		writeUI32(0)
	}

	def writeUI64(value: BigInt): Unit = aligned { write(value.toByteArray.reverse) }

	override def close() = {
		flush()
		output.close()
	}

	override def flush() = aligned { output.flush() }

	override def write(value: Array[Byte]) = output write value

	override def write(value: Array[Byte], offset: Int, length: Int) = output.write(value, offset, length)

	override def write(value: Int) = output write value
}
