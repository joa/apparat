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

import com.joa_ebert.apparat.taas.TaasExpression;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class AbstractUnaryExpr extends TaasExpression
{
	@TaasReference
	public TaasValue rhs;

	public final String operator;

	protected AbstractUnaryExpr( final TaasValue rhs, final String operator )
	{
		super( rhs.getType() );

		this.rhs = rhs;
		this.operator = operator;
	}

	protected AbstractUnaryExpr( final TaasValue rhs, final String operator,
			final TaasType type )
	{
		super( type );

		this.rhs = rhs;
		this.operator = operator;
	}

	@Override
	public boolean isConstant()
	{
		return rhs.isConstant();
	}

	@Override
	public String toString()
	{
		return "[AbstractUnaryExpr " + operator + " " + rhs.toString() + "]";
	}

	@Override
	public void updateType()
	{
		setType( rhs.getType() );
	}
}
