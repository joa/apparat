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
		val sort = (config("-s") getOrElse "false").toBoolean
		val lzma = (config("-l") getOrElse "false").toBoolean
		val matryoshkaType = (config("-t") getOrElse "quiet") match {
			case "quiet" => MatryoshkaType.QUIET
			case "preloader" => MatryoshkaType.PRELOADER
			case "custom" => MatryoshkaType.CUSTOM
			case "none" => MatryoshkaType.NONE
		}
		val matryoshka = config("-f") map { f => new JFile(f) }
		val mergeCF = (config("-b") getOrElse "false").toBoolean

		if(!input.exists) {
			error("Input "+input+" does not exist.")
		}

		if(matryoshka.isDefined && !matryoshka.get.exists) {
			error("Custom Matryoshka is defined but does not exist.")
		}

		new ReducerConfigurationImpl(input, output, quality, deblock,
			merge, sort, lzma, matryoshkaType, matryoshka, mergeCF)
	}
}
