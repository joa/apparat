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
package apparat.utils

import java.io.{OutputStream => JOutputStream}
import java.util.zip.{Deflater => JDeflater}

/**
 * @author Joa Ebert
 */
object Deflate {
	val enable7z = System.getProperty("apparat.7z", "true").toLowerCase == "true"

	def compress(bytes: Array[Byte], output: JOutputStream) = {
		val deflater = new JDeflater(JDeflater.BEST_COMPRESSION)
		val buffer = new Array[Byte](0x8000)
		var numBytesCompressed = 0

		deflater setInput bytes
		deflater.finish()

		do {
			numBytesCompressed = deflater deflate buffer
			output write (buffer, 0, numBytesCompressed)
		} while (0 != numBytesCompressed)
	}
}