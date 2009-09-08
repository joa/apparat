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

import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.utils.StringConverter;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.constants.TaasNamespace;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class MultinameType extends TaasType
{
	public final AbstractMultiname multiname;
	public final TaasValue runtimeName;
	public final TaasNamespace runtimeNamespace;

	public MultinameType( final AbstractMultiname multiname )
	{
		this.multiname = multiname;
		this.runtimeName = null;
		this.runtimeNamespace = null;

		switch( multiname.kind )
		{
			case RTQName:
			case RTQNameL:
			case MultinameL:
				throw new TaasException(
						"This runtime multiname needs additional information." );
		}
	}

	public MultinameType( final AbstractMultiname multiname,
			final TaasNamespace namespace, final TaasValue name )
	{
		this.multiname = multiname;
		this.runtimeName = name;
		this.runtimeNamespace = namespace;
	}

	public boolean equals( final MultinameType other )
	{
		if( multiname.equals( other.multiname ) )
		{
			if( null != runtimeName || null != other.runtimeName )
			{
				if( null == runtimeName || null == other.runtimeName )
				{
					return false;
				}
				else
				{
					if( runtimeName.equals( other.runtimeName ) )
					{
						if( null != runtimeNamespace
								|| null != other.runtimeNamespace )
						{
							if( null == runtimeNamespace
									|| null == other.runtimeNamespace )
							{
								return false;
							}
							else
							{
								if( runtimeNamespace
										.equals( other.runtimeNamespace ) )
								{
									return true;
								}
								else
								{
									return false;
								}
							}
						}
						else
						{
							return true;
						}
					}
					else
					{
						return false;
					}
				}
			}
			else
			{
				if( null != runtimeNamespace || null != other.runtimeNamespace )
				{
					if( null == runtimeNamespace
							|| null == other.runtimeNamespace )
					{
						return false;
					}
					else
					{
						if( runtimeNamespace.equals( other.runtimeNamespace ) )
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				}
				else
				{
					return true;
				}
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof MultinameType )
		{
			return equals( (MultinameType)other );
		}

		return false;
	}

	@Override
	public String toString()
	{
		return "[MultinameType " + StringConverter.toString( multiname ) + "]";
	}
}
