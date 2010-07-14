package apparat.lzma {
	/**
	 * @author Joa Ebert
	 */
	internal final class BitTreeDecoder {
		private var _models: Vector.<int>
		private var _numBitLevels: int

		public function BitTreeDecoder(numBitLevels: int) {
			_numBitLevels = numBitLevels
			_models = new Vector.<int>(1 << numBitLevels, true)
		}

		public function init(): void {
			Decoder.initBitModels(_models)
		}

		public function decode(rangeDecoder: Decoder): int {
			var m: int = 1

			for(var bitIndex: int = _numBitLevels; bitIndex != 0; --bitIndex) {
				m = (m << 1) + rangeDecoder.decodeBit(_models, m)
			}

			return m - (1 << _numBitLevels)
		}

		public function reverseDecode(rangeDecoder: Decoder): int {
			var m: int = 1
			var symbol: int = 0
			var bit: int;
			
			for(var bitIndex: int = 0; bitIndex < _numBitLevels; ++bitIndex) {
				bit = rangeDecoder.decodeBit(_models, m)

				m <<= 1
				m += bit

				symbol |= bit << bitIndex
			}

			return symbol
		}

		public static function reverseDecode(models: Vector.<int>, startIndex: int,
											 rangeDecoder: Decoder, NumBitLevels: int): int {
			var m: int = 1
			var symbol: int = 0
			var bit: int

			for(var bitIndex: int = 0; bitIndex < NumBitLevels; ++bitIndex) {
				bit = rangeDecoder.decodeBit(models, startIndex + m)

				m <<= 1
				m += bit

				symbol |= (bit << bitIndex)
			}

			return symbol
		}
	}
}