package com.joa_ebert.apparat.tests.test15
{
	public final class Particle
	{
		public var vel: Vector2D;
		public var pos: Vector2D;
		
		public var jitter: Vector2D;
		
		public var next: Particle;
		
		public function Particle( x: Number, y: Number )
		{
			vel = new Vector2D(
				Math.random() - Math.random(),
				Math.random() - Math.random()
			);
			
			pos = new Vector2D( x, y );
			
			jitter = new Vector2D(
				Math.random() * 100.0,
				Math.random() * 100.0
			);
		}
	}
}