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

public class DebugUtil
{
	public static void hexDump( final byte[] buffer )
	{
		hexDump( buffer, System.out );
	}

	public static void hexDump( final byte[] buffer,
			final PrintStream printStream )
	{
		if( null == buffer )
		{
			System.out.println( "Hex dump: (NULL)\n" );
			return;
		}

		int p1;

		int i = 0;
		int j;
		int n;

		final StringBuilder output = new StringBuilder( buffer.length * 4 );

		int byte_;

		n = buffer.length;

		final PrintfFormat hexPosFormat = new PrintfFormat( "%08Xh " );
		final PrintfFormat hexByteFormat = new PrintfFormat( "%02X " );

		for( ; i < n; i += 0x10 )
		{
			output.append( hexPosFormat.sprintf( i ) );

			final StringBuilder bytes = new StringBuilder( " " );
			final StringBuilder text = new StringBuilder( 0x10 );

			for( j = 0; j < 0x10; ++j )
			{
				p1 = i + j;

				if( p1 >= n )
				{
					bytes.append( "   " );
					text.append( " " );
				}
				else
				{
					byte_ = buffer[ p1 ];

					if( byte_ < 0 )
					{
						byte_ += 0x100;
					}

					bytes.append( hexByteFormat.sprintf( byte_ ) );
					text.append( ( byte_ > 0x20 && byte_ < 0x7f ) ? (char)byte_
							: "." );
				}

				if( ( j & 0x03 ) == 0x03 && 0x0f != j )
				{
					bytes.append( "| " );
				}
			}

			output.append( bytes );
			output.append( ' ' );
			output.append( text );
			output.append( '\n' );
		}

		output.append( "len: " + buffer.length );

		printStream.println( "Hex dump:" );
		printStream.println( output.toString() );
		printStream.flush();
	}
}
