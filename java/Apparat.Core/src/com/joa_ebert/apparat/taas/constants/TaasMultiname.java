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
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasTyper;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.MultinameType;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasMultiname extends TaasConstant
{
	public final AbstractMultiname multiname;

	@TaasReference
	public final TaasValue runtimeName;

	public final TaasNamespace runtimeNamespace;

	private final boolean noRuntimeName;

	public TaasMultiname( final AbstractMultiname multiname )
	{
		super( TaasTyper.toTaasType( multiname ) );

		this.multiname = multiname;
		this.runtimeName = null;
		this.runtimeNamespace = null;

		noRuntimeName = true;

		switch( multiname.kind )
		{
			case RTQName:
			case RTQNameL:
			case MultinameL:
				throw new TaasException(
						"This runtime multiname needs additional information." );
		}
	}

	public TaasMultiname( final AbstractMultiname multiname,
			final TaasNamespace namespace, final TaasValue name )
	{
		super( new MultinameType( multiname, namespace, name ) );

		this.multiname = multiname;
		this.runtimeName = name;
		this.runtimeNamespace = namespace;

		noRuntimeName = false;
	}

	@Override
	public TaasValue dup()
	{
		return ( noRuntimeName ) ? new TaasMultiname( multiname )
				: new TaasMultiname( multiname, runtimeNamespace, runtimeName );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		switch( multiname.kind )
		{
			case RTQName:
				runtimeNamespace.emit( environment, body, code );
				break;
			case RTQNameL:
				runtimeName.emit( environment, body, code );
				runtimeNamespace.emit( environment, body, code );
				break;
			case MultinameL:
				runtimeName.emit( environment, body, code );
				break;
		}
	}

	@Override
	public boolean isConstant()
	{
		return noRuntimeName;
	}

	@Override
	public String toString()
	{
		switch( multiname.kind )
		{
			case RTQName:
				return "[TaasMultiname runtimeName: " + runtimeName.toString()
						+ ", type: " + getType().toString() + "]";
			case RTQNameL:
				return "[TaasMultiname runtimeName: " + runtimeName.toString()
						+ ", runtimeNamespace: " + runtimeNamespace.toString()
						+ ", type: " + getType().toString() + "]";
			case MultinameL:
				return "[TaasMultiname runtimeName: " + runtimeName.toString()
						+ ", type: " + getType().toString() + "]";

			default:
				return "[TaasMultiname type: " + getType().toString() + "]";
		}
	}

	@Override
	public TaasConstant widen( final TaasType type )
	{
		throw new TaasException( "Can not convert from " + getType() + " to "
				+ type );
	}
}
