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
package apparat.abc.optimization

import apparat.log.SimpleLog
import apparat.abc.analysis.IdenticalMethodsFinder
import apparat.abc.{AbcMethod, Abc}

/**
 * @author Joa Ebert
 */
object IdenticalMethodSort extends (Abc => Boolean) with SimpleLog {
	override def apply(abc: Abc) = {
		//See Issue 41 or Issue 34 for instance.
		//Since 10.1 this is causing trouble. It is not very efficient anyways.
		if(true) {
			false
		} else {
			val mapping = IdenticalMethodsFinder(abc)
			val values = mapping.valuesIterator.toList.distinct
			val n = values.length

			log.debug("%d identical method(s) found.", n)

			abc.methods = abc.methods sortWith {
				(a, b) => {
					val posA = mapping get a map { values indexOf _ } getOrElse n
					val posB = mapping get b map { values indexOf _ } getOrElse n
					posA < posB
				}
			}

			mapping.nonEmpty
		}
	}
}
