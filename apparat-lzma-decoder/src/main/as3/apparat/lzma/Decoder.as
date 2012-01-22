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
	import flash.utils.IDataInput

	/**
	 * @author Joa Ebert
	 */
	internal final class Decoder {
		public static const kTopMask: int = ~((1 << 24) - 1)
		public static const kNumBitModelTotalBits: int = 11
		public static const kBitModelTotal: int = (1 << kNumBitModelTotalBits)
		public static const kNumMoveBits: int = 5

		private var _range: int
		private var _code: int
		private var _stream: IDataInput

		public function setStream(stream: IDataInput): void {
			_stream = stream
		}

		public function releaseStream(): void {
			_stream = null
		}

		public function init(): void {
			_code = 0
			_range = -1

			for(var i: int = 0; i < 5; ++i) {
				_code = (_code << 8) | _stream.readUnsignedByte()
			}
		}

		public function decodeDirectBits(numTotalBits: int): int {
			var result: int = 0
			var t: int

			for(var i: int = numTotalBits; i != 0; --i) {
				_range >>>= 1
				t = ((_code - _range) >>> 31)
				_code -= _range & (t - 1)
				result = (result << 1) | (1 - t)

				if((_range & kTopMask) == 0) {
					_code = (_code << 8) | _stream.readUnsignedByte()
					_range <<= 8
				}
			}

			return result
		}

		public function decodeBit(probs: Vector.<int>, index: int): int {
			var prob: int = probs[index]
			var newBound: int = (_range >>> kNumBitModelTotalBits) * prob

			if((_code ^ 0x80000000) < (newBound ^ 0x80000000)) {
				_range = newBound;
				probs[index] = (prob + ((kBitModelTotal - prob) >>> kNumMoveBits))

				if ((_range & kTopMask) == 0) {
					_code = (_code << 8) | _stream.readUnsignedByte()
					_range <<= 8
				}

				return 0
			} else {
				_range -= newBound
				_code -= newBound
				probs[index] = (prob - ((prob) >>> kNumMoveBits))

				if((_range & kTopMask) == 0) {
					_code = (_code << 8) | _stream.readUnsignedByte()
					_range <<= 8
				}

				return 1
			}
		}

		public static function initBitModels(probs: Vector.<int>): void {
			for(var i: int = 0; i < probs.length; ++i) {
				probs[i] = (kBitModelTotal >>> 1)
			}
		}
	}
}
