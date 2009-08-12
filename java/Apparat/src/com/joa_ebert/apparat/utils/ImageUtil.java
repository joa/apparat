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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

/**
 * @author Joa Ebert
 * 
 */
public class ImageUtil
{
	public static byte[] encodeJPG( final BufferedImage input,
			final float quality ) throws IOException
	{
		ImageWriter writer = null;

		final Iterator<ImageWriter> iter = ImageIO
				.getImageWritersByFormatName( "jpg" );

		if( iter.hasNext() )
		{
			writer = iter.next();
		}
		else
		{
			return null;
		}

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		writer.setOutput( ImageIO.createImageOutputStream( output ) );

		try
		{
			final ImageWriteParam writeParam = writer.getDefaultWriteParam();

			writeParam.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			writeParam.setCompressionQuality( quality );

			writer.write( null, new IIOImage( input.getData(), null, null ),
					writeParam );
		}
		finally
		{
			try
			{
				output.flush();
				output.close();
			}
			catch( final IOException ioException )
			{
			}

			writer.dispose();
		}

		return output.toByteArray();
	}
}
