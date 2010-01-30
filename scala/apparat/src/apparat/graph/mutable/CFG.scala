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
 */
package apparat.graph.mutable

import apparat.graph._

// FIXME is it ok to manage entry and exit vertex like that ?

trait EntryVertex {
	override def toString() = "Entry"
}
trait ExitVertex {
	override def toString() = "Exit"
}

abstract class CFG[T, V <: BasicBlockVertex[T]] extends MutableGraphWithAdjacencyMatrix[V] with DOTExportAvailable[V] {
	type Block = Seq[T]

	protected[graph] def newEntryVertex: V

	protected[graph] def newExitVertex(): V

	val entryVertex = newEntryVertex
	val exitVertex = newExitVertex

	add(entryVertex)
	add(exitVertex)

	protected[graph] def add(block: Block)(implicit b2v: Block => V): V = {
		val blockAsV: V = block
		add(blockAsV)
		blockAsV
	}

	override def add(edge: E) = {
		assert(edge.kind != EdgeKind.Default)
		super.add(edge)
	}

	def isEntry(vertex: V) = entryVertex == vertex

	def isExit(vertex: V) = exitVertex == vertex

	def find(elm: T) = verticesIterator.find(_ contains elm)

	//error: name clash
	//def contains(elm: T) = verticesIterator.exists(_ contains elm)

	override def toString = "[CFG]"

	def edgeToString(edge: E) = edge match {
		case DefaultEdge(x, y) => error("CFG may not contain default edges.")
		case JumpEdge(x, y) => "jump"
		case TrueEdge(x, y) => "true"
		case FalseEdge(x, y) => "false"
		case DefaultCaseEdge(x, y) => "default"
		case CaseEdge(x, y) => "case"
		case ThrowEdge(x, y) => "throw"
		case ReturnEdge(x, y) => "return"
	}

	def vertexToString(vertex: V) = vertex.toString()

	override def dotExport = {
		new DOTExport(this, vertexToString, edgeToString)
	}
}