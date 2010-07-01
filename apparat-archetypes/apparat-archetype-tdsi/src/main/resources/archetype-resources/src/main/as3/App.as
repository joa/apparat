package ${package}
{
	import apparat.math.FastMath;

	public class App
	{
		/**
		 * Creates and returns a new App object.
		 */
		public function App()
		{
		}

		public function min(a: int,  b: int): int
		{
			return FastMath.min(a, b);
		}

		/**
		 * Generates and returns the string representation
		 * of the current object.
		 *
		 * @return The string representation of the current object.
		 */
		public function toString(): String
		{
			return '[App]';
		}
	}
}