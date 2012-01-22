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
package apparat.utils

import java.io.{
	InputStream => JInputStream,
	OutputStream => JOutputStream,
	Writer => JWriter,
	ByteArrayOutputStream => JByteArrayOutputStream
}

object IO {
	def dump(bytes: Array[Byte]): Unit = dump(bytes, Console.out)
	def dump(bytes: Array[Byte], printStream: { def println(value: String); def flush(): Unit }): Unit = {
		printStream println "Hex dump:"
		printStream println (if(null == bytes) {
			"(null)"
		} else {
			val length = bytes.length
			val output = new StringBuilder(length << 2)
			for(i <- 0 until length by 0x10) {
				val hs = i.toHexString
				for(j <- 0 until (8 - hs.length))
					output append '0'
				output append hs
				output append 'h'
				output append ' '

				val text = new StringBuilder(0x10)

				for(j <- 0 until 0x10) {
					val bufferIndex = i + j
					if(bufferIndex >= length) {
						output append "   "
						text append ' '
					} else {
						val currentByte = bytes(bufferIndex) & 0xff
						val hexString = currentByte.toHexString

						if(currentByte < 0x10)
							output append '0'

						output append hexString
						output append ' '

						text append (if(currentByte > 0x20 && currentByte < 0x7f) currentByte.asInstanceOf[Char] else '.')
					}

					if((j & 0x03) == 0x03 && 0x0f != j)
						output append "| "
				}

				output append ' '
				output append text
				output append '\n'
			}

			output append  ("Length: " + length + " bytes")
			output.toString
		})
		printStream.flush()
	}

	def read(length: Int)(implicit input: JInputStream): Array[Byte] = readBytes(length, new Array[Byte](length))

	def readBytes(length: Int, bytes: Array[Byte])(implicit input: JInputStream): Array[Byte] = {
		var offset = 0
		while (offset < length)
			offset += input.read(bytes, offset, length - offset)
		bytes
	}

	def byteArrayOf(implicit input: JInputStream) = {
		val output = new JByteArrayOutputStream
		val buffer = new Array[Byte](0x2000)
		var bytesRead = 0

		bytesRead = input.read(buffer)

		while (bytesRead >= 0) {
			output write (buffer, 0, bytesRead)
			bytesRead = input.read(buffer)
		}

		output.close()
		output.toByteArray()
	}

	def using[A, B <: { def close() }](stream: B)(body: B => A): A = {
		try {
			body(stream)
		} finally {
			if (null != stream) {
				try {
					stream.close()
				} catch {
					case _ => {}
				}
			}
		}
	}
}
