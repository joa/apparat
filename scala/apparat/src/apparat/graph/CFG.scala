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

class CFG extends GraphLikeWithAdjacencyMatrix[BasicBlockVertex] with DOTExportAvailable[BasicBlockVertex] {
	override def add(edge: E) = {
		assert(edge.kind != EdgeKind.Default)
		super.add(edge)
	}

	override def toString = "[CFG]"

	override def dotExport = {
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
		new DOTExport(this, (_: BasicBlockVertex).toString, edgeToString)
	}
}