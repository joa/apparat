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

package com.joa_ebert.apparat.tools.io;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class SwfStrategy implements ITagIOStrategy
{
	private Swf swf;
	private File file;

	public void close()
	{
		if( null == swf )
		{
			return;
		}

		swf = null;
	}

	public Swf getSwf()
	{
		return swf;
	}

	public List<ITag> getTags()
	{
		if( null == swf )
		{
			return new LinkedList<ITag>();
		}

		return swf.tags;
	}

	public void read() throws IOException
	{
		swf = new Swf();

		try
		{
			swf.read( file );
		}
		catch( final Exception exception )
		{
			throw new IOException( exception );
		}
	}

	public void setFile( final File file )
	{
		this.file = file;
	}

	public void write() throws IOException
	{
		if( null == swf )
		{
			return;
		}

		swf.write( file );
	}

}
