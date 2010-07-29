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
import scala.util.Properties
import apparat.abc.Abc
import apparat.utils.IO._
import apparat.taas.frontend.abc.AbcFrontend
import apparat.taas.TaasCompiler
import apparat.taas.backend.jbc.{JbcClassLoader, JbcBackend}
import java.lang.{Thread => JThread}
import apparat.swf.{SymbolClass, SwfTags, Swf}

/**
 * @author Joa Ebert
 */
object JITB {
	def main(arguments: Array[String]): Unit = {
		Log.level = if(System.getProperty("apparat.debug", "false").toLowerCase == "true") Debug else Info
		Log.addOutput(new ConsoleOutput())

		val log = Log.newLogger

		log.info("Initializing JITB")
		log.debug("Scala version: %s", Properties.versionString)
		
		val jitb = try {
			val parser = new JITBCliParser(arguments) 
			val configuration = parser.configuration
			log.debug("File: %s", configuration.file)
			Some(new JITB(configuration))
		} catch {
			case exception @ JITBCliParserException(message) => {
				log.debug("Exception:", exception.toString)
				log.info("Usage: jitb [file]")
				log.fatal("Error: %s", message)
				None
			}
			case other => {
				log.debug("Exception:", other.toString)
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
		val swf = Swf fromFile configuration.file
		val mainClass = swf.mainClass getOrElse { throw JITBException("Could not find main class.") }

		log.debug("Main class: %s", mainClass)

		//
		// TODO
		// This is incorrect behaviour of course. It is currently only
		// a test case to support loading of a single ABC from a SWF.
		// We do not care at this point where the ABC occurs in the SWF.
		//
		
		val loader = new JbcClassLoader(compile(Abc fromSwf swf get), JThread.currentThread.getContextClassLoader)
		JThread.currentThread setContextClassLoader loader

		log.debug("Creating main class instance ...")
		log.debug("Result: %s", Class.forName(mainClass, true, loader).newInstance())
	}

	def compile(abc: Abc) = {
		val frontend = new AbcFrontend(abc, builtins)
		val backend = new JbcBackend()
		val comp = new TaasCompiler(frontend, backend)
		
		comp.compile()

		backend.classMap
	}

	lazy val builtins = {
		val builtin = getClass getResource "/builtin.abc"
		val toplevel = getClass getResource "/toplevel.abc"
		val playerglobal = getClass getResource "/playerglobal.abc"

		if(null == builtin) {
			log.debug("Failed to load /builtin.abc")
			true
		} else if(null == toplevel) {
			log.debug("Failed to load /toplevel.abc")
			true
		} else if(null == playerglobal) {
			log.debug("Failed to load /playerglobal.abc")
			true
		} else { false } match {
			case true => throw JITBException("Could not load builtins.")
			case false =>
		}

		val builtinABC = using(builtin.openStream) { Abc fromInputStream _ }
		val toplevelABC = using(toplevel.openStream) { Abc fromInputStream _ }
		val playerglobalABC = using(playerglobal.openStream) { Abc fromInputStream _ }

		builtinABC :: toplevelABC :: playerglobalABC :: Nil
	}
}