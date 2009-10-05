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

package com.joa_ebert.apparat.abc;

import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.bytecode.Bytecode;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class MethodBody implements ITraitsOwner
{
	public Method method;
	public int maxStack;
	public int localCount;
	public int initScopeDepth;
	public int maxScopeDepth;
	public Bytecode code;
	public List<ExceptionHandler> exceptions;
	public List<AbstractTrait> traits;

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );

		if( null != exceptions )
		{
			for( final ExceptionHandler handler : exceptions )
			{
				handler.accept( context, visitor );
			}
		}

		if( null != traits )
		{
			for( final AbstractTrait trait : traits )
			{
				trait.accept( context, visitor );
			}
		}
	}

	public ExceptionHandler getExceptionHandler( final int index )
	{
		if( null != exceptions )
		{
			return exceptions.get( index );
		}

		return null;
	}

	public int getIndex( final ExceptionHandler value )
	{
		if( null == value )
		{
			return 0;
		}

		if( null == exceptions )
		{
			exceptions = new LinkedList<ExceptionHandler>();
		}

		final int index = exceptions.indexOf( value );

		if( -1 == index )
		{
			exceptions.add( value );

			return exceptions.size() - 1;
		}

		return index;
	}

	@Override
	public List<AbstractTrait> getTraits()
	{
		return traits;
	}
}
