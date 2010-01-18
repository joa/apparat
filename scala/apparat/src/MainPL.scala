import apparat.abc.{Abc}
import apparat.graph._
import apparat.swc.Swc
import apparat.swf.{DoABC, SwfTags, Swf}
import apparat.test.{BCode}
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
		val swf = Swf fromSwc (Swc fromFile "assets/playerglobal.swc")

		val idx = swf.tags.findIndexOf(_.kind == SwfTags.DoABC)
		val abc = Abc fromDoABC swf.tags(idx).asInstanceOf[DoABC]
		abc.loadBytecode()

		for {
			body<-abc.methods(3).body
			bc<-body.bytecode
		}{
			bc.dump()
			val g = BytecodeCFGBuilder(bc)
			g.dotExport to Console.out			
		}
	}
}