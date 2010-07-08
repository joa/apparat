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
package apparat.tools.reducer

import apparat.tools.{ApparatConfiguration, ApparatConfigurationFactory}
import java.io.{File => JFile}

/**
 * @author Joa Ebert
 */
object ReducerConfigurationFactory extends ApparatConfigurationFactory[ReducerConfiguration] {
	override def fromConfiguration(config: ApparatConfiguration): ReducerConfiguration = {
		val input = new JFile(config("-i") getOrElse error("Input is required."))
		val output = config("-o") map { pathname => new JFile(pathname) } getOrElse input
		val deblock = java.lang.Float parseFloat config("-d").getOrElse("0.0")
		val quality = java.lang.Float parseFloat config("-q").getOrElse("0.99")
		val merge = (config("-m") getOrElse "false").toBoolean
		
		assert(input.exists, "Input has to exist.")

		new ReducerConfigurationImpl(input, output, quality, deblock, merge)
	}
}