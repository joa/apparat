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

package apparat.asm {
	/**
	 * The __cint function ensures the use of integer typed calculus.
     *
	 * It is the developers duty to make sure that all operands are
     * actually of type <code>int</code>.
	 *
	 * @param value Any arithmetic expression.
     * @example
     * <pre>
     *   var x: int = 2
     *   trace(__cint(x*x))//uses now MultiplyInt instead of Multiply
     * </pre>
	 */
	public function __cint(value: *): int {return 0;}
}