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
	internal final class LiteralDecoder {
		private var _coders: Vector.<Decoder2>
		private var _numPrevBits: int
		private var _numPosBits: int
		private var _posMask: int

		public function create(numPosBits: int, numPrevBits: int): void {
			if(_coders != null && _numPrevBits == numPrevBits && _numPosBits == numPosBits) {
				return
			}

			_numPosBits = numPosBits
			_posMask = (1 << numPosBits) - 1
			_numPrevBits = numPrevBits

			var numStates: int = 1 << (_numPrevBits + _numPosBits)
			_coders = new Vector.<Decoder2>(numStates, true)

			for(var i: int = 0; i < numStates; ++i) {
				_coders[i] = new Decoder2()
			}
		}

		public function init(): void {
			var numStates: int = 1 << (_numPrevBits + _numPosBits)

			for(var i: int = 0; i < numStates; ++i) {
				_coders[i].init()
			}
		}

		public function getDecoder(pos: int, prevByte: int): Decoder2 {
			return _coders[int(((pos & _posMask) << _numPrevBits) + ((prevByte & 0xFF) >>> (8 - _numPrevBits)))]
		}
	}
}
