package com.joa_ebert.apparat 
{
	import flash.events.Event;
	import flash.events.IEventDispatcher;
	import flash.system.System;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.text.TextFormat;
	import flash.utils.getTimer;

	/**
	 * @author Joa Ebert
	 */
	public final class PerformanceDisplay extends TextField 
	{
		private var fps: int;
		private var fpsLast: int;
		private var fpsTime: int;
		
		private var lastTime: int;
		
		public function PerformanceDisplay( eventDispatcher: IEventDispatcher )
		{
			autoSize = TextFieldAutoSize.LEFT;
			background = true;
			backgroundColor = 0;
			selectable = false;
			multiline = true;
			wordWrap = false;
			background = true;
			embedFonts = false;
			defaultTextFormat = new TextFormat( "arial", 9, 0xffffff );

			eventDispatcher.addEventListener( Event.ENTER_FRAME, onEnterFrame );
		}
		
		private function onEnterFrame( event: Event ): void
		{
			var time: int = getTimer();
			
			++fps;
			
			if( time - fpsTime >= 1000 )
			{
				fpsTime = getTimer( );
				fpsLast = fps;
				fps = 0;
			}
			
			text = "fps: " + fpsLast.toString() + "\nmem: " + ( System.totalMemory >> 10 ) + "\nms: " + ( time - lastTime );
			lastTime = time;
		}
	}
}
