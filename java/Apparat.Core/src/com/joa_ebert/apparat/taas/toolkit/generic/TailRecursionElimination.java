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

package com.joa_ebert.apparat.taas.toolkit.generic;

import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.Taas;
import com.joa_ebert.apparat.taas.TaasCode;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.taas.constants.TaasMultiname;
import com.joa_ebert.apparat.taas.expr.TCallProperty;
import com.joa_ebert.apparat.taas.expr.TReturn;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.types.MultinameType;

/**
 * @author Joa Ebert
 * 
 */
public final class TailRecursionElimination implements ITaasTool
{
	private static final Taas TAAS = new Taas();

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = false;

		final TaasCode code = method.code;
		final List<TaasVertex> vertices = code.vertexList();
		final AbcEnvironment.PropertyInfo propertyInfo = method.propertyInfo;

		TReturn targetReturn = null;
		TaasVertex returnVertex = null;
		TCallProperty targetCallProperty = null;

		for( final TaasVertex vertex : vertices )
		{
			final TaasValue value = vertex.value;

			if( value instanceof TReturn )
			{
				final TReturn return$ = (TReturn)value;

				if( return$.value instanceof TCallProperty )
				{
					final TCallProperty callProperty = (TCallProperty)return$.value;

					if( null != callProperty )
					{
						final TaasValue object = callProperty.object;
						final TaasMultiname property = callProperty.property;

						if( object.getType() instanceof MultinameType
								&& property.getType() instanceof MultinameType )
						{
							final MultinameType mobj = (MultinameType)object
									.getType();
							final MultinameType mprp = (MultinameType)property
									.getType();

							if( mobj.runtimeName != null
									|| mprp.runtimeName != null )
							{
								continue;
							}

							final AbcEnvironment.PropertyInfo calledProperty = method.typer
									.findProperty( mobj, mprp );

							if( null == calledProperty )
							{
								continue;
							}
							else
							{
								if( propertyInfo.equals( calledProperty ) )
								{
									//
									// Found tail recursion.
									//

									returnVertex = vertex;
									targetReturn = return$;
									targetCallProperty = callProperty;

									break;
								}
							}
						}
					}
				}
			}
		}

		if( null != targetReturn && null != targetCallProperty )
		{
			changed = true;

			final TaasValue[] parameters = targetCallProperty.parameters;
			final TaasLocal[] registers = new TaasLocal[ parameters.length ];

			for( int i = 0, n = parameters.length; i < n; ++i )
			{
				registers[ i ] = TaasToolkit.createRegister( method );
			}

			TaasVertex lastVertex = null;

			for( int i = 0, n = parameters.length; i < n; ++i )
			{
				registers[ i ].typeAs( parameters[ i ].getType() );

				TaasToolkit.insertBefore( method, returnVertex, new TaasVertex(
						TAAS.setLocal( registers[ i ], parameters[ i ] ) ) );
			}

			for( int i = 0, n = parameters.length; i < n; ++i )
			{
				lastVertex = new TaasVertex( TAAS.setLocal( method.locals
						.get( 1 + i ), registers[ i ] ) );

				TaasToolkit.insertBefore( method, returnVertex, lastVertex );
			}

			try
			{
				TaasToolkit.remove( method, returnVertex );

				TaasEdge exitEdge = null;

				final List<TaasEdge> outgoingOf = code.outgoingOf( lastVertex );

				for( final TaasEdge edge : outgoingOf )
				{
					if( edge.endVertex.kind == VertexKind.Exit )
					{
						exitEdge = edge;
						break;
					}
				}

				if( null == exitEdge )
				{
					throw new TaasException( "Code got corrupted." );
				}

				exitEdge.endVertex = code.outgoingOf( code.getEntryVertex() )
						.get( 0 ).endVertex;
			}
			catch( final ControlFlowGraphException exception )
			{
				throw new TaasException( exception );
			}
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS && changed )
		{
			TaasToolkit.debug( "TailRecursionElimination", method );
		}

		return changed;
	}
}
