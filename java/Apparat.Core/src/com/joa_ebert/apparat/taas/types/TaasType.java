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

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class TaasType extends TaasValue
{
	public static TaasType typeOf( final TaasValue a, final TaasValue b )
	{
		final TaasType typeA = a.getType();
		final TaasType typeB = b.getType();

		if( typeA.equals( typeB ) )
		{
			return typeA;
		}
		else if( typeA == StringType.INSTANCE || typeB == StringType.INSTANCE )
		{
			return StringType.INSTANCE;
		}
		else if( typeA == NumberType.INSTANCE || typeB == NumberType.INSTANCE )
		{
			return NumberType.INSTANCE;
		}
		else if( ( typeA == UIntType.INSTANCE && typeB != UIntType.INSTANCE )
				|| ( typeA != UIntType.INSTANCE && typeB == UIntType.INSTANCE ) )
		{
			return NumberType.INSTANCE;
		}
		else if( typeA == IntType.INSTANCE || typeB == IntType.INSTANCE )
		{
			return IntType.INSTANCE;
		}
		else
		{
			throw new TaasException( "Implicit type conversion of "
					+ typeA.toString() + " and " + typeB.toString()
					+ " is not supported." );
		}
	}

	protected TaasType()
	{
		super( null );
	}

	@Override
	public TaasValue dup()
	{
		throw new TaasException( "Can not duplicate a TaasType." );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		throw new TaasException( "Can not emit the operation for a TaasType." );
	}

	@Override
	public abstract boolean equals( final Object other );
}
