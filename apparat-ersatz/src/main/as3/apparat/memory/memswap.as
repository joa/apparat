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
	import apparat.asm.DecLocalInt;
	import apparat.asm.GetByte;
	import apparat.asm.GetInt;
	import apparat.asm.GetShort;
	import apparat.asm.IfEqual;
	import apparat.asm.IncLocalInt;
	import apparat.asm.Jump;
	import apparat.asm.LookupSwitch;
	import apparat.asm.SetByte;
	import apparat.asm.SetInt;
	import apparat.asm.SetLocal;
	import apparat.asm.SetShort;
	import apparat.asm.__as3;
	import apparat.asm.__asm;

    /**
     * Swaps two memory regions.
     *
     * @param pS1 The address of the first region.
     * @param pS2 The address of the second region.
     * @param size The number of bytes to swap.
     */
	public function memswap(pS1: int, pS2: int, size: int): void {
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
}