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
package apparat.lzma {
	/**
	 * @author Joa Ebert
	 */
	internal final class Decoder2 {
		private var _decoders: Vector.<int> = new Vector.<int>(0x300, true)

		public function init(): void {
			Decoder.initBitModels(_decoders)
		}

		public function decodeNormal(rangeDecoder: Decoder): int {
			var symbol: int = 1

			do {
				symbol = (symbol << 1) | rangeDecoder.decodeBit(_decoders, symbol)
			} while(symbol < 0x100)

			return symbol
		}

		public function decodeWithMatchByte(rangeDecoder: Decoder, matchByte: int): int {
			var symbol: int = 1
			var matchBit: int
			var bit: int

			do {
				matchBit = (matchByte >> 7) & 1
				matchByte <<= 1
				bit = rangeDecoder.decodeBit(_decoders, ((1 + matchBit) << 8) + symbol)
				symbol = (symbol << 1) | bit

				if(matchBit != bit) {
					while(symbol < 0x100) {
						symbol = (symbol << 1) | rangeDecoder.decodeBit(_decoders, symbol)
					}
					break
				}
			} while(symbol < 0x100)

			return symbol
		}
	}
}
