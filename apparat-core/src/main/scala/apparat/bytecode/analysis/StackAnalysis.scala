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
package apparat.bytecode.analysis

import apparat.bytecode.Bytecode
import apparat.graph.immutable.BytecodeControlFlowGraphBuilder
import apparat.bytecode.operations.AbstractOp
import apparat.graph.{BlockVertex, BytecodeControlFlowGraph}

object StackAnalysis {
	def apply(bytecode: Bytecode): (Int, Int) = apply(BytecodeControlFlowGraphBuilder(bytecode))

	def apply[V <: BlockVertex[AbstractOp]](cfg: BytecodeControlFlowGraph[V]): (Int, Int) = {
		//
		// The stack analysis is quite easy. We have to keep the invariant in mind that
		// the stack is the same at each cf-merge. This means we do not have to compute
		// all (n-1)! possible paths but can simply walk from top->bottom without visiting
		// any vertex twice.
		//
		// The maximum stack that we record is the final result.
		//

		var visited = cfg vertexMap { v => false }
		var maxOperand = 0
		var maxScope = 0

		def loop(vertex: V, initOperand: Int, initScope: Int): Unit = {
			if(!visited(vertex)) {
				var operand = initOperand
				var scope = initScope

				for(op <- vertex.block) {
					operand += op.operandDelta
					scope += op.scopeDelta

					if(operand > maxOperand) {
						maxOperand = operand
					}

					if(scope > maxScope) {
						maxScope = scope
					}
				}

				visited = visited.updated(vertex, true)

				for(successor <- cfg outgoingOf vertex) {
					loop(successor.endVertex, operand, scope)
				}
			}
		}

		loop(cfg.entryVertex, 0, 0)

		(maxOperand, maxScope)
	}
}
