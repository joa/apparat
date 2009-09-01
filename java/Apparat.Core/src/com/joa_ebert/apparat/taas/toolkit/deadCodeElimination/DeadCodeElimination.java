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

package com.joa_ebert.apparat.taas.toolkit.deadCodeElimination;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.TaasCode;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.expr.AbstractLocalExpr;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class DeadCodeElimination implements ITaasTool
{
	private boolean changed;

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		changed = false;

		try
		{
			final TaasCode graph = method.code;
			final List<TaasVertex> vertices = graph.vertexList();
			final List<TaasVertex> removes = new LinkedList<TaasVertex>();
			final List<TaasLocal> deadLocals = new LinkedList<TaasLocal>();

			//
			// Remove any local variable with no use.
			//

			for( final TaasLocal local : method.locals.getRegisterList() )
			{
				if( local.getIndex() != 0 )
				{
					deadLocals.add( local );
				}
			}

			for( final TaasVertex vertex : vertices )
			{
				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				final Iterator<TaasLocal> iter = deadLocals.iterator();

				while( iter.hasNext() )
				{
					final TaasLocal local = iter.next();

					if( TaasToolkit.references( vertex.value, local ) )
					{
						iter.remove();
					}
				}

				if( deadLocals.isEmpty() )
				{
					break;
				}
			}

			if( !deadLocals.isEmpty() )
			{
				do
				{
					removes.clear();

					for( final TaasVertex vertex : vertices )
					{
						if( vertex.kind != VertexKind.Default )
						{
							continue;
						}

						final TaasValue value = vertex.value;

						if( value instanceof AbstractLocalExpr )
						{
							final AbstractLocalExpr localExpr = (AbstractLocalExpr)value;

							for( final TaasLocal local : deadLocals )
							{
								if( local == localExpr.local )
								{
									removes.add( vertex );
									break;
								}
							}
						}
					}

					for( final TaasVertex vertex : removes )
					{
						TaasToolkit.remove( method, vertex );

						changed = true;
					}

					if( !removes.isEmpty() )
					{
						changed = TaasToolkit.phiCleanup( method ) || changed;
					}
				}
				while( !removes.isEmpty() );

				for( final TaasLocal deadLocal : deadLocals )
				{
					method.locals.remove( deadLocal );
				}

				method.locals.defragment();
			}

			//
			// Remove any vertex V of G with indegree(G,V) = 0
			// 

			do
			{
				removes.clear();

				for( final TaasVertex vertex : vertices )
				{
					if( vertex.kind != VertexKind.Default )
					{
						continue;
					}

					if( 0 == graph.indegreeOf( vertex ) )
					{
						removes.add( vertex );
					}
				}

				for( final TaasVertex vertex : removes )
				{
					TaasToolkit.remove( method, vertex );

					changed = true;
				}

				if( !removes.isEmpty() )
				{
					changed = TaasToolkit.phiCleanup( method ) || changed;
				}
			}
			while( !removes.isEmpty() );
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}

		return changed;
	}
}
