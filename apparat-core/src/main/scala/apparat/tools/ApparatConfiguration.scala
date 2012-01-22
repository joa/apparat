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
package apparat.tools

import scala.collection.mutable.HashMap

object ApparatConfiguration {
	def fromArray(args: Array[String]) = {
		val config = new ApparatConfiguration()
		config parse args
		config
	}
}

class ApparatConfiguration {
	private val options = new HashMap[String, String]

	def apply(key: String) = options.get(key)

	def update(key: String, value: String) = options(key) = value

	def parse(args: Array[String]) = {
		val n = args.length
		val m = n - 1
		var i = 0

		while (i < n) {
			val arg = args(i)
			if (arg startsWith "-") {
				if (i == m) {
					options += arg -> "true"
				} else {
					val value = args(i + 1)
					if (value startsWith "-") {
						options += arg -> "true"
					} else {
						i += 1
						options += arg -> value
					}
				}
			} else {error("Argument error!")}
			i += 1
		}
	}
}
