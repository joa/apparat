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
package apparat.abc

object AbcOutputUtil {
	def lengthOf(value: Int) = value match {
		case x if x < 0 => 5
		case x if x > 268435455 => 5
		case x if x > 2097151 => 4
		case x if x > 16383 => 3
		case x if x > 127 => 2
		case _ => 1
	}

	def writeS24(output: Array[Byte], offset: Int, value: Int) = {
		output(offset) = (value & 0xff).asInstanceOf[Byte]
		output(offset+1) = ((value & 0xff00) >> 0x08).asInstanceOf[Byte]
		output(offset+2) = ((value & 0xff0000) >> 0x10).asInstanceOf[Byte]
	}
}
