/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
	 *	Bar.helloWorld(__byRef(foo.myProperty)
	 *	trace(foo.myProperty) // output "Hello world"
	 *
	 * </pre>
	 */
	public function __byRef(value:*):* {
	}
}
