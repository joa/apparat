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

package com.joa_ebert.apparat.abc.bytecode.analysis;

import java.util.HashMap;
import java.util.Map;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Marker;
import com.joa_ebert.apparat.abc.bytecode.MarkerManager;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.Jump;
import com.joa_ebert.apparat.abc.bytecode.operations.LookupSwitch;
import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class ControlFlowGraphBuilder implements IInterpreter
{
	private final ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> graph = new ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>>();
	private final Map<AbstractOperation, BytecodeVertex> vertices = new HashMap<AbstractOperation, BytecodeVertex>();
	private BytecodeVertex entryPoint;
	private BytecodeVertex exitPoint;
	private Bytecode bytecode;
	private MarkerManager markers;

	public ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> getGraph()
	{
		return graph;
	}

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		reset();

		this.bytecode = bytecode;

		markers = bytecode.markers;

		entryPoint = new BytecodeVertex( VertexKind.Entry );
		exitPoint = new BytecodeVertex( VertexKind.Exit );

		try
		{
			//
			// Setup the graph. This allows easy backwards and forwards jumping.
			//

			graph.setEntryPoint( entryPoint );
			graph.setExitPoint( exitPoint );

			for( final AbstractOperation operation : bytecode )
			{
				final BytecodeVertex vertex = new BytecodeVertex( operation );

				vertices.put( operation, vertex );
				graph.add( vertex );
			}

			//
			// Add edges for all vertices that contain an operation which
			// can throw.
			//

			for( final ExceptionHandler exceptionHandler : bytecode.methodBody.exceptions )
			{
				AbstractOperation currentOperation = markers
						.getOperationFor( exceptionHandler.from );

				final AbstractOperation lastOperation = markers
						.getOperationFor( exceptionHandler.to );
				final AbstractOperation targetOperation = markers
						.getOperationFor( exceptionHandler.target );

				do
				{
					final int index = bytecode.indexOf( currentOperation );

					if( null != currentOperation && null != targetOperation
							&& Op.canThrow( currentOperation ) )
					{
						graph.add( new Edge<BytecodeVertex>(
								vertex( currentOperation ),
								vertex( targetOperation ), EdgeKind.Throw ) );
					}

					if( index != -1 && index != ( bytecode.size() - 1 ) )
					{
						currentOperation = bytecode.get( bytecode
								.indexOf( currentOperation ) + 1 );
					}
					else
					{
						break;
					}
				}
				while( currentOperation != lastOperation );

			}

			//
			// Start from the beginning of the method.
			//

			walk( 0, entryPoint, vertex( bytecode.get( 0 ) ) );

			for( final ExceptionHandler exceptionHandler : bytecode.methodBody.exceptions )
			{
				final int index = bytecode.indexOf( vertex(
						exceptionHandler.target ).getOperation() ) + 1;

				final BytecodeVertex nextVertex = ( index != bytecode.size() ) ? vertex( bytecode
						.get( index ) )
						: null;

				if( null != nextVertex )
				{
					//
					// Start from the beginning of an exception handler.
					//

					walk( 0, vertex( exceptionHandler.target ), nextVertex );
				}
			}
		}
		catch( final ControlFlowGraphException e )
		{
			e.printStackTrace();
		}
	}

	public void reset()
	{
		graph.clear();
		vertices.clear();
	}

	private BytecodeVertex vertex( final AbstractOperation operation )
	{
		return vertices.get( operation );
	}

	private BytecodeVertex vertex( final Marker marker )
	{
		return vertices.get( markers.getOperationFor( marker ) );
	}

	private void walk( final int scope, final BytecodeVertex previousVertex,
			final BytecodeVertex currentVertex )
			throws ControlFlowGraphException
	{
		walk( scope, previousVertex, currentVertex, EdgeKind.Default );
	}

	private void walk( final int scope, final BytecodeVertex previousVertex,
			final BytecodeVertex currentVertex, final EdgeKind kind )
			throws ControlFlowGraphException
	{
		//
		// NOTE: This method can be optimized a lot of course by getting rid of
		// the recursion.
		//

		if( null == previousVertex || null == currentVertex
				|| previousVertex.equals( currentVertex ) )
		{
			//
			// Stop if we create a loop or reach a null-vertex.
			//

			return;
		}

		if( !graph.containsEdge( previousVertex, currentVertex ) )
		{
			//
			// Add the edge only if it is a new path.
			//

			graph.add( new Edge<BytecodeVertex>( previousVertex, currentVertex,
					kind ) );
		}
		else
		{
			//
			// This path has already been visited. Stop.
			//

			return;
		}

		BytecodeVertex nextVertex = null;
		int code = -1;

		if( currentVertex.kind != VertexKind.Entry
				&& currentVertex.kind != VertexKind.Exit )
		{
			code = currentVertex.getOperation().code;

			final int index = bytecode.indexOf( currentVertex.getOperation() ) + 1;

			if( index != bytecode.size() )
			{
				nextVertex = vertex( bytecode.get( index ) );
			}
		}

		if( null == nextVertex && -1 == code )
		{
			return;
		}

		switch( code )
		{
			case Op.IfEqual:
			case Op.IfFalse:
			case Op.IfGreaterEqual:
			case Op.IfGreaterThan:
			case Op.IfLessEqual:
			case Op.IfLessThan:
			case Op.IfNotEqual:
			case Op.IfNotGreaterEqual:
			case Op.IfNotGreaterThan:
			case Op.IfNotLessEqual:
			case Op.IfNotLessThan:
			case Op.IfStrictEqual:
			case Op.IfStrictNotEqual:
			case Op.IfTrue:
				walk( scope, currentVertex, vertex( ( (Jump)currentVertex
						.getOperation() ).marker ), EdgeKind.True );
				walk( scope, currentVertex, nextVertex, EdgeKind.False );
				break;

			case Op.Jump:
				walk( scope, currentVertex, vertex( ( (Jump)currentVertex
						.getOperation() ).marker ), EdgeKind.Jump );
				break;

			case Op.LookupSwitch:
				final LookupSwitch lookupSwitch = (LookupSwitch)currentVertex
						.getOperation();

				walk( scope, currentVertex,
						vertex( lookupSwitch.defaultMarker ),
						EdgeKind.DefaultCase );

				for( final Marker marker : lookupSwitch.caseMarkers )
				{
					walk( scope, currentVertex, vertex( marker ), EdgeKind.Case );
				}
				break;

			case Op.ReturnValue:
			case Op.ReturnVoid:
			case Op.Throw:
				if( 1 == scope )
				{
					//
					// If the scope stack has a depth of 1, we branch out to the
					// exit point. Otherwise we will just leave the current
					// scope on the stack.
					//

					walk( scope, currentVertex, exitPoint, EdgeKind.Return );
				}
				else
				{
					//
					// The last instruction will always branch out to the
					// exit point.
					//

					if( null == nextVertex )
					{
						walk( scope, currentVertex, exitPoint, EdgeKind.Return );
					}
					else
					{
						walk( scope, currentVertex, nextVertex );
					}
				}
				break;

			case Op.PushScope:

				//
				// Increase the scope depth.
				//

				walk( scope + 1, currentVertex, nextVertex );
				break;

			case Op.PopScope:

				//
				// Decrease the scope depth.
				//

				walk( scope - 1, currentVertex, nextVertex );
				break;

			default:
				walk( scope, currentVertex, nextVertex );
				break;
		}
	}
}
