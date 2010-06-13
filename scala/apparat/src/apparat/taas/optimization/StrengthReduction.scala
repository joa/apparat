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
import apparat.taas.ast.{T2, TConvert, TExpr}

/**
 * @author Joa Ebert
 */
object StrengthReduction {
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

		for(op <- block) op match {
			case t2 @ T2(TConvert(t), rhs, result) if t == rhs.`type` => modified = true
			case o => result = o :: result		
		}

		if(modified) {
			(true, result.reverse)
		} else {
			(false, block)
		}
	}
}