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

package com.joa_ebert.apparat.swf.tags;

import java.io.IOException;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.io.RECORDHEADER;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
@Tag( Tag.Type.Unknown )
public final class GenericTag implements ITag
{
	private byte[] data;
	private RECORDHEADER header;

	public void accept( final ITagVisitor visitor )
	{
		visitor.visit( this );
	}

	public int getLength()
	{
		if( null != data )
		{
			return data.length;
		}

		return -1;
	}

	public int getType()
	{
		if( null == header )
		{
			return -1;
		}

		return header.type;
	}

	public boolean isLengthKnown()
	{
		return null != data;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		this.header = header;

		final int length = header.length;
		int offset = 0;

		data = new byte[ length ];

		while( offset < length )
		{
			offset += input.read( data, offset, length - offset );
		}
	}

	@Override
	public String toString()
	{
		return "[GenericTag length: " + data.length + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		if( null == data )
		{
			return;
		}

		output.write( data );
	}
}
