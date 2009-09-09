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
import com.joa_ebert.apparat.abc.bytecode.operations.PushDouble;
import com.joa_ebert.apparat.abc.bytecode.operations.PushNaN;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.StringType;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasNumber extends TaasNumeric
{
	public final double value;

	public TaasNumber( final double value )
	{
		super( NumberType.INSTANCE );

		this.value = value;
	}

	@Override
	public TaasNumeric add( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( value + ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric bitAnd( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasInt( (int)value & (int)( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric bitNot()
	{
		return new TaasInt( ~(int)value );
	}

	@Override
	public TaasNumeric bitOr( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasInt( (int)value | (int)( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric bitXor( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasInt( (int)value ^ (int)( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric decrement()
	{
		return new TaasNumber( value - 1.0 );
	}

	@Override
	public TaasNumber divide( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( value / ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasValue dup()
	{
		return new TaasNumber( value );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		if( Double.isNaN( value ) )
		{
			code.add( new PushNaN() );
		}
		else
		{
			code.add( new PushDouble( value ) );
		}
	}

	@Override
	public TaasBoolean equals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasBoolean( value == ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasBoolean greaterEquals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasBoolean( value >= ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasBoolean greaterThan( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasBoolean( value > ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric increment()
	{
		return new TaasNumber( value + 1.0 );
	}

	@Override
	public TaasBoolean lessEquals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasBoolean( value <= ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasBoolean lessThan( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasBoolean( value < ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumber modulo( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( value % ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumber multiply( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( value * ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric negate()
	{
		return new TaasNumber( -value );
	}

	@Override
	public TaasNumeric shiftLeft( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( (int)value << (int)( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric shiftRight( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( (int)value >> (int)( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumeric shiftRightUnsigned( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber(
				(int)value >>> (int)( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasBoolean strictEquals( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasBoolean( value == ( (TaasNumber)numeric ).value );
	}

	@Override
	public TaasNumber subtract( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( value - ( (TaasNumber)numeric ).value );
	}

	@Override
	public String toString()
	{
		return "[TaasNumber value: " + Double.toString( value ) + "]";
	}

	@Override
	public TaasConstant widen( final TaasType type )
	{
		if( type == NumberType.INSTANCE )
		{
			return this;
		}
		else if( type == StringType.INSTANCE )
		{
			return new TaasString( Double.toString( value ) );
		}
		else
		{
			throw new TaasException( "Can not convert from " + getType()
					+ " to " + type );
		}
	}
}
