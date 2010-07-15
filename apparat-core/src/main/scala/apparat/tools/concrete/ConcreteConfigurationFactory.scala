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
package apparat.tools.concrete

import apparat.tools.{ApparatConfiguration, ApparatConfigurationFactory}
import java.io.{File => JFile}

/**
 * @author Joa Ebert
 */
object ConcreteConfigurationFactory extends ApparatConfigurationFactory[ConcreteConfiguration] {
	override def fromConfiguration(config: ApparatConfiguration): ConcreteConfiguration= {
		val libraries = config("-i") map { _ split JFile.pathSeparatorChar toList } map { pathname => for(p <- pathname) yield new JFile(p) } getOrElse List.empty[JFile]

		for(library <- libraries) {
			if(!library.exists) {
				error("Library "+library+" does not exist.")
			}
		}

		new ConcreteConfigurationImpl(libraries)
	}
}