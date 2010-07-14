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
 * Time: 21:34:43
 */
package apparat.graph

import apparat.bytecode.operations._
import annotation.tailrec
import collection.mutable.ListBuffer
import apparat.bytecode.{BytecodeExceptionHandler, MarkerManager, Marker, Bytecode}
import immutable.BytecodeControlFlowGraphBuilder

class BytecodeControlFlowGraph[V <: BlockVertex[AbstractOp]](graph: GraphLike[V], entryVertex: V, exitVertex: V) extends ControlFlowGraph[AbstractOp, V](graph, entryVertex, exitVertex) {
	def this(graph: ControlFlowGraph[AbstractOp, V]) = this (graph, graph.entryVertex, graph.exitVertex)

	override def toString = "[BytecodeControlFlowGraph]"

	override def optimized = simplified

	private lazy val simplified = {
		var g = super.optimized
		var g2 = g

		def reducible(edge: E) = (edge.startVertex != entryVertex) &&
				(edge.endVertex != exitVertex) &&
				(g.outdegreeOf(edge.startVertex) == 1) &&
				(g.indegreeOf(edge.endVertex) == 1) &&
				(!g.outgoingOf(edge.endVertex).exists(_.isInstanceOf[ThrowEdge[_]]))

		// coalesce successive block A jump B jump C into ABC
		@tailrec def coalesce() {
			g.edgesIterator.find(reducible(_)) match {
				case Some(branch) => {
					val newVertex = (branch.startVertex ++ branch.endVertex.block).asInstanceOf[V]
					val edges = g2.outgoingOf(branch.endVertex)
					g2 = g2 - branch
					g2 = g2.replace(branch.startVertex, newVertex)
					for (edge <- edges) {
						g2 = g2 + Edge.copy(edge, Some(newVertex))
					}
					g2 = g2 - branch.endVertex
				}
				case _ =>
			}
			if (g != g2) {
				g = g2
				coalesce()
			}
		}

		coalesce()

		new BytecodeControlFlowGraph(g, entryVertex, exitVertex)
	}

	def fromBytecode(bytecode: Bytecode) = BytecodeControlFlowGraphBuilder(_)

	lazy val bytecode = ControlFlowGraphBytecodeBuilder(optimized)

	override def edgeToString(edge: E) = {
		def headLabel(vertex: V) = " headlabel=\"" + ({
			if (vertex.length == 0)
				""
			else {
				vertex.last match {
					case op: OpWithMarker => op.marker.toString
					case _ => ""
				}
			}
		}) + "\""

		"[" + (edge match {
			case DefaultEdge(x, y) => label("")
			case JumpEdge(x, y) => label("  jump  ")
			case TrueEdge(x, y) => label("  true  ")
			case FalseEdge(x, y) => label("  false  ")
			case DefaultCaseEdge(x, y) => label("  default  ")
			case CaseEdge(x, y) => label("  case  ")
			case NumberedCaseEdge(x, y, n) => label("  case " + n)
			case ThrowEdge(x, y) => label("  throw  ") + " style=\"dashed\""
			case ReturnEdge(x, y) => label("  return  ")
		}) + "]"
	}
}

