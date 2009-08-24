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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swc.SwcException;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;

/**
 * 
 * @author Joa Ebert
 * 
 */
final class SwcStrategy implements ITagIOStrategy
{
	private Swc swc;
	private Swf swf;

	private File file;

	public void close()
	{
		swc = null;
		swf = null;
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
		swc = new Swc();

		try
		{
			swc.read( file );
		}
		catch( final Exception exception )
		{
			throw new IOException( exception );
		}

		swf = new Swf();

		ByteArrayInputStream input = null;

		try
		{
			input = new ByteArrayInputStream( swc.library );

			swf.read( input, swc.library.length );
		}
		catch( final Exception exception )
		{
			throw new IOException( exception );
		}
		finally
		{
			if( null != input )
			{
				input.close();
				input = null;
			}
		}
	}

	public void setFile( final File file )
	{
		this.file = file;
	}

	public void write() throws IOException
	{
		if( null == swf || null == swc )
		{
			return;
		}

		ByteArrayOutputStream output = null;
		byte[] library = null;

		output = new ByteArrayOutputStream();

		try
		{
			swf.write( output );

			output.flush();

			library = output.toByteArray();

			output.close();
		}
		catch( final Exception exception )
		{
			throw new IOException( exception );
		}
		finally
		{
			output.close();
		}

		swc.library = library;

		try
		{
			swc.write( file );
		}
		catch( final SwcException swcException )
		{
			throw new IOException( swcException );
		}
	}

}
