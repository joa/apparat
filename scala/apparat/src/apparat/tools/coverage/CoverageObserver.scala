package apparat.tools.coverage

trait CoverageObserver {
	def begin(file: String)
	def instrument(line: Int): Unit
	def end(file: String)
}