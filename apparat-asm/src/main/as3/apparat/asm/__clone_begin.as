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
	 * __clone_begin will tell TDSI to clone (at compile time) count times all operations between __clone_begin(count) and __clone_end
	 *
	 * __clone_begin(5) // repeat all the code below 5 times
	 * trace("hello world")
	 * __clone_end()
	 *
	 * @param count integer constant that represents how many times the operations have to be cloned
	 *
	 * @see __clone_end
	 */
	public function __clone_begin(count:int):void{};
}
