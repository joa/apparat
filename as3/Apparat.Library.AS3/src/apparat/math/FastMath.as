package apparat.math {
	import apparat.inline.Inlined;
	import apparat.memory.Memory;

	import flash.system.ApplicationDomain;
	import flash.utils.ByteArray;

	/**
	 * The FastMath class defines fast functions to work with Numbers.
	 *
	 * FastMath functions are inlined by Apparat. Trigonometric functions
	 * are only approximations. However all approximations should have an error
	 * less than <code>0.008</code>.
	 *
	 * @author Joa Ebert
	 */
	public final class FastMath extends Inlined {
		/**
		 * Initializes the Alchemy memory with the minimum required capacity.
		 *
		 * <p><b>Note:</b> You do not have to call this method if you are using the MemoryPool
		 * class. However the default MemoryPool will allocated much more space.</p>
		 */
		public static function initMemory(): void {
			var byteArray: ByteArray = new ByteArray()
			byteArray.length = ApplicationDomain.MIN_DOMAIN_MEMORY_LENGTH
			
			Memory.select(byteArray)
		}

		/**
		 * Computes and returns the sine of the specified angle in radians.
		 *
		 * To calculate a radian, see the overview of the Math class.
		 *
		 * This method is only a fast sine approximation.
		 *
		 * @param angleRadians A number that represents an angle measured in radians.
		 * @return A number from -1.0 to 1.0.
		 */
		public static function sin(angleRadians: Number): Number {
			//
			// http://lab.polygonal.de/wp-content/articles/fast_trig/fastTrig.as
			//

			if(angleRadians < -3.14159265) {
				angleRadians += 6.28318531
			} else if(angleRadians > 3.14159265) {
				angleRadians -= 6.28318531
			}

			angleRadians = (angleRadians < 0.0) ? (1.27323954 * angleRadians + .405284735 * angleRadians * angleRadians) : (1.27323954 * angleRadians - 0.405284735 * angleRadians * angleRadians)
			return (angleRadians < 0.0) ? (0.225 * (angleRadians *-angleRadians - angleRadians) + angleRadians) : (0.225 * (angleRadians * angleRadians - angleRadians) + angleRadians)
		}

		/**
		 * Computes and returns the cosine of the specified angle in radians.
		 *
		 * To calculate a radian, see the overview of the Math class.
		 *
		 * This method is only a fast cosine approximation.
		 *
		 * @param angleRadians A number that represents an angle measured in radians.
		 * @return A number from -1.0 to 1.0.
		 */
		public static function cos(angleRadians: Number): Number {
			//
			// http://lab.polygonal.de/wp-content/articles/fast_trig/fastTrig.as
			//

			if(angleRadians < -3.14159265) {
				angleRadians += 6.28318531
			} else if(angleRadians > 3.14159265) {
				angleRadians -= 6.28318531
			}

			angleRadians += 1.57079632;
			if(angleRadians > 3.14159265) {
				angleRadians -= 6.28318531
			}

			angleRadians = (angleRadians < 0.0) ? (1.27323954 * angleRadians + .405284735 * angleRadians * angleRadians) : (1.27323954 * angleRadians - 0.405284735 * angleRadians * angleRadians)
			return (angleRadians < 0.0) ? (0.225 * (angleRadians *-angleRadians - angleRadians) + angleRadians) : (0.225 * (angleRadians * angleRadians - angleRadians) + angleRadians)
		}

		/**
		 * Computes and returns an absolute value.
		 *
		 * @param value The number whose absolute value is returned.
		 * @return The absolute value of the specified parameter.
		 */
		public static function abs(value: Number): Number {
			return (value > 0.0) ? value : -value
		}

		/**
		 * Returns the smallest value of the given parameters.
		 *
		 * @param value0 A number.
		 * @param value1 A number.
		 * @return The smallest of the parameters <code>value0</code> and <code>value1</code>.
		 */
		public static function min(value0: Number, value1: Number): Number {
			return (value0 < value1) ? value0 : value1
		}

		/**
		 * Returns the largest value of the given parameters.
		 *
		 * @param value0 A number.
		 * @param value1 A number.
		 * @return The largest of the parameters <code>value0</code> and <code>value1</code>.
		 */
		public static function max(value0: Number, value1: Number): Number {
			return (value0 > value1) ? value0 : value1
		}

		/**
		 * Computes and returns the square root of the specified number.
		 *
		 * <p><b>Note:</b>Calling this function will overwrite the first
		 * four bytes of the ApplicationDomain.domainMemory ByteArray. It is
		 * required that such a ByteArray exists.</p>
		 *
		 * @param value A number or expression greater than or equal to 0.
		 * @see initMemory
		 * @return If the parameter val is greater than or equal to zero, a number; otherwise NaN (not a number).
		 * @throws TypeError If no <code>ApplicationDomain.domainMemory</code> has been set.
		 */
		public static function sqrt(value: Number): Number {
			if(value == 0.0) {
				return 0.0
			} else if(value < 0.0) {
				return Number.NaN
			}

			var originalValue: Number = value
			var halfValue: Number = value * 0.5

			Memory.writeFloat(value, 0)
			var i: int = 0x5f3759df - (Memory.readInt(0) >> 1)
			Memory.writeInt(i, 0)
			value = Memory.readFloat(0)

			return originalValue * value * (1.5 - halfValue * value * value)
		}

		/**
		 * Computes and returns the reciprocal of the square root for the specified number.
		 *
		 * <p><b>Note:</b>Calling this function will overwrite the first
		 * four bytes of the ApplicationDomain.domainMemory ByteArray. It is
		 * required that such a ByteArray exists.</p>
		 *
		 * @param value A number or expression greater than or equal to 0.
		 * @return If the parameter val is greater than or equal to zero, a number; otherwise NaN (not a number).
		 * @see initMemory
		 * @throws TypeError If no <code>ApplicationDomain.domainMemory</code> has been set.
		 */
		public static function rsqrt(value: Number): Number {
			if(value == 0.0) {
				return 0.0
			} else if(value < 0.0) {
				return Number.NaN
			}
			
			var halfValue: Number = value * 0.5

			Memory.writeFloat(value, 0)
			var i: int = 0x5f3759df - (Memory.readInt(0) >> 1)
			Memory.writeInt(i, 0)
			value = Memory.readFloat(0)

			return value * (1.5 - halfValue * value * value)
  		}

		/**
		 * Computes and returns the square root of the specified number.
		 *
		 * The address parameter should be a pointer to a <code>char[4]</code> in 
		 * the Alchemy memory buffer.
		 * 
		 * @param value A number or expression greater than or equal to 0.
		 * @param address The address in the Alchemy memory buffer.
		 * @return If the parameter val is greater than or equal to zero, a number; otherwise NaN (not a number).
		 * @throws TypeError If no <code>ApplicationDomain.domainMemory</code> has been set.
		 */
		public static function sqrt2(value: Number, address: int): Number {
			if(value == 0.0) {
				return 0.0
			} else if(value < 0.0) {
				return Number.NaN
			}

			var originalValue: Number = value
			var halfValue: Number = value * 0.5

			Memory.writeFloat(value, address)
			var i: int = 0x5f3759df - (Memory.readInt(address) >> 1)
			Memory.writeInt(i, address)
			value = Memory.readFloat(address)

			return originalValue * value * (1.5 - halfValue * value * value)
		}

		/**
		 * Computes and returns the reciprocal of the square root for the specified number.
		 *
		 * The address parameter should be a pointer to a <code>char[4]</code> in 
		 * the Alchemy memory buffer.
		 * 
		 * @param value A number or expression greater than or equal to 0.
		 * @param address The address in the Alchemy memory buffer.
		 * @return If the parameter val is greater than or equal to zero, a number; otherwise NaN (not a number).
		 * @see initMemory
		 * @throws TypeError If no <code>ApplicationDomain.domainMemory</code> has been set.
		 */
		public static function rsqrt2(value: Number, address: int): Number {
			if(value == 0.0) {
				return 0.0
			} else if(value < 0.0) {
				return Number.NaN
			}
			
			var halfValue: Number = value * 0.5

			Memory.writeFloat(value, address)
			var i: int = 0x5f3759df - (Memory.readInt(address) >> 1)
			Memory.writeInt(i, address)
			value = Memory.readFloat(address)

			return value * (1.5 - halfValue * value * value)
  		}
		
		/**
		 * Integer cast with respect to its sign.
		 * 
		 * @param value A number.
		 * @return The number casted to an integer with respect to its sign.
		 */
		public static function rint(value: Number): int {
			if(value > 0.0) {
				return value + 0.5
			} else if(value < 0.0) {
				return -int(-value + 0.5)
			} else {
				return 0
			}
		}
	}
}