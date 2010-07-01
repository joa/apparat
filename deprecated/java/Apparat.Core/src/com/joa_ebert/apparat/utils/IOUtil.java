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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * The IOUtil class is a utility to perform common tasks when working with
 * bytes.
 * 
 * @author Joa Ebert
 * 
 */
public final class IOUtil
{
	/**
	 * Deflate input data using ZLib compression standard.
	 * 
	 * @param input
	 *            The data to compress.
	 * 
	 * @throws IOException
	 *             If the compression fails.
	 */
	public static byte[] compress( final byte[] input ) throws IOException
	{
		final Deflater deflater = new Deflater( Deflater.BEST_COMPRESSION );

		deflater.setInput( input );
		deflater.finish();

		final byte[] buffer = new byte[ 0x400 ];
		int numBytesCompressed = 0;

		//
		// Is this actually a smart guess?
		//

		final ByteArrayOutputStream output = new ByteArrayOutputStream(
				input.length >> 4 );

		do
		{
			numBytesCompressed = deflater.deflate( buffer );
			output.write( buffer, 0, numBytesCompressed );
		}
		while( 0 != numBytesCompressed );

		output.flush();
		output.close();

		return output.toByteArray();
	}

	/**
	 * Inflate input into output using ZLib compression standard.
	 * 
	 * @param input
	 *            The ZLib compressed data.
	 * @param output
	 *            The desired output.
	 * 
	 * @throws DataFormatException
	 *             If the inflation process fails.
	 */
	public static void decompress( final byte[] input, final byte[] output )
			throws DataFormatException
	{
		final Inflater inflater = new Inflater();

		inflater.setInput( input );

		int offset = 0;

		while( !inflater.finished() )
		{
			offset = inflater.inflate( output );

			if( 0 == offset )
			{
				if( inflater.needsInput() )
				{
					throw new DataFormatException( "More input required." );
				}
				else
				{
					break;
				}
			}
		}
	}

	private IOUtil()
	{

	}
}
