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
import com.joa_ebert.apparat.abc.bytecode.operations.AsType;
import com.joa_ebert.apparat.abc.bytecode.operations.AsTypeLate;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.constants.TaasMultiname;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TAsType extends AbstractBinaryExpr
{
	private static final String OPERATOR = "as";

	public TAsType( final TaasValue lhs, final TaasValue rhs )
	{
		super( lhs, rhs, OPERATOR, rhs.getType() );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		lhs.emit( environment, body, code );

		if( rhs.isConstant() )
		{
			if( rhs instanceof TaasMultiname )
			{
				rhs.emit( environment, body, code );
				code.add( new AsType( ( (TaasMultiname)rhs ).multiname ) );
			}
			else
			{
				throw new TaasException( "TaasMultiname expected." );
			}
		}
		else
		{
			rhs.emit( environment, body, code );
			code.add( new AsTypeLate() );
		}

	}
}
