package apparat.graph.immutable

import apparat.bytecode.{Marker, Bytecode}
import apparat.bytecode.operations._
import apparat.graph._
import annotation.tailrec

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
		import collection.mutable.{ListBuffer, Queue}

		type V = ImmutableAbstractOpBlockVertex

		val ops = bytecode.ops
		val markers = bytecode.markers
		val exceptions = bytecode.exceptions

		val blockQueue = new Queue[(V, AbstractOp)]()

		// use to find a target marker
		var vertexMap: Map[V, List[AbstractOp]] = Map.empty

		// use to build the graph
		var edgeMap: Map[V, List[Edge[V]]] = Map.empty

		AbstractOpBasicBlockSlicer(bytecode).foreach {
			opList => {
				var newOpList = opList

				if (newOpList(0).isInstanceOf[Label])
					newOpList = newOpList drop 1

				val lastOp = newOpList.last
				if (lastOp.isInstanceOf[Jump])
					newOpList = newOpList dropRight 1

				val vertex = new V(newOpList)

				vertexMap = vertexMap updated (vertex, opList)

				edgeMap = edgeMap updated (vertex, Nil)

				blockQueue += ((vertex, lastOp))
			}
		}

		val entryVertex = new V() {override def toString = "[Entry]"}
		edgeMap = edgeMap updated (entryVertex, Nil)

		val exitVertex = new V() {override def toString = "[Exit]"}
		edgeMap = edgeMap updated (exitVertex, Nil)

		// connect the first block to the entry
		edgeMap = edgeMap updated (entryVertex, JumpEdge(entryVertex, blockQueue(0)._1) :: edgeMap(entryVertex))

		// find which vertex belong to an Op
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
			if (blockQueue.nonEmpty) {
				val (currentBlock, lastOp) = blockQueue.dequeue()

				def createVertexFromMarker[E <: Edge[V]](marker: Marker, edgeFactory: (V, V) => E) {
					marker.op map {
						findVertex(_) match {
							case Some(block) => edgeMap = edgeMap updated (currentBlock, edgeFactory(currentBlock, block) :: edgeMap(currentBlock))
							case _ => error("op not found into graph : " + marker.op.toString + "=>" + vertexMap)
						}
					}
				}

				// check the kind of the last instruction of block
				lastOp match {
					case condOp: AbstractConditionalOp => {
						// the next block into the queue is a false edge
						edgeMap = edgeMap updated (currentBlock, FalseEdge(currentBlock, blockQueue(0)._1) :: edgeMap(currentBlock))

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

						edgeMap = edgeMap updated (currentBlock, ThrowEdge(currentBlock, exitVertex) :: edgeMap(currentBlock))
					}
					case lookupOp: LookupSwitch => {
						createVertexFromMarker(lookupOp.defaultCase, DefaultCaseEdge[V] _)
						lookupOp.cases.zipWithIndex.foreach({
							case (marker, index)=> {
								def factory(a: V, b: V) = NumberedCaseEdge[V](a, b, index)
								createVertexFromMarker(marker, factory)
							}
						})
					}
					case returnOp: OpThatReturns => {
						edgeMap = edgeMap updated (currentBlock, ReturnEdge(currentBlock, exitVertex) :: edgeMap(currentBlock))
					}
					case _ => {
						// by default the next block is a jump edge
						// if it s not the last block of the queue
						if (blockQueue.nonEmpty)
							edgeMap = edgeMap updated (currentBlock, JumpEdge(currentBlock, blockQueue(0)._1) :: edgeMap(currentBlock))
					}
				}
				buildEdge()
			}
		}
		buildEdge()

		val g = new Graph(edgeMap)
		new BytecodeControlFlowGraph(g, entryVertex, exitVertex)
	}
}