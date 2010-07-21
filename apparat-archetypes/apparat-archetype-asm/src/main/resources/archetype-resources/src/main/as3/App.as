package ${package}
{
	import apparat.asm.*;
	import flash.display.Sprite;

	public class App extends Sprite
	{
		/**
		 * Creates and returns a new App object.
		 */
		public function App()
		{
			__asm(
				FindPropStrict(__as3(trace)),
				PushString("Hello World!"),
				CallPropVoid(__as3(trace), 1)
			);

			if(min(1,2) != 1)
			{
				throw new Error("__asm has not been expanded.");
			}
		}

		public function min(a: int,  b: int): int
		{
			__asm(
				GetLocal(a),			// Get parameter "a".
				GetLocal(b),			// Get parameter "b".
				IfLessThan("aIsLessThanB"),	// If "a" is less than "b" we jump to "aIsLessThanB".
				GetLocal(b),			// Otherwise we get parameter "b".
				ReturnValue,			// Return "b".
			"aIsLessThanB:",			// Define the label "aIsLessThanB".
				GetLocal(a),			// Get the parameter "a".
				ReturnValue			// Return it.
			);

			//
			// We need this return statement since the ASC does not understand
			// our __asm method.
			//

			return -1;
		}
	}
}
