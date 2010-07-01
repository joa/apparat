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

package com.joa_ebert.apparat.swf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.joa_ebert.apparat.swf.io.RECT;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.FileAttributesTag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Swf
{
	private static final int UNCOMPRESSED_HEADER_BYTES = 8;

	public boolean isCompressed;
	public int version;
	public RECT frameSize;
	public float frameRate;
	public int frameCount;
	public List<ITag> tags;

	public Swf()
	{
		isCompressed = true;

		version = 10;

		frameSize = new RECT();

		frameSize.minX = 0;
		frameSize.maxX = 320 * 20;
		frameSize.minY = 0;
		frameSize.maxY = 240 * 20;

		frameRate = 32.0f;
		frameCount = 1;

		tags = new LinkedList<ITag>();
	}

	public void accept( final ISwfVisitor visitor )
	{
		visitor.visit( this );

		accept( (ITagVisitor)visitor );
	}

	public void accept( final ITagVisitor visitor )
	{
		if( null != tags )
		{
			for( final ITag tag : tags )
			{
				tag.accept( visitor );
			}
		}
	}

	private SwfInputStream getDecompressedInputStream( final InputStream input,
			final long inputLength, final long fileLength ) throws IOException,
			DataFormatException
	{
		// TODO fix me

		final int totalBytes = (int)inputLength - UNCOMPRESSED_HEADER_BYTES;
		final Inflater inflater = new Inflater();

		final byte[] bufferIn = new byte[ totalBytes ];
		final byte[] bufferOut = new byte[ (int)fileLength
				- UNCOMPRESSED_HEADER_BYTES ];

		int offset;

		for( offset = 0; offset < totalBytes; )
		{
			offset += input.read( bufferIn, offset, totalBytes - offset );
		}

		inflater.setInput( bufferIn );

		while( !inflater.finished() )
		{
			offset = inflater.inflate( bufferOut );

			if( 0 == offset )
			{
				if( inflater.needsInput() )
				{
					throw new IOException( "Inflater needs more input." );
				}
				else
				{
					break;
				}
			}
		}

		return new SwfInputStream( new ByteArrayInputStream( bufferOut ) );
	}

	public void read( final File file ) throws FileNotFoundException,
			IOException, DataFormatException, SwfFormatException
	{
		final InputStream input = new FileInputStream( file );

		read( input, file.length() );

		input.close();
	}

	public void read( final InputStream input, final long inputLength )
			throws IOException, DataFormatException, SwfFormatException
	{
		read( new SwfInputStream( input ), inputLength );
	}

	public void read( final String pathname ) throws FileNotFoundException,
			IOException, DataFormatException, SwfFormatException
	{
		read( new File( pathname ) );
	}

	public void read( SwfInputStream input, final long inputLength )
			throws IOException, DataFormatException, SwfFormatException
	{
		final int[] signature = new int[ 3 ];

		signature[ 0 ] = input.readUI08();
		signature[ 1 ] = input.readUI08();
		signature[ 2 ] = input.readUI08();

		//
		// Header must match FWS for uncompressed SWF file or CWS for compressed
		// SWF file.
		//

		if( ( signature[ 0 ] != 'F' && signature[ 0 ] != 'C' )
				|| signature[ 1 ] != 'W' || signature[ 2 ] != 'S' )
		{
			throw new SwfFormatException(
					"SWFHeader must be either FWS or CWS." );
		}

		isCompressed = signature[ 0 ] == 'C';

		version = input.readUI08();

		final long fileLength = input.readUI32();

		if( isCompressed )
		{
			//
			// CWS format is supported in SWF 6 or later only.
			//

			if( version < 6 )
			{
				throw new SwfFormatException(
						"CWS file compression is permitted in SWF 6 or later only." );
			}

			input = getDecompressedInputStream( input, inputLength, fileLength );
		}

		frameSize = input.readRECT();

		//
		// The minX and minY value of the FrameSize RECT are always zero
		// according to the file format specification.
		//

		if( frameSize.minX != 0.0f )
		{
			throw new SwfFormatException( "FrameSize minX must be 0.0." );
		}

		if( frameSize.minY != 0.0f )
		{
			throw new SwfFormatException( "FrameSize minY must be 0.0." );
		}

		frameRate = input.readFIXED8();

		frameCount = input.readUI16();

		tags = new LinkedList<ITag>();

		ITag lastTag;

		do
		{
			tags.add( lastTag = input.readTag() );
		}
		while( input.available() > 0 );

		if( version > 7 )
		{
			final ITag firstTag = tags.get( 0 );

			if( firstTag.getType() != Tags.FileAttributes )
			{
				throw new SwfFormatException(
						"First Tag must be typed FileAttributes." );
			}
			else
			{
				if( firstTag instanceof FileAttributesTag )
				{
					final FileAttributesTag fileAttributes = (FileAttributesTag)firstTag;

					if( fileAttributes.hasMetadata )
					{
						final ITag secondTag = tags.get( 1 );

						if( secondTag.getType() != Tags.Metadata )
						{
							throw new SwfFormatException(
									"Second Tag must be typed Metadata since FileAttributes tag defines it." );
						}
					}
				}
			}
		}

		if( lastTag.getType() != Tags.End )
		{
			throw new SwfFormatException( "Last Tag must be typed End." );
		}
	}

	public void write( final File file ) throws IOException
	{
		final OutputStream output = new FileOutputStream( file );

		write( output );

		output.flush();
		output.close();
	}

	public void write( final OutputStream output ) throws IOException
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream(
				0x08 + tags.size() << 0x03 );

		SwfOutputStream swfOutput = new SwfOutputStream( buffer );

		swfOutput.writeRECT( frameSize );

		swfOutput.writeFIXED8( frameRate );

		swfOutput.writeUI16( frameCount );

		swfOutput.flush( true );

		for( final ITag tag : tags )
		{
			swfOutput.writeTag( tag );
		}

		swfOutput.flush( true );

		final byte[] bytes = buffer.toByteArray();

		swfOutput.close();

		final long fileLength = 8 + bytes.length;

		swfOutput = new SwfOutputStream( output );

		swfOutput.write( new byte[] {
				(byte)( ( isCompressed ) ? 'C' : 'F' ), (byte)'W', (byte)'S'
		} );

		swfOutput.writeUI08( version );

		swfOutput.writeUI32( fileLength );

		if( isCompressed )
		{
			final Deflater deflater = new Deflater( Deflater.BEST_COMPRESSION );

			deflater.setInput( bytes );
			deflater.finish();

			final byte[] compressBuffer = new byte[ 0x1000 ];
			int numBytesCompressed = 0;

			do
			{
				numBytesCompressed = deflater.deflate( compressBuffer );
				swfOutput.write( compressBuffer, 0, numBytesCompressed );
			}
			while( 0 != numBytesCompressed );
		}
		else
		{
			swfOutput.write( bytes );
		}

		swfOutput.flush();
	}

	public void write( final String pathname ) throws IOException
	{
		write( new File( pathname ) );
	}
}
