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
package apparat.tools.dump

import apparat.tools.{ApparatConfiguration, ApparatConfigurationFactory}
import java.io.{File => JFile}

/**
 * @author Joa Ebert
 */
object DumpConfigurationFactory extends ApparatConfigurationFactory[DumpConfiguration] {
	override def fromConfiguration(config: ApparatConfiguration): DumpConfiguration = {
		val input = config("-i") map { path => new JFile(path) } getOrElse error("Input is required.")
		val output = config("-o") map { path => new JFile(path) }
		val exportSWF = (config("-swf") getOrElse "false").toBoolean
		val exportUML = (config("-uml") getOrElse "false").toBoolean
		val exportABC = (config("-abc") getOrElse "false").toBoolean
		val bytecodeFormat = config("-bc") getOrElse "default" match {
			case "raw" => DumpBytecodeAsRaw
			case "cfg" => DumpBytecodeAsGraph
			case "default" => DumpBytecodeAsDefault
			case _ => error("Bytecode format must be either \"raw\", \"cfg\", or \"default\".")
		}

		if(!input.exists) {
			error("Input "+input+" does not exist.")
		}

		new DumpConfigurationImpl(input, output, exportSWF, exportUML, exportABC, bytecodeFormat)
	}
}
