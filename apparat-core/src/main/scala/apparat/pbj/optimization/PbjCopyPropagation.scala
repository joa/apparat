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

import apparat.pbj.pbjdata._
import annotation.tailrec

/**
 * @author Joa Ebert
 */
object PbjCopyPropagation extends (List[POp] => (List[POp], Boolean)) {
	override def apply(code: List[POp]) = {
		var l = List.empty[PDstAndSrc]
		var r = List.empty[POp]
		var m = false

		@inline def p(op: PDstAndSrc, a: List[PDstAndSrc]) = op match {
			case PCopy(dst, src) =>
				a.find { _ defines src.index } match {
					case Some(x) if x.dst.swizzle == dst.swizzle =>
						m = true
						x.mapDef(dst.index)
					case _ => op
				}
			case _ => op
		}

		@tailrec def loop(list: List[POp]): Unit = list match {
			case Nil =>
			case x :: xs => {
				x match {
					case op: PDstAndSrc =>
						r = p(op, l) :: r
						l = if(1 == (xs count { _ uses op.dst.index })) {
							op :: l.filterNot { _ uses op.dst.index }
						} else {
							l.filterNot { _ uses op.dst.index }
						}
					case other => r = other :: r
				}

				loop(xs)
			}
		}

		loop(code)
		if(m) { r.reverse -> true} else { code -> false }
	}
}