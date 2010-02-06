import apparat.abc.{Abc}
import apparat.bytecode.{Bytecode}
import apparat.graph.immutable.BytecodeControlFlowGraphBuilder
import apparat.graph.{Edge}
import apparat.swc.Swc
import apparat.swf.{DoABC, SwfTags, Swf}
import collection.mutable.{ListBuffer}
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
* Date: 19 déc. 2009
* Time: 02:38:51
*/

object MainPL {
	def main(args: Array[String]): Unit = {
//				val swf = Swf fromSwc (Swc fromFile "assets/playerglobal.swc")

		//		swf.tags foreach println
		//		for(x <- swf.tags if x.kind == SwfTags.DoABC) {
		//			val doABC = x.asInstanceOf[DoABC]
		//			val abc = Abc fromDoABC doABC
		//
		////			Swfs.swf(640, 480) {
		////				Swfs.doABC("Test15") {
		////					abc
		////				}
		////			} write "assets/Test15.synthesized.swf"
		//
		//			abc.loadBytecode()
		//
		//			abc.dump()
		//		}

		val swf = Swf fromFile "assets/_Test1.swf"

		val idx = swf.tags.findIndexOf(_.kind == SwfTags.DoABC)
		val abc = Abc fromDoABC swf.tags(idx).asInstanceOf[DoABC]
		abc.loadBytecode()
		for{
			body <- abc.methods(2).body
			bc <- body.bytecode
		} {
			bc.dump()
			val g = BytecodeControlFlowGraphBuilder(bc)
			g.dotExport to Console.out

			val nbc = g.bytecode
			nbc.dump()
			val ng = BytecodeControlFlowGraphBuilder(nbc)
			ng.dotExport to Console.out
	}
}
}