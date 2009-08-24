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

package com.joa_ebert.apparat.memory 
{
	/**
	 * The MemoryBlock class represents a block of occupied memory.
	 * 
	 * @author Joa Ebert
	 */
	public final class MemoryBlock
	{
		/**
		 * Creates and returns a new MemoryBlock object.
		 * 
		 * @param position The position of the block.
		 * @param length The size of the block in bytes.
		 */
		public static function create( position: uint, length: uint ):
			MemoryBlock
		{
			return new MemoryBlock( position, length );
		}
		
		/**
		 * Frees a memory block.
		 * 
		 * @param block The MemoryBlock to free.
		 */
		private static function free( block: MemoryBlock ): void
		{
		}
		
		/**
		 * The position of the block.
		 */
		public var position: uint;
		
		/**
		 * The size of the block in bytes.
		 */
		public var length: uint;
		
		/**
		 * Creates a new MemoryBlock object.
		 * 
		 * @param length The length of the block in bytes.
		 * @param position The position of the block in the global buffer.
		 */
		public function MemoryBlock( position: uint, length: uint )
		{
			this.position = position;
			this.length = length;
		}
		
		/**
		 * Creates and returns a string representation of the current object.
		 * 
		 * @return The string representation of the current object.
		 */
		public function toString(): String
		{
			return "[MemoryBlock position: 0x" + position.toString( 0x10 ) +
				", length: 0x" + length.toString( 0x10 ) + "]";
		}
		
		/**
		 * @private
		 */
		internal function dispose() : void
		{
			free( this );
			length = position = 0;
		}
	}
}
