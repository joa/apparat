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

import java.io.{File, FileInputStream, FileOutputStream}
import java.io.{InputStream, OutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import apparat.utils.IO._

class Abc {
  val cpool = new AbcConstantPool  

  def read(file: File): Unit = using(new FileInputStream(file)) (read _) 
  def read(pathname: String): Unit = read(new File(pathname))
  def read(input: InputStream): Unit = using(new AbcInputStream(input)) (read _)
  def read(data: Array[Byte]): Unit = using(new ByteArrayInputStream(data)) (read _)
  def read(input: AbcInputStream): Unit = {
    implicit val in = input 
    println(AbcQName("", AbcNamespace(0,"")))
    
    if(input.readU16 != 16) error("Only minor version 16 is supported.")
    if(input.readU16 != 46) error("Only major version 46 is supported.")
    
    cpool.clear()
    
    fillTable(cpool.ints) { input.readS32() }
    fillTable(cpool.uints) { input.readU32() }
    fillTable(cpool.doubles) { input.readD64() }
    fillTable(cpool.strings) { input.readString() }
    fillTable(cpool.namespaces) {
      AbcNamespace(input.readU08(), cpool.strings ? input.readU30())
    }
    fillTable(cpool.nssets) {
      AbcNSSet(Set((for(i <- 0 until input.readU08()) yield cpool.namespaces ? input.readU30()):_*))
    }
    println(cpool.nssets.length)
    fillTable(cpool.multinames) {
      input.readU08() match {
        case AbcNameKind.QName => {
          val namespace = input.readU30()
          val name = input.readU30()
          AbcQName(cpool.strings ? name, cpool.namespaces ? namespace)
        }
        case AbcNameKind.QNameA => {
          val namespace = input.readU30()
          val name = input.readU30()
          AbcQNameA(cpool.strings ? name, cpool.namespaces ? namespace)
        }
        case AbcNameKind.RTQName => AbcRTQName(cpool.strings ? input.readU30())
        case AbcNameKind.RTQNameA => AbcRTQNameA(cpool.strings ? input.readU30())
        case AbcNameKind.RTQNameL => AbcRTQNameL
        case AbcNameKind.RTQNameLA => AbcRTQNameLA
        case AbcNameKind.Multiname => AbcMultiname(cpool.strings ? input.readU30(), cpool.nssets ? input.readU30())
        case AbcNameKind.MultinameA => AbcMultinameA(cpool.strings ? input.readU30(), cpool.nssets ? input.readU30())
        case AbcNameKind.MultinameL => AbcMultinameL(cpool.nssets ? input.readU30())
        case AbcNameKind.MultinameLA => AbcMultinameLA(cpool.nssets ? input.readU30())
        case AbcNameKind.Typename => {
          AbcTypename((cpool.multinames ? input.readU30()).asInstanceOf[AbcQName], for(i <- 0 until input.readU30()) yield cpool.multinames ? input.readU30())
        }
        case _ => error("Unknown multiname kind.")
      }
    }
    println(cpool.multinames)
  }
  
  private def fillTable[T](table: ValueTable[T])(reader: => T)(implicit input: AbcInputStream) = for(i <- 1 until input.readU30()) table ! reader
}
