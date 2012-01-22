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
