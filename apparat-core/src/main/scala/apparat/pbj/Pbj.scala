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
package apparat.pbj

import pbjdata._

import java.io.{
	File => JFile,
	BufferedInputStream => JBufferedInputStream,
	ByteArrayInputStream => JByteArrayInputStream,
	FileInputStream => JFileInputStream,
	InputStream => JInputStream}

import apparat.utils.IO._
import collection.mutable.ListBuffer
import apparat.utils.{IndentingPrintWriter, Dumpable}

object Pbj {
	def main(args: Array[String]): Unit = {
		val pbj = new Pbj()
		pbj read args(0)
		pbj.dump()
	}
}

/**
 * @author Joa Ebert
 */
class Pbj extends Dumpable {
	var version = 1
	var name = ""
	var metadata = List.empty[PMeta]
	var parameters = List.empty[(PParam, List[PMeta])]
	var textures = List.empty[PTexture]
	var code = List.empty[POp]

	def read(file: JFile): Unit = using(new JBufferedInputStream(new JFileInputStream(file), 0x1000)) { read(_) }

	def read(pathname: String): Unit = read(new JFile(pathname))

	def read(input: JInputStream): Unit = using(new PbjInputStream(input)) { read(_) }

	def read(data: Array[Byte]): Unit = using(new JByteArrayInputStream(data)) { read(_) }

	def read(input: PbjInputStream): Unit = {
		var metadataBuffer = List.empty[PMeta]
		var parameterBuffer = List.empty[(PParam, ListBuffer[PMeta])]
		var parameterMetadataBuffer = ListBuffer.empty[PMeta]
		var textureBuffer = List.empty[PTexture]
		var codeBuffer = List.empty[POp]

		for(op <- input) op match {
			case PKernelMetaData(value) => metadataBuffer = value :: metadataBuffer
			case PParameterData(value) =>
				parameterMetadataBuffer = ListBuffer.empty[PMeta]
				parameterBuffer = (value -> parameterMetadataBuffer) :: parameterBuffer 
			case PParameterMetaData(value) => parameterMetadataBuffer += value
			case PTextureData(value) => textureBuffer = value :: textureBuffer
			case PKernelName(value) => name = value
			case PVersionData(value) => version = value
			case _ => codeBuffer = op :: codeBuffer
		}


		metadata = metadataBuffer.reverse
		parameters = parameterBuffer.reverse map { x => x._1 -> x._2.toList }
		textures = textureBuffer.reverse
		code = codeBuffer.reverse
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Pbj:"
		writer withIndent {
			writer <= "Version: "+version
			writer <= "Name: "+name
			writer <= metadata.length+" metadata:"
			writer <<< metadata
			writer <= parameters.length+" parameter(s):"
			writer <<< parameters
			writer <= textures.length+" texture(s):"
			writer <<< textures
			writer <= "Code:"
			writer <<< code
		}
	}
}