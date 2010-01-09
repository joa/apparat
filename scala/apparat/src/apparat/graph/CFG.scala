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
package apparat.graph

// FIXME is it ok to manage entry and exit vertex like that ?

trait EntryVertex {
	override def toString() = "Entry"
}
trait ExitVertex {
	override def toString() = "Exit"
}

class CFG[T] extends GraphLikeWithAdjacencyMatrix[BasicBlockVertex[T]] with DOTExportAvailable[BasicBlockVertex[T]] {
	type BlockVertex = BasicBlockVertex[T]

	protected[graph] var _entryVertex: BlockVertex = null

	def entryVertex = _entryVertex

	protected[graph] var _exitVertex: BlockVertex = null

	def exitVertex = _exitVertex

	protected[graph] def newEntryVertex() = new BlockVertex() with EntryVertex

	protected[graph] def newExitVertex() = new BlockVertex() with ExitVertex

	protected[graph] def setEntryVertex() {
		if (entryVertex != null) {
			entryVertex.clear()
			remove(entryVertex)
		}
		_entryVertex = newEntryVertex()
		add(entryVertex)
	}

	protected[graph] def setExitVertex() {
		if (exitVertex != null) {
			exitVertex.clear()
			remove(exitVertex)
		}
		_exitVertex = newExitVertex()
		add(exitVertex)
	}

	setEntryVertex()
	setExitVertex()

	protected[graph] def addBlock(block: Seq[T]): BlockVertex = {
		val bv = new BlockVertex(block)
		add(bv)
		bv
	}

	override def add(edge: E) = {
		assert(edge.kind != EdgeKind.Default)
		super.add(edge)
	}

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

	def vertexToString(vertex: BlockVertex) = vertex.toString()

	override def dotExport = {
		new DOTExport(this, vertexToString, edgeToString)
	}
}