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

package com.joa_ebert.apparat.swc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class Swc
{
	private static final String NAME_CATALOG = "catalog.xml";
	private static final String NAME_LIBRARY = "library.swf";

	private static byte[] extractBytes( final InputStream input, final long size )
			throws IOException
	{
		final byte[] result;

		if( -1 != size )
		{
			result = new byte[ (int)size ];

			int offset = 0;
			final int length = (int)size;

			while( offset < length )
			{
				offset += input.read( result, offset, length - offset );
			}
		}
		else
		{
			final ByteArrayOutputStream output = new ByteArrayOutputStream();

			final byte[] buffer = new byte[ 0x1000 ];
			int length;

			while( ( length = input.read( buffer ) ) >= 0 )
			{
				output.write( buffer, 0, length );
			}

			output.close();

			result = output.toByteArray();
		}

		return result;
	}

	public byte[] catalog;
	public byte[] library;

	public void read( final File file ) throws IOException, SwcException
	{
		read( new FileInputStream( file ) );
	}

	public void read( final InputStream input ) throws IOException,
			SwcException
	{
		read( new ZipInputStream( input ) );
	}

	public void read( final String pathname ) throws IOException, SwcException
	{
		read( new File( pathname ) );
	}

	public void read( final ZipInputStream input ) throws IOException,
			SwcException
	{
		ZipEntry zipEntry;

		while( null != ( zipEntry = input.getNextEntry() ) )
		{
			final String entryName = zipEntry.getName();
			final long size = zipEntry.getSize();

			if( zipEntry.isDirectory() )
			{
				throw new SwcException(
						"Unexpected directory entry in SWC file." );
			}

			if( entryName.equals( NAME_CATALOG ) )
			{
				if( null != catalog )
				{
					throw new SwcException( "Duplicate catalog entry." );
				}

				catalog = extractBytes( input, size );
			}
			else if( entryName.equals( NAME_LIBRARY ) )
			{
				if( null != library )
				{
					throw new SwcException( "Duplicate library entry." );
				}

				library = extractBytes( input, size );
			}

			input.closeEntry();
		}
	}

	public void write( final File file ) throws IOException, SwcException
	{
		final FileOutputStream output = new FileOutputStream( file );

		write( output );

		output.flush();
		output.close();
	}

	public void write( final OutputStream output ) throws IOException,
			SwcException
	{
		final ZipOutputStream zipOutput = new ZipOutputStream( output );

		zipOutput.setMethod( ZipOutputStream.DEFLATED );
		zipOutput.setLevel( Deflater.BEST_COMPRESSION );

		write( zipOutput );

		zipOutput.flush();
		zipOutput.close();
	}

	public void write( final ZipOutputStream output ) throws IOException,
			SwcException
	{
		if( null != catalog )
		{
			writeEntry( output, catalog, NAME_CATALOG );
		}
		else
		{
			throw new SwcException( "Catalog is missing." );
		}

		if( null != library )
		{
			writeEntry( output, library, NAME_LIBRARY );
		}
		else
		{
			throw new SwcException( "Library is missing." );
		}

		output.flush();
	}

	private void writeEntry( final ZipOutputStream output, final byte[] data,
			final String name ) throws IOException
	{
		final ZipEntry zipEntry = new ZipEntry( name );

		output.putNextEntry( zipEntry );
		output.write( data );
		output.closeEntry();
	}
}
