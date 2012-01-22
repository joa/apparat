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
package apparat.abc.utils

import apparat.abc._

/**
 * @author Joa Ebert
 */
class MethodMapper(map: Map[AbcMethod, AbcMethod]) {
	def transform(abc: Abc): Unit = {
		for(script <- abc.scripts) updateTraits(script)
		for(nominal <- abc.types) {
			updateTraits(nominal.inst)
			updateTraits(nominal.klass)

			nominal.inst.init = map get nominal.inst.init getOrElse nominal.inst.init
			nominal.klass.init = map get nominal.klass.init getOrElse nominal.klass.init
		}
		for {
			method <- abc.methods
			body <- method.body
		} updateTraits(body)
	}

	private def updateTraits(hasTraits: HasTraits): Unit = updateTraits(hasTraits.traits)
	private def updateTraits(array: Array[AbcTrait]): Unit = {
		var i = 0
		var n = array.length

		while(i < n) {
			array(i) = mapTrait(array(i))
			i += 1
		}
	}

	private def mapTrait(value: AbcTrait) = value match {
		case m: AbcTraitMethod if map contains m.method => m.copy(method = map get m.method getOrElse m.method)
		case g: AbcTraitGetter if map contains g.method => g.copy(method = map get g.method getOrElse g.method)
		case s: AbcTraitSetter if map contains s.method => s.copy(method = map get s.method getOrElse s.method)
		case other => other
	}
}
