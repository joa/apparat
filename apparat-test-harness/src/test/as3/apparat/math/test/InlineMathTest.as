package apparat.math.test {
	import apparat.math.FastMath;

	import apparat.memory.Memory;

	import flash.utils.ByteArray;

	import flexunit.framework.TestCase;

	/**
	 * @author Joa Ebert
	 */
	public class InlineMathTest extends TestCase {
		public function testAtan2(): void {
			const values: Array = [
				[1, 2], [2, 3], [4, 5], [6, 7],
				[7, 2], [8, 3], [9, 5], [10, 7],
				[7, 11], [8, 12], [9, 13], [10, 14],
				[15, 11], [16, 12], [17, 13], [18, 14],
				[-7, 11], [-8, 12], [-9, 13], [-10, 14],
				[-15, 11], [-16, 12], [-17, 13], [-18, 14],
				[7, -11], [8, -12], [9, -13], [10, -14],
				[15, -11], [16, -12], [17, -13], [18, -14],
				[-7, -11], [-8, -12], [-9, -13], [-10, -14],
				[-15, -11], [-16, -12], [-17, -13], [-18, -14]
			]

			var n: int = values.length

			while(--n > -1) {
				var value: Array = values[n]
				assertNumberEquals(Math.atan2(value[0], value[1]), FastMath.atan2(value[0], value[1]))
			}
		}

		public function testSin(): void {
			const values: Array = [-Math.PI * 2.0, -Math.PI, -Math.PI * 0.5, 0.0, Math.PI * 0.5, Math.PI * 2.0]

			for each(var value: Number in values) {
				assertNumberEquals(Math.sin(value), FastMath.sin(value))
			}
		}

		public function testCos(): void {
			const values: Array = [-Math.PI * 2.0, -Math.PI, -Math.PI * 0.5, 0.0, Math.PI * 0.5, Math.PI * 2.0]

			for each(var value: Number in values) {
				assertNumberEquals(Math.sin(value), FastMath.sin(value))
			}
		}

		public function testAbs(): void {
			assertEquals(1.0, FastMath.abs( 1.0))
			assertEquals(1.0, FastMath.abs(-1.0))
			assertEquals(0.0, FastMath.abs( 0.0))
		}

		public function testMin():void {
			assertEquals(-1.0, FastMath.min(-1.0,  0.0))
			assertEquals(-1.0, FastMath.min( 0.0, -1.0))
			assertEquals( 0.0, FastMath.min( 0.0,  1.0))
			assertEquals( 0.0, FastMath.min( 1.0,  0.0))
		}

		public function testMax():void {
			assertEquals(0.0, FastMath.max(-1.0,  0.0))
			assertEquals(0.0, FastMath.max( 0.0, -1.0))
			assertEquals(1.0, FastMath.max( 0.0,  1.0))
			assertEquals(1.0, FastMath.max( 1.0,  0.0))
		}

		//
		// Note: The testSqrt() and testInverseSqrt() tests will work only
		// when the SWF/SWC has been processed by TDSI. This is the case
		// if you run "mvn test".
		//

		public function testSqrt(): void {
			FastMath.initMemory()
			
			for(var i: Number = 1.0; i < 16.0; i+=0.25) {
				assertNumberEquals(Math.sqrt(i), FastMath.sqrt(i))
			}
		}

		public function testInverseSqrt(): void {
			FastMath.initMemory()

			for(var i: Number = 1.0; i < 16.0; i+=0.25) {
				assertNumberEquals(1.0 / Math.sqrt(i), FastMath.rsqrt(i))
			}
		}

		public function testRInt(): void {
			assertEquals(-1, FastMath.rint(-0.5))
			assertEquals(-1, FastMath.rint(-1.0))
			assertEquals( 0, FastMath.rint( 0.0))
			assertEquals( 1, FastMath.rint( 0.5))
			assertEquals( 1, FastMath.rint( 1.0))
		}

		private function assertNumberEquals(expected: Number, actual: Number, error: Number = 0.008): void {
			if(Math.abs(expected - actual) > error) {
				fail("Expected: " + expected.toFixed(4.0) + ", Actual: " + actual.toFixed(4.0))
			}
		}
	}
}