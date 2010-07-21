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