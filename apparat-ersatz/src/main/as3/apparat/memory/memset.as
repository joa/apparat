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

    /**
     * The memset function sets a given memory interval to a given value.
     *
     * @param dst The address in memory where to start.
     * @param value The value to store.
     * @param size The number of bytes to write.
     */
	public function memset(dst: int, value: int, size: int): void {
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
}
