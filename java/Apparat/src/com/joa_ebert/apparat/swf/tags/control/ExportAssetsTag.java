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
import java.util.ArrayList;
import java.util.List;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.io.RECORDHEADER;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;
import com.joa_ebert.apparat.swf.tags.ControlTag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;

/**
 * @author Joa Ebert
 * 
 */
public class ExportAssetsTag extends ControlTag
{
	/**
	 * The Export class is a pair that maps a character to a name.
	 * 
	 * @author Joa Ebert
	 */
	public static final class Export
	{
		/**
		 * Character ID to export.
		 */
		public int tag;

		/**
		 * Identifier for the exported character.
		 */
		public String name;

		public Export( final int tag, final String name )
		{
			this.tag = tag;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return "[Export tag: " + tag + ", name: " + name + "]";
		}
	}

	public List<Export> exports;

	public void accept( final ITagVisitor visitor )
	{
		visitor.visit( this );
	}

	public int getLength()
	{
		return -1;
	}

	public int getType()
	{
		return Tags.ExportAssets;
	}

	public boolean isLengthKnown()
	{
		return false;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		final int count = input.readUI16();

		exports = new ArrayList<Export>( count );

		for( int i = 0; i < count; ++i )
		{
			exports.add( new Export( input.readUI16(), input.readSTRING() ) );
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder( 0x20 * exports.size() );

		for( final Export export : exports )
		{
			builder.append( " " + export.toString() );
		}

		return "[ExportAssets" + builder.toString() + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		output.writeUI16( exports.size() );

		for( final Export export : exports )
		{
			output.writeUI16( export.tag );
			output.writeSTRING( export.name );
		}
	}
}
