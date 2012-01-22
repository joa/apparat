/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.tools

import scala.util.Properties
import apparat.log._
import output.ConsoleOutput

object ApparatApplication extends SimpleLog {
	//val scalaVersionString = "version 2.8.0.final"
	var outputAdded = false

	def apply(tool: ApparatTool, args: Array[String]): Int = {
		val t0 = System.currentTimeMillis()
		var result = 0

		val (defines, arguments) = args partition { _ startsWith "-D" }

		for(define <- defines if define.length > 2) {
			val defineString = define substring 2
			val index = defineString indexOf '='

			if(0 == index) {}
			else if(-1 == index) {
				System.setProperty(defineString, "true")
			} else {
				System.setProperty(defineString.substring(0, index),
					defineString.substring(index + 1))
			}
		}

		Log.level = if(System.getProperty("apparat.debug", "false").toLowerCase == "true") Debug else Info

		if((System.getProperty("apparat.quiet", "false").toLowerCase == "false") && !outputAdded) {
			outputAdded = true
			Log.addOutput(new ConsoleOutput())
		}

		try {
			log.info("Apparat -- http://apparat.googlecode.com/")

			/*Properties.versionString match {
				case x if x == scalaVersionString =>
				case other => {
					log.error("Apparat requires Scala %s.", scalaVersionString)
					log.error("Download it here: http://www.scala-lang.org/downloads")
					return -1
				}
			}*/

			log.info("Launching tool: %s", tool.name)

			if(!apparat.actors.Actor.threadsEnabled) {
				log.warning("Apparat is running in mono-thread mode.")
			}

			val config = new ApparatConfiguration
			var valid = arguments.length > 0

			try {
				config parse arguments
			} catch {
				case t => {
					valid = false
					result = -1
					log.fatal(t.getMessage)
					log.debug(t.getStackTraceString)
				}
			}

			if (!valid || config("help").isDefined || config("h").isDefined) {
				log.info("Help:%n%s", tool.help)
			} else {
				tool configure config
				tool.run()
			}
		} catch {
			case t => {
				t.printStackTrace()
				log.fatal(t.getMessage)
				log.debug(t.getStackTraceString)
				result = -1
			}
		}
		val t1 = System.currentTimeMillis()
		log.info("Completed in %dms.", t1 - t0)
		result
	}
}
