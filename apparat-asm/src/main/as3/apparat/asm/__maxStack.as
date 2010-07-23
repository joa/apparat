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
 * Sets the maximum stack for a method manually.
 *
 * <p>It is very important to remember that the maximum stack of a function
 * is a very critical value. If the real stack exceeds the maximum stack the
 * Flash Player will throw a VerifyError.</p>
 *
 * <p>Although for experienced users this function might be interesting to
 * use. Usually the inline compiler can figure out the maximum stack by
 * itself automatically.
 * There are some scenarios where this is not possible. Imagine the following
 * loop:</p>
 *
 * <code>
 * for(var i: int = 0; i &lt; stage.mouseX; ++i)
 * {
 *   __asm(PushInt(0));
 * }
 * </code>
 *
 * <p>The stack increases in this function based on a runtime value. The compiler
 * can not know the maximum x-coordinate of the stage. So even this scenario is
 * very odd you can still achieve it by using the <code>__maxStack()</code> function and
 * set the value manually.</p>
 *
 * @param value The maximum stack for the method.
 *
 * @author Joa Ebert
 * @author Patrick Le Clec'h
 */

package apparat.asm {
	public function __maxStack(value:uint):void {}
}