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

import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class TaasNumeric extends TaasConstant
{
	protected TaasNumeric( final TaasType type )
	{
		super( type );
	}

	public abstract TaasNumeric add( TaasNumeric numeric );

	public abstract TaasNumeric bitAnd( TaasNumeric numeric );

	public abstract TaasNumeric bitNot();

	public abstract TaasNumeric bitOr( TaasNumeric numeric );

	public abstract TaasNumeric bitXor( TaasNumeric numeric );

	public abstract TaasNumeric decrement();

	public abstract TaasNumeric divide( TaasNumeric numeric );

	public abstract TaasBoolean equals( TaasNumeric numeric );

	public abstract TaasBoolean greaterEquals( TaasNumeric numeric );

	public abstract TaasBoolean greaterThan( TaasNumeric numeric );

	public abstract TaasNumeric increment();

	public abstract TaasBoolean lessEquals( TaasNumeric numeric );

	public abstract TaasBoolean lessThan( TaasNumeric numeric );

	public abstract TaasNumeric modulo( TaasNumeric numeric );

	public abstract TaasNumeric multiply( TaasNumeric numeric );

	public abstract TaasNumeric negate();

	public abstract TaasNumeric shiftLeft( TaasNumeric numeric );

	public abstract TaasNumeric shiftRight( TaasNumeric numeric );

	public abstract TaasNumeric shiftRightUnsigned( TaasNumeric numeric );

	public abstract TaasBoolean strictEquals( TaasNumeric numeric );

	public abstract TaasNumeric subtract( TaasNumeric numeric );

	@Override
	public String toString()
	{
		return "[TaasNumeric type: " + getType().toString() + "]";
	}

	protected void verifyType( final TaasNumeric numeric,
			final Class<? extends TaasNumeric> type )
	{
		if( !numeric.getClass().equals( type ) )
		{
			throw new IllegalArgumentException( "Expected type "
					+ type.getName() + "." );
		}
	}
}
