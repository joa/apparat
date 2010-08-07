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
package apparat.abc.optimization

import apparat.log.SimpleLog
import apparat.abc.analysis.IdenticalMethodsFinder
import apparat.abc.{AbcMethod, Abc}

/**
 * @author Joa Ebert
 */
object IdenticalMethodSort extends (Abc => Boolean) with SimpleLog {
	override def apply(abc: Abc) = {
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