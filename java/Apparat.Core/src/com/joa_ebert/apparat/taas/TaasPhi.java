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

import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.UnknownType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasPhi extends TaasExpression
{
	public static final class Element
	{
		@TaasReference
		public TaasValue value;

		public TaasEdge edge;

		public Element( final TaasValue value, final TaasEdge edge )
		{
			this.value = value;
			this.edge = edge;
		}

		@Override
		public String toString()
		{
			return value.toString();
		}
	}

	public final List<Element> values = new LinkedList<Element>();

	public TaasPhi( final TaasValue value, final TaasEdge edge )
	{
		super( value.getType() );

		values.add( new Element( value, edge ) );
	}

	public boolean add( final TaasValue value, final TaasEdge edge )
	{
		final boolean result = values.add( new Element( value, edge ) );

		updateTypeInformation();

		return result;
	}

	public boolean contains( final TaasValue value )
	{
		for( final Element element : values )
		{
			if( element.value.equals( value ) )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public TaasValue dup()
	{
		throw new TaasException( "Can not duplicate a TaasPhi expression." );
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		throw new TaasException( "TaasPhi existing during emit." );
	}

	@Override
	public boolean isConstant()
	{
		return( values.size() == 1 && values.get( 0 ).value.isConstant() );
	}

	public boolean remove( final TaasValue value )
	{
		Element elementToRemove = null;

		for( final Element element : values )
		{
			if( element.value.equals( value ) )
			{
				elementToRemove = element;
				break;
			}
		}

		if( null == elementToRemove )
		{
			return false;
		}

		final boolean result = values.remove( elementToRemove );

		updateTypeInformation();

		return result;
	}

	@Override
	public String toString()
	{
		return "[TaasPhi " + values.toString() + "]";
	}

	private void updateTypeInformation()
	{
		TaasType currentType = null;

		if( values.isEmpty() )
		{
			setType( UnknownType.INSTANCE );
		}
		else
		{
			int n = values.size();

			while( --n > -1 )
			{
				currentType = ( null == currentType ) ? values.get( n ).value
						.getType() : TaasType.typeOf( currentType, values
						.get( n ).value.getType() );
			}

			setType( currentType );
		}
	}
}
