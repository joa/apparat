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
	import flash.utils.ByteArray;
	import flash.utils.Endian;

	/**
	 * The ImmutableByteArray class represents a ByteArray whose endian and
	 * length can not be changed.
	 *
	 * @private
	 *
	 * @author Joa Ebert
	 */
	internal final class ImmutableByteArray extends ByteArray
	{
		public function ImmutableByteArray( length: uint )
		{
			super();

			super.length = length;
			super.endian = Endian.LITTLE_ENDIAN;
		}


		override public function set endian( type : String ) : void
		{
			throw new Error( 'You are not allowed to change the endian.' );
		}

		override public function set length( value : uint ) : void
		{
			throw new Error( 'You are not allowed to change the length.' );
		}
	}
}
