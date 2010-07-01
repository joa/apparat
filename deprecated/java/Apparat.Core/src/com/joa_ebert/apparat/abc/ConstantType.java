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
public enum ConstantType
{
	Int( 0x03 ),
	UInt( 0x04 ),
	Double( 0x06 ),
	Utf8( 0x01 ),
	True( 0x0b ),
	False( 0x0a ),
	Null( 0x0c ),
	Undefined( 0x00 ),
	Namespace( 0x08 ),
	PackageNamespace( 0x16 ),
	PackageInternalNamespace( 0x17 ),
	ProtectedNamespace( 0x18 ),
	ExplicitNamespace( 0x19 ),
	StaticProtectedNamespace( 0x1a ),
	PrivateNamespace( 0x05 );

	public static ConstantType getKind( final int value ) throws AbcException
	{
		switch( value )
		{
			case 0x03:
				return Int;
			case 0x04:
				return UInt;
			case 0x06:
				return Double;
			case 0x01:
				return Utf8;
			case 0x0b:
				return True;
			case 0x0a:
				return False;
			case 0x0c:
				return Null;
			case 0x00:
				return Undefined;
			case 0x08:
				return Namespace;
			case 0x16:
				return PackageNamespace;
			case 0x17:
				return PackageInternalNamespace;
			case 0x18:
				return ProtectedNamespace;
			case 0x19:
				return ExplicitNamespace;
			case 0x1a:
				return StaticProtectedNamespace;
			case 0x05:
				return PrivateNamespace;
			default:
				throw new AbcException( "Unknown optional parameter kind "
						+ value );
		}
	}

	private final int value;

	private ConstantType( final int value )
	{
		this.value = value;
	}

	public final int getByte()
	{
		return value;
	}
}
