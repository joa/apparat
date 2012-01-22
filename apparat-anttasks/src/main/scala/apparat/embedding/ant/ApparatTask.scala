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
package apparat.embedding.ant

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
