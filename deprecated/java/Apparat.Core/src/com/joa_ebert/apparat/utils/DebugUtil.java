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

import java.io.PrintStream;

/**
 * The DebugUtil class implements methods useful for debugging.
 * 
 * @author Joa Ebert
 * 
 */
public final class DebugUtil
{
	/**
	 * Pretty-prints the content of a byte array to the default System output
	 * stream.
	 * 
	 * @param buffer
	 *            The buffer of bytes to print.
	 */
	public static void hexDump( final byte[] buffer )
	{
		hexDump( buffer, System.out );
	}

	/**
	 * Pretty-prints the content of a byte array to a given PrintStream.
	 * 
	 * @param buffer
	 *            The buffer of bytes to print.
	 * @param printStream
	 *            The PrintStream to use.
	 */
	public static void hexDump( final byte[] buffer,
			final PrintStream printStream )
	{
		if( null == buffer )
		{
			System.out.println( "Hex dump: (NULL)\n" );
			return;
		}

		final int bufferLength = buffer.length;
		int currentByte;

		final StringBuilder output = new StringBuilder( buffer.length << 2 );

		for( int i = 0; i < bufferLength; i += 0x10 )
		{
			String hexString = Integer.toHexString( i );
			int padding = 8 - hexString.length();

			while( --padding > -1 )
			{
				output.append( '0' );
			}

			output.append( hexString );
			output.append( "h " );

			final StringBuilder text = new StringBuilder( 0x10 );

			for( int j = 0; j < 0x10; ++j )
			{
				final int bufferIndex = i + j;

				if( bufferIndex >= bufferLength )
				{
					output.append( "   " );
					text.append( " " );
				}
				else
				{
					currentByte = buffer[ bufferIndex ] & 0xff;
					hexString = Integer.toHexString( currentByte );

					if( hexString.length() != 2 )
					{
						output.append( '0' );
					}

					output.append( hexString );
					output.append( ' ' );

					text
							.append( ( currentByte > 0x20 && currentByte < 0x7f ) ? (char)currentByte
									: "." );
				}

				if( ( j & 0x03 ) == 0x03 && 0x0f != j )
				{
					output.append( "| " );
				}
			}

			output.append( ' ' );
			output.append( text );
			output.append( '\n' );
		}

		output.append( "Length: " + buffer.length + " bytes" );

		printStream.println( "Hex dump:" );
		printStream.println( output.toString() );
		printStream.flush();
	}
}
