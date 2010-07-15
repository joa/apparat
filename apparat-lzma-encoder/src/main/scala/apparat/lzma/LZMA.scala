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
package apparat.lzma

import apparat.sevenzip.compression.lzma.{Encoder => JEncoder}
import java.io.{InputStream => JInputStream, OutputStream => JOutputStream}

/**
 * @author Joa Ebert
 */
object LZMA {
	private val DictionarySize = 1 << 23
	private val Lc = 3
	private val Lp = 0
	private val Pb = 2
	private val Fb = 128
	private val Eos = false
	private val Algorithm = 2
	private val MatchFinder = 1

	def encode(inputStream: JInputStream, inputLength: Long, outputStream: JOutputStream) = {
		val encoder = new JEncoder()
		val eos = Eos

		if(!(encoder setAlgorithm Algorithm)) {
			throw new LZMAException("Incorrect compression mode.")
		}

		if(!(encoder setDictionarySize DictionarySize)) {
			throw new Exception("Incorrect dictionary size.")
		}

		if(!(encoder setNumFastBytes Fb)) {
			throw new Exception("Incorrect fast-bytes value.")
		}

		if(!(encoder setMatchFinder MatchFinder)) {
			throw new Exception("Incorrect match-finder value.")
		}

		if(!encoder.setLcLpPb(Lc, Lp, Pb)) {
			throw new Exception("Incorrect LC or LP or PB value.")
		}

		encoder setEndMarkerMode eos
		encoder writeCoderProperties outputStream

		val fileSize = if(eos) -1 else inputLength

		for(i <- 0 until 8) {
			outputStream.write((fileSize >>> (8 * i)).asInstanceOf[Int] & 0xFF)
		}

		encoder.code(inputStream, outputStream, -1, -1, null)
	}
}