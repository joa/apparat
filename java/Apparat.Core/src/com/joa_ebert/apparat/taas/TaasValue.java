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

package com.joa_ebert.apparat.taas;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class TaasValue
{
	private boolean emitted = false;
	private TaasType type;

	public TaasValue( final TaasType type )
	{
		this.type = type;
	}

	public abstract TaasValue dup();

	public void emit( final AbcEnvironment environment, final MethodBody body,
			final Bytecode code )
	{
		if( emitted )
		{
			throw new TaasException( "Value has already been emitted." );
		}

		emitOps( environment, body, code );
		emitted = true;
	}

	protected abstract void emitOps( AbcEnvironment environment,
			MethodBody body, Bytecode code );

	public TaasType getType()
	{
		if( this instanceof TaasType )
		{
			return (TaasType)this;
		}

		return type;
	}

	public boolean isConstant()
	{
		return false;
	}

	public boolean isEmitted()
	{
		return emitted;
	}

	public boolean isType( final TaasType type )
	{
		return getType().equals( type );
	}

	public void setType( final TaasType value )
	{
		if( this instanceof TaasType )
		{
			throw new TaasException( "Can not set the type of a type." );
		}

		type = value;
	}

	@Override
	public String toString()
	{
		return "[TaasValue type: " + type.toString() + "]";
	}

	public void updateType()
	{

	}
}
