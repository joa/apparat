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
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal0;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal1;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal2;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal3;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TSetLocal extends AbstractLocalExpr
{
	@TaasReference
	public TaasValue value;

	public TSetLocal( final TaasLocal local, final TaasValue value )
	{
		super( local );

		this.value = value;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		value.emit( environment, body, code );

		final int index = local.getIndex();

		AbstractOperation op = null;

		switch( index )
		{
			case 0:
				op = new SetLocal0();
				break;

			case 1:
				op = new SetLocal1();
				break;

			case 2:
				op = new SetLocal2();
				break;

			case 3:
				op = new SetLocal3();
				break;

			default:
				op = new SetLocal( index );
				break;
		}

		code.add( op );
	}

	@Override
	public String toString()
	{
		return "[TSetLocal " + local.toString() + " = " + value.toString()
				+ "]";
	}
}
