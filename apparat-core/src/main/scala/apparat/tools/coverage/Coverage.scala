package apparat.tools.coverage

import apparat.utils.TagContainer
import apparat.actors.Futures._
import java.io.{File => JFile}
import apparat.tools.{ApparatConfiguration, ApparatTool, ApparatApplication}
import apparat.bytecode.operations._
import apparat.bytecode.combinator._
import apparat.bytecode.combinator.BytecodeChains._
import java.io.{File => JFile}
import apparat.abc.{AbcConstantPool, AbcQName, AbcNamespace, Abc}
import compat.Platform
import apparat.swf.{SwfTag, SwfTags, DoABC}

object Coverage {
	def main(args: Array[String]): Unit = ApparatApplication(new CoverageTool, args)

	class CoverageTool extends ApparatTool {
		val debugLine = partial { case DebugLine(line) => line }
		val coverageQName = AbcQName('Coverage, AbcNamespace(22, Symbol("apparat.coverage")))
		val coverageOnSample = AbcQName('onSample, AbcNamespace(22, Symbol("")))
		val coverageScope = GetLex(coverageQName)
		val coverageMethod = CallPropVoid(coverageOnSample, 2)
		
		var input: JFile = _
		var output: JFile = _
		var sourcePath = List.empty[String]

		var observers = List.empty[CoverageObserver]

		override def name = "Coverage"

		override def help = """  -i [file]	Input file
  -o [file]	Output file (optional)
  -s [dir]	Source path to instrument"""

		override def configure(config: ApparatConfiguration): Unit = configure(CoverageConfigurationFactory fromConfiguration config)

		def configure(config: CoverageConfiguration): Unit = {
			input = config.input
			output = config.output
			sourcePath = config.sourcePath
		}
		
		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case SwfTags.DoABC1 => Some(new DoABC)
				case _ => None
			}

			val cont = TagContainer fromFile input
			cont foreachTag coverage
			cont write output
		}

		def addObserver(observer: CoverageObserver) = {
			observers = observer :: observers
		}

		def removeObserver(observer: CoverageObserver) = {
			observers = observers filterNot (_ == observer)
		}

		private def coverage: PartialFunction[SwfTag, Unit] = {
			case doABC: DoABC => {
				var abcModified = false
				val abc = Abc fromDoABC doABC

				abc.loadBytecode()

				for {
					method <- abc.methods
					body <- method.body
					bytecode <- body.bytecode
				} {
					bytecode.ops find (_.opCode == Op.debugfile) match {
						case Some(op) => {
							val debugFile = op.asInstanceOf[DebugFile]
							val file = debugFile.file
							if(sourcePath.isEmpty || (sourcePath exists (file.name startsWith _))) {
								abcModified = true

								bytecode.replaceFrom(4, debugLine) {
									x =>
										observers foreach (_.instrument(file.name, x))

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

				if(abcModified) {
					abc.cpool = (abc.cpool add coverageQName) add coverageOnSample
					abc.saveBytecode()
					abc write doABC
				}
			}
		}

		private def pushLine(line: Int) = line match {
			case x if x < 0x80 => PushByte(x)
			case x if x < 0x8000 => PushShort(x)
			case x => error("Too many lines.")
		}
	}
}