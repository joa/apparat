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

package com.joa_ebert.apparat.abc.controlflow.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joa_ebert.apparat.abc.controlflow.BasicBlock;
import com.joa_ebert.apparat.abc.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.EdgeKind;
import com.joa_ebert.apparat.abc.controlflow.Vertex;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class BasicBlockBuilder<V extends Vertex, E extends Edge<V>>
{
	private final ControlFlowGraph<V, E> abstractGraph;
	private final BasicBlockGraph<V> blockGraph;
	private final Map<V, Boolean> visited;
	private final Map<V, BasicBlock<V>> knownBlocks;

	public BasicBlockBuilder( final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		abstractGraph = graph;
		blockGraph = new BasicBlockGraph<V>();

		visited = new HashMap<V, Boolean>( graph.vertexList().size() );
		knownBlocks = new HashMap<V, BasicBlock<V>>( graph.vertexList().size() );

		build( graph.getEntryVertex(), blockGraph.getEntryVertex() );

		for( final BasicBlock<V> block : blockGraph.vertexList() )
		{
			if( block.vertices().contains( graph.getExitVertex() ) )
			{
				connect( block, blockGraph.getExitVertex(), EdgeKind.Default );
				break;
			}
		}
	}

	private void build( final V abstractVertex, final BasicBlock<V> currentBlock )
			throws ControlFlowGraphException
	{
		if( visited.containsKey( abstractVertex ) )
		{
			return;
		}
		else
		{
			visited.put( abstractVertex, true );
		}

		currentBlock.vertices().add( abstractVertex );

		final List<E> outgoingOf = abstractGraph.outgoingOf( abstractVertex );

		boolean containsThrow = false;
		boolean containsDefault = false;

		for( final E edge : outgoingOf )
		{
			switch( edge.kind )
			{
				case Default:
					containsDefault = true;
					break;

				case Throw:
					containsThrow = true;
					break;
			}

			if( containsDefault && containsThrow )
			{
				break;
			}
		}

		boolean defaultFound = false;

		for( final E edge : outgoingOf )
		{
			final V abstractNext = edge.endVertex;

			switch( edge.kind )
			{
				case Default:
				{
					if( defaultFound )
					{
						throw new ControlFlowGraphException(
								"Illegal ControlFlowGraph. Only one outgoing default edge is allowed for "
										+ abstractVertex.toString() + "." );
					}

					defaultFound = true;

					if( ( abstractGraph.indegreeOf( abstractNext ) > 1 )
							|| ( containsThrow && containsDefault ) )
					{
						final BasicBlock<V> nextBlock = createBlock( abstractNext );
						connect( currentBlock, nextBlock, EdgeKind.Default );
						build( abstractNext, nextBlock );
					}
					else
					{
						build( abstractNext, currentBlock );
					}
					break;
				}

				case Case:
				case DefaultCase:
				case False:
				case Jump:
				case Throw:
				case True:
				{
					final BasicBlock<V> nextBlock = createBlock( abstractNext );
					connect( currentBlock, nextBlock, edge.kind );
					build( abstractNext, nextBlock );
					break;
				}

				case Return:
					break;

				default:
					throw new RuntimeException( "Unreachable by definition." );
			}
		}
	}

	private void connect( final BasicBlock<V> startBlock,
			final BasicBlock<V> endBlock, final EdgeKind kind )
			throws ControlFlowGraphException
	{
		blockGraph.add( new Edge<BasicBlock<V>>( startBlock, endBlock, kind ) );
	}

	private BasicBlock<V> createBlock( final V vertex )
			throws ControlFlowGraphException
	{
		if( knownBlocks.containsKey( vertex ) )
		{
			return knownBlocks.get( vertex );
		}

		final BasicBlock<V> result = new BasicBlock<V>();

		knownBlocks.put( vertex, result );

		blockGraph.add( result );

		return result;
	}

	public BasicBlockGraph<V> getBlockGraph()
	{
		return blockGraph;
	}
}
