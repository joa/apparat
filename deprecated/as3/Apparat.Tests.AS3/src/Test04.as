package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test04 extends ApparatAsset 
	{
		public function Test04()
		{
			super();
			littleEuler();
		}
		
		private function littleEuler(): void
		{
			var x: int = 0;
			
			for( var i: int = 0; i < 100; ++i )
			{
				x += i;
			}
			
			trace( x );	
		}
	}
}
