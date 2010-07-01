package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	import flash.events.Event;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.text.TextFormat;
	import flash.utils.getTimer;

	[SWF(backgroundColor='0x000000',frameRate='255',width='320',height='240')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test02 extends ApparatAsset
	{
		private var textField: TextField = new TextField();
		
		public function Test02()
		{
			textField.autoSize = TextFieldAutoSize.LEFT;
			textField.defaultTextFormat = new TextFormat('arial', 9, 0xffffff);
			addChild( textField );
			addEventListener( Event.ENTER_FRAME, run );
		}
		
		private function run( event: Event ): void
		{
			var t0: int = getTimer();
			var x0: int = 0;
			
			for( var i: int = 0; i < 1000000; ++i )
			{
				if( i + 1 != 0 )
				{
					x0 += calc( calc( i / 2, i * 4 ), i * 2 );
				}	
			}
			
			var t1: int = getTimer();

			setText( i + ", " + x0 + ", " + String( t1 - t0 ) );
		}
		
		private function setText( x: String ): void
		{
			textField.text = x;
		}
		
		private function calc( a: Number, b: Number ): int
		{
			a += 2;
			
			return a + b;
		}
	}
}
