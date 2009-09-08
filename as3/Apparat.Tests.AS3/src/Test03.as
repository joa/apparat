package  
{
	import com.joa_ebert.apparat.ApparatAsset;
	import com.joa_ebert.apparat.PerformanceDisplay;

	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.events.Event;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test03 extends ApparatAsset 
	{
		private var pixels: Vector.<uint>;
		private var screen: BitmapData;
		private var t: int = 0;
		private var b: Boolean = true;
		
		public function Test03()
		{
			super();
			init();
		}
		
		private function init(): void
		{
			pixels = new Vector.<uint>( 0x100 * 0x100, true );
			screen = new BitmapData( 0x100, 0x100, false, 0 );
			
			addChild( new Bitmap( screen ) );
			addChild( new PerformanceDisplay( this ) );
			
			addEventListener(Event.ENTER_FRAME, onEnterFrame );
		}
		
		private function onEnterFrame( event: Event ): void
		{
			for( var y: int = 0; y < 0x100; y++ )
			{
				for( var x: int = 0; x < 0x100; ++x )
				{
					var color: int = x ^ y;
				
					setPixel( x, y, getRGB( x, y, color - t ) );			
				}
			}
			
			if( b )
				t += 1;
			else
				t -= 1;
				
			if( t == 0x100 )
			{
				t = 0xff;
				b = !b;
			}
			else if( t == -1 )
			{
				t = 0x00;
				b = !b;
			}
			
			screen.lock();
			screen.setVector( screen.rect, pixels );
			screen.unlock();
		}
		
		private function setPixel( x: int, y: int, color: int ): void
		{
			pixels[ y * 0x100 + x ] = color;
		}
		
		private function getRGB( r: int, g: int, b: int ): int
		{
			return ( r << 0x10 ) |  ( g << 0x08 ) | b;
		}
	}
}
