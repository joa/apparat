/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.memory.strategies 
{

	/**
	 * The MemoryChunk class represents a chunk of memory for a strategy making
	 * use of static or dynamic chunks.
	 * 
	 * @private
	 * 
	 * @author Joa Ebert
	 */
	internal final class MemoryChunk
	{
		/**
		 * Creates and returns a new MemoryChunk object.
		 */
		public static function create(): MemoryChunk
		{
			return new MemoryChunk();
		}
		
		/**
		 * Releases a given MemoryChunk object.
		 * 
		 * @param memoryChunk The object to release.
		 */
		private static function release( memoryChunk: MemoryChunk ): void
		{
		}
		
		/**
		 * The position of the chunk.
		 */
		public var position: uint;
		
		/**
		 * The length of the chunk.
		 */
		public var length: uint;
		
		/**
		 * The next chunk in a free-list.
		 */
		public var next: MemoryChunk;
		
		/**
		 * The previous chunk in a free-list.
		 */
		public var prev: MemoryChunk;
		
		/**
		 * Creates and returns a string representation of the current object.
		 * 
		 * @return The string representation of the current object.
		 */
		public function toString(): String
		{
			return '[MemoryChunk position: 0x' + position.toString( 0x10 ) +
				', size: 0x' + length.toString( 0x10 ) + ']';
		}
		
		/**
		 * @private
		 */
		public function dispose() : void
		{
			release( this );

			position = 0;
			length = 0;
			next = null;
			prev = null;
		}
	}
}
