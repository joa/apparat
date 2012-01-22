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
	import flash.utils.IDataOutput

	/**
	 * @author Joa Ebert
	 */
	public final class LZMADecoder {
		private var _outWindow: OutWindow = new OutWindow()
		private var _rangeDecoder: Decoder = new Decoder()
		private var _isMatchDecoders: Vector.<int> = new Vector.<int>(Base.kNumStates << Base.kNumPosStatesBitsMax, true)
		private var _isRepDecoders: Vector.<int> = new Vector.<int>(Base.kNumStates, true)
		private var _isRepG0Decoders: Vector.<int> = new Vector.<int>(Base.kNumStates, true)
		private var _isRepG1Decoders: Vector.<int> = new Vector.<int>(Base.kNumStates, true)
		private var _isRepG2Decoders: Vector.<int> = new Vector.<int>(Base.kNumStates, true)
		private var _isRep0LongDecoders: Vector.<int> = new Vector.<int>(Base.kNumStates << Base.kNumPosStatesBitsMax, true)
		private var _posSlotDecoder: Vector.<BitTreeDecoder> = new Vector.<BitTreeDecoder>(Base.kNumLenToPosStates, true)
		private var _posDecoders: Vector.<int> = new Vector.<int>(Base.kNumFullDistances - Base.kEndPosModelIndex, true)
		private var _posAlignDecoder: BitTreeDecoder = new BitTreeDecoder(Base.kNumAlignBits)
		private var _lenDecoder: LenDecoder = new LenDecoder()
		private var _repLenDecoder: LenDecoder = new LenDecoder()
		private var _literalDecoder: LiteralDecoder = new LiteralDecoder()
		private var _dictionarySize: int = -1
		private var _dictionarySizeCheck: int =  -1
		private var _posStateMask: int

		public function LZMADecoder():void {
			for(var i: int = 0; i < Base.kNumLenToPosStates; ++i) {
				_posSlotDecoder[i] = new BitTreeDecoder(Base.kNumPosSlotBits)
			}
		}

		private function setDictionarySize(dictionarySize: int): Boolean {
			if(dictionarySize < 0) {
				return false
			}

			if(_dictionarySize != dictionarySize) {
				_dictionarySize = dictionarySize
				_dictionarySizeCheck = Math.max(_dictionarySize, 1)
				_outWindow.create(Math.max(_dictionarySizeCheck, (1 << 12)))
			}

			return true
		}

		private function setLcLpPb(lc: int, lp: int, pb: int): Boolean {
			if(lc > Base.kNumLitContextBitsMax || lp > 4 || pb > Base.kNumPosStatesBitsMax) {
				return false
			}

			_literalDecoder.create(lp, lc)
			var numPosStates: int = 1 << pb
			_lenDecoder.create(numPosStates)
			_repLenDecoder.create(numPosStates)
			_posStateMask = numPosStates - 1

			return true
		}

		private function init(): void
		{
			_outWindow.init(false)

			Decoder.initBitModels(_isMatchDecoders)
			Decoder.initBitModels(_isRep0LongDecoders)
			Decoder.initBitModels(_isRepDecoders)
			Decoder.initBitModels(_isRepG0Decoders)
			Decoder.initBitModels(_isRepG1Decoders)
			Decoder.initBitModels(_isRepG2Decoders)
			Decoder.initBitModels(_posDecoders)

			_literalDecoder.init()

			var i: int

			for (i = 0; i < Base.kNumLenToPosStates; ++i) {
				_posSlotDecoder[i].init()
			}

			_lenDecoder.init()
			_repLenDecoder.init()
			_posAlignDecoder.init()
			_rangeDecoder.init()
		}

		public function code(inStream: IDataInput, outStream: IDataOutput, outSize: uint): Boolean {
			_rangeDecoder.setStream(inStream)
			_outWindow.setStream(outStream)
			init()

			var state: int = Base.stateInit()
			var nowPos64: uint = 0
			var prevByte: int = 0
			var rep0: int = 0
			var rep1: int = 0
			var rep2: int = 0
			var rep3: int = 0

			while(outSize < 0 || nowPos64 < outSize) {
				var posState: int = nowPos64 & _posStateMask

				if(_rangeDecoder.decodeBit(_isMatchDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
					var decoder2: Decoder2 = _literalDecoder.getDecoder(nowPos64, prevByte)

					if(!Base.stateIsCharState(state)) {
						prevByte = decoder2.decodeWithMatchByte(_rangeDecoder, _outWindow.getByte(rep0))
					} else {
						prevByte = decoder2.decodeNormal(_rangeDecoder)
					}

					_outWindow.putByte(prevByte)
					state = Base.stateUpdateChar(state)
					nowPos64++
				} else {
					var len: int

					if(_rangeDecoder.decodeBit(_isRepDecoders, state) == 1) {
						len = 0

						if(_rangeDecoder.decodeBit(_isRepG0Decoders, state) == 0) {
							if(_rangeDecoder.decodeBit(_isRep0LongDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
								state = Base.stateUpdateShortRep(state)
								len = 1
							}
						} else {
							var distance: int

							if (_rangeDecoder.decodeBit(_isRepG1Decoders, state) == 0) {
								distance = rep1
							} else {
								if (_rangeDecoder.decodeBit(_isRepG2Decoders, state) == 0) {
									distance = rep2
								} else {
									distance = rep3
									rep3 = rep2
								}

								rep2 = rep1
							}

							rep1 = rep0
							rep0 = distance
						}

						if(len == 0) {
							len = _repLenDecoder.decode(_rangeDecoder, posState) + Base.kMatchMinLen
							state = Base.stateUpdateRep(state)
						}
					} else {
						rep3 = rep2
						rep2 = rep1
						rep1 = rep0
						len = Base.kMatchMinLen + _lenDecoder.decode(_rangeDecoder, posState)
						state = Base.stateUpdateMatch(state)

						var posSlot: int = _posSlotDecoder[Base.getLenToPosState(len)].decode(_rangeDecoder)

						if(posSlot >= Base.kStartPosModelIndex) {
							var numDirectBits: int = (posSlot >> 1) - 1
							rep0 = ((2 | (posSlot & 1)) << numDirectBits)

							if(posSlot < Base.kEndPosModelIndex) {
								rep0 += BitTreeDecoder.reverseDecode(_posDecoders,
										rep0 - posSlot - 1, _rangeDecoder, numDirectBits)
							} else {
								rep0 += (_rangeDecoder.decodeDirectBits(
										numDirectBits - Base.kNumAlignBits) << Base.kNumAlignBits)
								rep0 += _posAlignDecoder.reverseDecode(_rangeDecoder)

								if(rep0 < 0) {
									if(rep0 == -1) {
										break
									}

									return false
								}
							}
						} else {
							rep0 = posSlot
						}
					}

					if(rep0 >= nowPos64 || rep0 >= _dictionarySizeCheck) {
						return false
					}

					_outWindow.copyBlock(rep0, len)
					nowPos64 += len
					prevByte = _outWindow.getByte(0)
				}
			}

			_outWindow.flush()
			_outWindow.releaseStream()
			_rangeDecoder.releaseStream()

			return true
		}

		public function setDecoderProperties(properties: Vector.<int>): Boolean {
			if(properties.length < 5) {
				return false
			}

			var val: int = properties[0] & 0xFF
			var lc: int = val % 9
			var remainder: int = val / 9
			var lp: int = remainder % 5
			var pb: int = remainder / 5
			var dictionarySize: int = 0

			for (var i: int = 0; i < 4; ++i) {
				dictionarySize += (properties[int(1 + i)] & 0xFF) << (i * 8)
			}

			if(!setLcLpPb(lc, lp, pb)) {
				return false
			}

			return setDictionarySize(dictionarySize)
		}
	}
}
