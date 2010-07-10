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
package apparat.graph.mutable

import apparat.bytecode.operations._
import annotation.tailrec
import apparat.bytecode.{Marker, Bytecode}
import apparat.graph._
import immutable.BytecodeControlFlowGraphBuilder

object MutableBytecodeControlFlowGraphBuilder extends (Bytecode => MutableBytecodeControlFlowGraph) {
	def apply(bytecode: Bytecode) = {
		import collection.mutable.{Queue}

		type V = MutableAbstractOpBlockVertex

		val ops = bytecode.ops
		val markers = bytecode.markers
		val exceptions = bytecode.exceptions

		val blockQueue = new Queue[(V, AbstractOp)]()

		val graph = new MutableBytecodeControlFlowGraph()

		// use to find a target marker
		var vertexMap: Map[V, List[AbstractOp]] = Map.empty

		AbstractOpBasicBlockSlicer(bytecode).foreach {
			opList => {
				var newOpList = opList

				val lastOp = newOpList.last

				// remove label from block
				newOpList.headOption match {
					case Some(op) => if (op.isInstanceOf[Label]) newOpList = newOpList.tail
					case _ =>
				}

				//remove jum from block
				newOpList.lastOption match {
					case Some(op) => if (op.isInstanceOf[Jump]) newOpList = newOpList dropRight 1
					case _ =>
				}

				val vertex = new V(newOpList)

				vertexMap = vertexMap updated (vertex, opList)

				graph += vertex

				blockQueue += ((vertex, lastOp))
			}
		}

		val entryVertex = graph.entryVertex

		val exitVertex = graph.exitVertex

		// connect the first block to the entry
		graph += JumpEdge(entryVertex, blockQueue.head._1)

		def createVertexFromMarker[E <: Edge[V]](startBlock: V, marker: Marker, edgeFactory: (V, V) => E) {
			marker.op map {
				op => vertexMap.view.find(v_op => ((v_op._2 contains op) || (v_op._1 contains op))) match {
					case Some((vertexBlock, ops)) => graph += edgeFactory(startBlock, vertexBlock)
					case _ => {
						println(vertexMap.view.mkString("\n"));
						error("op not found into graph : " + op.toString + "=>" + vertexMap)
					}
				}
			}
		}

		@tailrec def buildEdge() {
			if (blockQueue.nonEmpty) {
				val (currentBlock, lastOp) = blockQueue.dequeue()

				// check the kind of the last instruction of block
				lastOp match {
					case condOp: AbstractConditionalOp => {
						// the next block into the queue is a false edge
						if (blockQueue.nonEmpty)
							graph += FalseEdge(currentBlock, blockQueue.head._1)

						// the marker is a TrueEdge
						createVertexFromMarker(currentBlock, condOp.marker, TrueEdge[V] _)
					}

					case jumpOp: Jump => createVertexFromMarker(currentBlock, jumpOp.marker, JumpEdge[V] _)

					case throwOp: Throw => graph += ThrowEdge(currentBlock, exitVertex)

					case lookupOp: LookupSwitch => {
						createVertexFromMarker(currentBlock, lookupOp.defaultCase, DefaultCaseEdge[V] _)
						lookupOp.cases.zipWithIndex.foreach({
							case (marker, index) => {
								def factory(a: V, b: V) = NumberedCaseEdge[V](a, b, index)
								createVertexFromMarker(currentBlock, marker, factory)
							}
						})
					}
					case returnOp: OpThatReturns => {
						graph += ReturnEdge(currentBlock, exitVertex)
					}
					case _ => {
						// by default the next block is a jump edge
						// if it s not the last block of the queue
						if (blockQueue.nonEmpty)
							graph += JumpEdge(currentBlock, blockQueue.head._1)
					}
				}
				// check if it exists a try catch for this block
				if (!currentBlock.isEmpty) {
					val startOpIndex = ops indexOf (currentBlock.head)
					val endOpIndex = ops indexOf (currentBlock.last)
					exceptions.filter(exc => {
						startOpIndex >= ops.indexOf(exc.from.op.get) &&
								endOpIndex <= ops.indexOf(exc.to.op.get) &&
								ops.view(startOpIndex, endOpIndex).exists(op=>op.canThrow && !op.isInstanceOf[DebugOp])
					}).foreach(exc => createVertexFromMarker(currentBlock, exc.target, ThrowEdge[V]))
				}
				buildEdge()
			}
		}
		buildEdge()

		graph.optimized
	}
	
} 