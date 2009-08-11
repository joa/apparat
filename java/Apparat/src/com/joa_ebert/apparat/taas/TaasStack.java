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

import java.util.Stack;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasStack
{
	private final Stack<TaasValue> stack = new Stack<TaasValue>();

	private final int maxStack;

	public TaasStack()
	{
		this( Integer.MAX_VALUE );
	}

	public TaasStack( final int maxStack )
	{
		this.maxStack = maxStack;
	}

	@Override
	public TaasStack clone()
	{
		final TaasStack result = new TaasStack( maxStack );

		final int n = stack.size();

		for( int i = 0; i < n; ++i )
		{
			result.stack.push( stack.get( i ) );
		}

		return result;
	}

	public String dump()
	{
		final StringBuilder builder = new StringBuilder( "Stack dump:" );

		for( final TaasValue value : stack )
		{
			builder.append( "\n" + value.toString() );
		}

		return builder.toString();
	}

	public TaasValue get( final int index )
	{
		return stack.get( index );
	}

	public TaasValue peek()
	{
		return stack.peek();
	}

	public TaasValue pop() throws TaasException
	{
		if( 0 == stack.size() )
		{
			throw new TaasException( "Stack underflow error." );
		}

		return stack.pop();
	}

	public TaasValue push( final TaasValue value ) throws TaasException
	{
		if( stack.size() >= maxStack )
		{
			throw new TaasException( "Stack overflow error." );
		}

		return stack.push( value );
	}

	public TaasValue set( final int index, final TaasValue value )
	{
		return stack.set( index, value );
	}

	public int size()
	{
		return stack.size();
	}

	@Override
	public String toString()
	{
		return "[TaasStack]";
	}
}
