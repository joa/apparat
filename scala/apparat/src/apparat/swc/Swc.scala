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
package apparat.swc

import java.io.{File, FileInputStream, FileOutputStream, ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}
import java.util.zip.{Deflater, ZipInputStream, ZipOutputStream, ZipEntry}
import apparat.utils.IO._
import apparat.utils.IO

object Swc {
	def fromFile(file: File): Swc = {
		val swc = new Swc
		swc read file
		swc
	}

	def fromFile(pathname: String): Swc = fromFile(new File(pathname))

	def fromInputStream(input: InputStream) = {
		val swc = new Swc
		swc read input
		swc
	}
}

class Swc {
	var catalog: Option[Array[Byte]] = None
	var library: Option[Array[Byte]] = None

	def read(file: File): Unit = using(new FileInputStream(file))(read _)

	def read(pathname: String): Unit = read(new File(pathname))

	def read(input: InputStream): Unit = using(new ZipInputStream(input))(read _)

	def read(data: Array[Byte]): Unit = using(new ByteArrayInputStream(data))(read _)

	def read(input: ZipInputStream): Unit = extract(input)

	def write(file: File): Unit = using(new FileOutputStream(file))(write _)

	def write(pathname: String): Unit = write(new File(pathname))

	def write(output: OutputStream): Unit = {
		val zipOutput = new ZipOutputStream(output)
		try {
			zipOutput setMethod ZipOutputStream.DEFLATED
			zipOutput setLevel Deflater.BEST_COMPRESSION

			write(zipOutput)

			zipOutput flush ()
			zipOutput close ()
		} finally {
			try {zipOutput close ()} catch {case _ => {}}
		}
	}

	def write(output: ZipOutputStream): Unit = {
		write(output, catalog, "catalog.xml")
		write(output, library, "library.swf")
		output flush ()
	}

	private def write(output: ZipOutputStream, target: Option[Array[Byte]], name: String): Unit = target match {
		case Some(data) => {
			output putNextEntry new ZipEntry(name)
			output write data
			output closeEntry
		}
		case None => error(name + " is missing.")
	}

	private def extract(implicit input: ZipInputStream): Unit = input getNextEntry match {
		case null => {}
		case _@entry => {
			val name = entry.getName()
			val size = entry.getSize()

			if (entry isDirectory) error("Unexpected directory in SWC.")

			name match {
				case "catalog.xml" => catalog = Some(extractBytes(size.asInstanceOf[Int]))
				case "library.swf" => library = Some(extractBytes(size.asInstanceOf[Int]))
			}

			extract(input)
		}
	}

	private def extractBytes(length: Int)(implicit input: ZipInputStream) = length match {
		case -1 => byteArrayOf(input)
		case _ => IO read length
	}
}
