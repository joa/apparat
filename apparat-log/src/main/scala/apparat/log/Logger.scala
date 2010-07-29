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

import java.io.{Writer => JWriter}

/**
 * @author Joa Ebert
 */
trait Logger {
	//allows us to write: log << "message"
	def <<(message: String): Unit = info(message)

	//allows us to write: log << Warning << "message"
	def <<(filter: LogLevel): Logger = new Logger {
		override def <<(message: String): Unit = filter match {
			case Debug => if(debugEnabled) debug(message)
			case Info => if(infoEnabled) info(message)
			case Warning => if(warningEnabled) warning(message)
			case Error => if(errorEnabled) error(message)
			case Fatal => if(fatalEnabled) fatal(message)
			case Off =>
		}
		
		override def debug(format: String, arguments: Any*):Unit = Logger.this.debug(format, arguments: _*)
		override def info(format: String, arguments: Any*): Unit = Logger.this.info(format, arguments: _*)
		override def warning(format: String, arguments: Any*): Unit = Logger.this.warning(format, arguments: _*)
		override def error(format: String, arguments: Any*): Unit = Logger.this.error(format, arguments: _*)
		override def fatal(format: String, arguments: Any*): Unit = Logger.this.fatal(format, arguments: _*)
		override def log(level: LogLevel, message: String): Unit = Logger.this.log(level, message)
		override def debugEnabled: Boolean = Logger.this.debugEnabled
		override def infoEnabled: Boolean = Logger.this.infoEnabled
		override def warningEnabled: Boolean = Logger.this.warningEnabled
		override def errorEnabled: Boolean = Logger.this.errorEnabled
		override def fatalEnabled: Boolean = Logger.this.fatalEnabled
		override def asWriterFor(level: LogLevel): JWriter = Logger.this.asWriterFor(level)
	}

	def debug(format: String, arguments: Any*): Unit
	def info(format: String, arguments: Any*): Unit
	def warning(format: String, arguments: Any*): Unit
	def error(format: String, arguments: Any*): Unit
	def fatal(format: String, arguments: Any*): Unit
	def log(level: LogLevel, message: String): Unit

	def debugEnabled: Boolean
	def infoEnabled: Boolean
	def warningEnabled: Boolean
	def errorEnabled: Boolean
	def fatalEnabled: Boolean

	def ifDebug(message: => String) = if(debugEnabled) { debug(message) }
	def ifInfo(message: => String) = if(infoEnabled) { info(message) }
	def ifWarning(message: => String) = if(warningEnabled) { warning(message) }
	def ifError(message: => String) = if(errorEnabled) { error(message) }
	def ifFatal(message: => String) = if(fatalEnabled) { fatal(message) }

	def asWriterFor(level: LogLevel): JWriter
}