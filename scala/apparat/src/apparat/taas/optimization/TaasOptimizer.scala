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
package apparat.taas.optimization

import annotation.tailrec
import apparat.taas.ast.{TaasMethod, TaasCode}

/**
 * @author Joa Ebert
 */
class TaasOptimizer(optimizations: List[TaasOptimization], level: Int) {
	def optimize(method: TaasMethod): Unit = method.code match {
		case Some(code) => optimize(code)
		case None =>
	}

	def optimize(code: TaasCode): Unit = {
		@tailrec def next(optimizations: List[TaasOptimization], context: TaasOptimizationContext): TaasOptimizationContext = optimizations match {
			case x :: xs => next(xs, x optimize context)
			case Nil => context
		}

		@tailrec def loop(context: TaasOptimizationContext): Unit = {
			next(optimizations, context) match {
				case newContext @ TaasOptimizationContext(_, true, _, _) => loop(newContext)
				case _ =>
			}

		}

		loop(TaasOptimizationContext(code, false, level, TaasOptimizationFlags.NONE))
	}
}