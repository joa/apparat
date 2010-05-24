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
package apparat.tools.tdsi

import apparat.tools.{ApparatConfiguration, ApparatApplication, ApparatTool}
import java.io.{File => JFile}
import apparat.utils.TagContainer
import apparat.actors.Futures._
import apparat.abc._
import apparat.bytecode.operations._
import apparat.bytecode.combinator._
import apparat.bytecode.combinator.BytecodeChains._
import apparat.swf._
import apparat.bytecode.optimization.{InlineExpansion, MacroExpansion, PeepholeOptimizations, InlineMemory}

/**
 * @author Joa Ebert
 */
object TurboDieselSportInjection {
	def main(args: Array[String]): Unit = ApparatApplication(new TDSITool, args)

	class TDSITool extends ApparatTool
	{
		var input = ""
		var output = ""
		var alchemy = true
		var macros = true
		var inline = true

		override def name = "Turbo Diesel Sport Injection"

		override def help = """  -i [file]			Input file
  -o [file]			Output file (optional)
  -a (true|false)	Inline Alchemy operations
  -e (true|false)	Inline expansion
  -m (true|false)	Macro expansion"""

		override def configure(config: ApparatConfiguration) = {
			input = config("-i") getOrElse error("Input is required.")
			output = config("-o") getOrElse input
			alchemy = (config("-a") getOrElse "true").toBoolean
			macros = (config("-m") getOrElse "true").toBoolean
			inline = (config("-e") getOrElse "true").toBoolean
			assert(new JFile(input) exists, "Input has to exist.")
		}

		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case SwfTags.DoABC1 => Some(new DoABC)
				case _ => None
			}

			val source = new JFile(input)
			val target = new JFile(output)
			val cont = TagContainer fromFile source
			val allABC = Map((for(doABC <- cont.tags collect { case doABC: DoABC => doABC }) yield (doABC -> (Abc fromDoABC doABC))):_*)
			val macroExpansion = if(macros) Some(new MacroExpansion(allABC.valuesIterator.toList)) else None
			val inlineExpansion = if(inline) Some(new InlineExpansion(allABC.valuesIterator.toList)) else None

			for((doABC, abc) <- allABC.iterator) {
				abc.loadBytecode()

				for(method <- abc.methods) {
					method.body match {
						case Some(body) => {
							body.bytecode match {
								case Some(bytecode) => {
									PeepholeOptimizations(bytecode)

									if(alchemy) {
										InlineMemory(bytecode)
									}

									if(inline) {
										inlineExpansion.get expand bytecode
									}
									
									if(macros) {
										macroExpansion.get expand bytecode
									}
								}
								case None =>
							}
						}
						case None =>
					}
				}

				abc.saveBytecode()
				abc write doABC
			}

			cont.tags = cont.tags map injection
			cont write target
		}

		private def injection(tag: SwfTag) = tag match {
			case doABC: DoABC => {
				val f = future {
					val abc = Abc fromDoABC doABC
					abc.loadBytecode()

					for(method <- abc.methods) method.body match {
						case Some(body) => {
							body.bytecode match {
								case Some(bytecode) => InlineMemory(PeepholeOptimizations(bytecode))
								case None =>
							}
						}
						case None =>
					}

					abc.saveBytecode()
					abc write doABC
					doABC
				}
				f()
			}
			case _ => tag
		}
	}
}