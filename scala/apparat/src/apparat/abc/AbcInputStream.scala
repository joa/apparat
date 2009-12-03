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
package apparat.abc

import apparat.utils.IO
import java.io.InputStream

class AbcInputStream(input: InputStream) extends InputStream {
  var pos: Int = 0
  def position = pos
  
  private def decodeInt(): Long = {
    var b: Long = read() & 0xff
    var u32 = b
	
    if(!((u32 & 0x00000080) == 0x00000080)) return u32

    b = read() & 0xff
    u32 = u32 & 0x0000007f | b << 7

    if(!((u32 & 0x00004000) == 0x00004000)) return u32

    b = read() & 0xff
    u32 = u32 & 0x00003fff | b << 14

    if(!((u32 & 0x00200000) == 0x00200000)) return u32

    b = read() & 0xff;
    u32 = u32 & 0x001fffff | b << 21;

    if(!((u32 & 0x10000000) == 0x10000000)) return u32

    b = read() & 0xff
    u32 & 0x0fffffff | b << 28;
  }
  
  def readD64() = {
    val first: Long = read() | (read() << 8) | (read() << 16) | (read() << 24);
    val second: Long = read() | (read() << 8) | (read() << 16) | (read() << 24);
    java.lang.Double.longBitsToDouble(first & 0xFFFFFFFFL | second << 32);
  }
  
  def readS24() = {
    val b0 = read
    val b1 = read
    val r = (read() << 0x10) | (b1 << 0x08) | b0;

    if(0 != (r & 0x800000)) ((r & 0x7fffff) - 0x800000)
    else r
  }
  
  def readS32(): Int = {
    val r = decodeInt
    if(0 != (r & 0x80000000)) ((r & 0x7fffffff) - 0x80000000).asInstanceOf[Int]
    else r.asInstanceOf[Int]
  }
  
  def readString() = new String(IO.read(readU30)(this), "UTF8")
  
  def readU08() = read

  def readU16() = {
    val b0 = read
    ( read() << 0x08 ) | b0
  }
  
  def readU30() = (decodeInt & 0x3fffffffL).asInstanceOf[Int]
  
  def readU32() = decodeInt
  
  override def available() = input available
  override def close() = input close()
  override def read() = {
    pos += 1
    input read()
  }
  override def read(b: Array[Byte]) = {
    val n = input read(b)
    pos += n
    n
  }
  override def read(b: Array[Byte], off: Int, len: Int) = {
    val n = input read(b, off, len)
    pos += n
    n
  }
  override def reset() = input reset()
  override def skip(n: Long) = {
    pos += n.asInstanceOf[Int]
    input skip n
  }
}
