package apparat.graph

import collection.mutable.Queue
import apparat.bytecode.operations._
import annotation.tailrec
import apparat.bytecode.{Marker, Bytecode}

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
 * User: Patrick Le Clec'h
 * Date: 9 janv. 2010
 * Time: 21:03:59
 */

object BytecodeCFGBuilder {
	def apply(bytecode: Bytecode) = {
		val graph = new BytecodeCFG()

		val ops = bytecode.ops
		val markers = bytecode.markers
		val exceptions = bytecode.exceptions

		val basicBlockQueue = new Queue[graph.BasicBlockVertex]()

		import conversions.BytecodeGraphImplicits._
		AbstractOpBasicBlockSlicer(bytecode).foreach(basicBlockQueue += graph add _)

		// add the first block to the entry
		graph += JumpEdge(graph.entryVertex, basicBlockQueue(0))

		@tailrec def buildEdge() {
			if (basicBlockQueue.nonEmpty) {
				val currentBlock = basicBlockQueue.dequeue()

				def createVertexFromMarker[E <: Edge[graph.BasicBlockVertex]](marker: Marker, edgeFactory: (graph.BasicBlockVertex, graph.BasicBlockVertex) => E) {
					marker.op map {
						graph.find(_) match {
							case Some(block) => graph += edgeFactory(currentBlock, block)
							case _ => error("op not found into graph : " + marker.op.toString)
						}
					}
				}

				// check the kind of the last instruction of block
				currentBlock(currentBlock.length - 1) match {
					case condOp: AbstractConditionalOp => {
						// the next block into the queue is a false edge
						graph += FalseEdge(currentBlock, basicBlockQueue(0))

						// the marker is a TrueEdge
						createVertexFromMarker(condOp.marker, TrueEdge[graph.BasicBlockVertex] _)
					}
					case jumpOp: Jump => createVertexFromMarker(jumpOp.marker, JumpEdge[graph.BasicBlockVertex] _)
					case throwOp: Throw => {
						val indexOfThrow = ops indexOf throwOp

						for (exc <- exceptions) {
							// FIXME do not use get directly
							if (ops.indexOf(exc.from.op.get) <= indexOfThrow && indexOfThrow <= ops.indexOf(exc.to.op.get))
								createVertexFromMarker(exc.target, ThrowEdge[graph.BasicBlockVertex])
						}

						graph += ThrowEdge(currentBlock, graph.exitVertex)
					}
					case lookupOp: LookupSwitch => {
						createVertexFromMarker(lookupOp.defaultCase, DefaultCaseEdge[graph.BasicBlockVertex] _)
						for (caseMarker <- lookupOp.cases)
							createVertexFromMarker(caseMarker, CaseEdge[graph.BasicBlockVertex] _)
					}
					case returnOp: OpThatReturns => {
						graph += ReturnEdge(currentBlock, graph.exitVertex)
					}
					case _ => {
						// by default the next block is a jump edge
						// if it s not the last block of the queue
						if (basicBlockQueue.nonEmpty)
							graph += JumpEdge(currentBlock, basicBlockQueue(0))
					}
				}
				buildEdge()
			}
		}
		buildEdge()
		
		graph
	}
} 