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
package apparat.bytecode.optimization

import apparat.bytecode.Bytecode
import apparat.graph.mutable.{MutableAbstractOpBlockVertex, MutableBytecodeControlFlowGraph, MutableBytecodeControlFlowGraphBuilder}
import scala.math.min
import apparat.graph.EdgeKind

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
			val edges = cfg.incomingOf(vertex)
			val predecessors = edges.map(p => p.startVertex).toList

			if (predecessors.length==2 && !edges.exists(_.kind match {case EdgeKind.Jump => false; case _ => true})) {
				// for the moment only merge block that comes from a jump
				val ops = predecessors map { _.block.toArray }

				val b0 = ops(0)
				val b1 = ops(1)

				var n = b0.length - 1
				var m = b1.length - 1
				var i = 0

				while((n > -1) && (m > -1)) {
					if(b0(n) ~== b1(m)) {
						i += 1
					} else {
						n = 0//a kingdom for a break!
						m = 0
					}
					n -= 1
					m -= 1
				}

				if(i != 0) {
					vertex.block = (b0 takeRight i).toList ::: vertex.block
					for(predecessor <- predecessors) {
						predecessor.block = predecessor.block dropRight i
					}

					modified = true
				}

				for(predecessor <- predecessors) {
					loop(predecessor)
				}
			}
		}

		for(vertex <- cfg predecessorsOf cfg.exitVertex) {
			loop(vertex)
		}

		(modified, cfg.bytecode)
	}
}
