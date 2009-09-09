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

package com.joa_ebert.apparat.taas.constants;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.operations.PushFalse;
import com.joa_ebert.apparat.abc.bytecode.operations.PushTrue;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.BooleanType;
import com.joa_ebert.apparat.taas.types.IntType;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.StringType;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.UIntType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasBoolean extends TaasConstant
{
	public final boolean value;

	public TaasBoolean( final boolean value )
	{
		super( BooleanType.INSTANCE );

		this.value = value;
	}

	@Override
	public TaasValue dup()
	{
		return new TaasBoolean( value );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		code.add( value ? new PushTrue() : new PushFalse() );
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof TaasBoolean )
		{
			return ( (TaasBoolean)other ).value == value;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 2203;

		hash = 31 * hash + ( value ? 1 : 0 );

		return hash;
	}

	@Override
	public String toString()
	{
		return "[TaasBoolean value: " + Boolean.toString( value ) + "]";
	}

	@Override
	public TaasConstant widen( final TaasType type )
	{
		if( type == BooleanType.INSTANCE )
		{
			return this;
		}
		else if( type == IntType.INSTANCE )
		{
			return new TaasInt( value ? 1 : 0 );
		}
		else if( type == UIntType.INSTANCE )
		{
			return new TaasUInt( value ? 1 : 0 );
		}
		else if( type == NumberType.INSTANCE )
		{
			return new TaasNumber( value ? 1.0 : 0.0 );
		}
		else if( type == StringType.INSTANCE )
		{
			return new TaasString( Boolean.toString( value ) );
		}
		else
		{
			throw new TaasException( "Can not convert from " + getType()
					+ " to " + type );
		}
	}
}
