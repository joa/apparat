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
object DeadCodeElimination {
	def apply(graph: TaasGraph) = {
		var modified: Boolean = false

		for(vertex <- graph.verticesIterator) {
			val (m, r) = removeDeadDefs(vertex.block)
			vertex.block = r
			modified = m || modified
		}

		modified
	}

	private def removeDeadDefs(block: List[TExpr]) = {
		var r = List.empty[TDef]
		var h = List.empty[TDef]

		for(op <- block) {
			op match {
				case t2 @ T2(TOp_Nothing, a: TReg, b) if a.index == b.index => r = t2 :: r
				case d: TDef => {
					val p = h partition { h => h.register == d.register && !(d uses h.register) }
					r = p._1 ::: r
					h = d :: (p._2 filterNot { d uses _.register })
				}
				case o => h = h filterNot { o uses _.register }
			}
		}

		if(r.nonEmpty) {
			(true, block filterNot { r contains _ })
		} else {
			(false, block)
		}
	}
}