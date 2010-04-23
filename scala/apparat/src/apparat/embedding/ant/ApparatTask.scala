package apparat.embedding.ant

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
 * User: Patrick Le Clec'h
 * Date: 12 févr. 2010
 * Time: 12:50:50
 */
import org.apache.tools.ant.taskdefs.Java
import collection.mutable.HashMap

trait OutParameter {
	def setArgument(name: String, value: Any)

	def setOut(name: String) = setArgument("o", name)
}

class ApparatTask(tool: {def getClass: Class[_]}, name: String) extends Java {
	setClassname(tool.getClass.getName.dropRight(1))
	setTaskName(name)

	val arguments: HashMap[String, String] = HashMap.empty

	//	@deprecated("Use setArgument instead of createArg")
	override def createArg = error("Use setArgument instead of createArg")

	def setArgument(name: String, value: Any) = {
		arguments(name) = value.toString
	}

	override def execute = {
		clearArgs()

		arguments.keysIterator.foreach(name => {
			super.createArg().setValue("-" + name)
			super.createArg().setValue(arguments(name))
		})

		super.execute()
	}

	//	def out = arguments.getOrElse("o", "")
	//
	//	def int = arguments.getOrElse("i", "")

	def setIn(name: String) = setArgument("i", name)
}