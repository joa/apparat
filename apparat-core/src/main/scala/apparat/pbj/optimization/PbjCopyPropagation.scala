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
	override def apply(code: List[POp]): (List[POp], Boolean) = {
		if((code exists { _.opCode == POp.If }) && code.last.opCode != POp.If) {
			@tailrec def split(result: List[List[POp]], in: List[POp]): List[List[POp]] = {
				val (s, r) = in splitAt {
					(in indexWhere { op => op.opCode == POp.If || op.opCode == POp.Else || op.opCode == POp.Endif }) + 1}
				if(s != Nil) split(s :: result, r) else (r :: result).reverse
			}

			val mapped = split(List.empty, code) map bb
			(mapped flatMap { _._1 }) -> (mapped exists { _._2 })
		} else bb(code) 
	}

	private def bb(code: List[POp]): (List[POp], Boolean) = {
		var l = List.empty[PDstAndSrc]
		var r = List.empty[POp]
		var m = false

		@inline def p(op: POp, a: List[PDstAndSrc]) = op match {
			case PCopy(dst, src) =>
				a find { _ defines src.code } match {
					case Some(x) if x.dst.swizzle == dst.swizzle => m = true
						x mapDef dst.code
					case _ => op
				}
			case PIf(condition) =>
				a find { _ defines condition.code } match {
					case Some(PCopy(dst, src)) if dst.swizzle == condition.swizzle &&
							condition.swizzle == src.swizzle =>
						m = true
						PIf(src)
					case _ => op
				}
			case _ => op
		}

		@inline def update(op: PDstAndSrc, code: Int, xs: List[POp]) = {
			l = if(1 == (xs count { _ uses code })) {
				op :: (l filterNot { _ uses code })
			} else { l filterNot { _ uses code } }
		}

		@tailrec def loop(list: List[POp]): Unit = list match {
			case Nil =>
			case x :: xs =>
				x match {
					case op: PLogical =>
						r = p(op, l) :: r
						update(op, 0x8000, xs)
					case op: PDstAndSrc =>
						r = p(op, l) :: r
						update(op, op.dst.code, xs)
					case op: PIf => r = p(op, l) :: r
					case other => r = other :: r
				}

				loop(xs)
		}

		loop(code)

		if(m) { r.reverse -> true} else { code -> false }
	}
}