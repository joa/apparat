package apparat.lzma {
	import flash.utils.IDataOutput

	/**
	 * @author Joa Ebert
	 */
	internal final class OutWindow {
		private var _buffer: Vector.<int>
		private var _pos: int
		private var _windowSize: int = 0
		private var _streamPos: int
		private var _stream: IDataOutput

		public function create(windowSize: int): void {
			if(_buffer == null || _windowSize != windowSize) {
				_buffer = new Vector.<int>(windowSize, true)
			}

			_windowSize = windowSize
			_pos = 0
			_streamPos = 0
		}

		public function setStream(stream: IDataOutput): void {
			releaseStream()
			_stream = stream
		}

		public function releaseStream(): void {
			flush()
			_stream = null
		}

		public function init(solid: Boolean): void {
			if(!solid) {
				_streamPos = 0
				_pos = 0
			}
		}

		public function flush(): void {
			var size: int = _pos - _streamPos

			if(size == 0) {
				return
			}

			//This is: _stream.write(_buffer, _streamPos, size);
			for(var i: int = _streamPos, n: int = _streamPos + size; i < n; ++i) {
				_stream.writeByte(_buffer[i])
			}

			if(_pos >= _windowSize) {
				_pos = 0
			}

			_streamPos = _pos
		}

		public function copyBlock(distance: int, len: int): void {
			var pos: int = _pos - distance - 1

			if(pos < 0) {
				pos += _windowSize
			}

			for(; len != 0; --len) {
				if (pos >= _windowSize) {
					pos = 0
				}

				_buffer[_pos++] = _buffer[pos++]

				if(_pos >= _windowSize) {
					flush()
				}
			}
		}

		public function putByte(b: int): void {
			_buffer[_pos++] = b

			if (_pos >= _windowSize) {
				flush()
			}
		}

		public function getByte(distance: int): int {
			var pos: int = _pos - distance - 1

			if (pos < 0) {
				pos += _windowSize
			}

			return _buffer[pos]
		}
	}
}