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
import com.joa_ebert.apparat.abc.bytecode.operations.Decrement;
import com.joa_ebert.apparat.abc.bytecode.operations.DecrementInt;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.IntType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TDecrement extends AbstractUnaryExpr
{
	private static final String OPERATOR = "--";

	public TDecrement( final TaasValue rhs )
	{
		super( rhs, OPERATOR );
	}

	@Override
	public TaasValue dup()
	{
		return new TDecrement( rhs.dup() );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		rhs.emit( environment, body, code );
		code.add( getType() == IntType.INSTANCE ? new DecrementInt()
				: new Decrement() );
	}

	@Override
	public String toString()
	{
		return "[TDecrement " + rhs.toString() + "]";
	}
}
