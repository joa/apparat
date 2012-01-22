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
package apparat.log

import reflect.BeanProperty

/**
 * @author Joa Ebert
 */
object Log {
	@BeanProperty
	var level: LogLevel = Info

	private var _outputs = List.empty[LogOutput]

	def outputs = _outputs

	def newLogger: Logger = new LoggerImpl(level, _outputs)
	def addOutput(output: LogOutput) = _outputs = output :: _outputs
	def removeOutput(output: LogOutput) = _outputs = _outputs filterNot { _ == output }
}
