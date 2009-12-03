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
import apparat.abc._
import apparat.swf._
import apparat.swf.SwfPrimitives._
import apparat.swf.SwfTags._
import apparat.utils.Performance._
import apparat.utils._
    
object Main {
  def main(args : Array[String]) : Unit = {
    val container = TagContainer fromFile "assets/sandbox.swf"
    (container tags) foreach (println _)
    container write "assets/s2.swf"
    
    /*val swf = Swf fromFile "assets/Test02.swf"
    
    for(x <- swf.tags if x.kind == SwfTags.DoABC) {
      val doABC = x.asInstanceOf[DoABC]
      measure(doABC.name) {
        val abc = new Abc
        abc read doABC.abcData
      }
    }*/
    
    /*val swf = new Swf
    
    measure("Swf.read()") {
      swf read("assets/sandbox.swf")
    }
    
    swf.tags foreach (println _)
    
    measure("Swf.write()") {
      swf write("assets/sandbox.out.swf")
    }
    
    measure("Verify") {
      (Swf fromFile "assets/sandbox.out.swf" tags) foreach (println _)
    }
    
    measure("playerglobal.swc") {
      (Swf fromFile "assets/playerglobal.swc" tags) foreach (println _)
    }*/
  }
}
