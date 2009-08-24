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

package com.joa_ebert.apparat.swf.io;

import com.joa_ebert.apparat.utils.HexUtil;

/**
 * @author Joa Ebert
 * 
 */
public final class UUID
{
	public final byte[] hash;

	public UUID()
	{
		this( new byte[ 0x10 ] );
	}

	public UUID( final byte[] bytes )
	{
		this.hash = bytes;
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof UUID )
		{
			return equals( (UUID)other );
		}

		return false;
	}

	public boolean equals( final UUID other )
	{
		if( hash.length != other.hash.length )
		{
			return false;
		}

		int n = hash.length;

		while( --n > -1 )
		{
			if( hash[ n ] != other.hash[ n ] )
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "[UUID hash: " + HexUtil.toString( hash ) + "]";
	}
}
