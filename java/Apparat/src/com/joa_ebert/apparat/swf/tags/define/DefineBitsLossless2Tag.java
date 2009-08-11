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

package com.joa_ebert.apparat.swf.tags.define;

import java.io.IOException;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.io.RECORDHEADER;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;
import com.joa_ebert.apparat.swf.tags.DefineTag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class DefineBitsLossless2Tag extends DefineTag
{
	public int bitmapFormat;
	public int bitmapWidth;
	public int bitmapHeight;
	public int bitmapColorTableSize;
	public byte[] zlibBitmapData;

	public void accept( final ITagVisitor visitor )
	{
		visitor.visit( this );
	}

	public int getLength()
	{
		return 7 + zlibBitmapData.length + ( 3 == bitmapFormat ? 1 : 0 );
	}

	public int getType()
	{
		return Tags.DefineBitsLossless2;
	}

	public boolean isLengthKnown()
	{
		return true;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		characterId = input.readUI16();

		bitmapFormat = input.readUI08();

		bitmapWidth = input.readUI16();
		bitmapHeight = input.readUI16();

		int length = header.length - 7;

		if( 3 == bitmapFormat )
		{
			bitmapColorTableSize = input.readUI08();
			--length;
		}

		zlibBitmapData = new byte[ length ];

		int offset = 0;

		while( offset < length )
		{
			offset += input.read( zlibBitmapData, offset, length - offset );
		}
	}

	@Override
	public String toString()
	{
		return "[DefineBitsLossless2Tag characterId: " + characterId
				+ ", bitmapFormat: " + bitmapFormat + ", bitmapWidth: "
				+ bitmapWidth + ", bitmapHeight: " + bitmapHeight
				+ ", bitmapColorTableSize: " + bitmapColorTableSize
				+ ", zlibBitmapDataLength: " + zlibBitmapData.length + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		output.writeUI16( characterId );

		output.writeUI08( bitmapFormat );

		output.writeUI16( bitmapWidth );

		output.writeUI16( bitmapHeight );

		if( 3 == bitmapFormat )
		{
			output.writeUI08( bitmapColorTableSize );
		}

		output.write( zlibBitmapData );
	}

}
