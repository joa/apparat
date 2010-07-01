package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test12 extends ApparatAsset 
	{
		public function Test12()
		{
			super();

			for( var i: int = 0; i < 5; ++i )
			{
				//
				// Simple version:
				//
				
				switch( i )
				{
					case 0:
						trace('0');
						break;
						
					default:
						trace('default');
						break;
				}
				
				//
				// More complex version:
				//
				
				switch( i )
				{
					case 0:
						trace('0');
						break;
						
					case 1:
						trace('1');
						break;
						
					case 2:
						trace( '2' );
					case 3:
						trace( '2, 3');
						break;
					
					default:
						trace('default');
						break;
				}
			}
		}
	}
}
