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
import apparat.bytecode.optimization.PeepholeOptimizations
import apparat.graph.immutable.{BytecodeControlFlowGraphBuilder, Graph}
import apparat.graph.{DefaultEdge, Vertex}
import apparat.swc.Swc
import apparat.swc.Swcs._
import apparat.swf.Swfs._
import apparat.swf.{Swfs, DoABC, SwfTags, Swf}
import apparat.taas.ast.{TaasAST, TaasTarget}
import apparat.taas.frontend.abc.AbcFrontend
import apparat.utils.Performance
import apparat.utils.Performance._
import collection.mutable.{ListBuffer}
import java.io.PrintWriter
import apparat.bytecode.operations._
import apparat.bytecode.combinator.BytecodeChains._
import apparat.bytecode.combinator._
import apparat.bytecode.Bytecode._
object Main {
	def main(args: Array[String]): Unit = {
		implicit val factory = DefaultEdge[String](_, _)
		val G = Graph(
			"Entry" -> "A",
			"A" -> "B",
			"B" -> "C",
			"B" -> "D",
			"D" -> "F",
			"F" -> "E",
			"E" -> "F",
			"E" -> "C",
			"C" -> "A",
			"C" -> "Exit")

		G.dotExport to Console.out

		G.sccs foreach println
		G.sccs map { _.entry } foreach println
		G.sccs filter { _.canSearch } map { _.subcomponents } foreach { _ foreach println }

		val swf = Swf fromFile "assets/Test04.swf"
		val swc = Swf fromSwc (Swc fromFile "assets/playerglobal.swc")

		val frontend = new AbcFrontend(
			Abc fromSwf swf get, (swc.tags partialMap {
				case doABC: DoABC => Abc fromByteArray doABC.abcData
			}) ::: (Abc fromFile "assets/playerglobal.abc") ::
					(Abc fromFile "assets/builtin.abc") ::
			 		(Abc fromFile "assets/toplevel.abc") :: Nil)

		//measure { frontend.getAST.units(0).dump() }
		measure { frontend.getAST.dump() }
		/*implicit val factory = DefaultEdge[Vertex](_, _)
		val g = Graph(Vertex("Entry") -> Vertex("E"), Vertex("Entry") -> Vertex("A"), Vertex("E") -> Vertex("B"), Vertex("A") -> Vertex("B"), Vertex("B") -> Vertex("C"), Vertex("B") -> Vertex("D"), Vertex("D") -> Vertex("Exit"), Vertex("C") -> Vertex("Exit"))

		for(v <- g.verticesIterator) {
			println(v + " -> " + g.dominance.frontiersOf(v))
		}*/

		/*val b = bytecode {
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
		println(LocalCount(b))*/

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

		/*swc {
			swf(width = 400, height = 400) {
				Nil
			}
		} write "assets/new.swc"

		measure {
			val swf = Swf fromFile "assets/Test15.swf"
			swf.tags foreach println
			for(x <- swf.tags if x.kind == SwfTags.DoABC) {
				val doABC = x.asInstanceOf[DoABC]
				val abc = Abc fromDoABC doABC

				Swfs.swf(640, 480) {
					Swfs.doABC("Test15") {
						abc
					}
				} write "assets/Test15.synthesized.swf"

				abc.loadBytecode()

				abc.dump()

				/*abc.methods foreach {
					method => method.body match {
						case Some(body) => body.bytecode match {
							case Some(bytecode) => {
								bytecode.dump()
								val g = BytecodeControlFlowGraphBuilder(bytecode)
								g.dotExport to Console.out
							}
							case None =>
						}
						case None =>
					}
				}*/

				abc.cpool = AbcConstantPoolBuilder using abc
				//abc.cpool.dump()
				abc.saveBytecode()
				abc write doABC
				//write doABC
			}

			swf write "assets/Test15.output.swf"
		}*/


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
