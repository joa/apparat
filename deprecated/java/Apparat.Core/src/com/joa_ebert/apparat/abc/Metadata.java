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

package com.joa_ebert.apparat.abc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Metadata
{
	public String name = "";
	public final Map<String, String> attributes = new HashMap<String, String>();

	public Metadata()
	{
	}

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );
	}

	public boolean equals( final Metadata other )
	{
		if( !name.equals( other.name ) )
		{
			return false;
		}

		if( attributes.size() != other.attributes.size() )
		{
			return false;
		}

		for( final Entry<String, String> entry : attributes.entrySet() )
		{
			if( !other.attributes.containsKey( entry.getKey() ) )
			{
				return false;
			}

			if( !entry.getValue()
					.equals( other.attributes.get( entry.getKey() ) ) )
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof Metadata )
		{
			return equals( (Metadata)other );
		}

		return false;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder( "[Metadata [" );

		builder.append( name );
		builder.append( '(' );

		final int n = attributes.size() - 1;
		int i = 0;

		for( final Entry<String, String> entry : attributes.entrySet() )
		{
			builder.append( entry.getKey() );
			builder.append( "=\"" );
			builder.append( entry.getValue() );
			builder.append( '"' );

			if( i++ != n )
			{
				builder.append( ", " );
			}
		}

		builder.append( ")]" );

		return builder.toString();
	}
}
