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

package com.joa_ebert.apparat.taas.toolkit.constantFolding;

import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.controlflow.utils.DepthFirstIterator;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasPhi;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.taas.constants.TaasBoolean;
import com.joa_ebert.apparat.taas.constants.TaasInt;
import com.joa_ebert.apparat.taas.constants.TaasNumber;
import com.joa_ebert.apparat.taas.constants.TaasNumeric;
import com.joa_ebert.apparat.taas.expr.AbstractBinaryExpr;
import com.joa_ebert.apparat.taas.expr.TAdd;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.types.IntType;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class ConstantFolding implements ITaasTool
{
	private static final TaasBoolean TRUE = new TaasBoolean( true );
	private static final TaasBoolean FALSE = new TaasBoolean( false );

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = TaasToolkit.phiCleanup( method );

		final List<TaasPhi> phiExprs = TaasToolkit.phisOf( method );

		for( final TaasPhi phiExpr : phiExprs )
		{
			if( phiExpr.values.size() == 1 )
			{
				TaasToolkit.replace( method, phiExpr,
						phiExpr.values.get( 0 ).value );
			}
		}

		try
		{
			final DepthFirstIterator<TaasVertex, TaasEdge> iter = new DepthFirstIterator<TaasVertex, TaasEdge>(
					method.code );

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				if( !vertex.value.isConstant() )
				{
					continue;
				}

				if( vertex.value instanceof TaasPhi )
				{
					//
					// Fold phi nodes if the element of values is exactly one.
					// This means the phi is unnecessary.
					//

					final TaasPhi phiExpr = (TaasPhi)vertex.value;

					if( phiExpr.values.size() == 1 )
					{
						TaasToolkit.replace( method, phiExpr, phiExpr.values
								.get( 0 ).value );
					}
				}
				else if( vertex.value instanceof AbstractBinaryExpr )
				{
					//
					// Fold binary expressions if their lhs and rhs are
					// constant.
					//

					final AbstractBinaryExpr binExpr = (AbstractBinaryExpr)vertex.value;

					final TaasValue lhs = binExpr.lhs;
					final TaasValue rhs = binExpr.rhs;

					final TaasType type = binExpr.getType();

					if( binExpr instanceof TAdd )
					{
						if( type == NumberType.INSTANCE )
						{
							final TaasNumber nlhs = (TaasNumber)lhs;
							final TaasNumber nrhs = (TaasNumber)rhs;

							final TaasNumeric result = nlhs.add( nrhs );

							TaasToolkit.replace( method, binExpr, result );

							changed = true;
						}
						else if( type == IntType.INSTANCE )
						{
							final TaasInt nlhs = (TaasInt)lhs;
							final TaasInt nrhs = (TaasInt)rhs;

							final TaasNumeric result = nlhs.add( nrhs );

							TaasToolkit.replace( method, binExpr, result );

							changed = true;
						}
					}
				}
				else if( vertex.value instanceof TIf )
				{
					//
					// Remove control flow expression if the condition is
					// constant. This will cut the edge for the unnecessary
					// flow. This will lead to dead code which can be eliminated
					// by a dead code elimination algorithm.
					//

					final TIf ifExpr = (TIf)vertex.value;

					switch( ifExpr.operator )
					{
						case True:
							if( ifExpr.lhs.equals( TRUE ) )
							{
								changed = remove( method, vertex,
										EdgeKind.False )
										|| changed;
							}
							else if( ifExpr.lhs == FALSE )
							{
								changed = remove( method, vertex, EdgeKind.True )
										|| changed;
							}
							break;

						case False:
							if( ifExpr.lhs.equals( FALSE ) )
							{
								changed = remove( method, vertex,
										EdgeKind.False )
										|| changed;
							}
							else if( ifExpr.lhs == TRUE )
							{
								changed = remove( method, vertex, EdgeKind.True )
										|| changed;
							}
							break;
					}
				}
			}
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS && changed )
		{
			TaasToolkit.debug( "ConstantFolding", method );
		}

		return changed;
	}

	private boolean remove( final TaasMethod method, final TaasVertex vertex,
			final EdgeKind kind ) throws ControlFlowGraphException
	{
		final List<TaasEdge> edges = method.code.outgoingOf( vertex );

		for( final TaasEdge edge : edges )
		{
			if( edge.kind == EdgeKind.False )
			{
				method.code.remove( edge );

				TaasToolkit.remove( method, vertex );

				return true;
			}
		}

		return false;
	}
}
