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

import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TIf extends AbstractControlTransferExpr
{
	public static enum Operator
	{
		Equal( "==" ),
		False( "!" ),
		GreaterEqual( ">=" ),
		GreaterThan( ">" ),
		LessEqual( "<=" ),
		LessThan( "<" ),
		NotEqual( "!=" ),
		NotGreaterEqual( "!>=" ),
		NotGreaterThan( "!>" ),
		NotLessEqual( "!<=" ),
		NotLessThan( "!<" ),
		StrictEqual( "===" ),
		StrictNotEqual( "!===" ),
		True( "" );

		private final String stringRep;

		private Operator( final String stringRep )
		{
			this.stringRep = stringRep;
		}

		@Override
		public String toString()
		{
			return stringRep;
		}
	}

	@TaasReference
	public TaasValue lhs;

	@TaasReference
	public TaasValue rhs;

	public final Operator operator;

	public TIf( final TaasValue lhs, final Operator operator )
	{
		this( lhs, null, operator );
	}

	public TIf( final TaasValue lhs, final TaasValue rhs,
			final Operator operator )
	{
		this.lhs = lhs;
		this.rhs = rhs;

		this.operator = operator;
	}

	@Override
	public boolean isConstant()
	{
		if( lhs != null && rhs != null )
		{
			return lhs.isConstant() && rhs.isConstant();
		}
		else if( lhs != null && rhs == null )
		{
			return lhs.isConstant();
		}
		else
		{
			return rhs.isConstant();
		}
	}

	@Override
	public String toString()
	{
		if( null != rhs )
		{
			return "[TIf " + lhs.toString() + " " + operator.toString() + " "
					+ rhs.toString() + "]";
		}
		else
		{
			return "[TIf " + operator.toString() + lhs.toString() + "]";
		}
	}
}
