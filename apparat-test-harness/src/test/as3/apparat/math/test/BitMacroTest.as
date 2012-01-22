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
package apparat.math.test {
	import apparat.math.BitMacro;

	import flexunit.framework.TestCase;

	/**
	 * @author Joa Ebert
	 */
	public class BitMacroTest extends TestCase {
		public function testSwap(): void {
			var x: int = 1
			var y: int = 2

			BitMacro.swap(x, y)

			assertEquals(2, x)
			assertEquals(1, y)
		}
	}
}
