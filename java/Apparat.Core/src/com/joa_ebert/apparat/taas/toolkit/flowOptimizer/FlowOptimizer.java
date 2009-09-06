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

package com.joa_ebert.apparat.taas.toolkit.flowOptimizer;

import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.controlflow.utils.SCCFinder;
import com.joa_ebert.apparat.controlflow.utils.SCComponent;
import com.joa_ebert.apparat.taas.TaasCode;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;

/**
 * @author Joa Ebert
 * 
 */
public class FlowOptimizer implements ITaasTool
{
	private TIf.Operator invertOp( final TIf.Operator op )
	{
		switch( op )
		{
			case Equal:
				return TIf.Operator.NotEqual;

			case False:
				return TIf.Operator.True;

			case GreaterEqual:
				return TIf.Operator.NotGreaterEqual;

			case GreaterThan:
				return TIf.Operator.NotGreaterThan;

			case LessEqual:
				return TIf.Operator.NotLessEqual;

			case LessThan:
				return TIf.Operator.NotLessThan;

			case NotEqual:
				return TIf.Operator.Equal;

			case NotGreaterEqual:
				return TIf.Operator.GreaterEqual;

			case NotGreaterThan:
				return TIf.Operator.GreaterThan;

			case NotLessEqual:
				return TIf.Operator.LessEqual;

			case NotLessThan:
				return TIf.Operator.LessThan;

			case StrictEqual:
				return TIf.Operator.StrictNotEqual;

			case StrictNotEqual:
				return TIf.Operator.StrictEqual;

			case True:
				return TIf.Operator.False;

			default:
				throw new TaasException( "Unreachable by definition." );
		}
	}

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = false;

		final SCCFinder<TaasVertex, TaasEdge> sccFinder = new SCCFinder<TaasVertex, TaasEdge>();

		try
		{
			final TaasCode code = method.code;
			final List<SCComponent<TaasVertex>> sccs = sccFinder.find( code );

			for( final SCComponent<TaasVertex> scc : sccs )
			{
				for( final TaasVertex vertex : scc.vertices )
				{
					if( vertex.kind != VertexKind.Default )
					{
						continue;
					}

					final TaasValue value = vertex.value;

					if( value instanceof TIf )
					{
						TaasEdge trueEdge = null;
						TaasEdge falseEdge = null;

						for( final TaasEdge edge : code.outgoingOf( vertex ) )
						{
							if( EdgeKind.True == edge.kind )
							{
								trueEdge = edge;
							}
							else if( EdgeKind.False == edge.kind )
							{
								falseEdge = edge;
							}
						}

						if( null == falseEdge || null == trueEdge )
						{
							// let DCE/CF clean this one up...
							continue;
						}

						if( scc.vertices.contains( trueEdge.endVertex )
								&& !scc.vertices.contains( falseEdge.endVertex ) )
						{
							final TIf taasIf = (TIf)value;

							taasIf.operator = invertOp( taasIf.operator );

							final TaasVertex h = trueEdge.endVertex;

							trueEdge.endVertex = falseEdge.endVertex;
							falseEdge.endVertex = h;

							changed = true;
						}
					}
				}
			}

		}
		catch( final ControlFlowGraphException exception )
		{
			throw new TaasException( exception );
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS && changed )
		{
			TaasToolkit.debug( "FlowOptimizer", method );
		}

		return changed;
	}
}
