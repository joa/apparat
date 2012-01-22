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
package apparat.utils

import java.io.{Writer => JWriter, PrintWriter => JPrintWriter}
import apparat.log.{LogLevel, Debug, Logger}

trait Dumpable {
	def dump(): Unit = dump(new JPrintWriter(Console.out))
	def dump(writer: JPrintWriter): Unit = {
		val indentingPrintWriter = new IndentingPrintWriter(writer)
		dump(indentingPrintWriter)
		indentingPrintWriter.flush()
	}

	def dump(log: Logger, level: LogLevel = Debug): Unit = {
		val indentingPrintWriter = new IndentingPrintWriter(log asWriterFor level)

		dump(indentingPrintWriter)

		indentingPrintWriter.flush()
		indentingPrintWriter.close()
	}

	def dump(writer: IndentingPrintWriter): Unit
}
