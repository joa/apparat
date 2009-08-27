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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.expr.TJump;
import com.joa_ebert.apparat.taas.expr.TLookupSwitch;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasEmitter
{
	private static final Taas TAAS = new Taas();

	private void continueWith( final TaasVertex vertex, final TaasCode code,
			final LinkedList<TaasValue> result,
			final LinkedHashMap<TaasValue, TaasValue> jumps )
			throws ControlFlowGraphException
	{
		if( result.contains( vertex.value ) )
		{
			final TJump jump = TAAS.jump();

			markJump( jump, vertex.value, jumps );

			result.add( jump );
		}
		else
		{
			order( vertex, code, result, jumps );
		}
	}

	public MethodBody emit( final TaasMethod method )
	{
		//
		// Assumptions for now:
		//
		// 1) We assume that we are not in SSA form.
		// 2) We assume all TaasPhi vertices have been resolved for us.
		// 3) No jumps exist (throw error otherwise!)
		// 

		//
		// Strategy:
		// 1) Convert CFG into a linked list of instructions (done)
		// 2) Insert TaasJump for non-reachable edges (done)
		// 3) Convert linked list to bytecode while solving jumps
		//

		//
		// Once done, work on phi-nodes. Exit SSA and expand phi-nodes for non-
		// register values into predecessors.
		//

		final LinkedList<TaasValue> list = new LinkedList<TaasValue>();
		final LinkedHashMap<TaasValue, TaasValue> jumps = new LinkedHashMap<TaasValue, TaasValue>();

		try
		{
			order( method.code.getEntryVertex(), method.code, list, jumps );
		}
		catch( final ControlFlowGraphException exception )
		{
			throw new TaasException( exception );
		}

		System.out.println( "List of instructions:\n" );

		for( final TaasValue value : list )
		{
			System.out.println( value.toString() );
		}

		System.out.println( "\nList of jumps:\n" );

		for( final Entry<TaasValue, TaasValue> jump : jumps.entrySet() )
		{
			System.out.println( jump.getKey().toString() + " -> "
					+ jump.getValue().toString() );
		}

		return null;
	}

	private void invalidCode()
	{
		throw new TaasException( "Invalid code." );
	}

	private void markJump( final TaasValue from, final TaasValue to,
			final LinkedHashMap<TaasValue, TaasValue> jumps )
	{
		jumps.put( from, to );
	}

	private void order( final TaasVertex vertex, final TaasCode code,
			final LinkedList<TaasValue> list,
			final LinkedHashMap<TaasValue, TaasValue> jumps )
			throws ControlFlowGraphException
	{
		final VertexKind kind = vertex.kind;

		if( VertexKind.Entry == kind )
		{
			//
			// The entry has only one outgoing edge which will be the
			// first vertex to evaluate.
			//

			final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

			if( 1 != outgoingEdges.size() )
			{
				invalidCode();
			}
			else
			{
				order( outgoingEdges.get( 0 ).endVertex, code, list, jumps );
			}
		}
		else if( VertexKind.Exit == kind )
		{
			//
			// We have reached the exit, stop here.
			//

			return;
		}
		else if( VertexKind.Default == kind )
		{
			final TaasValue value = vertex.value;

			list.add( value );

			if( value instanceof TJump )
			{
				throw new TaasException( "Unexpected TJump in code." );
			}
			else if( value instanceof TIf )
			{
				TaasEdge trueEdge = null;
				TaasEdge falseEdge = null;

				final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

				for( final TaasEdge edge : outgoingEdges )
				{
					final EdgeKind edgeKind = edge.kind;

					if( EdgeKind.True == edgeKind )
					{
						if( null != trueEdge )
						{
							invalidCode();
						}

						trueEdge = edge;
					}
					else if( EdgeKind.False == edgeKind )
					{
						if( null != falseEdge )
						{
							invalidCode();
						}

						falseEdge = edge;
					}
				}

				if( null == trueEdge || null == falseEdge )
				{
					invalidCode();
				}
				else
				{
					//
					// We continue with the false-branch since it will
					// fall through.
					//
					continueWith( falseEdge.endVertex, code, list, jumps );

					//
					// The true-branch is a jump we have to mark.
					//

					final TaasValue trueValue = trueEdge.endVertex.value;

					markJump( value, trueValue, jumps );

					//
					// We might have visited the true-branch already so we
					// will only continue if it has not been visited before.
					//

					if( !list.contains( trueValue ) )
					{
						continueWith( trueEdge.endVertex, code, list, jumps );
					}
				}
			}
			else if( value instanceof TLookupSwitch )
			{
				// TODO implement this mess...
			}
			else
			{
				TaasEdge defaultEdge = null;

				final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

				for( final TaasEdge edge : outgoingEdges )
				{
					final EdgeKind edgeKind = edge.kind;

					if( EdgeKind.Default == edgeKind )
					{
						if( null != defaultEdge )
						{
							invalidCode();
						}

						defaultEdge = edge;
					}
				}

				if( null == defaultEdge )
				{
					invalidCode();
				}

				continueWith( defaultEdge.endVertex, code, list, jumps );
			}
		}
	}
}
