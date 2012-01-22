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

package apparat.asm {
	/**
	 * The __asm function is a prototype that allows you to write inline assembler
	 * instructions by using ActionScript 3 syntax.
	 *
	 * <p>TurboDieselSportInjection expands the __asm method in your code and
	 * replaces all instructions with the corresponding bytecode.</p>
	 *
	 * @author Patrick Le Clec'h
	 * @see __as3
	 *
	 * @example
	 * <pre>
	 * //trace('Hello World');
	 * __asm(
	 *	  FindPropStrict(__as3(trace)),
	 *	  PushString('Hello World!'),
	 *	  CallPropVoid(__as3(trace), 1)
	 * );
	 * </pre>
	 */
	public function __asm(...args): void {}
}
