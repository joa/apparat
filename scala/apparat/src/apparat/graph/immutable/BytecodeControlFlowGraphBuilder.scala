package apparat.graph.immutable

import apparat.bytecode.{Marker, Bytecode}
import apparat.bytecode.operations._
import apparat.graph._
import annotation.tailrec
import mutable.MutableBytecodeCFG
import collection.mutable.{ListBuffer, Queue}
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
 * Date: 31 janv. 2010
 * Time: 19:52:30
 */

object BytecodeControlFlowGraphBuilder {
	def apply(bytecode: Bytecode) = {
		type V = ImmutableAbstractOpBlockVertex
		//		val graph = new MutableBytecodeCFG()

		val ops = bytecode.ops
		val markers = bytecode.markers
		val exceptions = bytecode.exceptions

		val basicBlockQueue = new Queue[V]()
		var vertexMap: Map[V, List[AbstractOp]] = Map.empty

		//make Vertex
		AbstractOpBasicBlockSlicer(bytecode).foreach {
			opList => {
				val vertex = new V(opList)
				vertexMap = vertexMap updated (vertex, opList)
				basicBlockQueue += vertex
			}
		}

		// make edges
		var map: Map[V, List[Edge[V]]] = Map.empty

		val entryVertex = new V()
		val exitVertex = new V()

		map = map updated (entryVertex, Nil)
		map = map updated (exitVertex, Nil)

		// add the first block to the entry
		map = map updated (entryVertex, JumpEdge(entryVertex, basicBlockQueue(0)) :: map.getOrElse(entryVertex, Nil))

		// find a vertex belonging to an Op
		def findVertex(op: AbstractOp) = {
			var vertex: Option[V] = None
			def findAndStore(kv: (V, List[AbstractOp])) = {
				val b = kv._2.contains(op)
				if (b) vertex = Some(kv._1)
				!b
			}
			vertexMap.takeWhile(findAndStore)
			vertex
		}

		@tailrec def buildEdge() {
			if (basicBlockQueue.nonEmpty) {
				val currentBlock = basicBlockQueue.dequeue()

				def createVertexFromMarker[E <: Edge[V]](marker: Marker, edgeFactory: (V, V) => E) {
					marker.op map {
						findVertex(_) match {
							case Some(block) => map = map updated (currentBlock, edgeFactory(currentBlock, block) :: map.getOrElse(currentBlock, Nil))
							case _ => {
								error("op not found into graph : " + marker.op.toString + "\n" + vertexMap)
							}
						}
					}
				}

				// check the kind of the last instruction of block
				currentBlock(currentBlock.length - 1) match {
					case condOp: AbstractConditionalOp => {
						// the next block into the queue is a false edge
						map = map updated (currentBlock, FalseEdge(currentBlock, basicBlockQueue(0)) :: map.getOrElse(currentBlock, Nil))

						// the marker is a TrueEdge
						createVertexFromMarker(condOp.marker, TrueEdge[V] _)
					}

					case jumpOp: Jump => createVertexFromMarker(jumpOp.marker, JumpEdge[V] _)

					case throwOp: Throw => {
						val indexOfThrow = ops indexOf throwOp

						for (exc <- exceptions) {
							// FIXME do not use get directly
							if (ops.indexOf(exc.from.op.get) <= indexOfThrow && indexOfThrow <= ops.indexOf(exc.to.op.get))
								createVertexFromMarker(exc.target, ThrowEdge[V])
						}

						map = map updated (currentBlock, ThrowEdge(currentBlock, exitVertex) :: map.getOrElse(currentBlock, Nil))
					}
					case lookupOp: LookupSwitch => {
						createVertexFromMarker(lookupOp.defaultCase, DefaultCaseEdge[V] _)
						for (caseMarker <- lookupOp.cases)
							createVertexFromMarker(caseMarker, CaseEdge[V] _)
					}
					case returnOp: OpThatReturns => {
						map = map updated (currentBlock, ReturnEdge(currentBlock, exitVertex) :: map.getOrElse(currentBlock, Nil))
					}
					case _ => {
						// by default the next block is a jump edge
						// if it s not the last block of the queue
						if (basicBlockQueue.nonEmpty)
							map = map updated (currentBlock, JumpEdge(currentBlock, basicBlockQueue(0)) :: map.getOrElse(currentBlock, Nil))
					}
				}
				buildEdge()
			}
		}
		buildEdge()

		// remove label and jump from basic block
		//todo
		//		graph removeLabelAndJump ()

		val g = new Graph(map)
		new BytecodeControlFlowGraph(g, entryVertex, exitVertex)
	}
}