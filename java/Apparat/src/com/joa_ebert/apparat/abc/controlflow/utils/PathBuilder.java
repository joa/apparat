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

import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.controlflow.BasicBlock;
import com.joa_ebert.apparat.abc.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.Path;
import com.joa_ebert.apparat.abc.controlflow.Vertex;

/**
 * The PathBuilder class finds all possible paths between two vertices.
 * 
 * @author Joa Ebert
 * 
 * @deprecated The PathBuilder should not be used any longer since the memory
 *             consumption is enormous. It makes only sense to use the
 *             PathBuilder for graphs that are guaranteed to be sparse.
 */
@Deprecated
public final class PathBuilder<V extends Vertex, E extends Edge<V>>
{
	/**
	 * Detects and returns all paths in a basic block graph.
	 * 
	 * The search is started from the default entry vertex and ends at the
	 * default exit vertex.
	 * 
	 * @param blockGraph
	 *            The graph of basic blocks.
	 * @return A list of all paths in the graph.
	 * 
	 * @throws ControlFlowGraphException
	 *             If the graph is corrupted.
	 */
	public List<Path<BasicBlock<V>, Edge<BasicBlock<V>>>> getBasicBlockPaths(
			final BasicBlockGraph<V> blockGraph )
			throws ControlFlowGraphException
	{
		return getBasicBlockPaths( blockGraph, blockGraph.getEntryVertex(),
				blockGraph.getExitVertex() );
	}

	/**
	 * Detects and returns all paths in a basic block graph between two blocks.
	 * 
	 * @param blockGraph
	 *            The graph of basic blocks.
	 * @param entryBlock
	 *            The entry block.
	 * @param exitBlock
	 *            The exit block.
	 * @return A list of all possible paths between entry and exit.
	 * 
	 * @throws ControlFlowGraphException
	 *             If the graph is corrupted.
	 */
	public List<Path<BasicBlock<V>, Edge<BasicBlock<V>>>> getBasicBlockPaths(
			final BasicBlockGraph<V> blockGraph,
			final BasicBlock<V> entryBlock, final BasicBlock<V> exitBlock )
			throws ControlFlowGraphException
	{
		final List<Path<BasicBlock<V>, Edge<BasicBlock<V>>>> result = new LinkedList<Path<BasicBlock<V>, Edge<BasicBlock<V>>>>();

		walk( entryBlock, exitBlock,
				new Path<BasicBlock<V>, Edge<BasicBlock<V>>>(), blockGraph,
				result );

		return result;
	}

	/**
	 * Detects and returns all paths in a control flow graph.
	 * 
	 * The search is started at the default entry and ends at the default exit
	 * vertex.
	 * 
	 * @param graph
	 *            The graph to analyze.
	 * @return A list of all possible paths.
	 * 
	 * @throws ControlFlowGraphException
	 *             If the graph is corrupted.
	 */
	public List<Path<V, E>> getPaths( final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		return getPaths( graph, graph.getEntryVertex(), graph.getExitVertex() );
	}

	/**
	 * Detects and returns all paths in a control flow graph between two given
	 * vertices.
	 * 
	 * @param graph
	 *            The graph to analyze.
	 * @param startVertex
	 *            The start vertex.
	 * @param endVertex
	 *            The end vertex.
	 * @return A list of all possible paths between start and end vertex.
	 * 
	 * @throws ControlFlowGraphException
	 *             If the graph is corrupted.
	 */
	public List<Path<V, E>> getPaths( final ControlFlowGraph<V, E> graph,
			final V startVertex, final V endVertex )
			throws ControlFlowGraphException
	{
		final List<Path<V, E>> result = new LinkedList<Path<V, E>>();

		walk( startVertex, endVertex, new Path<V, E>(), graph, result );

		return result;
	}

	private void walk( final BasicBlock<V> vertex,
			final BasicBlock<V> endVertex,
			final Path<BasicBlock<V>, Edge<BasicBlock<V>>> path,
			final BasicBlockGraph<V> graph,
			final List<Path<BasicBlock<V>, Edge<BasicBlock<V>>>> result )
			throws ControlFlowGraphException
	{
		if( path.contains( vertex ) )
		{
			//
			// Loops are added even if they do not reach the exit point.
			//

			result.add( path );
			return;
		}

		path.add( vertex );

		if( vertex.equals( endVertex ) )
		{
			result.add( path );
			return;
		}

		final List<Edge<BasicBlock<V>>> outgoingEdges = graph
				.outgoingOf( vertex );

		if( 1 == outgoingEdges.size() )
		{
			//
			// Only one possible path available. Continue walking.
			//

			walk( outgoingEdges.get( 0 ).endVertex, endVertex, path, graph,
					result );
		}
		else
		{
			//
			// Create a new path for each possibility.
			//

			for( final Edge<BasicBlock<V>> edge : outgoingEdges )
			{
				walk( edge.endVertex, endVertex, path.clone(), graph, result );
			}
		}
	}

	private void walk( final V vertex, final V endVertex,
			final Path<V, E> path, final ControlFlowGraph<V, E> graph,
			final List<Path<V, E>> result ) throws ControlFlowGraphException
	{
		if( path.contains( vertex ) )
		{
			//
			// Loops are added even if they do not reach the exit point.
			//

			result.add( path );
			return;
		}

		path.add( vertex );

		if( vertex.equals( endVertex ) )
		{
			result.add( path );
			return;
		}

		final List<E> outgoingEdges = graph.outgoingOf( vertex );

		if( 1 == outgoingEdges.size() )
		{
			//
			// Only one possible path available. Continue walking.
			//

			walk( outgoingEdges.get( 0 ).endVertex, endVertex, path, graph,
					result );
		}
		else
		{
			//
			// Create a new path for each possibility.
			//

			for( final E edge : outgoingEdges )
			{
				walk( edge.endVertex, endVertex, path.clone(), graph, result );
			}
		}
	}
}
