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

object AbcConstantPool {
  val EMPTY_STRING = ""
  val EMPTY_NAMESPACE = AbcNamespace(0,EMPTY_STRING)
  val EMPTY_NSSET = AbcNSSet(Set(EMPTY_NAMESPACE))
  val EMPTY_NAME = AbcQName(EMPTY_STRING,EMPTY_NAMESPACE)
}

class AbcConstantPool {
  def clear() = {
    ints clear()
    uints clear()
    doubles clear()
    strings clear()
    namespaces clear()
    nssets clear()
    multinames clear()
    
    ints ! 0
    uints ! 0L
    //string is handled in its clear method
    doubles ! Double.NaN
    namespaces ! AbcConstantPool.EMPTY_NAMESPACE
    nssets ! AbcConstantPool.EMPTY_NSSET
    multinames ! AbcConstantPool.EMPTY_NAME
    println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ")
  }
  
  val ints = new ValueTable[Int](this)
  
  val uints = new ValueTable[Long](this)
  
  val doubles = new ValueTable[Double](this)
  
  val strings = new ValueTable[String](this) {
    override def indexOf(value: String, fromIndex: Int) = {
      if(AbcConstantPool.EMPTY_STRING == value) 0
      else super.indexOf(value, fromIndex)
    }
    override def clear() = {
      super.clear
      buffer += AbcConstantPool.EMPTY_STRING
      buffer += ""
    }
  }
  
  val namespaces = new ValueTable[AbcNamespace](this) {
    override def indexOf(value: AbcNamespace, fromIndex: Int) = {
      cpool ! value.name 
      if(AbcConstantPool.EMPTY_NAMESPACE == value) 0
      else super.indexOf(value, fromIndex)
    }
  }
  
  val nssets = new ValueTable[AbcNSSet](this) {
    override def indexOf(value: AbcNSSet, fromIndex: Int) = {
      for(x <- value.set)
        cpool ! x
      if(AbcConstantPool.EMPTY_NSSET == value) 0
      else super.indexOf(value, fromIndex)
    }
  }
  
  val multinames = new ValueTable[AbcName](this) {
    override def indexOf(value: AbcName, fromIndex: Int) = {
      value match {
        case AbcQName(x,y) => {
          cpool ! x
          cpool ! y
        }
        case AbcQNameA(x,y) => {
          cpool ! x
          cpool ! y
        }
        case AbcRTQName(x) => cpool ! x
        case AbcRTQNameA(x) => cpool ! x
        case AbcMultiname(x,y) => {
          cpool ! x
          cpool ! y
        }
        case AbcMultinameA(x,y) => {
          cpool ! x
          cpool ! y
        }
        case AbcMultinameL(x) => cpool ! x
        case AbcMultinameLA(x) => cpool ! x
        case AbcTypename(x, y) => {
          cpool ! x
          y foreach (cpool ! _)
        }
        case AbcRTQNameL | AbcRTQNameLA => {}
      }
      if(AbcConstantPool.EMPTY_NAME == value) 0
      else super.indexOf(value, fromIndex)
    }
  }
  
  def ! (that: Int): Unit = ints ! that
  def ! (that: Long): Unit = uints ! that
  def ! (that: Double): Unit = doubles ! that
  def ! (that: String): Unit = strings ! that
  def ! (that: AbcNamespace): Unit = namespaces ! that
  def ! (that: AbcNSSet): Unit = nssets ! that
  def ! (that: AbcName): Unit = multinames ! that
  
  def !! (that: Int): Unit = ints !! that
  def !! (that: Long): Unit = uints !! that
  def !! (that: Double): Unit = doubles !! that
  def !! (that: String): Unit = strings !! that
  def !! (that: AbcNamespace): Unit = namespaces !! that
  def !! (that: AbcNSSet): Unit = nssets !! that
  def !! (that: AbcName): Unit = multinames !! that
}

import scala.collection.mutable.{Buffer, ArrayBuffer};

class ValueTable[T](constantPool: AbcConstantPool) {
  protected val cpool = constantPool
  protected val buffer: Buffer[T] = new ArrayBuffer;
    
  def ! (that: T) = { println("that: \"" + that + "\""); indexOf(that, 0) }
  def !!(that: T) = indexOf(that, 1)
  def ? (that: Int) = valueOf(that)
    
  def indexOf(value: T, fromIndex: Int): Int = {
    val n = buffer.size
    for(i <- fromIndex until n) {
      if(buffer(i) == value) { 
        return i
      }
    }
    buffer += value
    n
  }

  def length = buffer length
  def valueOf(index: Int): T = buffer(index)
  def clear() = buffer clear()
  override def toString = buffer toString 
}