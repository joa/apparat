package apparat.graph

import apparat.bytecode.operations._
import apparat.bytecode.Bytecode
import collection.Seq
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
 * Date: 9 janv. 2010
 * Time: 16:08:01
 */

class AbstractOpBasicBlockVertex(block: Seq[AbstractOp] = Nil) extends BasicBlockVertex[AbstractOp](block)

class BytecodeCFG extends CFG[AbstractOp, AbstractOpBasicBlockVertex] {
	type BasicBlockVertex = AbstractOpBasicBlockVertex

	protected[graph] def newEntryVertex = new BasicBlockVertex() with EntryVertex

	protected[graph] def newExitVertex = new BasicBlockVertex() with ExitVertex

	def fromBytecode(bytecode: Bytecode) = BytecodeCFGBuilder(_)

	override def toString = "[BytecodeCFG]"

	override def vertexToString(vertex: BasicBlockVertex) = {
		val str = vertex.toString
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

		loop(new StringBuilder("{"), 0).append("}") toString
	}

	def removeLabelAndJump() = {
		for (vertex <- verticesIterator if (!(isEntry(vertex) || isExit(vertex)))) {
			if (vertex(0).isInstanceOf[Label])
				vertex removeFirst ()
			if (vertex(vertex.length - 1).isInstanceOf[Jump])
				vertex removeLast ()
		}
		this
	}

	override def dotExport = {
		new DOTExport(this, vertexToString, edgeToString)
	}
}
