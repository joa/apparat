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
 * Author : Patrick Le Clec'h
 */

package apparat.memory {
	import apparat.asm.AddInt;
	import apparat.asm.BitOr;
	import apparat.asm.DecLocalInt;
	import apparat.asm.Dup;
	import apparat.asm.GetByte;
	import apparat.asm.GetInt;
	import apparat.asm.GetShort;
	import apparat.asm.IfEqual;
	import apparat.asm.IncLocalInt;
	import apparat.asm.IncrementInt;
	import apparat.asm.Jump;
	import apparat.asm.LookupSwitch;
	import apparat.asm.SetByte;
	import apparat.asm.SetInt;
	import apparat.asm.SetLocal;
	import apparat.asm.SetShort;
	import apparat.asm.ShiftLeft;
	import apparat.asm.__as3;
	import apparat.asm.__asm;
	import apparat.inline.Macro;

	public class MemoryMacro extends Macro {
		/**
		 * The memset function sets a given memory interval to a given value.
		 *
		 * @param dst The address in memory where to start.
		 * @param value The byte value to store.
		 * @param size The number of bytes to write.
		 */
		public static function memset(dst:int, value:int, size:int):void
		{
			var rest:int = size & 7;
			size = size >> 3;
			__asm(
					__as3(value),
					__as3(value),
					__as3(8),
					ShiftLeft,
					BitOr,
					Dup,
					__as3(16),
					ShiftLeft,
					BitOr,
					SetLocal(value),
					"loop:",
					__as3(size),
					__as3(0),
					IfEqual("copyRest"),
					__as3(value),
					__as3(dst),
					SetInt,
					__as3(dst),
					__as3(4),
					AddInt,
					SetLocal(dst),
					__as3(value),
					__as3(dst),
					SetInt,
					__as3(dst),
					__as3(4),
					AddInt,
					SetLocal(dst),
					DecLocalInt(size),
					Jump("loop"),
					"copyRest:",
					__as3(rest),
					LookupSwitch("0", "0", "1", "2", "3", "4", "5", "6", "7"),
					"7:",
					__as3(value),
					__as3(dst),
					SetByte,
					__as3(value),
					IncLocalInt(dst),
					__as3(dst),
					SetShort,
					__as3(value),
					__as3(dst),
					IncrementInt,
					IncrementInt,
					SetInt,
					Jump("0"),
					"6:",
					__as3(value),
					__as3(dst),
					SetShort,
					__as3(value),
					__as3(dst),
					IncrementInt,
					IncrementInt,
					SetInt,
					Jump("0"),
					"5:",
					__as3(value),
					__as3(dst),
					SetByte,
					__as3(value),
					__as3(dst),
					IncrementInt,
					SetInt,
					Jump("0"),
					"4:",
					__as3(value),
					__as3(dst),
					SetInt,
					Jump("0"),
					"3:",
					__as3(value),
					__as3(dst),
					SetByte,
					__as3(value),
					__as3(dst),
					IncrementInt,
					SetShort,
					Jump("0"),
					"2:",
					__as3(value),
					__as3(dst),
					SetShort,
					Jump("0"),
					"1:",
					__as3(value),
					__as3(dst),
					SetByte,
					"0:"
					);
		}

		/**
		 * Swaps two memory regions.
		 *
		 * @param pS1 The address of the first region.
		 * @param pS2 The address of the second region.
		 * @param size The number of bytes to swap.
		 */
		public static function memswap(pS1:int, pS2:int, size:int):void
		{
			var rest:int = size & 7;
			size = size >> 3;
			__asm(
					"loop:",
					__as3(size),
					__as3(0),
					IfEqual("copyRest"),
					__as3(pS1),
					GetInt,
					__as3(pS2),
					GetInt,
					__as3(pS1),
					SetInt,
					__as3(pS2),
					SetInt,
					__as3(pS1),
					__as3(4),
					AddInt,
					SetLocal(pS1),
					__as3(pS2),
					__as3(4),
					AddInt,
					SetLocal(pS2),
					__as3(pS1),
					GetInt,
					__as3(pS2),
					GetInt,
					__as3(pS1),
					SetInt,
					__as3(pS2),
					SetInt,
					__as3(pS1),
					__as3(4),
					AddInt,
					SetLocal(pS1),
					__as3(pS2),
					__as3(4),
					AddInt,
					SetLocal(pS2),
					DecLocalInt(size),
					Jump("loop"),
					"copyRest:",
					__as3(rest),
					LookupSwitch("0", "0", "1", "2", "3", "4", "5", "6", "7"),
					"7:",
					__as3(pS1),
					GetByte,
					__as3(pS2),
					GetByte,
					__as3(pS1),
					SetByte,
					__as3(pS2),
					SetByte,
					IncLocalInt(pS1),
					IncLocalInt(pS2),
					__as3(pS1),
					GetShort,
					__as3(pS2),
					GetShort,
					__as3(pS1),
					SetShort,
					__as3(pS2),
					SetShort,
					IncLocalInt(pS1),
					IncLocalInt(pS1),
					IncLocalInt(pS2),
					IncLocalInt(pS2),
					__as3(pS1),
					GetInt,
					__as3(pS2),
					GetInt,
					__as3(pS1),
					SetInt,
					__as3(pS2),
					SetInt,
					Jump("0"),
					"6:",
					__as3(pS1),
					GetShort,
					__as3(pS2),
					GetShort,
					__as3(pS1),
					SetShort,
					__as3(pS2),
					SetShort,
					IncLocalInt(pS1),
					IncLocalInt(pS1),
					IncLocalInt(pS2),
					IncLocalInt(pS2),
					__as3(pS1),
					GetInt,
					__as3(pS2),
					GetInt,
					__as3(pS1),
					SetInt,
					__as3(pS2),
					SetInt,
					Jump("0"),
					"5:",
					__as3(pS1),
					GetByte,
					__as3(pS2),
					GetByte,
					__as3(pS1),
					SetByte,
					__as3(pS2),
					SetByte,
					IncLocalInt(pS1),
					IncLocalInt(pS2),
					__as3(pS1),
					GetInt,
					__as3(pS2),
					GetInt,
					__as3(pS1),
					SetInt,
					__as3(pS2),
					SetInt,
					Jump("0"),
					"4:",
					__as3(pS1),
					GetInt,
					__as3(pS2),
					GetInt,
					__as3(pS1),
					SetInt,
					__as3(pS2),
					SetInt,
					Jump("0"),
					"3:",
					__as3(pS1),
					GetByte,
					__as3(pS2),
					GetByte,
					__as3(pS1),
					SetByte,
					__as3(pS2),
					SetByte,
					IncLocalInt(pS1),
					IncLocalInt(pS2),
					__as3(pS1),
					GetShort,
					__as3(pS2),
					GetShort,
					__as3(pS1),
					SetShort,
					__as3(pS2),
					SetShort,
					Jump("0"),
					"2:",
					__as3(pS1),
					GetShort,
					__as3(pS2),
					GetShort,
					__as3(pS1),
					SetShort,
					__as3(pS2),
					SetShort,
					Jump("0"),
					"1:",
					__as3(pS1),
					GetByte,
					__as3(pS2),
					GetByte,
					__as3(pS1),
					SetByte,
					__as3(pS2),
					SetByte,
					"0:"
					);
		}

		/**
		 * The memcpy function copies a source memory region to a given destination.
		 *
		 * @param dst The destination address.
		 * @param src The source address.
		 * @param size The number of bytes to copy.
		 */
		public static function memcpy(dst:int, src:int, size:int):void
		{
			var rest:int = size & 7;
			size = size >> 3;
			__asm(
					"loop:",
					__as3(size),
					__as3(0),
					IfEqual("copyRest"),
					__as3(src),
					GetInt,
					__as3(dst),
					SetInt,
					__as3(src),
					__as3(4),
					AddInt,
					SetLocal(src),
					__as3(dst),
					__as3(4),
					AddInt,
					SetLocal(dst),
					__as3(src),
					GetInt,
					__as3(dst),
					SetInt,
					__as3(src),
					__as3(4),
					AddInt,
					SetLocal(src),
					__as3(dst),
					__as3(4),
					AddInt,
					SetLocal(dst),
					DecLocalInt(size),
					Jump("loop"),
					"copyRest:",
					__as3(rest),
					LookupSwitch("0", "0", "1", "2", "3", "4", "5", "6", "7"),
					"7:",
					__as3(src),
					GetByte,
					__as3(dst),
					SetByte,
					__as3(src),
					IncrementInt,
					GetShort,
					__as3(dst),
					IncrementInt,
					SetShort,
					__as3(src),
					IncrementInt,
					IncrementInt,
					GetInt,
					__as3(dst),
					IncrementInt,
					IncrementInt,
					SetInt,
					Jump("0"),
					"6:",
					__as3(src),
					GetShort,
					__as3(dst),
					SetShort,
					__as3(src),
					IncrementInt,
					IncrementInt,
					GetInt,
					__as3(dst),
					IncrementInt,
					IncrementInt,
					SetInt,
					Jump("0"),
					"5:",
					__as3(src),
					GetByte,
					__as3(dst),
					SetByte,
					__as3(src),
					IncrementInt,
					GetInt,
					__as3(dst),
					IncrementInt,
					SetInt,
					Jump("0"),
					"4:",
					__as3(src),
					GetInt,
					__as3(dst),
					SetInt,
					Jump("0"),
					"3:",
					__as3(src),
					GetByte,
					__as3(dst),
					SetByte,
					__as3(src),
					IncrementInt,
					GetShort,
					__as3(dst),
					IncrementInt,
					SetShort,
					Jump("0"),
					"2:",
					__as3(src),
					GetShort,
					__as3(dst),
					SetShort,
					Jump("0"),
					"1:",
					__as3(src),
					GetByte,
					__as3(dst),
					SetByte,
					"0:"
					);
		}
	}
}
