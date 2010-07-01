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
public class DefineBitsJPEG3Tag extends DefineTag
{
	public byte[] imageData;
	public byte[] alphaData;

	public void accept( final ITagVisitor visitor )
	{
		visitor.visit( this );
	}

	public int getLength()
	{
		return 6 + imageData.length + alphaData.length;
	}

	public int getType()
	{
		return Tags.DefineBitsJPEG3;
	}

	public boolean isLengthKnown()
	{
		return true;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		characterId = input.readUI16();

		final int alphaOffset = (int)input.readUI32();
		final int alphaLength = header.length - alphaOffset - 6;

		imageData = new byte[ alphaOffset ];
		alphaData = new byte[ alphaLength ];

		int offset = 0;

		while( offset < alphaOffset )
		{
			offset += input.read( imageData, offset, alphaOffset - offset );
		}

		offset = 0;

		while( offset < alphaLength )
		{
			offset += input.read( alphaData, offset, alphaLength - offset );
		}
	}

	@Override
	public String toString()
	{
		return "[DefineBitsJPEG3Tag characterId: " + characterId
				+ ", imageLength: " + imageData.length + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		output.writeUI16( characterId );

		output.writeUI32( imageData.length );

		output.write( imageData );

		output.write( alphaData );
	}

}
