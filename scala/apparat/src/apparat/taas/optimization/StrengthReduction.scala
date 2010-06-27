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

import apparat.taas.graph.TaasGraph
import apparat.taas.ast._

/**
 * @author Joa Ebert
 */
object StrengthReduction extends TaasOptimization {
	def name = "StrengthReduction"
	def optimize(context: TaasOptimizationContext) = apply(context.code.graph) match {
		case true => context.copy(modified = true)
		case false => context
	}
	
	def apply(graph: TaasGraph): Boolean = {
		var modified = false

		for(vertex <- graph.verticesIterator) {
			val (m, r) = apply(vertex.block)
			vertex.block = r
			modified |= m
		}

		modified
	}

	def apply(block: List[TExpr]): (Boolean, List[TExpr]) = {
		var result = List.empty[TExpr]
		var modified: Boolean = false

		for(op <- block) {
			var default = false
			op match {
				// x:type = (type)x ->
				case t2 @ T2(TConvert(t), rhs, result) if t == rhs.`type` =>

				// x = y:int + y:int -> x = y << 1
				case T3(TOp_+, lhs: TReg, rhs: TReg, r) if lhs.index == rhs.index && lhs.`type` == TaasIntType => {
					result = T3(TOp_<<, lhs, TInt(1), r) :: result
				}

				// x = y - y -> x = 0
				case t3 @ T3(TOp_-, lhs: TReg, rhs: TReg, r) if lhs.index == rhs.index => {
					lhs.`type` match {
						case TaasIntType => result = T2(TOp_Nothing, TInt(0), r) :: result
						case TaasLongType => result = T2(TOp_Nothing, TLong(0L), r) :: result
						case TaasDoubleType => result = T2(TOp_Nothing, TDouble(0.0), r) :: result
						case _ => { default = true; result = t3 :: result }
					}
				}

				case o => { default = true; result = o :: result }
			}

			if(!default) {
				modified = true
			}
		}

		if(modified) {
			(true, result.reverse)
		} else {
			(false, block)
		}
	}
}