	
	TurboDieselSportInjection

	
	NOTE: You will need at least Java 1.6 SE to run this tool.

	A tool to use the Alchemy opcodes for Flash.
	By default all common options are set. You should just do
	
		java -jar tdsi.jar -input old.swf -output new.swf


	You can specify to use integer calculus (AddInt,MultiplyInt etc.) by
	providing the option "-integer-calculus true".
	The integer calculus is a search-and-replace method which is of course
	error prone.

	Example:

		PushDouble 1.9
		PushDouble 1000.0
		Multiply
		ConvertInt

	In this case the integer calculus would create:

		PushDouble 1.9
		PushDouble 1000.0
		MultiplyInt

	This is of course wrong since the multiplication has to run in the
	floating point space. A difference of 900.0 is the result.
	
	All other bytecode permutations are safe to use and not destructive.
	They are turned on by default.


	The Memory API works in a way that you first call Memory.select() with a
	ByteArray that has a minimum length of 1024 bytes. After that ByteArray
	has been selected all calls to Memory will use that ByteArray.

	To use the Memory API simply copy the contents of the "as3" directory into
	your project and run TDSI afterwards.


	Inverse square root example:

	public function prepare(): void
	{
    		_b = new ByteArray();
    		_b.length = 1024;
		Memory.select( _b );
	}

	private function invSqrt( value: Number ): Number
	{
		var half: Number = 0.5 * value;
		Memory.writeFloat( value, 0 );
		Memory.writeInt( 0x5f3759df - ( Memory.readInt( 0 ) >> 1 ), 0 );   		
		value = Memory.readFloat( 0 );
		value = value * ( 1.5 - half * value * value );
		return value;			
	}	

	

	Best,

		Joa
		http://www.joa-ebert.com/