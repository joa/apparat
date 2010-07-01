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

import apparat.taas.ast._
import apparat.taas.runtime.{Convert, Eval}
import apparat.taas.graph.{TaasGraphLinearizer, TaasGraph}

/**
 * @author Joa Ebert
 */
object ConstantFolding extends TaasOptimization {
	var i = 0

	def name = "Constant Folding"
	
	def optimize(context: TaasOptimizationContext) = apply(context.code.graph) match {
		case true => context.copy(modified = true)
		case false => context
	}

	def apply(graph: TaasGraph): Boolean = {
		var modified = false

		for(vertex <- graph.verticesIterator) {
			val (m, r) = foldConstantExpressions(vertex.block)
			modified |= m
			vertex.block = r
		}

		modified
	}

	def foldConstantExpressions(block: List[TExpr]): (Boolean, List[TExpr]) = {
		var modified = false
		var r = List.empty[TExpr]

		for(op <- block) op match {
			//x = y op z if y && z are const -> x = evaluate(y op z)
			case t3 @ T3(op, lhs: TConst, rhs: TConst, result) => Eval(op, lhs, rhs) match {
				case Some(evaluated) => {
					modified = true
					r = T2(TOp_Nothing, evaluated, result) :: r
				}
				case None => r = t3 :: r
			}

			//x = (y)z:y -> x = z
			case T2(TConvert(t), rhs: TaasTyped, result) if rhs.`type` == t => {
				modified = true
				r = T2(TOp_Nothing, rhs, result) :: r
			}

			//x = (y)z:w -> x = z:y
			case t2 @ T2(TConvert(t), rhs: TConst, result) => Convert(rhs, t) match {
				case Some(converted) => {
					modified = true
					r = T2(TOp_Nothing, converted, result) :: r
				}
				case None => r = t2 :: r
			}

			case other => r = other :: r
		}

		if(modified) {
			(true, r.reverse)
		} else {
			(false, block)
		}
	}
}