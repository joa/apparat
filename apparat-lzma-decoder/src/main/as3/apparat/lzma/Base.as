package apparat.lzma {
	/**
	 * @author Joa Ebert
	 */
	internal final class Base {
		public static const kNumStates: int = 12
		public static const kNumPosSlotBits: int = 6
		public static const kNumLenToPosStates: int = 4
		public static const kMatchMinLen: int = 2
		public static const kNumAlignBits: int = 4
		public static const kAlignMask: int = ((1 << kNumAlignBits) - 1)
		public static const kStartPosModelIndex: int = 4
		public static const kEndPosModelIndex: int = 14
		public static const kNumFullDistances: int = 1 << (kEndPosModelIndex / 2)
		public static const kNumLitContextBitsMax: int = 8
		public static const kNumPosStatesBitsMax: int = 4
		public static const kNumPosStatesMax: int = (1 << kNumPosStatesBitsMax)
		public static const kNumLowLenBits: int = 3
		public static const kNumMidLenBits: int = 3
		public static const kNumHighLenBits: int = 8
		public static const kNumLowLenSymbols: int = 1 << kNumLowLenBits
		public static const kNumMidLenSymbols: int = 1 << kNumMidLenBits
		public static const kNumLenSymbols: int = kNumLowLenSymbols + kNumMidLenSymbols + (1 << kNumHighLenBits)
		public static const kMatchMaxLen: int = kMatchMinLen + kNumLenSymbols - 1
		
		public static function stateInit(): int	{
			return 0
		}

		public static function stateUpdateChar(index: int): int {
			if(index < 4) {
				return 0
			}

			if(index < 10) {
				return index - 3
			}

			return index - 6
		}

		public static function stateUpdateMatch(index: int): int {
			return index < 7 ? 7 : 10
		}

		public static function stateUpdateRep(index: int): int {
			return index < 7 ? 8 : 11
		}

		public static function stateUpdateShortRep(index: int): int {
			return index < 7 ? 9 : 11
		}

		public static function stateIsCharState(index: int): Boolean {
			return index < 7
		}

		public static function getLenToPosState(len: int): int {
			len -= kMatchMinLen

			if(len < kNumLenToPosStates) {
				return len
			}

			return kNumLenToPosStates - 1
		}
	}
}