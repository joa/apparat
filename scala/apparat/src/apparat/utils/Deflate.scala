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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.utils

import java.io.{
	OutputStream => JOutputStream,
	File => JFile,
	FileOutputStream => JFileOutputStream,
	FileInputStream => JFileInputStream,
	IOException => JIOException,
	BufferedReader => JBufferedReader,
	ByteArrayOutputStream => JByteArrayOutputStream,
	InputStreamReader => JInputStreamReader}
import java.util.zip.{Deflater => JDeflater, Adler32 => JAdler32}
import java.lang.{ProcessBuilder => JProcessBuilder}
import apparat.tools.ApparatLog
import apparat.utils.IO._

/**
 * @author Joa Ebert
 */
object Deflate {
	private var _7z = System.getProperty("apparat.7z.enabled", "true").toLowerCase == "true"
	private val _7zexe = System.getProperty("apparat.7z.path", "7z" + (System getProperty "os.name" indexOf "Windows" match {
		case -1 => "a"
		case _ => ".exe"
	}))

	private lazy val adler32 = new JAdler32()

	def compress(bytes: Array[Byte], output: JOutputStream) = {
		if(_7z) {
			compressUsing7z(bytes, output)
		} else {
			compressUsingDeflater(bytes, output)
		}
	}

	private def compressUsingDeflater(bytes: Array[Byte], output: JOutputStream) = {
		val deflater = new JDeflater(JDeflater.BEST_COMPRESSION)
		val buffer = new Array[Byte](0x8000)
		var numBytesCompressed = 0

		deflater setInput bytes
		deflater.finish()

		do {
			numBytesCompressed = deflater deflate buffer
			output.write(buffer, 0, numBytesCompressed)
		} while (0 != numBytesCompressed)

		output.flush()
	}

	private def compressUsing7z(bytes: Array[Byte], output: JOutputStream) = {
		try {
			//if(bytes.length < 0x40000) {
			//	compress7zInSTDIO(bytes, output)
			//} else {
				compress7zOnDisk(bytes, output)
			//}
		} catch {
			case ioException: JIOException => {
				_7z = false
				ApparatLog warn "7z is not present on PATH. Fallback to normal compression."
				compressUsingDeflater(bytes, output)
			}
			case other => {
				_7z = false
				ApparatLog warn "7z failed. Fallback to normal compression."
				compressUsingDeflater(bytes, output)
			}
		}
	}

	private def compress7zOnDisk(bytes: Array[Byte], output: JOutputStream) = {
		val gzInput = JFile.createTempFile("apparat", "input")
		val gzOutput = JFile.createTempFile("apparat", "output")

		gzInput.deleteOnExit
		gzOutput.deleteOnExit

		using(new JFileOutputStream(gzInput)) { _ write bytes }

		val builder = new JProcessBuilder(_7zexe, "a", gzOutput.getAbsolutePath, "-tgzip", "-mx9", gzInput.getAbsolutePath)
		val process = builder.start()

		ApparatLog("Waiting for 7z ...")
		assert(0 == process.waitFor())

		val sevenZipOutput = new JFile(gzOutput.getAbsolutePath + ".gz")
		val fileInputStream = new JFileInputStream(sevenZipOutput)

		sevenZipOutput.deleteOnExit()

		try {
			writeGZIP(bytes, output, byteArrayOf(fileInputStream))
		} finally {
			try { fileInputStream.close() } catch { case _ => {} }
			try { gzInput.delete() } catch { case _ => {} }
			try { gzOutput.delete() } catch { case _ => {} }
			try { sevenZipOutput.delete() } catch { case _ => {} }
		}
	}

	private def compress7zInSTDIO(bytes: Array[Byte], output: JOutputStream) = {
		val builder = new JProcessBuilder(_7zexe, "a", "apparat", "-tgzip", "-mx9", "-siswf", "-so")
		val process = builder.start()
		val outputStream = process.getOutputStream
		val inputStream = process.getInputStream

		outputStream write bytes
		outputStream.close()

		val buffer = new Array[Byte](0x8000)
		var bytesTotal = 0
		val byteArrayOutputStream = new JByteArrayOutputStream()

		ApparatLog("Waiting for 7z ...")
		
		while(inputStream.available != 0) {
			val bytesRead = inputStream.read(buffer)
			bytesTotal += bytesRead
			byteArrayOutputStream.write(buffer, 0, bytesRead)
			Thread sleep 8// Can we fix this?
		}

		inputStream.close()
		assert(0 == process.waitFor())
		writeGZIP(bytes, output, byteArrayOutputStream.toByteArray)
	}

	private def writeGZIP(bytes: Array[Byte], output: JOutputStream, gzipBuffer: Array[Byte]) = {
		// GZIP specification:
		// http://www.ietf.org/rfc/rfc1952
		assert((gzipBuffer(0) & 0xff) == 0x1f, "GZip header is corrupt.")
		assert((gzipBuffer(1) & 0xff) == 0x8b, "GZip header is corrupt.")
		assert((gzipBuffer(2) & 0xff) == 0x08, "Deflate stream required.")

		val flags = gzipBuffer(3) & 0xff

		assert(0 == (flags & (1 << 0)), "FTEXT must not be set.")

		// Skip 4b of modification time
		// XFL is unimportant (byte 8)
		// OS is unimportant (byte 9)

		var bufferPos = 10

		if(0 != (flags & (1 << 2))) {
			bufferPos += (gzipBuffer(10) & 0xff)
		}

		if(0 != (flags & (1 << 3))) {
			while(gzipBuffer(bufferPos) != 0x00) {
				bufferPos += 1
			}

			bufferPos += 1
		}

		if(0!= (flags & (1 << 4))) {
			while(gzipBuffer(bufferPos) != 0x00) {
				bufferPos += 1
			}

			bufferPos += 1
		}

		if(0 != (flags & (1 << 1))) {
			bufferPos += 2
		}

		adler32.reset()
		adler32 update bytes

		val checksum = adler32.getValue().asInstanceOf[Int]

		//ZLIB Header
		output write 0x78
		output write 0xda

		// Deflate Stream
		output.write(gzipBuffer, bufferPos, gzipBuffer.length - bufferPos - 8)

		// Adler32 Checksum
		output write ((checksum >> 0x18) & 0xff)
		output write ((checksum >> 0x10) & 0xff)
		output write ((checksum >> 0x08) & 0xff)
		output write (checksum & 0xff)

		output.flush()
	}
}