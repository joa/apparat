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
package apparat.utils

object IO {
  import java.io.{InputStream, OutputStream}
  import java.io.ByteArrayOutputStream
  
  def read(input: InputStream, length: Int): Array[Byte] = readBytes(input, length)(new Array[Byte](length))
  
  def readBytes(input: InputStream, length: Int)(bytes: Array[Byte]): Array[Byte] = {
    var offset = 0
    while(offset < length)
      offset += input.read(bytes, offset, length - offset)
    bytes
  }
  
  def byteArrayOf(input: InputStream) = {
    val output = new ByteArrayOutputStream
    val buffer = new Array[Byte](0x1000)
    var bytesRead = 0

    bytesRead = input.read(buffer)
    
    while(bytesRead >= 0) {
      output write (buffer, 0, bytesRead )
      bytesRead = input.read(buffer)
    }
    
    output.close()
    output.toByteArray()
  }
  
  def using[A, B <: { def close() }](stream: B)(body: B => A): A = {
    try {
      body(stream)
    }
    finally {
      if(null != stream) {
        try {
          stream close()
        } catch {
          case _ => {}
        }
      }
    }
  }
}
