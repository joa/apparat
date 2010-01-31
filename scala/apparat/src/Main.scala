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
import apparat.abc.analysis.AbcConstantPoolBuilder
import apparat.abc.{AbcNamespace, AbcQName, Abc}
import apparat.bytecode.analysis.{LocalCount, FrequencyDistribution}
import apparat.bytecode.PeepholeOptimizations
import apparat.graph.{DefaultEdge, Vertex}
import apparat.graph.immutable.Graph
import apparat.swc.Swc
import apparat.swf.{DoABC, SwfTags, Swf}
import apparat.utils.Performance
import apparat.utils.Performance._
import java.io.PrintWriter
import apparat.bytecode.operations._
import apparat.bytecode.combinator.BytecodeChains._
import apparat.bytecode.combinator._
import apparat.bytecode.Bytecode._
object Main {
	def main(args: Array[String]): Unit = {
		implicit val factory = DefaultEdge[Vertex](_, _)
		val g = Graph(Vertex("A") -> Vertex("B"), Vertex("B") -> Vertex("C"), Vertex("B") -> Vertex("D"))

		(0 until 100) foreach { i => g.topsort foreach println }

		val b = bytecode {
			GetLocal(0)		::
			PushScope()		::
			FindPropStrict(AbcQName('Math, AbcNamespace(22, Symbol("")))) ::
			GetProperty(AbcQName('Math, AbcNamespace(22, Symbol("")))) ::
			GetLocal(1)		::
			DecrementInt()	::
			Dup()			::
			ConvertInt()	::
			SetLocal(1)		::
			ReturnValue()	:: Nil
		}
		b.dump()
		println("")
		PeepholeOptimizations(b)
		b.dump()
		println(LocalCount(b))

		/*val swf = Swf fromSwc (Swc fromFile "assets/playerglobal.swc")
		val frequencyDistribution = new FrequencyDistribution()

		Performance.measure("Total") {
			for (x <- swf.tags if x.kind == SwfTags.DoABC) {
				val doABC = x.asInstanceOf[DoABC];
				val abc = Abc fromDoABC doABC

				abc.loadBytecode()
				abc.methods foreach {
					method => method.body match {
						case Some(body) => {
							frequencyDistribution.analyze(body.bytecode.get)
						}
						case None => {}
					}
				}
			}
		}

		frequencyDistribution.dump()
		println(frequencyDistribution.toCSV)*/
		
		//val swf = Swf fromSwc (Swc fromFile "assets/playerglobal.swc")

		measure {
			val swf = Swf fromFile "assets/Test15.swf"

			for(x <- swf.tags if x.kind == SwfTags.DoABC) {
				val doABC = x.asInstanceOf[DoABC]
				val abc = Abc fromDoABC doABC
				abc.loadBytecode()

				/*abc.methods foreach {
					method => method.body match {
						case Some(body) => body.bytecode.get.dump()
						case None => {}
					}
				}*/

				abc.cpool = AbcConstantPoolBuilder using abc
				//abc.cpool.dump()
				abc.saveBytecode()
				abc write doABC
				//write doABC
			}

			swf write "assets/Test15.output.swf"
		}


		/*val check = Swf fromFile "assets/Test00.output.swf"

		for(tag <- check.tags) {
			(Abc fromTag tag) match {
				case Some(abc) => {}//abc.cpool.dump()
				case None => {}
			}
		}*/

		/*measure {
				val container = TagContainer fromFile "assets/Test15.swf"
				(container.tags) foreach (println _)
				container write "assets/Test15.out.swf"
			 }*/

		/*val swf = new Swf

			measure("Swf.read()") {
			  swf read("assets/sandbox.swf")
			}

			swf.tags foreach (println _)

			measure("Swf.write()") {
			  swf write("assets/sandbox.out.swf")
			}

			measure("Verify") {
			  (Swf fromFile "assets/sandbox.out.swf" tags) foreach (println _)
			}

			measure("playerglobal.swc") {
			  (Swf fromFile "assets/playerglobal.swc" tags) foreach (println _)
			}*/
	}
}
