package  
{
	import com.joa_ebert.apparat.PerformanceDisplay;
	import com.joa_ebert.apparat.ApparatAsset;
	import com.joa_ebert.apparat.tests.test15.*;

 	import flash.events.Event;
 	import flash.display.*;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]

	/**
	 * @author Joa Ebert
	 */
	public class Test15 extends ApparatAsset 
	{
		private var _screen: BitmapData;
		private var _buffer: Vector.<uint>;
		private var _particles: Particle;
		
		private var _mouse: Vector2D;
		private var _position: Vector2D;

		private var _friction: Vector2D;
		private var _inertia: Vector2D;
		private var _t: Number;
		
		public function Test15()
		{
			super();
		
			init();
		}
		
		private function init(): void
		{
			var n: int = 10000;
			var start: Particle;

			_particles = start = createParticle();
			
			while( --n != 0 )
			{
				_particles = _particles.next = createParticle();
			}
			
			_particles = start;
			_t = 0.0;
			_position = new Vector2D( 0.0, 0.0 );
			_mouse = new Vector2D( 0.0, 0.0 );
			
			_friction = new Vector2D( 0.01, 0.01 );
			_inertia = new Vector2D( 0.6125, 0.6125 );
				
			_screen = new BitmapData( 256, 256, false, 0 );
			_buffer = new Vector.<uint>( 256 * 256, true );
			
			addChild( new Bitmap( _screen ) );
			addChild( new PerformanceDisplay( this ) );
				
			addEventListener( Event.ENTER_FRAME, onEnterFrame );
		}
		
		private function createParticle(): Particle
		{
			return new Particle(
				Math.random() * 256,
				Math.random() * 256
			);
		}
		
		private function setPixel( x: int, y: int, color: int ): void
		{
			if( x > 0 && x < 256 && y > 0 && y < 256 )
			{
				_buffer[ x + y * 256 ] = color;
			}
		}
		
		private function onEnterFrame( event: Event ): void
		{
			_mouse.x = stage.mouseX;
			_mouse.y = stage.mouseY;
			
			_mouse.sub( _position );
			_mouse.mul( _inertia );
			_position.add( _mouse );

			//var i: int = 256 * 256;
			//
			//while( --i > -1 )
			//{
			//	_buffer[ i ] = 0;
			//}
			
			_screen.fillRect( _screen.rect, 0 );			
			
			var current: Particle = _particles;
			var diff: Vector2D = new Vector2D( 0.0, 0.0 );
			var rotation: Vector2D = new Vector2D( 0.0, 0.0 );
			var cos: Number = Math.cos( _t );
			var sin: Number = Math.sin( _t );
			
			_t += 0.01;
			
			while( current )
			{
				_position.assignTo( diff );
				
				rotation.x = cos;
				rotation.y = sin;
				
				rotation.mul( current.jitter );
				
				diff.sub( current.pos );
				diff.mul( _inertia );
				
				current.vel.add( diff );
				current.vel.add( rotation );
				current.vel.mul( _friction );

				current.pos.add( current.vel );				
				
				_screen.setPixel( current.pos.x, current.pos.y, 0xffffff );
				//setPixel( current.pos.x, current.pos.y, 0xffffff );	
				
				current = current.next;
			}
			
			//_screen.lock();
			//_screen.setVector( _screen.rect, _buffer );
			//_screen.unlock( _screen.rect );
		}
	}
}