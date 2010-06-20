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
package apparat.swf

import apparat.swc.Swc
import apparat.utils.IO._
import java.io.{
	BufferedInputStream => JBufferedInputStream,
	File => JFile,
	FileInputStream => JFileInputStream,
	FileOutputStream => JFileOutputStream,
	ByteArrayInputStream => JByteArrayInputStream,
	ByteArrayOutputStream => JByteArrayOutputStream,
	InputStream => JInputStream,
	OutputStream => JOutputStream
}
import java.util.zip.{Inflater => JInflater}
import scala.annotation.tailrec
import apparat.utils.{Dumpable, Deflate, IndentingPrintWriter}

object Swf {
	def fromFile(file: JFile): Swf = {
		val name = file.getName.toLowerCase

		if(name endsWith ".swc") {
			fromSwc(Swc fromFile file)
		} else if(name endsWith ".swf") {
			val swf = new Swf
			swf read file
			swf
		} else {
			using(new JFileInputStream(file)) {
				input => {
					val b0 = input.read()

					if(('F' == b0 || 'C' == b0) && 'W' == input.read() && 'S' == input.read()) {
						val swf = new Swf
						swf read file
						swf
					} else if ('P' == b0 && 'K' == input.read()) {
						fromSwc(Swc fromFile file)
					} else {
						error("Unknown file "+file.getAbsolutePath+".")
					}
				}
			}
		}
	}

	def fromFile(pathname: String): Swf = fromFile(new JFile(pathname))

	def fromSwc(swc: Swc) = {
		val swf = new Swf
		swf read swc
		swf
	}

	def fromInputStream(input: JInputStream, length: Long) = {
		val swf = new Swf
		swf.read(input, length)
		swf
	}
}

final class Swf extends Dumpable {
	var compressed: Boolean = true
	var version: Int = 10
	var frameSize: Rect = new Rect(0, 20000, 0, 20000)
	var frameRate: Float = 255.0f
	var frameCount: Int = 1
	var tags: List[SwfTag] = Nil

	def foreach(body: SwfTag => Unit) = tags foreach body

	def read(file: JFile): Unit = using(new JBufferedInputStream(new JFileInputStream(file), 0x1000))(read(_, file.length))

	def read(pathname: String): Unit = read(new JFile(pathname))

	def read(input: JInputStream, inputLength: Long): Unit = using(new SwfInputStream(input))(read(_, inputLength))

	def read(data: Array[Byte]): Unit = using(new JByteArrayInputStream(data))(read(_, data.length))

	def read(swc: Swc): Unit = {
		swc.library match {
			case Some(data) => read(data)
			case None =>
		}
	}

	def read(input: SwfInputStream, inputLength: Long): Unit = {
		(input.readUI08(), input.readUI08(), input.readUI08()) match {
			case (x, 'W', 'S') => compressed = x match {
				case 'C' => true
				case 'F' => false
				case _ => error("Not a SWF file.")
			}
			case _ => error("Not a SWF file.")
		}

		version = input.readUI08()

		val uncompressedLength = input.readUI32()
		val uncompressed = compressed match {
			case true => {
				assert(version > 5)
				uncompress(inputLength, uncompressedLength)(input)
			}
			case false => input
		}

		try {
			frameSize = uncompressed.readRECT()
			frameRate = uncompressed.readFIXED8()
			frameCount = uncompressed.readUI16()

			assert(frameSize.minX == 0 && frameSize.minY == 0)
			assert(frameRate >= 0)
			assert(frameCount > 0)

			tags = tagsOf(uncompressed)
		} finally {
			if(compressed) {
				try {
					uncompressed.close()
				} catch {
					case _ =>
				}
			}
		}
	}

	private def tagsOf(implicit input: SwfInputStream): List[SwfTag] = {
		@tailrec def loop(tag: SwfTag, acc: List[SwfTag]): List[SwfTag] = {
			val result = tag :: acc
			if(tag.kind == SwfTags.End) result else loop(input.readTAG(), result)
		}

		loop(input.readTAG(), List.empty).reverse
	}

	def write(file: JFile): Unit = using(new JFileOutputStream(file))(write _)

	def write(pathname: String): Unit = write(new JFile(pathname))

	def write(output: JOutputStream): Unit = using(new SwfOutputStream(output))(write _)

	def write(swc: Swc): Unit = {
		val byteArrayOutputStream = new JByteArrayOutputStream()

		try {
			write(byteArrayOutputStream)
			swc.library = Some(byteArrayOutputStream.toByteArray)
		} finally {
			try {
				byteArrayOutputStream.close()
			} catch {
				case _ =>
			}
		}
	}

	def write(output: SwfOutputStream): Unit = {
		val byteArrayOutputStream = new JByteArrayOutputStream(0x08 + (tags.length << 0x03))
		val buffer = new SwfOutputStream(byteArrayOutputStream)

		try {
			buffer.writeRECT(frameSize)
			buffer.writeFIXED8(frameRate)
			buffer.writeUI16(frameCount)
			buffer.flush()

			tags foreach { buffer writeTAG _ }
			buffer.flush()

			val bytes = byteArrayOutputStream.toByteArray

			buffer.close()

			output.write(Array[Byte](if (compressed) 'C' else 'F', 'W', 'S'))
			output.writeUI08(version)
			output.writeUI32(8 + bytes.length)

			if (compressed) {
				Deflate.compress(bytes, output)
			} else {
				output write bytes
			}

			output.flush()
		} finally {
			if (null != buffer) {
				try {
					buffer.close()
				} catch {
					case _ =>
				}
			}
		}
	}

	def uncompress(inputLength: Long, uncompressedLength: Long)(implicit input: JInputStream) = {
		val totalBytes = (inputLength - 8).asInstanceOf[Int]//magic 8 is static part of header length
		val inflater = new JInflater()
		val bufferIn = new Array[Byte](totalBytes)
		val bufferOut = new Array[Byte]((uncompressedLength - 8).asInstanceOf[Int])

		readBytes(totalBytes, bufferIn)

		inflater setInput (bufferIn)

		var offset = -1
		while (0 != offset && !inflater.finished()) {
			offset = inflater inflate bufferOut
			if (0 == offset && inflater.needsInput) {
				error("Need more input.")
			}
		}

		new SwfInputStream(new JByteArrayInputStream(bufferOut))
	}

	def toByteArray = {
		val byteArrayOutputStream = new JByteArrayOutputStream()
		using(byteArrayOutputStream) { write _ }
		byteArrayOutputStream.toByteArray
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Swf:"
		writer withIndent {
			writer <= "Compressed: "+compressed
			writer <= "Version: "+version
			writer <= "Framesize:"+frameSize
			writer <= "Framerate:"+frameRate
			writer <= "Framecount:"+frameCount
			writer <= "Tags:"
			writer withIndent {
				for(tag <- tags) tag match {
					case dumpable: Dumpable => dumpable dump writer
					case other => writer <= other.toString
				}
			}
		}
	}
}
