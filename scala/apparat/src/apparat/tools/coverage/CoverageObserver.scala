package apparat.tools.coverage

trait CoverageObserver {
	def instrument(file: String, line: Int): Unit
}