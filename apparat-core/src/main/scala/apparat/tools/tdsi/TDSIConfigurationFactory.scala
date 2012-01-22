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
package apparat.tools.tdsi

import apparat.tools.{ApparatConfiguration, ApparatConfigurationFactory}
import java.io.{File => JFile}

/**
 * @author Joa Ebert
 */
object TDSIConfigurationFactory extends ApparatConfigurationFactory[TDSIConfiguration] {
	override def fromConfiguration(config: ApparatConfiguration): TDSIConfiguration = {
		val input = new JFile(config("-i") getOrElse error("Input is required."))
		val output = config("-o") map { pathname => new JFile(pathname) } getOrElse input
		val alchemy = (config("-a") getOrElse "true").toBoolean
		val macros = (config("-m") getOrElse "true").toBoolean
		val inline = (config("-e") getOrElse "true").toBoolean
		val fixAlchemy = (config("-f") getOrElse "false").toBoolean
		val asm = (config("-s") getOrElse "true").toBoolean
		val libraries = config("-l") map { _ split JFile.pathSeparatorChar toList } map { pathname => for(p <- pathname) yield new JFile(p) } getOrElse List.empty[JFile]

		for(library <- libraries) {
			if(!library.exists) {
				error("Library "+library+" does not exist.")
			}
		}

		if(!input.exists) {
			error("Input "+input+" does not exist.")
		}

		new TDSIConfigurationImpl(input, output, alchemy, macros, inline, fixAlchemy, asm, libraries)
	}
}
