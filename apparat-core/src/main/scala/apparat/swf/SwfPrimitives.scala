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
package apparat.swf

object SwfPrimitives {
	implicit def tuple2Recordheader(a: (Int, Int)) = new Recordheader(a._1, a._2)
	implicit def tuple2RGB(a: (Int, Int, Int)) = new RGB(a._1, a._2, a._3)
	implicit def tuple2Rect(a: (Int, Int, Int, Int)) = new Rect(a._1, a._2, a._3, a._4)
}

//can make use of case classes, but i do not see a reason for that yet.
class Recordheader(val kind: Int, val length: Int) extends Tuple2[Int, Int](kind, length)
class RGB(val red: Int, val green: Int, val blue: Int) extends Tuple3[Int, Int, Int](red, green, blue)
class Rect(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int) extends Tuple4[Int, Int, Int, Int](minX, maxX, minY, maxY)
