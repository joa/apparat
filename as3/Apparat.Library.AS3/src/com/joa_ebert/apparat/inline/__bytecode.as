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
 */

package com.joa_ebert.apparat.inline 
{
	/**
	 * Injects a set of raw bytes.
	 * 
	 * <p>The <code>__bytecode</code> method can not be compared with the
	 * <code>__asm</code> method since
	 * the <code>__bytecode</code> method requires from the developer to write
	 * the exact bytes required. This means an operation like PushInt becomes
	 * nearly impossible to inject using <code>__bytecode</code> since one
	 * would have to know the constant pool index of the integer in advance.</p>
	 * 
	 * @param bytes The bytes to inject.
	 * 
	 * @author Joa Ebert
	 */
	public function __bytecode( ... bytes ): void {}
}
