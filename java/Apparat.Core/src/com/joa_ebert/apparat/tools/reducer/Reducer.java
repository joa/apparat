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

package com.joa_ebert.apparat.tools.reducer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.joa_ebert.apparat.swf.tags.DefineTag;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG2Tag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG3Tag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG4Tag;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsLossless2Tag;
import com.joa_ebert.apparat.tools.ITool;
import com.joa_ebert.apparat.tools.IToolConfiguration;
import com.joa_ebert.apparat.tools.ToolLog;
import com.joa_ebert.apparat.tools.ToolRunner;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Reducer implements ITool
{
	public static void main( final String[] arguments )
	{
		final ToolRunner toolRunner = new ToolRunner( new Reducer(), arguments );

		toolRunner.run();
	}

	private IToolConfiguration config;

	public String getHelp()
	{
		return "-input [file]\tThe input file.\n"
				+ "-input [file]\tThe output file.\n"
				+ "-quality [float]\tCompression quality from 0.0 to 1.0.\n"
				+ "-deblock [float]\tStrength of deblocking filter.\n"
				+ "-keep-alpha-premultiplied [boolean]\tWhether or not to keep premultiplied alpha.";
	}

	public String getName()
	{
		return "Reducer";
	}

	public boolean needsOutput()
	{
		return true;
	}

	private void replace( final DefineBitsLossless2Tag tag,
			final float deblock, final float quality,
			final boolean keepPremultiplied, final List<ITag> tags )
	{
		final int width = tag.bitmapWidth;
		final int height = tag.bitmapHeight;

		if( width <= 0x10 && height <= 0x10 )
		{
			return;
		}

		final Inflater inflater = new Inflater();
		final byte[] lossless = new byte[ ( width * height ) << 2 ];
		final byte[] alphaData = new byte[ width * height ];

		boolean needsAlpha = false;

		//
		// Decompress the ZLIB BitmapData which is ARGB pre-multiplied.
		//

		inflater.setInput( tag.zlibBitmapData );

		int offset = 0;

		while( !inflater.finished() )
		{
			try
			{
				offset = inflater.inflate( lossless );
			}
			catch( final DataFormatException dataFormatException )
			{
				ToolLog.error( dataFormatException );
				return;
			}

			if( 0 == offset )
			{
				if( inflater.needsInput() )
				{
					ToolLog
							.warn( "Inflater needs more input. Can not replace lossless image." );
					return;
				}
				else
				{
					break;
				}
			}
		}

		// 
		// Create a BufferedImage which will be used to export a JPEG for us.
		// Fill the alphaData with the alpha values.
		// 

		final BufferedImage buffer = new BufferedImage( width, height,
				BufferedImage.TYPE_INT_ARGB );

		final int widthMul4 = width << 2;
		int yIndexMul4 = 0;
		int yIndex = 0;

		for( int y = 0; y < height; ++y )
		{
			for( int x = 0; x < width; ++x )
			{
				final int xIndexMul4 = x << 2;
				final int index = xIndexMul4 + yIndexMul4;
				final int alpha = lossless[ index ] & 0xff;
				int red = lossless[ index + 1 ] & 0xff;
				int green = lossless[ index + 2 ] & 0xff;
				int blue = lossless[ index + 3 ] & 0xff;

				if( 0xff != alpha )
				{
					needsAlpha = true;
				}

				if( !keepPremultiplied && alpha > 0 && alpha < 0xff )
				{
					final float alphaMultiplier = 255.0f / alpha;

					red = (int)( red * alphaMultiplier );
					green = (int)( green * alphaMultiplier );
					blue = (int)( blue * alphaMultiplier );

					if( red < 0 )
					{
						red = 0;
					}
					else if( red > 0xff )
					{
						red = 0xff;
					}

					if( green < 0 )
					{
						green = 0;
					}
					else if( green > 0xff )
					{
						green = 0xff;
					}

					if( blue < 0 )
					{
						blue = 0;
					}
					else if( blue > 0xff )
					{
						blue = 0xff;
					}
				}

				alphaData[ x + yIndex ] = lossless[ index ];
				buffer.setRGB( x, y, ( 0xff << 0x18 ) | ( red << 0x10 )
						| ( green << 0x08 ) | blue );

			}

			yIndex += width;
			yIndexMul4 += widthMul4;
		}

		//
		// ZLIB compress the alphaData.
		//

		final Deflater deflater = new Deflater( Deflater.BEST_COMPRESSION );

		deflater.setInput( alphaData );
		deflater.finish();

		final byte[] compressBuffer = new byte[ 0x400 ];
		int numBytesCompressed = 0;

		final ByteArrayOutputStream alphaOutput = new ByteArrayOutputStream();

		do
		{
			numBytesCompressed = deflater.deflate( compressBuffer );
			alphaOutput.write( compressBuffer, 0, numBytesCompressed );
		}
		while( 0 != numBytesCompressed );

		//
		// JPEG compress the buffer.
		//

		ImageWriter writer = null;

		final Iterator<ImageWriter> iter = ImageIO
				.getImageWritersByFormatName( "jpg" );

		if( iter.hasNext() )
		{
			writer = iter.next();
		}
		else
		{
			ToolLog.error( "Your VM does not contain a JPG encoder." );
			return;
		}

		final ByteArrayOutputStream imageOutput = new ByteArrayOutputStream();

		try
		{
			writer.setOutput( ImageIO.createImageOutputStream( imageOutput ) );
		}
		catch( final IOException ioException )
		{
			ToolLog.error( ioException );
			return;
		}

		try
		{
			final ImageWriteParam writeParam = writer.getDefaultWriteParam();

			writeParam.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			writeParam.setCompressionQuality( quality );

			writer.write( null, new IIOImage( buffer.getData(), null, null ),
					writeParam );
		}
		catch( final IOException ioException )
		{
			ToolLog.error( ioException );
			return;
		}
		finally
		{
			try
			{
				imageOutput.flush();
				imageOutput.close();
			}
			catch( final IOException ioException )
			{
				ToolLog.warn( ioException.getMessage() );
			}

			writer.dispose();
		}

		//
		// Now replace the DefineBitsLossless2 tag with a DefineBitsJPEG2/3/4
		// tag.
		// 

		try
		{
			alphaOutput.flush();
			alphaOutput.close();
		}
		catch( final IOException e )
		{
			ToolLog.error( e );
			return;
		}

		DefineTag newTag;

		if( needsAlpha )
		{
			if( 0.0f != deblock )
			{
				final DefineBitsJPEG4Tag defineBitsJPEG4 = new DefineBitsJPEG4Tag();

				defineBitsJPEG4.deblock = deblock;

				defineBitsJPEG4.alphaData = alphaOutput.toByteArray();
				defineBitsJPEG4.imageData = imageOutput.toByteArray();

				newTag = defineBitsJPEG4;
			}
			else
			{
				final DefineBitsJPEG3Tag defineBitsJPEG3 = new DefineBitsJPEG3Tag();

				defineBitsJPEG3.alphaData = alphaOutput.toByteArray();
				defineBitsJPEG3.imageData = imageOutput.toByteArray();

				newTag = defineBitsJPEG3;
			}
		}
		else
		{
			final DefineBitsJPEG2Tag defineBitsJPEG2 = new DefineBitsJPEG2Tag();

			defineBitsJPEG2.imageData = imageOutput.toByteArray();

			newTag = defineBitsJPEG2;
		}

		newTag.characterId = tag.characterId;

		if( newTag.isLengthKnown() && tag.isLengthKnown()
				&& newTag.getLength() < tag.getLength() )
		{
			final int index = tags.indexOf( tag );

			tags.add( index, newTag );
			tags.remove( tag );

			ToolLog.success( "Optimized character " + tag.characterId + "." );
		}
	}

	public void run() throws Exception
	{
		final long length0 = new File( config.getInput() ).length();

		final TagIO tagIO = new TagIO( config.getInput() );

		float deblock = 0.00f;
		float quality = 0.99f;
		boolean keepPremultiplied = true;

		if( config.hasOption( "deblock" ) )
		{
			deblock = Float.parseFloat( config.getOption( "deblock" ) );
		}

		if( config.hasOption( "quality" ) )
		{
			quality = Float.parseFloat( config.getOption( "quality" ) );
		}

		if( config.hasOption( "keep-alpha-premultiplied" ) )
		{
			keepPremultiplied = Boolean.parseBoolean( config
					.getOption( "keep-alpha-premultiplied" ) );
		}

		tagIO.read();

		final List<ITag> tags = tagIO.getTags();
		final List<DefineBitsLossless2Tag> targets = new LinkedList<DefineBitsLossless2Tag>();

		for( final ITag tag : tags )
		{
			if( tag.getType() == Tags.DefineBitsLossless2 )
			{
				final DefineBitsLossless2Tag defineBitsLossless2 = (DefineBitsLossless2Tag)tag;

				if( 5 == defineBitsLossless2.bitmapFormat )
				{
					targets.add( defineBitsLossless2 );
				}
			}
		}

		for( final DefineBitsLossless2Tag defineBitsLossless2 : targets )
		{
			replace( defineBitsLossless2, deblock, quality, keepPremultiplied,
					tags );
		}

		tagIO.write( config.getOutput() );
		tagIO.close();

		final long length1 = new File( config.getOutput() ).length();

		final long delta = length0 - length1;
		final float ratio = ( (float)delta / length0 ) * 100.0f;

		ToolLog.info( "Ratio: " + Float.toString( ratio ) + "%" );
		ToolLog.info( "Total bytes: " + delta );
	}

	public void setConfiguration( final IToolConfiguration configuration )
	{
		config = configuration;
	}
}
