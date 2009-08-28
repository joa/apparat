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
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.CallProperty;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.constants.TaasMultiname;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.VoidType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TCallProperty extends AbstractCallExpr
{
	@TaasReference
	public TaasValue object;

	@TaasReference
	public TaasMultiname property;

	public TCallProperty( final TaasValue object, final TaasMultiname property,
			final TaasValue[] parameters )
	{
		this( object, property, parameters, VoidType.INSTANCE );
	}

	public TCallProperty( final TaasValue object, final TaasMultiname property,
			final TaasValue[] parameters, final TaasType returnType )
	{
		super( parameters, returnType );

		this.object = object;
		this.property = property;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		emitParams( environment, body, code );
		// property.emit( environment, body, code );
		object.emit( environment, body, code );

		if( getType() == VoidType.INSTANCE )
		{
			code
					.add( new CallPropVoid( property.multiname,
							parameters.length ) );
		}
		else
		{
			code
					.add( new CallProperty( property.multiname,
							parameters.length ) );
		}
	}

	@Override
	public String toString()
	{
		return "[TCallProperty " + object.toString() + ", "
				+ property.toString() + ", " + getType().toString() + ", "
				+ parametersToString() + "]";
	}
}
