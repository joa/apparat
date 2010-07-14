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