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