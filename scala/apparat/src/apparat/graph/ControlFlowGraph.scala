package apparat.graph

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
 * Time: 14:15:41
 */

class ControlFlowGraph[T, V <: BlockVertex[T]](val graph: GraphLike[V], val entryVertex: V, val exitVertex: V) extends GraphLike[V] with ControlFlow[V] with DOTExportAvailable[V] {
	override type G = ControlFlowGraph[T, V]

	type ControlFlowVertex = V
	type ControlFlowEdge = E
	type ControlFlowElm = T

	override def topsort = graph.topsort

	override def sccs = graph.sccs

	override def dominance = graph.dominance

	override def predecessorsOf(vertex: V) = graph.predecessorsOf(vertex)

	override def successorsOf(vertex: V) = graph.successorsOf(vertex)

	override def incomingOf(vertex: V) = graph.incomingOf(vertex)

	override def verticesIterator = graph.verticesIterator

	override def edgesIterator = graph.edgesIterator

	override def indegreeOf(vertex: V) = graph.indegreeOf(vertex)

	override def outdegreeOf(vertex: V) = graph.outdegreeOf(vertex)

	override def contains(edge: E) = graph.contains(edge)

	override def outgoingOf(vertex: V) = graph.outgoingOf(vertex)

	override def contains(vertex: V) = graph.contains(vertex)

	override def +(edge: E) = new G(graph + edge, entryVertex, exitVertex)

	override def -(edge: E) = new G(graph - edge, entryVertex, exitVertex)

	override def toString = "[ControlFlowGraph]"

	// edge like A->0->B became A->B
	lazy val withNoEmptyJump = {
		var g = graph
		for (edge <- edgesIterator.filter(v => v.endVertex.isEmpty && !isExit(v.endVertex))) {
			val startEdge = edge
			var endEdge = edge
			while ((outdegreeOf(endEdge.endVertex) == 1) && {
				endEdge = outgoingOf(endEdge.endVertex).head
				if (endEdge.endVertex.isEmpty) {
					g = g - endEdge
					true
				} else
					false
			}) {}
			if (startEdge != endEdge) {
				g = (g - edge) + JumpEdge(startEdge.startVertex, endEdge.endVertex)
			}
		}
		if (g != graph)
			new G(g, entryVertex, exitVertex)
		else
			this
	}

	def cleanString(str: String) = {
		val len = str.length
		@tailrec def loop(sb: StringBuilder, strIndex: Int): StringBuilder = {
			if (strIndex >= len)
				sb
			else {
				str(strIndex) match {
					case '"' => sb append "\\\""
					case '>' => sb append "&gt;"
					case '<' => sb append "&lt;"
					case c => sb append c
				}
				loop(sb, strIndex + 1)
			}
		}
		loop(new StringBuilder(), 0) toString
	}

	def label(value: String) = "label=\"" + cleanString(value) + "\""

	def vertexToString(vertex: V) = "[" + label({
		if (isEntry(vertex))
			"Entry"
		else if (isExit(vertex))
			"Exit"
		else
			vertex toString
	}) + "]"

	def edgeToString(edge: E) = "[" + label(edge match {
		case DefaultEdge(x, y) => ""
		case JumpEdge(x, y) => "jump"
		case TrueEdge(x, y) => "true"
		case FalseEdge(x, y) => "false"
		case DefaultCaseEdge(x, y) => "default"
		case CaseEdge(x, y) => "case"
		case NumberedCaseEdge(x, y, n) => "case " + n
		case ThrowEdge(x, y) => "throw"
		case ReturnEdge(x, y) => "return"
	}) + "]"

	override def dotExport = {
		new DOTExport(this, (vertex: V) => vertexToString(vertex), (edge: E) => edgeToString(edge))
	}
}