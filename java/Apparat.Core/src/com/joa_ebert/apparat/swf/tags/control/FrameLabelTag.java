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

package com.joa_ebert.apparat.swf.tags.control;

import java.io.IOException;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.io.RECORDHEADER;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;
import com.joa_ebert.apparat.swf.tags.ControlTag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class FrameLabelTag extends ControlTag
{
	public String name;

	public void accept( final ITagVisitor visitor )
	{
		visitor.visit( this );
	}

	public int getLength()
	{
		return -1;
		// try
		// {
		// // TODO fix me
		// return name.getBytes( "UTF8" ).length;
		// }
		// catch( UnsupportedEncodingException e )
		// {
		// return name.getBytes().length;
		// }
	}

	public int getType()
	{
		return Tags.FrameLabel;
	}

	public boolean isLengthKnown()
	{
		return false;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		name = input.readSTRING();
	}

	@Override
	public String toString()
	{
		return "[FrameLabelTag name: " + name + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		output.writeSTRING( name );
	}
}
