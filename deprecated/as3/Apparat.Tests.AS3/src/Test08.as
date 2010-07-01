package  
{
	import com.joa_ebert.apparat.ApparatAsset;
	import com.joa_ebert.apparat.PerformanceDisplay;

	import flash.events.Event;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test08 extends ApparatAsset 
	{
		private var phase: Number = 0.0;
		private var phaseInc: Number = 440.0;
		
		public function Test08()
		{
			super();
			
			addChild( new PerformanceDisplay( this ) );
			addEventListener( Event.ENTER_FRAME, onEnterFrame );
		}
		
		private function onEnterFrame( event : Event ) : void
		{
			for( var y: int = 0; y < 1000; ++y )
			{
				var value: Number;
				
				for( var x: int = 0; x < 1000; ++x )
				{
					phase += phaseInc;
					if( phase > 1.0 ) --phase;
					
					value += Math.sin( phase * Math.PI * 2.0 );
				}
			}
		}
	}
}
