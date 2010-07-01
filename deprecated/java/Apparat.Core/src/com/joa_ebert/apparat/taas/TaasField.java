/*
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

package com.joa_ebert.apparat.taas;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.operations.GetGlobalSlot;
import com.joa_ebert.apparat.abc.bytecode.operations.GetSlot;
import com.joa_ebert.apparat.taas.constants.TaasGlobalScope;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * @author Joa Ebert
 * 
 */
public class TaasField extends TaasValue
{
	@TaasReference
	public TaasValue object;

	public final int slot;

	public TaasField( final TaasValue object, final TaasType type )
	{
		this( object, type, -1 );
	}

	public TaasField( final TaasValue object, final TaasType type,
			final int slot )
	{
		super( type );

		this.object = object;
		this.slot = slot;
	}

	@Override
	public TaasValue dup()
	{
		return this;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		if( object instanceof TaasGlobalScope )
		{
			code.add( new GetGlobalSlot( slot ) );
		}
		else
		{
			object.emit( environment, body, code );
			code.add( new GetSlot( slot ) );
		}
	}

	@Override
	public boolean hasSideEffects()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "[TaasField object: " + object + ", slot: " + slot + ", type: "
				+ getType().toString() + "]";
	}

}
