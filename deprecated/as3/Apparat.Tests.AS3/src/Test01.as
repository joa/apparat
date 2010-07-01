package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	/**
	 * @author Joa Ebert
	 */
	public class Test01 extends ApparatAsset
	{
		public function Test01()
		{
			var x: Number;
			
			if( ( Math.random() + 0.5 ) >= 0.5 )
			{
				x = 1.0;
			}
			else
			{
				x = 0.0;
			}
			
			trace( x );
		}
	}
}
