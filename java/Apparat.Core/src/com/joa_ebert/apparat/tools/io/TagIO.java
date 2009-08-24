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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.swf.tags.ITag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TagIO
{
	private final File file;
	private ITagIOStrategy strategy;

	public TagIO( final File file )
	{
		this.file = file;
	}

	public TagIO( final String pathname )
	{
		this( new File( pathname ) );
	}

	public void close()
	{
		if( null == strategy )
		{
			return;
		}

		strategy.close();
	}

	public List<ITag> getTags()
	{
		if( null == strategy )
		{
			return new LinkedList<ITag>();
		}

		return strategy.getTags();
	}

	public void read() throws IOException
	{
		if( file.getName().endsWith( ".swf" ) )
		{
			strategy = new SwfStrategy();
		}
		else if( file.getName().endsWith( ".swc" ) )
		{
			strategy = new SwcStrategy();
		}
		else
		{
			final byte[] header = new byte[ 3 ];
			final InputStream input = new FileInputStream( file );

			int i = 0;

			while( ( i += input.read( header, i, 3 - i ) ) < 3 )
			{
				continue;
			}

			input.close();

			if( ( header[ 0 ] == 'F' || header[ 0 ] == 'C' )
					&& header[ 1 ] == 'W' && header[ 2 ] == 'S' )
			{
				strategy = new SwfStrategy();
			}
			else
			{
				strategy = new SwcStrategy();
			}
		}

		strategy.setFile( file );
		strategy.read();
	}

	public void write() throws IOException
	{
		write( file );
	}

	public void write( final File file ) throws IOException
	{
		if( null == strategy )
		{
			return;
		}

		strategy.setFile( file );
		strategy.write();
	}

	public void write( final String pathname ) throws IOException
	{
		write( new File( pathname ) );
	}
}
