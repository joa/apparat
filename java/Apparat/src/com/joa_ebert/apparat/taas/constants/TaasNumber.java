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

import com.joa_ebert.apparat.taas.types.NumberType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasNumber extends TaasNumeric
{
	public static final TaasNumber NaN = new TaasNumber( Double.NaN );

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
	public TaasNumber divide( final TaasNumeric numeric )
	{
		verifyType( numeric, TaasNumber.class );
		return new TaasNumber( value / ( (TaasNumber)numeric ).value );
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
}
