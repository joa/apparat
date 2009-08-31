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

package com.joa_ebert.apparat.taas.toolkit.copyPropagation;

import java.util.Iterator;
import java.util.LinkedList;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class CopyPropagation implements ITaasTool
{
	private boolean changed;

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		changed = false;

		try
		{
			final LinkedList<TaasVertex> list = method.code.vertexList();
			final LinkedList<TaasVertex> removes = new LinkedList<TaasVertex>();
			final Iterator<TaasVertex> iter = list.listIterator();

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( VertexKind.Default != vertex.kind )
				{
					continue;
				}

				final TaasValue value = vertex.value;

				if( value instanceof TaasConstant && value.isConstant() )
				{
					removes.add( vertex );
				}
				else if( value instanceof TaasLocal )
				{
					removes.add( vertex );
				}
				else
				{
					final Iterator<TaasVertex> refIter = list
							.descendingIterator();

					boolean canRemove = false;

					while( refIter.hasNext() )
					{
						final TaasVertex refVert = refIter.next();
						final TaasValue refValue = refVert.value;

						if( refValue == value )
						{
							break;
						}
						else if( TaasToolkit.references( refValue, value ) )
						{
							canRemove = true;
							break;
						}
					}

					if( canRemove )
					{
						removes.add( vertex );
					}
				}
			}

			changed = !removes.isEmpty();

			for( final TaasVertex vertex : removes )
			{
				TaasToolkit.remove( method, vertex );
			}
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}

		return changed;
	}
}
