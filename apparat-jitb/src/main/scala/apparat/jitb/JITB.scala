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
import math.TwipsMath
import scala.util.Properties
import apparat.abc.Abc
import apparat.utils.IO._
import apparat.taas.frontend.abc.AbcFrontend
import apparat.taas.TaasCompiler
import java.lang.{Thread => JThread}
import flash.display.{DisplayObject, Stage, Sprite}
import java.util.{TimerTask, Timer}
import jitb.display.DisplaySystem
import org.lwjgl.opengl.{GL11, DisplayMode, PixelFormat, Display}
import jitb.errors.{ErrorUtil, Throw}
import apparat.taas.backend.jbc.{JbcClassWriter, JbcClassLoader, JbcBackend}
import java.io.{File => JFile}
import jitb.lang.AVM
import apparat.swc.Swc
import apparat.swf.{DoABC, SymbolClass, SwfTags, Swf}
import jitb.events.EventSystem

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

		if(log.debugEnabled) {
			swf.tags foreach { t => log.debug("Tag: %s", t) }
		}
		
		val mainClass = liftToplevel(swf.mainClass getOrElse { throw JITBException("Could not find main class.") })

		log.debug("Main class: %s", mainClass)

		//
		// TODO
		// This is incorrect behaviour of course. It is currently only
		// a test case to support loading of a single ABC from a SWF.
		// We do not care at this point where the ABC occurs in the SWF.
		//

		val binaries = compile(Abc fromSwf swf get)
		val loader = new JbcClassLoader(binaries, JThread.currentThread.getContextClassLoader)
		JThread.currentThread setContextClassLoader loader

		//new JbcClassWriter(binaries).write(new JFile("/home/joa/classes"))

		val main = Class.forName(mainClass, true, loader)

		AVM.basePath(configuration.file.getParent)
		AVM.start()

		try {
			if(classOf[DisplayObject] isAssignableFrom main) {
				runWithDisplay(swf, main)
			} else {
				//
				// For now we use a hardcoded empty array.
				//
				val arguments: Array[String] = Array.empty[String]

				log.debug("Using a headless runner without a stage.")

				AVMContext {
					main.getMethod("main", arguments.getClass).invoke(main, arguments)
				} match {
					case Right(_) => log.debug("Code executed WITHOUT errors.")
					case Left(_) => log.debug("Code executed WITH errors.")
				}
			}
		} finally {
			AVM.stop()
		}
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
		val playerglobal = getClass getResource "/playerglobal.swc"

		if(null == builtin) {
			log.debug("Failed to load /builtin.abc")
			true
		} else if(null == toplevel) {
			log.debug("Failed to load /toplevel.abc")
			true
		} else if(null == playerglobal) {
			log.debug("Failed to load /playerglobal.swc")
			true
		} else { false } match {
			case true => throw JITBException("Could not load builtins.")
			case false =>
		}

		val builtinABC = using(builtin.openStream) { Abc fromInputStream _ }
		val toplevelABC = using(toplevel.openStream) { Abc fromInputStream _ }
		val playerglobalSWC = using(playerglobal.openStream) { Swc fromInputStream _ }

		(builtinABC :: toplevelABC :: Nil) ::: (Swf fromSwc playerglobalSWC).tags collect { case doABC: DoABC => Abc fromDoABC doABC }
	}

	private def runWithDisplay(swf: Swf, main: Class[_]): Unit = {
		//
		// Initialize display
		//

		Display.setTitle("JITB "+configuration.file.toString)
		Display.setFullscreen(true)
		Display.setVSyncEnabled(true)
		Display.setDisplayMode(new DisplayMode(swf.width, swf.height))
		Display.create()

		//
		// Orthographic projection with 1:1 pixel ratio.
		//

		GL11.glMatrixMode(GL11.GL_PROJECTION)
		GL11.glLoadIdentity()
		GL11.glOrtho(0.0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0.0, -1.0, 1.0)
		GL11.glMatrixMode(GL11.GL_MODELVIEW)
		GL11.glLoadIdentity()
		GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight())

		//
		// Generic setup
		//

		GL11.glEnable(GL11.GL_TEXTURE_2D)

		GL11.glClearDepth(1.0)
		GL11.glDisable(GL11.GL_DEPTH_TEST)

		//temporary for color transform until fragment shader
		GL11.glShadeModel(GL11.GL_SMOOTH)
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST)
		GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST)

		swf.backgroundColor match {
			case Some(rgb) => GL11.glClearColor(
				rgb.red.toFloat / 255.0f,
				rgb.green.toFloat / 255.0f,
				rgb.blue.toFloat / 255.0f, 1.0f)
			case None =>
		}

		//
		// DisplaySystem setup
		//

		val stage = new Stage()

		stage.frameRate(swf.frameRate.asInstanceOf[Double])

		val documentRoot = AVMContext { main.newInstance() } match {
			case Right(value) => value
			case Left(_) => error("Could not create DocumentRoot.")
		}

		log.debug("Main class is a DisplayObject")
		log.debug("SWF info:")
		log.debug("\tFramerate: %.2f", stage.frameRate)
		log.debug("\tWidth: %d", swf.width)
		log.debug("\tHeight: %d", swf.height)
		swf.backgroundColor match {
			case Some(rgb) => log.debug("\tBackground: %d, %d, %d", rgb.red, rgb.green, rgb.blue)
			case None => log.debug("\tBackground: None")
		}

		log.debug("Created Stage %s.", stage)
		log.debug("Created DocumentRoot %s.", documentRoot)

		stage.addChild(documentRoot.asInstanceOf[DisplayObject])
		
		//
		// Render loop
		//

		var noErrorOccurred = true

		while(noErrorOccurred && !Display.isCloseRequested) {
			//
			// LWJGL magic.
			//

			Display.update()

			AVMContext {
				//
				// Dispatch all events in the queue.
				//
				
				EventSystem.dispatchEvents()

				//
				// Dispatch an ENTER_FRAME event to every DisplayObject
				//

				DisplaySystem.enterFrame()

				//
				// Render all objects in the display list.
				//

				DisplaySystem render stage

				//
				// Dispatch an EXIT_FRAME event to every DisplayObject
				//

				DisplaySystem.exitFrame()
			} match {
				case Right(_) =>
					//
					// Synchronize to frame rate
					//

					Display sync stage.frameRate.asInstanceOf[Int]
				case Left(_) => noErrorOccurred = false
			}
		}

		Display.destroy()
	}

	private def liftToplevel(qname: String) = qname indexOf '.' match {
		case -1 => "jitb.lang."+ qname
		case _ => qname
	}

	private def AVMContext[A](body: => A): Either[Unit, A] = {
		try { Right(body) } catch {
			case actionScriptError: Throw => {
				actionScriptError.value match {
					case error: jitb.lang.Error =>
						log.error("%s: Error #%d: %s", error.name, error.errorID, error.message)
						log.error("%s", error.getStackTrace())
					case other =>
						log.error("Object has not been catched.")
						log.error("%s", other)
				}
				Left(())
			}
			case npe: NullPointerException => {
				npe.printStackTrace()
				val error = ErrorUtil.error1009()
				log.error("%s: Error #%d: %s", error.name, error.errorID, error.message)
				log.error("%s", error.getStackTrace())
				Left(())
			}
			case other => {
				other.printStackTrace()
				log.error("An internal error occurred.")
				log.error("%s", other)
				Left(())
			}
		}
	}
}