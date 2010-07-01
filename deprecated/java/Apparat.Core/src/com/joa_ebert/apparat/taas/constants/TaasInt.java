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
import com.joa_ebert.apparat.abc.bytecode.operations.PushByte;
import com.joa_ebert.apparat.abc.bytecode.operations.PushInt;
import com.joa_ebert.apparat.abc.bytecode.operations.PushShort;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;
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
public class TaasInt extends TaasNumeric
{
	public final int value;

	public TaasInt( final int value )
	{
		super( IntType.INSTANCE );

		this.value = value;
	}

	@Override
	public TaasNumeric add( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value + ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric bitAnd( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value & ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric bitNot()
	{
		return new TaasInt( ~value );
	}

	@Override
	public TaasNumeric bitOr( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value | ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric bitXor( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value ^ ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric decrement()
	{
		return new TaasInt( value - 1 );
	}

	@Override
	public TaasNumeric divide( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value / ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasValue dup()
	{
		return new TaasInt( value );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		if( value <= 127 && value >= -128 )
		{
			code.add( new PushByte( value ) );
		}
		else if( value <= 32767 && value >= -32768 )
		{
			code.add( new PushShort( value ) );
		}
		else
		{
			code.add( new PushInt( value ) );
		}
	}

	@Override
	public TaasBoolean equals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasBoolean( value == ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasBoolean greaterEquals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasBoolean( value >= ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasBoolean greaterThan( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasBoolean( value > ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric increment()
	{
		return new TaasInt( value + 1 );
	}

	@Override
	public TaasBoolean lessEquals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasBoolean( value <= ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasBoolean lessThan( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasBoolean( value < ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric modulo( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value % ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric multiply( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value * ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric negate()
	{
		return new TaasInt( -value );
	}

	@Override
	public TaasNumeric shiftLeft( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value << ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric shiftRight( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value >> ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric shiftRightUnsigned( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value >>> ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasBoolean strictEquals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasBoolean( value == ( (TaasInt)numeric ).value );
	}

	@Override
	public TaasNumeric subtract( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasInt.class );
		return new TaasInt( value - ( (TaasInt)numeric ).value );
	}

	@Override
	public String toString()
	{
		return "[TaasInt value: " + Integer.toString( value ) + "]";
	}

	@Override
	public TaasConstant widen( final TaasType type )
	{
		if( type == IntType.INSTANCE )
		{
			return this;
		}
		else if( type == UIntType.INSTANCE )
		{
			return new TaasUInt( value );
		}
		else if( type == NumberType.INSTANCE )
		{
			return new TaasNumber( value );
		}
		else if( type == StringType.INSTANCE )
		{
			return new TaasString( Integer.toString( value ) );
		}
		else
		{
			throw new TaasException( "Can not convert from " + getType()
					+ " to " + type );
		}
	}
}
