package apparat.tools.coverage

import apparat.utils.TagContainer
import apparat.actors.Futures._
import java.io.{File => JFile}
import apparat.tools.{ApparatConfiguration, ApparatTool, ApparatApplication}
import apparat.swf.{SwfTag, SwfTags, DoABC}
import apparat.bytecode.operations._
import apparat.bytecode.combinator._
import apparat.bytecode.combinator.BytecodeChains._
import java.io.{File => JFile}
import apparat.abc.{AbcConstantPool, AbcQName, AbcNamespace, Abc}
import compat.Platform

object Coverage {
	def main(args: Array[String]): Unit = ApparatApplication(new CoverageTool, args)

	class CoverageTool extends ApparatTool {
		val debugLine = partial { case DebugLine(line) => line }
		val coverageQName = AbcQName('Coverage, AbcNamespace(22, Symbol("apparat.coverage")))
		val coverageOnSample = AbcQName('onSample, AbcNamespace(22, Symbol("")))
		val coverageScope = GetLex(coverageQName)
		val coverageMethod = CallPropVoid(coverageOnSample, 2)
		
		var input = ""
		var output = ""
		var sourcePath = List.empty[String]

		override def name = "Coverage"

		override def help = """  -i [file]	Input file
  -o [file]	Output file (optional)
  -s [dir]	Source path to instrument"""

		override def configure(config: ApparatConfiguration) = {
			input = config("-i") getOrElse error("Input is required.")
			output = config("-o") getOrElse input
			sourcePath = config("-s") map { _ split JFile.pathSeparatorChar toList } getOrElse List.empty[String]
			assert(new JFile(input) exists, "Input has to exist.")
		}
		
		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case _ => None
			}

			val source = new JFile(input)
			val target = new JFile(output)
			val cont = TagContainer fromFile source
			cont.tags = cont.tags map coverage
			cont write target
		}

		private def coverage(tag: SwfTag) = tag match {
			case doABC: DoABC => {
				val f = future {
					var abcModified = false
					val abc = Abc fromDoABC doABC

					abc.loadBytecode()

					for(method <- abc.methods) {
						method.body match {
							case Some(body) => {
								body.bytecode match {
									case Some(bytecode) => {
										bytecode.ops find (_.opCode == Op.debugfile) match {
											case Some(op) => {
												val debugFile = op.asInstanceOf[DebugFile]
												val file = debugFile.file
												if(sourcePath.isEmpty || (sourcePath exists (file.name startsWith _))) {
													abcModified = true
													bytecode.replace(debugLine) {
														x =>
															DebugLine(x) ::
															coverageScope ::
															PushString(file) ::
															pushLine(x) ::
															coverageMethod :: Nil
													}
													body.maxStack += 3
												}
											}
											case None =>
										}
									}
									case None =>
								}
							}
							case None =>
						}
					}

					if(abcModified) {
						abc.cpool = (abc.cpool add coverageQName) add coverageOnSample
						abc.saveBytecode()
						abc write doABC
					}

					doABC
				}
				f()
			}
			case _ => tag
		}

		private def pushLine(line: Int) = line match {
			case x if x < 0x80 => PushByte(x)
			case x if x < 0x8000 => PushShort(x)
			case x => error("Too many lines.")
		}
	}
}