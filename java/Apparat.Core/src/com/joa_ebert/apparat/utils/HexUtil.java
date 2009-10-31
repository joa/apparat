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

	public static String toString( final int value )
	{
		final StringBuffer sb = new StringBuffer( "0x" ).append( Integer
				.toHexString( value ) );
		return sb.toString();
	}

	public static String toString( final int value, final int length )
	{
		final StringBuffer sb = new StringBuffer( Integer.toHexString( value ) );
		while( sb.length() < length )
		{
			sb.insert( 0, '0' );
		}
		sb.insert( 0, "0x" );
		return sb.toString();
	}

	public static String toString( final Long value )
	{
		final StringBuffer sb = new StringBuffer( "0x" ).append(
				Long.toHexString( value ) ).append( "L" );
		return sb.toString();
	}

	public static String toString( final Long value, final int length )
	{
		final StringBuffer sb = new StringBuffer( Long.toHexString( value ) );
		while( sb.length() < length )
		{
			sb.insert( 0, '0' );
		}
		sb.insert( 0, "0x" );
		sb.append( "L" );
		return sb.toString();
	}
}
