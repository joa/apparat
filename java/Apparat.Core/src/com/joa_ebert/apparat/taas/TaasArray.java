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

import java.util.ArrayList;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.operations.NewArray;
import com.joa_ebert.apparat.taas.types.ObjectType;

/**
 * @author Joa Ebert
 * 
 */
public class TaasArray extends TaasValue
{
	@TaasReference
	public ArrayList<TaasValue> elements;

	public TaasArray( final int numElements )
	{
		super( ObjectType.INSTANCE );

		elements = new ArrayList<TaasValue>( numElements );
	}

	@Override
	public TaasValue dup()
	{
		final TaasArray result = new TaasArray( elements.size() );

		int i = 0;
		final int n = elements.size();

		for( ; i < n; ++i )
		{
			result.elements.add( elements.get( i ).dup() );
		}

		return result;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		int i = 0;
		final int n = elements.size();

		for( ; i < n; ++i )
		{
			elements.get( i ).emit( environment, body, code );
		}

		code.add( new NewArray( n ) );
	}

	@Override
	public String toString()
	{
		return "[TaasArray]";
	}
}
