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
import java.io.{File, FileInputStream, FileOutputStream, ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}
import java.util.zip.{Inflater, Deflater}

object Swf {
  def fromFile(file: File): Swf = {
    if(file.getName() endsWith "swc") {
      fromSwc(Swc fromFile file)
    } else {
      val swf = new Swf
      swf read file
      swf
    }
  }
  
  def fromFile(pathname: String): Swf = fromFile(new File(pathname))
  
  def fromSwc(swc: Swc) = {
    val swf = new Swf
    swf read swc
    swf
  }
}

class Swf {
  var compressed: Boolean = true
  var version: Int = 10
  var frameSize: Rect = new Rect(0,20000,0,20000)
  var frameRate: Float = 255.0f
  var frameCount: Int = 1
  var tags: List[SwfTag] = Nil
  
  def read(file: File): Unit = using(new FileInputStream(file)) (read(_, file length)) 
  def read(pathname: String): Unit = read(new File(pathname))
  def read(input: InputStream, inputLength: Long): Unit = using(new SwfInputStream(input)) (read(_, inputLength))
  def read(data: Array[Byte]): Unit = using(new ByteArrayInputStream(data)) (read(_, data length))
  def read(swc: Swc): Unit = {
    swc.library match {
      case Some(data) => read(data)
      case None => {}
    }
  }
  
  def read(input: SwfInputStream, inputLength: Long): Unit = {
    (input readUI08, input readUI08, input readUI08) match {
      case (_ @ x,'W','S') => compressed = x match {
        case 'C' => true
        case 'F' => false
        case _ => error("Not a SWF file.")
      }
      case _ => error("Not a SWF file.")
    }
    
    version = input readUI08

    val uncompressedLength = input readUI32
    val uncompressed = compressed match {
      case true => {
        assert(version > 5)
        uncompress(inputLength, uncompressedLength)(input)
      }
      case false => input
    }
    
    try {
      frameSize = uncompressed readRECT;
      frameRate = uncompressed readFIXED8;
      frameCount = uncompressed readUI16
    
      assert(frameSize.minX == 0 && frameSize.minY == 0)
      assert(frameRate >= 0)
      assert(frameCount > 0)
    
      tags = tagsOf(uncompressed)
    } finally {
      if(compressed) {
        try { uncompressed close() } catch { case _ => {} }
      }
    }
  }
  
  private def tagsOf(input: SwfInputStream): List[SwfTag] = {
    val tag = input readTAG()
    tag :: (tag.kind match {
      case SwfTags.End => Nil
      case _ => tagsOf(input)
    })
  }
  
  def write(file: File): Unit = using(new FileOutputStream(file)) (write _)
  def write(pathname: String): Unit = write(new File(pathname))
  def write(output: OutputStream): Unit = using(new SwfOutputStream(output)) (write _)
  def write(output: SwfOutputStream): Unit = {
    val baos = new ByteArrayOutputStream(0x08 + (tags.length << 0x03));
    val buffer = new SwfOutputStream(baos)
    try {
      buffer writeRECT(frameSize)
      buffer writeFIXED8(frameRate)
	  buffer writeUI16(frameCount)
      buffer flush;
      tags foreach (buffer writeTAG(_))
      buffer flush;
    
      val bytes = baos toByteArray;
      buffer close;
    
      output write (List[Byte](if(compressed) 'C' else 'F', 'W', 'S') toArray)
      output writeUI08 version
      output writeUI32 (8 + bytes.length)
    
      if(compressed) {
        val deflater = new Deflater(Deflater.BEST_COMPRESSION)
        val buffer2 = new Array[Byte](0x1000);
        var numBytesCompressed = 0;
        deflater setInput bytes
        deflater finish
      
        do {
          numBytesCompressed = deflater.deflate(buffer2);
          output write (buffer2, 0, numBytesCompressed);
        } while(0 != numBytesCompressed);
      } else {
        output write bytes
      }
    
      output flush();
    }
    finally {
      if(null != buffer) {
        try { buffer.close() }
        catch { case _ => {} }
      }
    }
  }
  
  def uncompress(inputLength: Long, uncompressedLength: Long)(implicit input: InputStream) = {
    val totalBytes = (inputLength - 8).asInstanceOf[Int]
    val inflater = new Inflater()
    val bufferIn = new Array[Byte](totalBytes)
    val bufferOut = new Array[Byte]((uncompressedLength - 8).asInstanceOf[Int])
    
    readBytes(totalBytes, bufferIn)
        
    inflater setInput(bufferIn)
    
    var offset = -1
    while(0 != offset && !inflater.finished) {
      offset = inflater inflate bufferOut
      if(0 == offset && inflater.needsInput) {
        error("Need more input.")
      }
    }
    
    new SwfInputStream(new ByteArrayInputStream(bufferOut));
  }
}
