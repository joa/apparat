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
package apparat.log

import org.specs.SpecificationWithJUnit
import output.ConsoleOutput
import apparat.log.{
	//We have to do this since Specs comes with a logger that shadows ours.
	Debug => ApparatDebug, Info => ApparatInfo, Warning => ApparatWarning,
	Error => ApparatError, Fatal => ApparatFatal, Off => ApparatOff
}

/**
 * @author Joa Ebert
 */
class LogSpec extends SpecificationWithJUnit {
	"The Apparat log" should {
		"not fail without initialization" in {
			// Works when test does not throw an exception
			Log.newLogger << ""
			true must beTrue
		}

		"allow output appending and removal" in {
			val output = new ConsoleOutput
			Log addOutput output
			Log.outputs must contain(output)
			Log removeOutput output
			Log.outputs mustNot contain(output)
		}

		"initialize to Info level" in {
			Log.level mustEqual ApparatInfo
		}

		"provide a DSL for C++ style streams" in {
			val output = new LogOutput {
				override def log(level: LogLevel, message: String) = {
					level mustEqual ApparatFatal
				}
			}

			try {
				Log.level = ApparatFatal
				Log addOutput output

				val log = Log.newLogger

				log << ApparatDebug << "debug"
				log << ApparatInfo << "info"
				log << ApparatWarning << "warning"
				log << ApparatError << "error"
				log << ApparatFatal << "fatal"
			} finally {
				Log.level = ApparatInfo
				Log removeOutput output
			}
		}

		"provide selective filtering" in {
			// Works when test does not throw an exception

			val output = new LogOutput {
				override def log(level: LogLevel, message: String) = {
					fail(level+", "+message)
				}
			}

			try {
				Log.level = ApparatOff
				Log addOutput output

				val log = Log.newLogger

				log ifDebug { fail("debug") }
				log ifInfo { fail("info") }
				log ifWarning { fail("warning") }
				log ifError { fail("error") }
				log ifFatal { fail("fatal") }
			} finally {
				Log.level = ApparatInfo
				Log removeOutput output
			}

			true must beTrue
		}
	}

	"The Apparat log" can {
		"mixin a log object" in {
			var touched = false
			val output = new LogOutput {
				override def log(level: LogLevel, message: String) = {
					level mustEqual ApparatInfo
					message mustEqual "info"
					touched = true
				}
			}

			try {
				Log addOutput output

				val x = new Object with SimpleLog
				x.log << "info"
			} finally {
				Log removeOutput output
			}

			touched must beTrue
		}

		"format its output" in {
			var touched = false
			val output = new LogOutput {
				override def log(level: LogLevel, message: String) = {
					level mustEqual ApparatInfo
					message mustEqual "this is a test"
					touched = true
				}
			}

			try {
				Log addOutput output

				val x = new Object with SimpleLog
				x.log.info("this %s a %s", "is", "test")
			} finally {
				Log removeOutput output
			}

			touched must beTrue
		}
	}
}
