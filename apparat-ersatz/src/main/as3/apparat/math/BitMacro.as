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
package apparat.math {
	import apparat.asm.*
	import apparat.inline.Macro

	/**
	 * The BitMacro class defines common bitwise operations.
	 *
	 * @author Joa Ebert
	 */
	public final class BitMacro extends Macro {
		/**
		 * Swap to integer values using the tripple XOR trick.
		 *
		 * @param value0 An integer value.
		 * @param value1 An integer value.
		 */
		public static function swap(value0: int, value1: int): void { __asm(
			GetLocal(value0),
			GetLocal(value1),
			SetLocal(value0),
			SetLocal(value1)
		)}

		/**
		 * Extract each chanel of a given color into three integers.
		 * The color value is not modified by this macro.
		 *
		 * @param color The color value in 0xRRGGBB format.
		 * @param redResult The result of the red channel.
		 * @param greenResult The result of the green channel.
		 * @param blueResult The result of the blue channel.
		 */
		public static function extractRGB(color: int, redResult: int, greenResult: int, blueResult: int):void {
			redResult = (color & 0xff0000) >> 0x10
			greenResult = (color & 0x00ff00) >> 0x08
			blueResult = (color & 0x0000ff)
		}

		/**
		 * Extract each chanel of a given color into four integers.
		 * The color value is not modified by this macro.
		 *
		 * @param color The color value in 0xAARRGGBB format.
		 * @param alphaResult The result of the alpha channel.
		 * @param redResult The result of the red channel.
		 * @param greenResult The result of the green channel.
		 * @param blueResult The result of the blue channel.
		 */
		public static function extractARGB(color: uint, alphaResult: int, redResult: int, greenResult: int, blueResult: int):void {
			alphaResult = (color & 0xff000000) >>> 0x18
			redResult = (color & 0x00ff0000) >> 0x10
			greenResult = (color & 0x0000ff00) >> 0x08
			blueResult = (color & 0x000000ff)
		}

		/**
		 * Rounds a value up to the next power of two.
		 *
		 * @param value An unsigned integer.
		 * @return The next power of two. The same value if it is already a
		 * power of two.
		 */
		public static function nextPow2(value: uint): void
		{
			--value
			value |= value >>> 0x01
			value |= value >>> 0x02
			value |= value >>> 0x04
			value |= value >>> 0x08
			value |= value >>> 0x10
			++value
		}
	}
}
