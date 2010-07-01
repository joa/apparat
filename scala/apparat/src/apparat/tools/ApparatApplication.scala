package apparat.tools

import scala.util.Properties

object ApparatApplication {
	val scalaVersionString = "version 2.8.0.RC7"
	def apply(tool: ApparatTool, args: Array[String]): Int = {
		val t0 = System.currentTimeMillis()
		var result = 0
		try {
			ApparatLog("Apparat -- http://apparat.googlecode.com/")

			Properties.versionString match {
				case x if x == scalaVersionString =>
				case other => {
					ApparatLog err "Apparat requires Scala "+scalaVersionString
					ApparatLog err "Download it here: http://www.scala-lang.org/downloads"
					return -1
				}
			}

			ApparatLog("Launching tool: " + tool.name)

			if(!apparat.actors.Actor.threadsEnabled) {
				ApparatLog.warn("Warning: Apparat actors running in single-thread mode")
			}

			val config = new ApparatConfiguration
			var valid = args.length > 0

			try {
				config parse args
			} catch {
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
