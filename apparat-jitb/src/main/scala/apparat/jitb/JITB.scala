/*
 * This file is part of Apparat.
 *
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.jitb

import apparat.log.output.ConsoleOutput
import apparat.log.{SimpleLog, Debug, Info, Log}

/**
 * @author Joa Ebert
 */
object JITB {
	def main(args: Array[String]): Unit = {
		Log.level = if(System.getProperty("apparat.debug", "false").toLowerCase == "true") Debug else Info
		Log.addOutput(new ConsoleOutput())

		val log = Log.newLogger
		val jitb = try {
			val parser = new JITBCliParser(arguments)
			val configuration = parser.configuration
			Some(new JITB(configuration))
		} catch {
			case exception @ JITBCliParserException(message) => {
				log.debug("Exception:", exception.toString)
				log.info("Usage: jitb [file]")
				log.fatal("Error: %s", message)
				None
			}
			case other => {
				log.debug("Exception:", exception.toString)
				log.fatal("Error: %s", other.getLocalizedMessage)
				None
			}
		}

		jitb match {
			case Some(value) => value.run()
			case None =>
		}
	}
}

class JITB(configuration: JITBConfiguration) extends SimpleLog {
	def run() = {
		log << "Hello World."
	}
}