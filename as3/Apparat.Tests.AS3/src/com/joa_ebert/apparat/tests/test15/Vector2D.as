package com.joa_ebert.apparat.tests.test15
{
	public final class Vector2D
	{
		public var x: Number;
		public var y: Number;
		
		public function Vector2D( x: Number, y: Number )
		{
			this.x = x;
			this.y = y;
		}
		
		public function assignTo( other: Vector2D ): void
		{
			other.x = x;
			other.y = y;
		}
		
		public function add( other: Vector2D ): void
		{
			x += other.x;
			y += other.y;
		}

		public function sub( other: Vector2D ): void
		{
			x -= other.x;
			y -= other.y;
		}
				
		public function mul( other: Vector2D ): void
		{
			x *= other.x;
			y *= other.y;
		}
		
		public function div( other: Vector2D ): void
		{
			x /= other.x;
			y /= other.y;
		}
		
		public function length(): Number
		{
			return Math.sqrt( x * x + y * y );
		}
	}
}