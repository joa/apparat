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
public enum MultinameKind
{
	QName( 0x07 ),
	QNameA( 0x0d ),
	RTQName( 0x0f ),
	RTQNameA( 0x10 ),
	RTQNameL( 0x11 ),
	RTQNameLA( 0x12 ),
	Multiname( 0x09 ),
	MultinameA( 0x0e ),
	MultinameL( 0x1b ),
	MultinameLA( 0x1c ),
	Typename( 0x1d );

	public static MultinameKind getKind( final int value ) throws AbcException
	{
		switch( value )
		{
			case 0x07:
				return QName;
			case 0x0d:
				return QNameA;
			case 0x0f:
				return RTQName;
			case 0x10:
				return RTQNameA;
			case 0x11:
				return RTQNameL;
			case 0x12:
				return RTQNameLA;
			case 0x09:
				return Multiname;
			case 0x0e:
				return MultinameA;
			case 0x1b:
				return MultinameL;
			case 0x1c:
				return MultinameLA;
			case 0x1d:
				return Typename;
			default:
				throw new AbcException( "Unknown multiname kind " + value );
		}
	}

	private final int value;

	private MultinameKind( final int value )
	{
		this.value = value;
	}

	public final int getByte()
	{
		return value;
	}

	@Override
	public final String toString()
	{
		switch( getByte() )
		{
			case 0x07:
				return "[QName]";
			case 0x0d:
				return "[QNameA]";
			case 0x0f:
				return "[RTQName]";
			case 0x10:
				return "[RTQNameA]";
			case 0x11:
				return "[RTQNameL]";
			case 0x12:
				return "[RTQNameLA]";
			case 0x09:
				return "[Multiname]";
			case 0x0e:
				return "[MultinameA]";
			case 0x1b:
				return "[MultinameL]";
			case 0x1c:
				return "[MultinameLA]";
			case 0x1d:
				return "[Typename]";
			default:
				return "(Unknown " + getByte() + ")";
		}
	}
}
