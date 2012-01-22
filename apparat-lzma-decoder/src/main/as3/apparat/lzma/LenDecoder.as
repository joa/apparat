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
	internal final class LenDecoder {
		private var _choice: Vector.<int> = new Vector.<int>(2, true)
		private var _lowCoder: Vector.<BitTreeDecoder> = new Vector.<BitTreeDecoder>(Base.kNumPosStatesMax, true)
		private var _midCoder: Vector.<BitTreeDecoder> = new Vector.<BitTreeDecoder>(Base.kNumPosStatesMax, true)
		private var _highCoder: BitTreeDecoder = new BitTreeDecoder(Base.kNumHighLenBits)
		private var _numPosStates: int = 0;

		public function create(numPosStates: int): void {
			for(; _numPosStates < numPosStates; ++_numPosStates) {
				_lowCoder[_numPosStates] = new BitTreeDecoder(Base.kNumLowLenBits)
				_midCoder[_numPosStates] = new BitTreeDecoder(Base.kNumMidLenBits)
			}
		}

		public function init(): void {
			Decoder.initBitModels(_choice)

			for(var posState: int = 0; posState < _numPosStates; ++posState) {
				_lowCoder[posState].init()
				_midCoder[posState].init()
			}

			_highCoder.init()
		}


		public function decode(rangeDecoder: Decoder, posState: int): int {
			if(rangeDecoder.decodeBit(_choice, 0) == 0) {
				return _lowCoder[posState].decode(rangeDecoder)
			}

			var symbol: int = Base.kNumLowLenSymbols

			if(rangeDecoder.decodeBit(_choice, 1) == 0) {
				symbol += _midCoder[posState].decode(rangeDecoder)
			} else {
				symbol += Base.kNumMidLenSymbols + _highCoder.decode(rangeDecoder)
			}

			return symbol
		}
	}
}
