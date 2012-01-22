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

package apparat.inline
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
