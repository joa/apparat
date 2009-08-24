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
	import flash.utils.ByteArray;

	/**
	 * The IMemoryPoolStrategy interface describes a strategy for memory
	 * allocation and deallocation.
	 * 
	 * @author Joa Ebert
	 */
	public interface IMemoryPoolStrategy
	{
		/**
		 * Initializes the strategy with a given buffer.
		 */
		function initialize( buffer: ByteArray ): void;
		
		/**
		 * Allocates the requested amount of memory.
		 *
		 * @param length The number of bytes to allocate.
		 * 
		 * @return A MemoryBlock object representing the occupied space.
		 * 
		 * @throws flash.errors.MemoryError A MemoryError is thrown if it is 
		 * 	not possible to allocated the requested amount of memory.
		 */
		function allocate( length: uint ): MemoryBlock;
		
		/**
		 * Frees a block of occupied space.
		 * 
		 * @param block The block of memory to free.
		 */
		function free( block: MemoryBlock ): void;
	}
}
