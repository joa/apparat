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

package com.joa_ebert.apparat.taas.types;

import com.joa_ebert.apparat.taas.TaasValue;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class ParameterizedType extends TaasType
{
	public final TaasType base;
	public final TaasType[] parameters;

	public ParameterizedType( final TaasType base, final TaasType[] parameters )
	{
		this.base = base;
		this.parameters = parameters;
	}

	public ParameterizedType( final TaasValue base, final TaasValue[] parameters )
	{
		final TaasType[] parameterTypes = new TaasType[ parameters.length ];
		int n = parameters.length;

		while( --n > -1 )
		{
			parameterTypes[ n ] = parameters[ n ].getType();
		}

		this.base = base.getType();
		this.parameters = parameterTypes;
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof ParameterizedType )
		{
			return equals( (ParameterizedType)other );
		}

		return false;
	}

	public boolean equals( final ParameterizedType other )
	{
		if( base.equals( other ) )
		{
			if( parameters.length == other.parameters.length )
			{
				int n = parameters.length;

				while( --n > -1 )
				{
					if( !parameters[ n ].equals( other.parameters[ n ] ) )
					{
						return false;
					}
				}

				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "[ParameterizedType " + base.toString() + ", "
				+ parameters.toString() + "]";
	}
}
