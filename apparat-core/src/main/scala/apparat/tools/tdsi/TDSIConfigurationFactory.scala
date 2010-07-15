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

		if(!input.exists) {
			error("Input "+input+" does not exist.")
		}

		new TDSIConfigurationImpl(input, output, alchemy, macros, inline, fixAlchemy)
	}
}