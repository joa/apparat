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

package com.joa_ebert.apparat.taas;

import java.util.LinkedHashMap;
import java.util.Map;

import com.joa_ebert.apparat.taas.constants.TaasString;
import com.joa_ebert.apparat.taas.types.ObjectType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasObject extends TaasValue
{
	private final Map<String, TaasValue> map = new LinkedHashMap<String, TaasValue>();

	public TaasObject()
	{
		super( ObjectType.INSTANCE );
	}

	public TaasValue get( final String key )
	{
		return map.get( key );
	}

	public TaasValue get( final TaasString key )
	{
		return get( key.value );
	}

	public void put( final String key, final TaasValue value )
	{
		map.put( key, value );
	}

	public void put( final TaasString key, final TaasValue value )
	{
		put( key.value, value );
	}

	@Override
	public String toString()
	{
		return "[TaasObject]";
	}
}
