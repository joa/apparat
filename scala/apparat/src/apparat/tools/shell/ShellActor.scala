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
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.tools.shell

import actors.Actor
import actors.Actor._
import apparat.tools.stripper.Stripper
import compat.Platform

/**
 * @author Joa Ebert
 */
class ShellActor extends Actor {
	def act() = loop {
		receive {
			case CommandEvent(command) => {
				val response = (command: Seq[Char]) match {
					case Seq('s','t','r','i','p','p','e','r',' ', rest @ _*) => {
						val t0 = Platform.currentTime
						lazy val t1 = Platform.currentTime
						Stripper.main(createArguments(rest))
					}
					case Seq('h','e','l','p', ' ', rest @ _*) => (rest mkString "") match {
						case "stripper" => "stripper -i input [-o output]"
						case "help" | "exit" | "quit" | "stop" => "No detail help available."
						case other => "Error: Unknown command \"" + other + "\""
					}
					case Seq('h', 'e', 'l', 'p') => """help [command] - For detailed help
quit - Exit the Apparat shell
stripper - Run the stripper tool"""
					case _ => "Error: Unknown command \"" + command + "\""
				}

				sender ! response
			}
		}
	}

	private def createArguments(arguments: Seq[Char]) = {
		arguments mkString "" split " "
	}
}