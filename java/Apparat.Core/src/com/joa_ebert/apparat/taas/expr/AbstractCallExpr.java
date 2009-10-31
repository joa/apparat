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

package com.joa_ebert.apparat.taas.expr;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.taas.TaasExpression;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class AbstractCallExpr extends TaasExpression
{
	@TaasReference
	public TaasValue[] parameters;

	protected AbstractCallExpr( final TaasValue[] parameters,
			final TaasType returnType )
	{
		super( returnType );

		this.parameters = parameters;
	}

	protected final void emitParams( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		for( int i = 0, n = parameters.length; i < n; ++i )
		{
			parameters[ i ].emit( environment, body, code );
		}
	}

	@Override
	public boolean hasSideEffects()
	{
		return true;
	}

	public String parametersToString()
	{
		final StringBuilder builder = new StringBuilder();

		final int n = parameters.length;
		final int m = n - 1;

		for( int i = 0; i < n; ++i )
		{
			builder.append( parameters[ i ].toString() );

			if( i != m )
			{
				builder.append( ", " );
			}
		}

		return builder.toString();
	}

	@Override
	public String toString()
	{
		return "[AbstractCallExpr]";
	}
}
