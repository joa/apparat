package ${package}
{
	import flexunit.framework.TestCase;

	public class TestApp extends TestCase
	{
		/**
		 * Tests the <code>App.min</code> method.
		 * @see ${package}.App#min
		 */
		public function testMin():void {
			var app: App = new App();
			assertEquals(1, app.min(1, 2));
		}
	}
}