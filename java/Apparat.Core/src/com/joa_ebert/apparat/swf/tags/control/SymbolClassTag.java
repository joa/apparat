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
public class SymbolClassTag extends ControlTag
{
	/**
	 * The Symbol class is a pair that maps a character to a name.
	 * 
	 * @author Joa Ebert
	 */
	public static final class Symbol
	{
		/**
		 * Character ID to associate.
		 */
		public int tag;

		/**
		 * Fully qualified name of the ActionScript 3.0 class with which to
		 * associate the symbol.
		 */
		public String name;

		public Symbol( final int tag, final String name )
		{
			this.tag = tag;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return "[Symbol tag: " + tag + ", name: " + name + "]";
		}
	}

	public List<Symbol> symbols;

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
		return Tags.SymbolClass;
	}

	public boolean isLengthKnown()
	{
		return false;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		final int numSymbols = input.readUI16();

		symbols = new ArrayList<Symbol>( numSymbols );

		for( int i = 0; i < numSymbols; ++i )
		{
			symbols.add( new Symbol( input.readUI16(), input.readSTRING() ) );
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder( 0x20 * symbols.size() );

		for( final Symbol symbol : symbols )
		{
			builder.append( " " + symbol.toString() );
		}

		return "[SymbolClass" + builder.toString() + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		output.writeUI16( symbols.size() );

		for( final Symbol symbol : symbols )
		{
			output.writeUI16( symbol.tag );
			output.writeSTRING( symbol.name );
		}
	}
}
