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
package apparat.log

/**
 * @author Joa Ebert
 */
class LoggerImpl(level: LogLevel, outputs: List[LogOutput]) extends Logger {
	override def debug(format: String, arguments: Any*) = logIf(debugEnabled, Debug, format, arguments: _*)
	override def info(format: String, arguments: Any*) = logIf(infoEnabled, Info, format, arguments: _*)
	override def warning(format: String, arguments: Any*) = logIf(warningEnabled, Warning, format, arguments: _*)
	override def error(format: String, arguments: Any*) = logIf(errorEnabled, Error, format, arguments: _*)
	override def fatal(format: String, arguments: Any*) = logIf(fatalEnabled, Fatal, format, arguments: _*)

	override val debugEnabled = Debug >= level
	override val infoEnabled = Info >= level
	override val warningEnabled = Warning >= level
	override val errorEnabled = Error >= level
	override val fatalEnabled = Fatal >= level

	private def logIf(condition: Boolean, level: LogLevel, format: String, arguments: Any*) = if(condition) {
		val message = format.format(arguments: _*)
		outputs foreach { _.log(level, message) }
	}
}