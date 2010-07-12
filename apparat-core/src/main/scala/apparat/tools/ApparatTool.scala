package apparat.tools

import apparat.log.SimpleLog

trait ApparatTool extends SimpleLog {
	def help: String

	def name: String

	def configure(config: ApparatConfiguration)

	def run()
}
