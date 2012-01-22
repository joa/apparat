/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.tools.dump

import apparat.utils.IO._
import apparat.abc._
import apparat.abc.analysis.AbcUMLExport
import apparat.tools.{ApparatConfiguration, ApparatTool, ApparatApplication}
import java.io.{File => JFile, PrintWriter => JPrintWriter, FileOutputStream => JFileOutputStream}
import apparat.swf.{Swf, DoABC}
import apparat.bytecode.{BytecodeDump, BytecodeDumpTypeCFG, BytecodeDumpTypeDefault}

/**
 * @author Joa Ebert
 */
object Dump {
	def main(args: Array[String]): Unit = ApparatApplication(new DumpTool, args)

	class DumpTool extends ApparatTool {
		var input: JFile = _
		var output: Option[JFile] = None

		var exportSWF = false
		var exportUML = false
		var exportABC = false

		var bytecodeFormat: DumpBytecodeFormat = DumpBytecodeAsDefault

		var sourceName = ""

		override def name = "Dump"

		override def help = """	-i file				Input file (SWC, SWF or ABC)
	-o [dir]			Optional output directory
	-swf 					Basic SWF information
	-uml					UML diagrams in DOT format
	-abc					Detailed ABC information
	-bc [raw|cfg|default] Bytecode format (raw bytes, graphs, default)"""

		override def configure(config: ApparatConfiguration): Unit = configure(DumpConfigurationFactory fromConfiguration config)

		def configure(config: DumpConfiguration) = {
			input = config.input
			output = config.output
			exportSWF = config.exportSWF
			exportUML = config.exportUML
			exportABC = config.exportABC
			bytecodeFormat = config.bytecodeFormat
		}

		override def run() = {
			if(!exportSWF && !exportABC && !exportUML) {
				log.warning("Nothing specified to export. Try the -h option for help.")
			}

			val source = input

			output match {
				case Some(dir) => {
					dir.mkdirs
					assert(dir.isDirectory, "Given output path must point to a directory.")
				}
				case None =>
			}

			sourceName = source.getName

			if(sourceName.toLowerCase endsWith ".abc") {
				if(exportUML || exportABC) {
					exportAbcs((Abc fromFile source) :: Nil)
				}
			} else {
				val swf = Swf fromFile source

				if(exportSWF) {
					log.info("Writing SWF information ...")
					using(writerFor("info")) { swf dump _ }
				}

				if(exportABC || exportUML) {
					val abcs = swf.tags collect {
						case doABC: DoABC => Abc fromDoABC doABC
					}

					exportAbcs(abcs)
				}
			}
		}

		private def exportAbcs(abcs: List[Abc]) = {
			if(exportUML) {
				log.info("Writing UML graph ...")
				using(writerFor("uml")) { new AbcUMLExport(abcs) to _ }
			}

			if(exportABC) {
				bytecodeFormat match {
					case DumpBytecodeAsDefault => {
						abcs foreach { _.loadBytecode() }
						BytecodeDump.`type` = BytecodeDumpTypeDefault
					}
					case DumpBytecodeAsGraph => {
						abcs foreach { _.loadBytecode() }
						BytecodeDump.`type` = BytecodeDumpTypeCFG
					}
					case DumpBytecodeAsRaw =>
				}

				var i = 0

				for(abc <- abcs) {
					log.info("Writing ABC information ...")
					using(writerFor("abc"+i.toString)) { abc dump _ }
					i += 1
				}
			}
		}

		private def writerFor(name: String) = new JPrintWriter(new JFileOutputStream(outputFile(name)))

		private def outputFile(name: String) = output match {
			case Some(dir) => new JFile(dir, outputName(name))
			case None => new JFile(outputName(name))
		}

		private def outputName(name: String) = sourceName+"."+name+".txt"
	}
}
