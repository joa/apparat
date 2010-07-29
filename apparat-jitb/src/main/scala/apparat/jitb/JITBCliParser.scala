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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.jitb

import java.io.{File => JFile}
/**
 * @author Joa Ebert
 */
class JITBCliParser(arguments: Array[String]) {
	/**
	 * @throws apparat.jitb.JITBCliParserException
	 */
	lazy val configuration = {
		if (1 != arguments.length) {
			throw JITBCliParserException("Expected exactly one argument.")
		}

		val pathname = arguments(0)
		val input = new JFile(pathname)

		if(!input.exists) {
			throw JITBCliParserException("File "+pathname+" does not exist.")
		}

		if(!input.isFile) {
			throw JITBCliParserException(pathname+" is not a file.")
		}

		if(!input.canRead) {
			throw JITBCliParserException("Cannot read "+input+".")
		}

		new JITBConfiguration {
			override def file = input
		}
	}
}