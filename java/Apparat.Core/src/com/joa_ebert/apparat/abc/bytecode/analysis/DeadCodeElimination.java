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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.IMethodVisitor;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Marker;
import com.joa_ebert.apparat.abc.bytecode.MarkerManager;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.Jump;
import com.joa_ebert.apparat.abc.bytecode.operations.Label;
import com.joa_ebert.apparat.abc.bytecode.operations.LookupSwitch;
import com.joa_ebert.apparat.abc.bytecode.operations.Pop;
import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.VertexKind;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class DeadCodeElimination implements IInterpreter, IMethodVisitor
{
	private final ControlFlowGraphBuilder graphBuilder = new ControlFlowGraphBuilder();
	private final boolean debug;
	private final Logger logger;

	public DeadCodeElimination()
	{
		this( false );
	}

	public DeadCodeElimination( final boolean debug )
	{
		this.debug = debug;

		logger = debug ? Logger.getLogger( DeadCodeElimination.class.getName() )
				: null;
	}

	private void fixFinallyBlocks( final Bytecode bytecode,
			final List<ExceptionHandler> exceptions )
	{
		final MarkerManager markers = bytecode.markers;
		final List<LookupSwitch> finallySwitches = new LinkedList<LookupSwitch>();

		for( final ExceptionHandler exceptionHandler : exceptions )
		{
			final int targetIndex = bytecode.indexOf( exceptionHandler.target );
			final List<Marker> knownFinallyMarkers = new LinkedList<Marker>();
			final Iterator<AbstractOperation> catchIter = bytecode
					.listIterator( exceptionHandler.target );

			finallySearch: while( catchIter.hasNext() )
			{
				final AbstractOperation catchOperation = catchIter.next();

				//
				// A catch operation will branch out to the finally block using
				// a jump.
				//

				if( catchOperation.code == Op.Jump )
				{
					final Jump jump = (Jump)catchOperation;

					if( knownFinallyMarkers.contains( jump.marker ) )
					{
						//
						// We know that this Jump will end up in a finally block
						// already since another catch scope referenced the same
						// marker.
						//

						break;
					}

					final Iterator<AbstractOperation> finallyIter = bytecode
							.listIterator( jump.marker );

					//
					// We assume the forwards-edge leads us to a finally block.
					//

					while( finallyIter.hasNext() )
					{
						final AbstractOperation targetOperation = finallyIter
								.next();
						final int code = targetOperation.code;

						switch( code )
						{
							//
							// Nope, this was definitely not a finally block.
							// A return happens only outside of finally blocks.
							//
							//

							case Op.ReturnValue:
							case Op.ReturnVoid:
							case Op.Throw:
								break finallySearch;

							//
							// Finally blocks end with a LookupSwitch. This
							// could be a candidate.
							//
							case Op.LookupSwitch:
								final LookupSwitch lookupSwitch = (LookupSwitch)targetOperation;

								boolean isFinallySwitch = false;

								for( final Marker caseMarker : lookupSwitch.caseMarkers )
								{
									//
									// This is a finally block with respect to
									// our definition if and only if the
									// LookupSwitch inside this blocks has a
									// backwards-edge that leads in front of
									// the catch target.
									//

									if( bytecode.indexOf( caseMarker ) < targetIndex )
									{
										isFinallySwitch = true;
										break;
									}
								}

								if( isFinallySwitch )
								{
									//
									// Multiple ExceptionHandler instances could
									// reference this switch.
									//

									if( !finallySwitches
											.contains( lookupSwitch ) )
									{
										knownFinallyMarkers.add( jump.marker );
										finallySwitches.add( lookupSwitch );
									}

									break finallySearch;
								}

								break;
						}
					}
				}
			}
		}

		for( final LookupSwitch lookupSwitch : finallySwitches )
		{
			for( final Marker caseMarker : lookupSwitch.caseMarkers )
			{
				if( debug )
				{
					logger
							.info( "Inserting (possible) dead code to satisfy verifier." );
				}

				final Label label = new Label();

				bytecode.add( bytecode.indexOf( markers
						.getOperationFor( caseMarker ) ), label );

				markers.mark( label );

				bytecode.add( bytecode.indexOf( markers
						.getOperationFor( caseMarker ) ), new Pop() );
			}
		}
	}

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		if( debug )
		{
			logger.entering( DeadCodeElimination.class.getName(), "interpret" );
		}

		graphBuilder.interpret( environment, bytecode );

		final ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> graph = graphBuilder
				.getGraph();
		final List<BytecodeVertex> verticesToRemove = new LinkedList<BytecodeVertex>();

		do
		{
			try
			{
				for( final BytecodeVertex vertex : verticesToRemove )
				{
					if( debug )
					{
						logger.info( "Removing " + vertex.getOperation() );
					}

					bytecode.remove( vertex.getOperation() );
					graph.remove( vertex );
				}

				verticesToRemove.clear();

				for( final BytecodeVertex vertex : graph.vertexList() )
				{
					if( vertex.kind == VertexKind.Entry )
					{
						continue;
					}

					if( 0 == graph.indegreeOf( vertex ) )
					{
						verticesToRemove.add( vertex );
					}
				}

			}
			catch( final ControlFlowGraphException e )
			{
				e.printStackTrace();
				return;
			}
		}
		while( !verticesToRemove.isEmpty() );

		//
		// So now that we have removed unreachable code we have got a problem.
		// The AVM+ verifier will evaluate dead code. What does this mean? If
		// we have a finally-statement the verifier expects some dead code
		// in front of backwards-edges which is exactly (Label, Pop). The ASC
		// inserts this statements as well exactly in front of a backwards-edge
		// from a finally block.
		//

		fixFinallyBlocks( bytecode, bytecode.methodBody.exceptions );

		if( debug )
		{
			logger.exiting( DeadCodeElimination.class.getName(), "interpret" );
		}
	}

	public void visit( final AbcContext context, final Method method )
	{
		if( debug )
		{
			logger.entering( DeadCodeElimination.class.getName(), "visit" );
		}

		if( null != method.body )
		{
			interpret( new AbcEnvironment( context ), method.body.code );
		}

		if( debug )
		{
			logger.exiting( DeadCodeElimination.class.getName(), "visit" );
		}
	}
}
