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
package apparat.pbj.optimization

import apparat.pbj.Pbj
import apparat.pbj.pbjdata._

/**
 * @author Joa Ebert
 */
object PbjDeadCodeElimination extends (Pbj => Boolean) {
	override def apply(pbj: Pbj) = {
		val dead = ((pbj.parameters map { _._1.register.code }) ::: (pbj.code collect {
			case l: PLogical => 0x8000
			case d: PDst => d.dst.code
		})).distinct diff ((pbj.parameters map { _._1.register.code }) ::: (pbj.code flatMap {
			case PCopy(_, src) => src.code :: Nil
			case d: PDstAndSrc => d.dst.code :: d.src.code :: Nil
			case s: PSrc => s.src.code :: Nil
			case _ => Nil
		})).distinct

		pbj.code = pbj.code filterNot {
			case l: PLogical => dead contains 0x8000
			case d: PDst => dead exists { _ == d.dst.code }
			case _ => false
		}

		dead.length > 0
	}
}