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

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class RECT
{
	public int minX;
	public int maxX;
	public int minY;
	public int maxY;

	@Override
	public boolean equals( final Object obj )
	{
		if( obj instanceof RECT )
		{
			return equals( (RECT)obj );
		}

		return false;
	}

	public boolean equals( final RECT obj )
	{
		return obj.minX == minX && obj.maxX == maxX && obj.minY == minY
				&& obj.maxY == maxY;
	}

	public int getNumberOfBits()
	{
		// TODO fix me for negative values

		final int a = 0x21 - Integer.numberOfLeadingZeros( minX );
		final int b = 0x21 - Integer.numberOfLeadingZeros( maxX );
		final int c = 0x21 - Integer.numberOfLeadingZeros( minY );
		final int d = 0x21 - Integer.numberOfLeadingZeros( maxY );

		return Math.max( Math.max( a, b ), Math.max( c, d ) );
	}

	@Override
	public String toString()
	{
		return "[RECT minX: " + minX + ", maxX: " + maxX + ", minY: " + minY
				+ ", maxY: " + maxY + "]";
	}
}
