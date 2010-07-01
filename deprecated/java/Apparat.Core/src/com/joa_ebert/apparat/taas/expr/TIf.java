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
import com.joa_ebert.apparat.abc.bytecode.operations.AbstractConditionalJump;
import com.joa_ebert.apparat.abc.bytecode.operations.IfEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfFalse;
import com.joa_ebert.apparat.abc.bytecode.operations.IfGreaterEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfGreaterThan;
import com.joa_ebert.apparat.abc.bytecode.operations.IfLessEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfLessThan;
import com.joa_ebert.apparat.abc.bytecode.operations.IfNotEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfNotGreaterEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfNotGreaterThan;
import com.joa_ebert.apparat.abc.bytecode.operations.IfNotLessEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfNotLessThan;
import com.joa_ebert.apparat.abc.bytecode.operations.IfStrictEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfStrictNotEqual;
import com.joa_ebert.apparat.abc.bytecode.operations.IfTrue;
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

	public Operator operator;

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
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		if( null != lhs )
		{
			lhs.emit( environment, body, code );
		}

		if( null != rhs )
		{
			rhs.emit( environment, body, code );
		}

		AbstractConditionalJump jump = null;

		switch( operator )
		{
			case Equal:
				jump = new IfEqual();
				break;

			case False:
				jump = new IfFalse();
				break;

			case GreaterEqual:
				jump = new IfGreaterEqual();
				break;

			case GreaterThan:
				jump = new IfGreaterThan();
				break;

			case LessEqual:
				jump = new IfLessEqual();
				break;

			case LessThan:
				jump = new IfLessThan();
				break;

			case NotEqual:
				jump = new IfNotEqual();
				break;

			case NotGreaterEqual:
				jump = new IfNotGreaterEqual();
				break;

			case NotGreaterThan:
				jump = new IfNotGreaterThan();
				break;

			case NotLessEqual:
				jump = new IfNotLessEqual();
				break;

			case NotLessThan:
				jump = new IfNotLessThan();
				break;

			case StrictEqual:
				jump = new IfStrictEqual();
				break;

			case StrictNotEqual:
				jump = new IfStrictNotEqual();
				break;

			case True:
				jump = new IfTrue();
				break;
		}

		code.add( jump );
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
