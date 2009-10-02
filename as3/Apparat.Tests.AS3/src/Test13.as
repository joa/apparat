package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test13 extends ApparatAsset 
	{
		public function Test13()
		{
			super();

			for( var i: uint = 0; i < 10; i++ )
			{
				trace( 'uint', i );
			}
		}
	}
}
