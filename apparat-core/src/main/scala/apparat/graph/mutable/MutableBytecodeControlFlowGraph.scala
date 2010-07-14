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
 * Time: 16:08:01
 */
package apparat.graph.mutable

import apparat.bytecode.operations._
import annotation.tailrec
import collection.mutable.ListBuffer
import apparat.bytecode.{BytecodeExceptionHandler, Marker, MarkerManager, Bytecode}
import apparat.graph._

class MutableAbstractOpBlockVertex(block: List[AbstractOp] = Nil) extends MutableBlockVertex[AbstractOp](block)

class MutableBytecodeControlFlowGraph extends MutableControlFlowGraph[AbstractOp, MutableAbstractOpBlockVertex] {
	type V = MutableAbstractOpBlockVertex

	protected[graph] def newEntryVertex = new V() with EntryVertex

	protected[graph] def newExitVertex = new V() with ExitVertex

	def fromBytecode(bytecode: Bytecode) = MutableBytecodeControlFlowGraphBuilder(_)

	override def toString = "[MutableBytecodeControlFlowGraph]"

	override def optimized = {simplified(); this}

	private def simplified() {
		super.optimized

		val g = this
		var modified = false

		def reducible(edge: E) = (edge.startVertex != entryVertex) &&
				(edge.endVertex != exitVertex) &&
				(g.outdegreeOf(edge.startVertex) == 1) &&
				(g.indegreeOf(edge.endVertex) == 1) &&
				(!g.outgoingOf(edge.endVertex).exists(_.isInstanceOf[ThrowEdge[_]]))

		// coalesce successive block A jump B jump C into ABC
		@tailrec def coalesce() {
			var verticesToRemove = List.empty[V]

			g.edgesIterator.find(reducible(_)) match {
				case Some(branch) => {
					val newVertex = (branch.startVertex ++ branch.endVertex.block).asInstanceOf[V]
					val edges = g.outgoingOf(branch.endVertex)

					g -= branch

					for (edge <- edges) {
						g += Edge.copy(edge, Some(newVertex))
					}

					verticesToRemove = branch.endVertex :: verticesToRemove

					modified = true
				}
				case _ =>
			}
			if (modified) {
				for (vertex <- verticesToRemove) g -= vertex

				modified = false
				coalesce()
			}
		}

		coalesce()
	}

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
