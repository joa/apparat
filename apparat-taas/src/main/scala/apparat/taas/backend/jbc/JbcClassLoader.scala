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
package apparat.taas.backend.jbc

import java.lang.String

/**
 * @author Joa Ebert
 */
class JbcClassLoader(map: Map[String, Array[Byte]], parent: ClassLoader) extends ClassLoader(parent) {
	override protected def findClass(name: String): Class[_] = {
		(map get name) match {
			case Some(result) => defineClass(name, result, 0, result.length)
			case None => parent.loadClass(name)
		}
	}
}
