package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test05 extends ApparatAsset 
	{
		public function Test05()
		{
			super();
			
			var t: Array = new Array();
			
			t[ 0 ] = 1;
			
			trace( t[ 0 ] );
		}
	}
}
