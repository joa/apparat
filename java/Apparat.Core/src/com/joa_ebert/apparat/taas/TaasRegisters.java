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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasRegisters
{
	private final List<TaasLocal> registers;
	private int numRegisters;

	public TaasRegisters( final int numRegisters )
	{
		registers = new ArrayList<TaasLocal>( numRegisters );

		for( int i = 0; i < numRegisters; ++i )
		{
			registers.add( new TaasLocal( i ) );
		}

		this.numRegisters = numRegisters;
	}

	public void add( final TaasLocal local )
	{
		final int index = local.getIndex();

		for( final TaasLocal localToCompare : registers )
		{
			if( localToCompare.getIndex() == index )
			{
				if( null != get( local.getIndex() ) )
				{
					throw new TaasException( "Register " + index
							+ " already exists." );
				}
			}
		}

		registers.add( local );
	}

	@Override
	public TaasRegisters clone()
	{
		final TaasRegisters result = new TaasRegisters( numRegisters );

		for( int i = 0; i < numRegisters; ++i )
		{
			result.registers.set( i, registers.get( i ) );
		}

		return result;
	}

	public TaasLocal create()
	{
		final TaasLocal result = new TaasLocal( numRegisters++ );

		registers.add( result );

		return result;
	}

	public String debug()
	{
		final StringBuilder builder = new StringBuilder( "Registers:" );

		for( int i = 0; i < numRegisters; ++i )
		{
			builder.append( "\n" + Integer.toString( i ) + ": "
					+ registers.get( i ).toString() );
		}

		return builder.toString();
	}

	public void debug( final OutputStream output )
	{
		debug( new PrintStream( output ) );
	}

	public void debug( final PrintStream output )
	{
		output.print( debug() );
		output.flush();
	}

	public TaasLocal get( final int index )
	{
		return get( index, 0 );
	}

	public TaasLocal get( final int index, final int subscript )
	{
		for( final TaasLocal local : registers )
		{
			if( local.getIndex() == index && local.getSubscript() == subscript )
			{
				return local;
			}
		}

		throw new TaasException( "Register (" + index + "," + subscript
				+ ") does not exist." );
	}

	public List<TaasLocal> getRegisterList()
	{
		return registers;
	}

	public int numRegisters()
	{
		return numRegisters;
	}

	public void offset( final int value )
	{
		for( final TaasLocal local : registers )
		{
			local.setIndex( local.getIndex() + value );
		}
	}
}
