package apparat.math {
	import apparat.inline.Inlined;

	/**
	 * The IntMath class defines fast functions for integer calculus.
	 *
	 * IntMath functions are inlined by Apparat. All functions are working
	 * without any branching operations.
	 *
	 * @author Joa Ebert
	 */
	public final class IntMath extends Inlined {
		/**
		 * Combines three color values into a 4-component integer of the
		 * format 0xAARRGGBB.
		 *
		 * @param alpha The alpha component of the color in the range from 0 to 255.
		 * @param red The red component of the color in the range from 0 to 255.
		 * @param green The green component of the color in the range from 0 to 255.
		 * @param blue The blue component of the color in the range from 0 to 255.
		 * @return An unsigned integer of the format 0xAARRGGBB.
		 */
		public static function toARGB(alpha: int, red: int, green: int, blue: int): uint {
			return (alpha << 0x18) | (red << 0x10) | (green << 0x08) | blue
		}

		/**
		 * Combines three color values into a 3-component integer of the
		 * format 0xRRGGBB.
		 *
		 * @param red The red component of the color in the range from 0 to 255.
		 * @param green The green component of the color in the range from 0 to 255.
		 * @param blue The blue component of the color in the range from 0 to 255.
		 * @return An integer of the format 0xRRGGBB.
		 */
		public static function toRGB(red: int, green: int, blue: int): int {
			return (red << 0x10) | (green << 0x08) | blue
		}

		/**
		 * Returns whether or not the given parameter is even.
		 *
		 * @param value An integer.
		 * @return <code>true</code> if the given parameter is even; <code>false</code> otherwise.
		 * @see isOdd
		 */
		public static function isEven(value: int): Boolean {
			return 0 == (value & 1)
		}

		/**
		 * Returns whether or not the given parameter is odd.
		 *
		 * @param value An integer.
		 * @return <code>true</code> if the given parameter is odd; <code>false</code> otherwise.
		 * @see isEven
		 */
		public static function isOdd(value: int): Boolean {
			return 1 == (value & 1)
		}

		/**
		 * Computes and returns an absolute value.
		 *
		 * @param value The integer whose absolute value is returned.
		 * @return The absolute value of the specified parameter.
		 */
		public static function abs(value: int): int {
			return (value ^ (value >> 31)) - (value >> 31)
		}

		/**
		 * Returns the smallest value of the given parameters.
		 *
		 * @param value0 An integer.
		 * @param value1 An integer.
		 * @return The smallest of the parameters <code>value0</code> and <code>value1</code>.
		 */
		public static function min(value0: int, value1: int):int{
			return value1 ^ ((value0 ^ value1) & -int(value0 < value1))
		}

		/**
		 * Returns the largest value of the given parameters.
		 *
		 * @param value0 An integer.
		 * @param value1 An integer.
		 * @return The largest of the parameters <code>value0</code> and <code>value1</code>.
		 */
		public static function max(value0: int, value1: int): int {
			return value0 ^ ((value0 ^ value1) & -int(value0 < value1))
		}

		/**
		 * Tests whether or not two values have an equal sign.
		 *
		 * @param value0 An integer.
		 * @param value1 An integer.
		 * @return <code>true</code> if both values have the same sign; <code>false</code> otherwise.
		 * @see unequalSign
		 */
		public static function equalSign(value0: int, value1: int): Boolean {
			return (value0 ^ value1) >= 0
		}

		/**
		 * Tests whether or not two values have an unequal sign.
		 *
		 * @param value0 An integer.
		 * @param value1 An integer.
		 * @return <code>true</code> if both values do not have the same sign; <code>false</code> otherwise.
		 * @see equalSign
		 */
		public static function unequalSign(value0: int, value1: int): Boolean {
			return (value0 ^ value1) < 0
		}

		/**
		 * Returns the sign of an integer. +1 for any value
		 * greater zero, 0 for the values equal to 0, -1 for 
		 * any value less than zero.
		 *
		 * @param value An integer.
		 * @return +1 if <code>value &gt; 0</code>; 0 if <code>value == 0</code>; -1 if <code>value &lt; 0</code>.
		 */
		public static function sign(value: int): int {
			return int(value > 0) - int(value < 0)
		}

		/**
		 * Tests whether or not a given value is a power of two.
		 *
		 * @param value An unsigned integer.
		 * @return <code>true</code> if the value is a power of two; <code>false.
		 * </code> otherwise.
		 */
		public static function isPow2(value: int): Boolean {
			return value && (0 == (value & (value - 1)))
		}

		/**
		 * Rounds a value up to the next power of two.
		 *
		 * @param value An unsigned integer.
		 * @return The next power of two. The same value if it is already a
		 * power of two.
		 */
		public static function nextPow2(value: uint): uint
		{
			--value
			value |= value >>> 0x01
			value |= value >>> 0x02
			value |= value >>> 0x04
			value |= value >>> 0x08
			value |= value >>> 0x10
			++value

			return value
		}

		/**
		 * Finds the most significant bit for a value that is a power of two.
		 *
		 * @param value A value that is a power of two.
		 * @return The logarithmus dualis for the given value.
		 */
		public static function msbOfPow2( value: uint ): uint
		{
			var result: uint

			result  = ( value & 0xaaaaaaaa) != 0 ? 1 : 0
			result |= ((value & 0xffff0000) != 0 ? 1 : 0) << 4
			result |= ((value & 0xff00ff00) != 0 ? 1 : 0) << 3
			result |= ((value & 0xf0f0f0f0) != 0 ? 1 : 0) << 2
			result |= ((value & 0xcccccccc) != 0 ? 1 : 0) << 1

			return result
		}
	}
}