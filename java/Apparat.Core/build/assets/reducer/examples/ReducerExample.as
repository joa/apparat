package  
{
	import flash.display.Graphics;
	import flash.display.Shape;
	import flash.display.Sprite;
	import flash.display.StageQuality;
	import flash.display.StageScaleMode;
	import flash.events.Event;
	import flash.geom.Rectangle;

	[SWF(width='800',height='600',frameRate='32',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public final class ReducerExample extends Sprite 
	{
		[Embed(source='picture.png')]
		private static const BITMAP_ASSET: Class;
		
		private var _phase: Number = 0.0;
		private var _shape: Shape;
		
		public function ReducerExample()
		{
			stage.quality = StageQuality.LOW;
			stage.scaleMode = StageScaleMode.NO_SCALE;
			stage.fullScreenSourceRect = new Rectangle( 0.0, 0.0, 800.0, 600.0 );
			stage.frameRate = 32.0;

			addChild( _shape = new Shape() );
			addChild( new BITMAP_ASSET() );
			
			addEventListener(Event.ENTER_FRAME, onEnterFrame);
		}
		
		private function onEnterFrame( event: Event ): void
		{
			var graphics: Graphics = _shape.graphics;
			
			_phase += 0.01;
			
			if( _phase > 1.0 )
				--_phase;
				
			var color: int = Math.sin( _phase * Math.PI ) * 0xff;
			
			color |= color << 0x08;
			color |= color << 0x08;

			graphics.clear();
			graphics.beginFill( color );
			graphics.drawRect( 0.0, 0.0, 800.0, 600.0 );
			graphics.endFill();
		}
	}
}
