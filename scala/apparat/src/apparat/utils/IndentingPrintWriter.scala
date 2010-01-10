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
package apparat.utils

import java.io.{Writer => JWriter, PrintWriter => JPrintWriter}

class IndentingPrintWriter(val writer: JWriter, useTabs: Boolean = true)
		extends JPrintWriter(writer)
{
	private val hasParent = writer.isInstanceOf[IndentingPrintWriter]
	private val parent = if (hasParent) Some(writer.asInstanceOf[IndentingPrintWriter]) else None
	private val indentChar = if (useTabs) '\t' else ' '
	private val indentShift = if (useTabs) 0 else 2
	private var indentBuffer = new Array[Char](0)
	private var indentLevel = 0

	private def updateIndent() = indentBuffer = Array.fill(indentLevel << indentShift)(indentChar);

	private def indent(body: => Unit): Unit = {
		print(indentBuffer)
		body
	}

	def withIndent(body: => Unit): Unit = {
		pushIndent()
		body
		popIndent()
	}

	def ++(levels: Int) = {
		indentLevel += levels
		updateIndent()
	}

	def --(levels: Int) = {
		indentLevel -= levels
		updateIndent()
	}

	def <==[T](value: Iterator[T]): Unit = println(value)(_.toString)

	def <<<[T](value: Iterator[T]): Unit = withIndent {
		println(value)(_.toString)
	}

	def println[T](value: Iterator[T])(stringOf: T => String): Unit = value foreach (x => println(stringOf(x)))

	def <==[T](value: Iterable[T]): Unit = println(value)(_.toString)

	def <<<[T](value: Iterable[T]): Unit = withIndent {
		println(value)(_.toString)
	}

	def println[T](value: Iterable[T])(stringOf: T => String): Unit = value foreach (x => println(stringOf(x)))

	def <==[T](value: Array[T]): Unit = println(value)(_.toString)

	def <<<[T](value: Array[T]): Unit = withIndent {
		println(value)(_.toString)
	}

	def println[T](value: Array[T])(stringOf: T => String): Unit = value foreach (x => println(stringOf(x)))

	def <=(): Unit = println()

	def <=(value: Boolean): Unit = println(value)

	def <=(value: Char): Unit = println(value)

	def <=(value: Array[Char]): Unit = println(value)

	def <=(value: Double): Unit = println(value)

	def <=(value: Float): Unit = println(value)

	def <=(value: Int): Unit = println(value)

	def <=(value: Long): Unit = println(value)

	def <=(value: Object): Unit = println(value)

	def <=(value: String): Unit = println(value)

	def <<(): Unit = withIndent {println()}

	def <<(value: Boolean): Unit = withIndent {println(value)}

	def <<(value: Char): Unit = withIndent {println(value)}

	def <<(value: Array[Char]): Unit = withIndent {println(value)}

	def <<(value: Double): Unit = withIndent {println(value)}

	def <<(value: Float): Unit = withIndent {println(value)}

	def <<(value: Int): Unit = withIndent {println(value)}

	def <<(value: Long): Unit = withIndent {println(value)}

	def <<(value: Object): Unit = withIndent {println(value)}

	def <<(value: String): Unit = withIndent {println(value)}

	def pushIndent() = {indentLevel += 1; updateIndent()}

	def popIndent() = {indentLevel -= 1; updateIndent()}

	override def println(): Unit = indent {
		parent match {
			case Some(p) => p.println()
			case None => super.println()
		}
	}

	override def println(value: Boolean): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Char): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Array[Char]): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Double): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Float): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Int): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Long): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: Object): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def println(value: String): Unit = indent {
		parent match {
			case Some(p) => p println value
			case None => super.println(value)
		}
	}

	override def flush() = parent match {
		case Some(p) => p.flush()
		case None => super.flush()
	}
}