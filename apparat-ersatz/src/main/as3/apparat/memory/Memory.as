/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.memory
{
	import flash.system.ApplicationDomain;
	import flash.utils.ByteArray;

	/**
	 * The Memory class is a high-level API for the Alchemy operations.
	 *
	 * <p>All calls to Memory work without running the optimizer since it
	 * implements the standard behaviour by default as well. This is of course
	 * slow.</p>
	 *
	 * <p>Calls to the Memory class will be inlined wherever possible. This
	 * means if you access the Memory class using a runtime namespace it will
	 * not be possible for the optimizer to inline the method call. In that
	 * case you will only benifit from the fact that the code inside the Memory
	 * class will be optimized using the Alchemy operations.</p>
	 *
	 * @author Joa Ebert
	 */
	public final class Memory {
		//
		// NOTE: This class works now only if the library has been processed
		// with TurboDieselSportInjection and alchemyExpansion turned on.
		//
		// The code might look completely wrong, but note that TDSI will replace
		// the calls like Memory.writeByte(address, value) with an Alchemy operation.
		// Therefore all methods are not recursive after processing the SWC with TDSI.
		//

		/**
		 * The current application domain.
		 * @private
		 */
		private static const applicationDomain: ApplicationDomain = ApplicationDomain.currentDomain

		/**
		 * Selects a ByteArray object as the current memory.
		 *
		 * @param byteArray The ByteArray object to work with.
		 */
		public static function select(byteArray: ByteArray): void {
			applicationDomain.domainMemory = byteArray
		}

		/**
		 * Writes a byte to the memory.
		 *
		 * @param address The address in memory.
		 * @param value The value to write.
		 */
		public static function writeByte(value: int, address: int): void {
			Memory.writeByte(value, address)
		}

		/**
		 * Writes a short to the memory.
		 *
		 * @param address The address in memory.
		 * @param value The value to write.
		 */
		public static function writeShort(value: int, address: int): void {
			Memory.writeShort(value, address)
		}

		/**
		 * Writes an integer to the memory.
		 *
		 * @param address The address in memory.
		 * @param value The value to write.
		 */
		public static function writeInt(value: int, address: int): void {
			Memory.writeInt(value, address)
		}

		/**
		 * Writes a float to the memory.
		 *
		 * @param address The address in memory.
		 * @param value The value to write.
		 */
		public static function writeFloat(value: Number, address: int): void {
			Memory.writeFloat(value, address)
		}

		/**
		 * Writes a double to the memory.
		 *
		 * @param address The address in memory.
		 * @param value The value to write.
		 */
		public static function writeDouble(value: Number, address: int): void {
			Memory.writeDouble(value, address)
		}

		/**
		 * Reads an unsigned byte from the memory.
		 *
		 * @param address The address in memory.
		 * @return Unsigned byte at given address in memory.
		 */
		public static function readUnsignedByte(address: int): int {
			return Memory.readUnsignedByte(address)
		}

		/**
		 * Reads an unsigned short from the memory.
		 *
		 * @param address The address in memory.
		 * @return Unsigned short at given address in memory.
		 */
		public static function readUnsignedShort(address: int): int {
			return Memory.readUnsignedShort(address)
		}

		/**
		 * Reads a signed integer from the memory.
		 *
		 * @param address The address in memory.
		 * @return Signed integer at given address in memory.
		 */
		public static function readInt(address: int): int {
			return Memory.readInt(address)
		}

		/**
		 * Reads a float from the memory.
		 *
		 * @param address The address in memory.
		 * @return Float at given address in memory.
		 */
		public static function readFloat(address: int): Number {
			return Memory.readFloat(address)
		}

		/**
		 * Reads a double from the memory.
		 *
		 * @param address The address in memory.
		 * @return Double at given address in memory.
		 */
		public static function readDouble(address: int): Number {
			return Memory.readDouble(address)
		}

		/**
		 * Sign-extension from 1 to 32 bits.
		 *
		 * @param value The value to extend.
		 * @return The extended value.
		 */
		public static function signExtend1(value: int): int {
			return Memory.signExtend1(value)
		}

		/**
		 * Sign-extension from 8 to 32 bits.
		 *
		 * @param value The value to extend.
		 * @return The extended value.
		 */
		public static function signExtend8(value: int): int {
			return Memory.signExtend8(value)
		}

		/**
		 * Sign-extension from 16 to 32 bits.
		 *
		 * @param value The value to extend.
		 * @return The extended value.
		 */
		public static function signExtend16(value: int): int {
			return Memory.signExtend16(value)
		}

		/**
		 * @private
		 */
		public function Memory() {
			throw new Error( 'Can not instantiate Memory object.' );
		}
	}
}
