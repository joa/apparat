/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.utils;

/**
 * @author Joa Ebert
 * 
 */
public class HexUtil
{
	public static String toString( final byte[] input )
	{
		if( null == input )
		{
			return "(null)";
		}

		final StringBuilder resultBuffer = new StringBuilder();
		final int length = input.length;

		for( int index = 0; index < length; ++index )
		{
			final int value = input[ index ] & 0xff;

			if( value < 0x10 )
			{
				resultBuffer.append( '0' );
			}

			resultBuffer.append( Integer.toHexString( value ) );
		}

		return resultBuffer.toString();
	}
}
