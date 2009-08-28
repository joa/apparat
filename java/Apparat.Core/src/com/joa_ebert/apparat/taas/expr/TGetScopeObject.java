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
import com.joa_ebert.apparat.abc.bytecode.operations.GetScopeObject;
import com.joa_ebert.apparat.taas.TaasExpression;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TGetScopeObject extends TaasExpression
{
	@TaasReference
	public TaasValue scopeObject;

	public TGetScopeObject( final TaasValue scopeObject )
	{
		super( scopeObject.getType() );

		this.scopeObject = scopeObject;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		//
		// This one will be hard. How should we know the index in the ScopeStack
		// of this value?
		//
		// throw new TaasException( "TODO" );
		// code.add( new GetScopeObject() );

		// FIXME verify this...
		// asc seems to use always only index 0 ...
		code.add( new GetScopeObject( 0 ) );
	}

	@Override
	public String toString()
	{
		return "[TGetScopeObject " + scopeObject.toString() + "]";
	}
}
