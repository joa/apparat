package  
{
	import com.joa_ebert.apparat.ApparatAsset;
	import com.joa_ebert.apparat.tests.test14.*;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test14 extends ApparatAsset 
	{
		public function Test14()
		{
			super();
			init();
		}
		
		private function init(): void
		{
			trace( Foo.bar() );

			var a: Vector2D = new Vector2D( 0.0, 1.0 );
			var b: Vector2D = new Vector2D( 1.0, 0.0 );
			var c: Vector2D = Vector2D.add2( a, b );

			trace( c );
			a.add( b );
			trace( a.toString() );
		}
	}
}

final class Vector2D
{
	public static function add2( a: Vector2D, b: Vector2D ): Vector2D
	{
		return new Vector2D( a.x + b.x, a.y + b.y );
	}
	
	private var _x: Number;
	private var _y: Number;
	
	public function Vector2D( x: Number, y: Number )
	{
		_x = x;
		_y = y;
	}
	
	public function add( other: Vector2D ): void
	{
		_x = addTwoValues( _x, other._x );
		_y = addTwoValues( _y, other._y );
	}

	private function addTwoValues( a: Number, b: Number ): Number
	{
		return a + b;
	}
		
	public function get x(): Number
	{
		return _x;
	}
	
	public function get y(): Number
	{
		return _y;
	}
	
	public function toString(): String
	{
		return '[Vector2D x: ' + x + ', y: ' + y + ']';
	}
}