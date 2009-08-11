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

/**
 * 
 * @author Joa Ebert
 * 
 */
public enum TraitKind
{
	Slot( 0 ),
	Method( 1 ),
	Getter( 2 ),
	Setter( 3 ),
	Class( 4 ),
	Function( 5 ),
	Const( 6 );

	public static TraitKind getKind( final int value ) throws AbcException
	{
		switch( value )
		{
			case 0:
				return Slot;
			case 1:
				return Method;
			case 2:
				return Getter;
			case 3:
				return Setter;
			case 4:
				return Class;
			case 5:
				return Function;
			case 6:
				return Const;
			default:
				throw new AbcException( "Unknown trait kind " + value );
		}
	}

	private final int value;

	private TraitKind( final int value )
	{
		this.value = value;
	}

	public final int getByte()
	{
		return value;
	}
}
