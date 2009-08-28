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
import com.joa_ebert.apparat.abc.bytecode.operations.Coerce;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceAny;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceBoolean;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceDouble;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceInt;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceObject;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceString;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceUInt;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.AnyType;
import com.joa_ebert.apparat.taas.types.BooleanType;
import com.joa_ebert.apparat.taas.types.IntType;
import com.joa_ebert.apparat.taas.types.MultinameType;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.ObjectType;
import com.joa_ebert.apparat.taas.types.StringType;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.UIntType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TCoerce extends AbstractBinaryExpr
{
	private static final String OPERATOR = "coerce";

	public TCoerce( final TaasValue lhs, final TaasType rhs )
	{
		super( lhs, rhs, OPERATOR, rhs );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		lhs.emit( environment, body, code );

		final TaasType type = getType();

		if( AnyType.INSTANCE == type )
		{
			code.add( new CoerceAny() );
		}
		else if( BooleanType.INSTANCE == type )
		{
			code.add( new CoerceBoolean() );
		}
		else if( IntType.INSTANCE == type )
		{
			code.add( new CoerceInt() );
		}
		else if( NumberType.INSTANCE == type )
		{
			code.add( new CoerceDouble() );
		}
		else if( ObjectType.INSTANCE == type )
		{
			code.add( new CoerceObject() );
		}
		else if( StringType.INSTANCE == type )
		{
			code.add( new CoerceString() );
		}
		else if( UIntType.INSTANCE == type )
		{
			code.add( new CoerceUInt() );
		}
		else
		{
			if( rhs instanceof MultinameType )
			{
				code.add( new Coerce( ( (MultinameType)rhs ).multiname ) );
			}
			else
			{
				throw new TaasException( "Unexpected type." );
			}
		}
	}
}
