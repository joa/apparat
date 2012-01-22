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
package apparat.tools.shell

import actors.Actor._
import actors.Actor
import apparat.tools.stripper.Stripper
import apparat.tools.reducer.Reducer
import apparat.tools.concrete.Concrete
import apparat.tools.coverage.Coverage
import apparat.tools.tdsi.TurboDieselSportInjection
import apparat.tools.dump.Dump

/**
 * @author Joa Ebert
 */
class ShellActor extends Actor {
	def act() = loop {
		receive {
			case CommandEvent(command) => {
				val response = if(command startsWith "stripper ") {
					Stripper.main(createArguments(command drop "stripper ".length))
				} else if(command startsWith "reducer ") {
					Reducer.main(createArguments(command drop "reducer ".length))
				} else if(command startsWith "coverage ") {
					Coverage.main(createArguments(command drop "coverage ".length))
				} else if(command startsWith "concrete ") {
					Concrete.main(createArguments(command drop "concrete ".length))
				} else if(command startsWith "tdsi ") {
					TurboDieselSportInjection.main(createArguments(command drop "tdsi ".length))
				} else if(command startsWith "dump ") {
					Dump.main(createArguments(command drop "dump ".length))
				} else if(command startsWith "help ") {
					(command drop "help ".length) match {
						case "reducer" => "reducer -i input [-o output] [-q quality]"
						case "stripper" => "stripper -i input [-o output]"
						case "tdsi" => "tdsi -i input [-o output]"
						case "coverage" => "coverage -i input [-o output]"
						case "concrete" => "concrete -i input"
						case "dump" => "dump -i input [-o output] [-uml] [-swf] [-abc] [-bc (raw|cfg|default)]"
						case "help" | "exit" | "quit" | "stop" => "No detail help available."
						case other => "Error: Unknown command \"" + other + "\""
					}
				} else if(command == "help") {
					"""help [command] - For detailed help
quit - Exit the Apparat shell
tdsi - Inline Alchemy operations
reducer - Convert lossless to lossy graphics
stripper - Strip traces and debug operations
concrete - Checks that [Abstract] methods are implemented
coverage - Inject coverage analytics
dump - Swf information"""
				} else {
					"Error: Unknown command \""+command+"\""
				}

				sender ! response
			}
			case ExitEvent => exit()
		}
	}

	private def createArguments(arguments: Seq[Char]) = {
		//TODO split correct based on platform
		arguments mkString "" split " "
	}
}
