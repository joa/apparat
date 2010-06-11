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
package apparat.bytecode.optimization

import apparat.bytecode.Bytecode
import apparat.graph.mutable.{MutableAbstractOpBlockVertex, MutableBytecodeControlFlowGraph, MutableBytecodeControlFlowGraphBuilder}

/**
 * @author Joa Ebert
 */
object BlockMerge {
	def apply(bytecode: Bytecode): (Boolean, Bytecode) = apply(MutableBytecodeControlFlowGraphBuilder(bytecode))

	def apply(cfg: MutableBytecodeControlFlowGraph): (Boolean, Bytecode) = {
		var visited = cfg vertexMap { vertex => false }
		var modified = false

		@inline def loop(vertex: MutableAbstractOpBlockVertex): Unit = if(!visited(vertex)) {
			visited = visited updated (vertex, true)
			
			val predecessors = (cfg predecessorsOf vertex).toList
			val ops = predecessors map { _.block.toArray }
			val numPredecessors = predecessors.length

			if(numPredecessors == 2) {
				val b0 = ops(0)
				val b1 = ops(1)

				var n = b0.length - 1
				var i = 0

				while(n > -1) {
					if(b0(n) ~== b1(n)) {
						i += 1
					} else {
						n = 0//a kingdom for a break!
					}

					n -= 1
				}

				if(i != 0) {
					vertex.block = (b0 takeRight i).toList ::: vertex.block

					for(predecessor <- predecessors) {
						predecessor.block = predecessor.block dropRight i
					}

					modified = true
				}
			}

			for(predecessor <- predecessors) {
				loop(predecessor)
			}
		}

		for(vertex <- cfg predecessorsOf cfg.exitVertex) {
			loop(vertex)
		}

		(modified, cfg.bytecode)
	}
}