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
	import apparat.math.IntMath;

	import flexunit.framework.TestCase;

	/**
	 * @author Joa Ebert
	 */
	public class IntMathTest extends TestCase {
		public function testToARGB(): void {
			assertEquals(0xff000000, IntMath.toARGB(0xff, 0x00, 0x00, 0x00))
			assertEquals(0xffff00ff, IntMath.toARGB(0xff, 0xff, 0x00, 0xff))
		}

		public function testToRGB(): void {
			assertEquals(0xff0000, IntMath.toRGB(0xff, 0x00, 0x00))
			assertEquals(0xff00ff, IntMath.toRGB(0xff, 0x00, 0xff))
		}

		public function testIsEven(): void {
			for(var i: int = -0x100; i <= 0x100; ++i) {
				assertEquals(i % 2 == 0, IntMath.isEven(i))
			}
		}

		public function testIsOdd(): void {
			for(var i: int = -0x100; i <= 0x100; ++i) {
				assertEquals(i % 2 != 0, IntMath.isOdd(i))
			}
		}

		public function testAbs(): void {
			for(var i: int = -0x100; i <= 0x100; ++i) {
				assertEquals(Math.abs(i), IntMath.abs(i))
			}

			assertEquals(Math.abs( 0xffffff), IntMath.abs( 0xffffff))
			assertEquals(Math.abs(-0xffffff), IntMath.abs(-0xffffff))
		}

		public function testMin():void {
			assertEquals(-1, IntMath.min(-1,  0))
			assertEquals(-1, IntMath.min( 0, -1))
			assertEquals( 0, IntMath.min( 0,  1))
			assertEquals( 0, IntMath.min( 1,  0))

			assertEquals(-0xffffff, IntMath.min(0, -0xffffff))
			assertEquals( 0x000000, IntMath.min(0,  0xffffff))
		}

		public function testMax():void {
			assertEquals(0, IntMath.max(-1,  0))
			assertEquals(0, IntMath.max( 0, -1))
			assertEquals(1, IntMath.max( 0,  1))
			assertEquals(1, IntMath.max( 1,  0))

			assertEquals(0x000000, IntMath.max(0, -0xffffff))
			assertEquals(0xffffff, IntMath.max(0,  0xffffff))
		}

		public function equalSign(): void {
			assertEquals(true, IntMath.equalSign(-1, -2))
			assertEquals(true, IntMath.equalSign( 1,  2))
			assertEquals(true, IntMath.equalSign( 0,  0))
			assertEquals(false, IntMath.equalSign(-1,  1))
			assertEquals(false, IntMath.equalSign( 1, -1))
		}

		public function unequalSign(): void {
			assertEquals(false, IntMath.unequalSign(-1, -2))
			assertEquals(false, IntMath.unequalSign( 1,  2))
			assertEquals(false, IntMath.unequalSign( 0,  0))
			assertEquals(true, IntMath.unequalSign(-1,  1))
			assertEquals(true, IntMath.unequalSign( 1, -1))
		}
	}
}
