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