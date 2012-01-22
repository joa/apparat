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
	import apparat.inline.Inlined;

	/**
	 * The MemoryMath class is a collection of functions
	 * that are often used when dealing with memory.
	 *
	 * @author Joa Ebert
	 */
	public final class MemoryMath extends Inlined
	{
		/**
		 * Finds the most significant bit for a value that is a power of two.
		 *
		 * @param value A value that is a power of two.
		 * @return The logarithmus dualis for the given value.
		 */
		public static function msb( value: uint ): uint
		{
			var result: uint;

			result  = (   value & 0xaaaaaaaa ) != 0 ? 1 : 0;
			result |= ( ( value & 0xffff0000 ) != 0 ? 1 : 0 ) << 4;
			result |= ( ( value & 0xff00ff00 ) != 0 ? 1 : 0 ) << 3;
			result |= ( ( value & 0xf0f0f0f0 ) != 0 ? 1 : 0 ) << 2;
			result |= ( ( value & 0xcccccccc ) != 0 ? 1 : 0 ) << 1;

			return result;
		}

		/**
		 * Rounds a value up to the next power of two.
		 *
		 * @param value An unsigned integer value.
		 * @return The next power of two. The same value if it is already a
		 * power of two.
		 */
		public static function nextPow2( value: uint ): uint
		{
			--value;
			value |= value >>> 0x01;
			value |= value >>> 0x02;
			value |= value >>> 0x04;
			value |= value >>> 0x08;
			value |= value >>> 0x10;
			++value;

			return value;
		}

		/**
		 * Tests whether or not a given value is a power of two.
		 *
		 * @param value An unsigned intever value.
		 * @return <code>true</code> if the value is a power of two; <code>false
		 * </code> otherwise.
		 */
		public static function isPow2( value: uint ): Boolean
		{
			if( value < 2 ) return false;
			else if( value & ( value - 1 ) ) return false;
			return true;
		}
	}
}
