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
	import com.joa_ebert.apparat.memory.strategies.StaticChunksStrategy;

	import flash.system.ApplicationDomain;
	import flash.utils.ByteArray;

	/**
	 * The MemoryPool class is an implementation of a memory pool for binary
	 * data.
	 * 
	 * The first 1024 bytes of the MemoryPool are always considered non-
	 * persistent space. This means any value stored in the range of [0, 1024]
	 * may be overriden at any time. This space is only available for temporary
	 * calculations that do not require more than 1024 bytes.
	 * 
	 * @author Joa Ebert
	 */
	public final class MemoryPool 
	{
		/**
		 * The strategy used to allocate and free memory.
		 */
		private static var _poolStrategy: IMemoryPoolStrategy;
		
		/**
		 * The internal buffer representing the memory.
		 */
		private static var _buffer: ImmutableByteArray;
		
		/**
		 * Initializes the MemoryPool.
		 * 
		 * <p>The minimum size of the MemoryPool is
		 * <code>ApplicationDomain.MIN_DOMAIN_MEMORY_LENGTH</code> and will be 
		 * rounded to the next power of two.</p>
		 * 
		 * @param length The size of the memory pool; defaults to 
		 * 	<code>16mb</code>.
		 * 	
		 * @param strategy The strategy of the pool; defaults to 
		 * 	StaticChunksStrategy with a block size of <code>1024b</code>.
		 */
		public static function initialize( length: uint = 0x1000000,
			strategy: IMemoryPoolStrategy = null ): void
		{
			if( null == strategy )
			{
				strategy = new StaticChunksStrategy();
			}
			
			if( length < ApplicationDomain.MIN_DOMAIN_MEMORY_LENGTH )
			{
				length = ApplicationDomain.MIN_DOMAIN_MEMORY_LENGTH;
			}

			if( !MemoryMath.isPow2( length ) )
			{
				length = MemoryMath.nextPow2( length );
			}
			
			_buffer = new ImmutableByteArray( length );
			_poolStrategy = strategy;
			_poolStrategy.initialize( _buffer );
			
			Memory.select( _buffer );
		}
	
		/**
		 * Returns the buffer used by the MemoryPool.
		 * 
		 * The endian is always <code>Endian.LITTLE_ENDIAN</code> and the total
		 * length never changes.
		 * 
		 * It is absolutely okay to change the <code>position</code> property
		 * of the buffer object. Changing the endian or the length of the 
		 * buffer will result in a runtime error.
		 */
		public static function get buffer(): ByteArray
		{
			return _buffer;
		}
		
		/**
		 * Allocates the requested amount of memory.
		 *
		 * @param length The number of bytes to allocate.
		 * 
		 * @return A MemoryBlock object representing the occupied space.
		 * 
		 * @throws flash.errors.MemoryError A MemoryError is thrown if it is not
		 *  possible to allocated the requested amount of memory.
		 */
		public static function allocate( length: uint ): MemoryBlock
		{
			return _poolStrategy.allocate( length );
		}	
		
		/**
		 * Frees a block of occupied space.
		 * 
		 * @param block The block of memory to free.
		 */
		public static function free( block: MemoryBlock ): void
		{
			_poolStrategy.free( block );
			
			block.dispose();
		}
	}
}
