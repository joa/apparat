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

package {
	import apparat.asm.*;

	import flash.display.Sprite;
	import flash.text.TextField;

	public class Test2 extends Sprite {
		private var _tf:TextField=new TextField();

		private function trace(...args):void{
			_tf.appendText(args.join("")+"\n");
		}

		public function Test2() {
			super();
			addChild(_tf);

			var foo:Function=function():void{
				trace("foo");
			};

			var i1:int = 1;
			var i2:int = 2;
			var i3:int;

			__asm(
				PushByte(-1),
				LookupSwitch("default", "l0", "l1"),
			"default:",
				__as3(trace("default")),
				Jump("out"),
			"l0:",
				__as3(trace("l0")),
				Jump("out"),
			"l1:",
				__as3(trace("l1")),
			"out:",
				Pop,
				this,
				PushString("is i1<i2:"),
				__as3(i1<i2),
				IfTrue("true"),
				PushString("false"),
				Jump("print"),
			"true:",
				PushString("true"),
			"print:",
				PushString(",i3="),
				GetLocal(i1),
				GetLocal(i2),
				AddInt,
				CallPropVoid(__as3(trace), 4)
			);
		}
	}
}
