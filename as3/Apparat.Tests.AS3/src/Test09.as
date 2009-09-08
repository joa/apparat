package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	import flash.display.Sprite;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test09 extends ApparatAsset 
	{
		public function Test09()
		{
			super();

			var obj: Sprite;
			
			while( 2 < Math.random() )
			{
				obj = new Sprite();
			}
			
			trace( obj );
		}
	}
}
