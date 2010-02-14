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

import actors.Actor._
import actors.{Exit, Futures, Actor}

/**
 * @author Joa Ebert
 */
object ApparatShell {
	private val shell = new ShellActor()
	private val exec = actor {
		loop {
			receive {
				case ExitEvent => Exit
				case CommandEvent(command) => shell ! CommandEvent(command)
				case () => 
				case other => println(other)
			}
		}
	}

	def main(args: Array[String]): Unit = {
		shell.start()
		println("Welcome to the Apparat!")
		println("Type \"help\" for a list of available commands ...")
		println("")
		run()
	}

	def exit(): Unit = {
		try {
			exec ! ExitEvent
			shell ! ExitEvent
			Futures.alarm(0)
		} catch {
			case _ =>
		}
		
		System exit 0
	}

	def run(): Unit = {
		val command = Console.readLine()
		command.toLowerCase match {
			case "exit" | "quit" | "stop" => exit()
			case _ => {
				exec ! CommandEvent(command)
				run()
			}
		}
	}
}