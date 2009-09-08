package  
{
	import flash.events.Event;
	import com.joa_ebert.apparat.PerformanceDisplay;
	import com.joa_ebert.apparat.ApparatAsset;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test06 extends ApparatAsset 
	{
		public function Test06()
		{
			super();
			
			addChild( new PerformanceDisplay( this ) );
			addEventListener( Event.ENTER_FRAME, onEnterFrame );
			
			trace( fact( 100 ) );
			trace( sum( 100, 0 ) );
		}
		
		private function onEnterFrame( event : Event ) : void
		{
			var val: int = 0;
			
			for( var i: int = 1; i <= 10000; ++i )
			{
				val += fact( 256 );
			}
		}
		
		private function sum( i: int, value: int ): int
		{
			if( i == 0 )
			{
				return value;
			}
		
			return sum( i - 1, value + i ); 
		}
		 
		private function fact( number: int ): int
		{
    		if( number == 0 )
    		{
           		return 1;
			}
			
        	return factorial_i( number, 1 );
		}
		
		private function factorial_i( currentNumber: int, sum: int ): int
		{
		    if( currentNumber == 1 )
		    {
		        return sum;
		    }
		    else
		    {
		        return factorial_i( currentNumber - 1, sum * currentNumber );
		    }
		}
	}
}
