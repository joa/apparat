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

package com.joa_ebert.apparat.abc.bytecode;

import com.joa_ebert.apparat.abc.AbcException;
import com.joa_ebert.apparat.abc.utils.StringConverter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class MarkerException extends AbcException
{
	private static final long serialVersionUID = -5301141085974253738L;

	MarkerException( final AbstractOperation operation )
	{
		super( "Can not patch jump of " + operation.toString() );
	}

	MarkerException( final int position )
	{
		super( "Could not solve marker at position 0x"
				+ Integer.toHexString( position ) );
	}

	MarkerException( final Marker unresolved, final int position )
	{
		super( "Could not solve marker "
				+ StringConverter.toString( unresolved ) + " at position 0x"
				+ Integer.toHexString( position ) );
	}

	MarkerException( final String message )
	{
		super( message );
	}
}
