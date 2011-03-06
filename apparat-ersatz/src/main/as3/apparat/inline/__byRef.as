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
 * Author : Patrick Le Clec'h
 */

package apparat.inline {
	/**
	 * __byRef allow to pass directly a property to a macro for reading or writing
	 *
	 * @see apparat.inline.Macro
	 *
	 * @param value reference to be used into the macro
	 * @return
	 *
	 * @example
	 * <pre>
	 *	 public class Foo {
	 *	   public var myProperty:String = "Hello"
	 *	 }
	 *
	 *	 public class Bar extends Macro {
	 *	   public static function helloWorld(out:String):void{
	 *		out += " world"
	 *	  }
	 *	}
	 *
	 *	var foo:Foo=new Foo()
	 *	Bar.helloWorld(foo.myProperty)
	 *	trace(foo.myProperty) // output "Hello world"
	 *
	 * </pre>
	 */
	public function __byRef(value:*):* {
	}
}
