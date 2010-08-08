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
package apparat.tools.stripper

import apparat.tools.{ApparatConfiguration, ApparatApplication, ApparatTool}
import java.io.{File => JFile}
import apparat.utils.TagContainer
import apparat.actors.Futures._
import apparat.abc._
import apparat.bytecode.operations._
import apparat.bytecode.combinator._
import apparat.bytecode.combinator.BytecodeChains._
import apparat.swf._

/**
 * @author Joa Ebert
 */
object Stripper {
	def main(args: Array[String]): Unit = ApparatApplication(new StripperTool, args)

	class StripperTool extends ApparatTool
	{
		/**
		 * The namespace of the trace() method.
		 */
		private lazy val qname = AbcQName('trace, AbcNamespace(AbcNamespaceKind.Package, Symbol("")))

		/**
		 * Rewrite rule that converts CallProperty into CallPropVoid.
		 *
		 * CallProperty(trace, N) Pop() => CallPropVoid(trace, N)
		 */
		private lazy val traceVoid = {
			partial { case CallProperty(name, args) if name == qname => args } ~ Pop() ^^ {
				case args ~ Pop() => CallPropVoid(qname, args) :: Nil
				case _ => error("Internal error.")
			}
		}

		/**
		 * Rewrite rule that will replace "trace(x, y)" with "x, y"
		 * on a bytecode level.
		 *
		 * FindPropStrict(trace) AnyOp* CallPropVoid(trace,N) => AnyOp* repeat(Pop(), N)
		 */
		private lazy val trace = {
			(FindPropStrict(qname) ~
				(filter {
					case CallPropVoid(name, args) if name == qname => false
					case _ => true
				}*) ~
				partial {
					case CallPropVoid(name, args) if name == qname => CallPropVoid(name, args)
				}
			) ^^ {
				case findProp ~ ops ~ callProp if ops.length == callProp.numArguments => {
					ops ::: (List.fill(callProp.numArguments) { Pop() })
				}
				case findProp ~ ops ~ callProp => findProp :: ops ::: List(callProp)
				case _ => error("Internal error.")
			}
		}

		var input: JFile = _
		var output: JFile = _
		
		override def name = "Stripper"

		override def help = """  -i [file]	Input file
  -o [file]	Output file (optional)"""

		override def configure(config: ApparatConfiguration): Unit = configure(StripperConfigurationFactory fromConfiguration config)

		def configure(config: StripperConfiguration): Unit = {
			input = config.input
			output = config.output
		}

		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case SwfTags.DoABC1 => Some(new DoABC)
				case _ => None
			}

			val cont = TagContainer fromFile input
			cont foreachTag strip
			cont write output
		}

		private def strip: PartialFunction[SwfTag, Unit] = {
			case doABC: DoABC => {
				val abc = Abc fromDoABC doABC

				abc.loadBytecode()

				for {
					method <- abc.methods
					body <- method.body
					bytecode <- body.bytecode
				} {
					//
					// Strip all debug information.
					//

					bytecode removeAll {
						_.opCode match {
							case Op.debug | Op.debugfile | Op.debugline => true
							case _ => false
						}
					}

					//
					// Strip all traces.
					//

					bytecode rewrite traceVoid
					bytecode rewrite trace
				}

				abc.saveBytecode()
				abc write doABC
			}
		}
	}
}