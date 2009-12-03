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

import java.io.InputStream

class SwfInputStream(val input: InputStream) extends InputStream {
  private var bitBuffer: Int = 0
  private var bitIndex: Int = 0
  
  private def aligned[A](body: => A): A = {
    bitBuffer = 0
    bitIndex = 0
    body
  }
  
  private def signed(mask: Int, r: Int) = {
    if(0 != (r & mask)) (r & (mask - 1)) - mask
    else r
  }
  
  private def signed(mask: Long, r: Long): Int = {
    if(0 != (r & mask)) ((r & (mask - 1L)) - mask).asInstanceOf[Int]
    else r.asInstanceOf[Int]
  }
  
  private def readBits() = {
    bitBuffer = read
    bitIndex = 8
  }
  
  private def isBitTrue() = {
    if(0 == bitIndex) readBits
    0 != (bitBuffer & (1 << (bitIndex - 1)))
  }
  
  private def bitAt(index: Int) = isBitTrue match {
    case true => { nextBit; 1 << index }
    case false => { nextBit; 0 }
  }
  
  private def nextBit() = bitIndex = bitIndex - 1
  
  def readFIXED() = {
    val a = readUI16 / 65535.0f
    readUI16() + a
  }
  
  def readFIXED8() = {
    val a = readUI08() / 255.0f;
    readUI08() + a
  }
  
  def readRECORDHEADER() = {
    val pack = readUI16
    new Recordheader(pack >> 6, if(0x3f == (pack & 0x3f)) readSI32() else pack & 0x3f)
  }
  
  def readRECT() = {
    val bits = readUB(5)
    new Rect(readSB(bits),readSB(bits),readSB(bits),readSB(bits))
  }
  
  def readRGB() = new RGB(readUI08,readUI08,readUI08)
  
  def readSTRING(): String = {
    def until0(seq: List[Byte]): List[Byte] = readUI08 match {
      case 0 => seq
      case _ @ y => {
        seq match {
          case Nil => until0(List(y.asInstanceOf[Byte]))
          case h :: t => until0(seq ::: List(y.asInstanceOf[Byte]))
        }
      }
    }

    new String(until0(Nil) toArray, "UTF8")
  }
  
  def readTAG(): SwfTag = {
    val h = readRECORDHEADER()
    val t = SwfTags create h.kind
    t read(h, this)
    t
  }
  
  def readUB(n: Int) = (List.range(0, n) reverse) map bitAt reduceLeft (_ | _)
  
  def readUI08() = aligned { read }
  
  def readUI16() = aligned {
    val b0 = readUI08
    (read << 0x08) | b0
  }
  
  def readUI24() = aligned {
    val b0 = readUI16
    (read << 0x10) | b0
  }
  
  def readUI32(): Long = aligned {
    val b0 = readUI24
    (read << 0x18) | b0 
  }
  
  def readUI64(): BigInt = aligned {
    val data: Array[Byte] = new Array[Byte](8);  
    assume(read(data) == 8)
    new BigInt(new java.math.BigInteger(1, data reverse))
  }
  
  def readSB(n: Int) = signed(1 << n, readUB(n))
  def readSI08() = signed(0x80, readUI08)
  def readSI16() = signed(0x8000, readUI16)
  def readSI24() = signed(0x800000, readUI24)
  def readSI32() = signed(0x80000000L, readUI32)
  
  override def available() = input available
  override def close() = input close()
  override def read() = input read()
  override def read(b: Array[Byte]) = input read(b)
  override def read(b: Array[Byte], off: Int, len: Int) = input read(b, off, len)
  override def reset() = input reset()
  override def skip(n: Long) = input skip n
}
