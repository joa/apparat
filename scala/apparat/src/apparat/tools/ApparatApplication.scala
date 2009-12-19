package apparat.tools

object ApparatApplication {
	def apply(tool: ApparatTool, args: Array[String]): Int = {
		val t0 = System.currentTimeMillis()
		var result = 0
		try {
			ApparatLog("Apparat -- http://apparat.googlecode.com/")
			ApparatLog("Launching tool: " + tool.name)

			val config = new ApparatConfiguration
			var valid = args.length > 0

			try {config parse args}
			catch {
				case t => {
					valid = false
					result = -1
					ApparatLog err t
				}
			}

			if (!valid || config("help").isDefined || config("h").isDefined) {
				ApparatLog help tool.help
			} else {
				tool configure config
				tool.run()
			}
		} catch {
			case t => {
				ApparatLog err t
				result = -1
			}
		}
		val t1 = System.currentTimeMillis()
		ApparatLog("Completed in " + (t1 - t0) + "ms.")
		result
	}
}
