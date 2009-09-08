package  
{
	import com.joa_ebert.apparat.ApparatAsset;

	[SWF(width='256',height='256',frameRate='255',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public class Test11 extends ApparatAsset 
	{
		public function Test11()
		{
			super();

			var a: int = -1234;
			var b: int = 1234;
			var c: uint = 1234;
			var d: uint = 1234;
			var e: Number = -1.234;
			var f: Number = 1.234;
			var g: Boolean = true;
			var h: Boolean = false;

			//
			// int op int
			//
			
			trace( ~a );
			trace( a * b );
			trace( a / b );
			trace( a + b );
			trace( a - b );
			trace( a % b );
			trace( a & b );
			trace( a | b );
			trace( a ^ b );
			trace( a >> b );
			trace( a << b );
			trace( a >>> b );
			trace( a > b );
			trace( a < b );
			trace( a == b );
			trace( a != b );
			trace( a >= b );
			trace( a <= b );
			trace( a === b );
			trace( a !== b );
			
			//
			// uint op uint
			//
			
			trace( ~c );
			trace( c * d );
			trace( c / d );
			trace( c + d );
			trace( c - d );
			trace( c % d );
			trace( c & d );
			trace( c | d );
			trace( c ^ d );
			trace( c >> d );
			trace( c << d );
			trace( c >>> d );
			trace( c > d );
			trace( c < d );
			trace( c == d );
			trace( c != d );
			trace( c >= d );
			trace( c <= d );
			trace( c === d );
			trace( c !== d );
			
			//
			// uint op int
			//
			
			trace( c * a );
			trace( c / a );
			trace( c + a );
			trace( c - a );
			trace( c % a );
			trace( c & a );
			trace( c | a );
			trace( c ^ a );
			trace( c >> a );
			trace( c << a );
			trace( c >>> a );
			trace( c > a );
			trace( c < a );
			trace( c == a );
			trace( c != a );
			trace( c >= a );
			trace( c <= a );
			trace( c === a );
			trace( c !== a );
			
			//
			// Number op Number
			//
			
			trace( ~e );
			trace( e * f );
			trace( e / f );
			trace( e + f );
			trace( e - f );
			trace( e % f );
			trace( e & f );
			trace( e | f );
			trace( e ^ f );
			trace( e >> f );
			trace( e << f );
			trace( e >>> f );
			trace( e > f );
			trace( e < f );
			trace( e == f );
			trace( e != f );
			trace( e >= f );
			trace( e <= f );
			trace( e === f );
			trace( e !== f );
			
			
			//
			// Number op int
			//
			
			trace( e * a );
			trace( e / a );
			trace( e + a );
			trace( e - a );
			trace( e % a );
			trace( e & a );
			trace( e | a );
			trace( e ^ a );
			trace( e >> a );
			trace( e << a );
			trace( e >>> a );
			trace( e > a );
			trace( e < a );
			trace( e == a );
			trace( e != a );
			trace( e >= a );
			trace( e <= a );
			trace( e === a );
			trace( e !== a );
	
			//
			// Number op uint
			//
			
			trace( e * c );
			trace( e / c );
			trace( e + c );
			trace( e - c );
			trace( e % c );
			trace( e & c );
			trace( e | c );
			trace( e ^ c );
			trace( e >> c );
			trace( e << c );
			trace( e >>> c );
			trace( e > c );
			trace( e < c );
			trace( e == c );
			trace( e != c );
			trace( e >= c );
			trace( e <= c );
			trace( e === c );
			trace( e !== c );
			trace( 1.0 / 3.0 );
			
			// Boolean op Boolean
			
			trace( !g );
			trace( g == h );
			trace( g != h );
			trace( g === h );
			trace( g !== h );
		}
	}
}
