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
package apparat.memory
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
		 * 	not possible to allocate the requested amount of memory.
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
