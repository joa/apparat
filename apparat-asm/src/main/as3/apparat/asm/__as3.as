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

/**
 * The __as3 function will allow you to directly output the bytecode generated
 * by the as3 compiler into you asm code
 *
 * There is special case where the code is not output as this, but lift of some operation
 * It's the case with Op that needs an Abc name or an Abc namespace
 * FindPropStrict is an example of such method, the compiler will try to guess
 * what name or namespace you want to reach by inspecting the as3 generated code
 *
 * @author Patrick Le Clec'h
 *
 * @see __asm
 *
 * @example
 * <pre>
 * __asm(
 *    // try to resolve the name trace
 *    // this is the same as FindPropStrict(AbcQName("trace", AbcNamespace(NamespaceKind.PACKAGE, "")))
 *    // but shorter since the as3 compiler resolve it for you
 *	  FindPropStrict(__as3(trace)),
 *	  PushString('Hello World!'),
 *	  CallPropVoid(__as3(trace), 1)
 * );
 * </pre>
 *
 * @example
 * <pre>
 * var x:int=12;
 * __asm(
 * // this is legal
 * // it will push on the stack the result of the comparison between var x and 10
 * 	__as3(x<10)
 * );
 * </pre>
 */

package apparat.asm {
	public function __as3(value:*):void {}
}