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

package com.joa_ebert.apparat.controlflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joa_ebert.apparat.controlflow.utils.BasicBlockBuilder;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class ControlFlowGraph<V extends Vertex, E extends Edge<V>>
{
	private final List<E> edges = new LinkedList<E>();
	private final List<V> vertices = new LinkedList<V>();
	private final Map<V, List<E>> adjacency = new HashMap<V, List<E>>();

	private V entryPoint;
	private V exitPoint;

	public boolean add( final E edge ) throws ControlFlowGraphException
	{
		if( !contains( edge.startVertex ) )
		{
			throw new ControlFlowGraphException( "Unknown start vertex." );
		}

		if( !contains( edge.endVertex ) )
		{
			throw new ControlFlowGraphException( "Unknown end vertex." );
		}

		return adjacency.get( edge.startVertex ).add( edge )
				&& edges.add( edge );
	}

	public boolean add( final V vertex ) throws ControlFlowGraphException
	{
		if( contains( vertex ) )
		{
			throw new ControlFlowGraphException(
					"Vertex has already been added." );
		}

		adjacency.put( vertex, new LinkedList<E>() );

		return vertices.add( vertex );
	}

	private void allPredecessorsOf( final List<V> result, final V predecessor )
			throws ControlFlowGraphException
	{
		if( predecessor.color != VertexColor.White )
		{
			return;
		}

		predecessor.color = VertexColor.Gray;

		result.add( predecessor );

		final List<E> incommingEdges = incommingOf( predecessor );

		for( final E edge : incommingEdges )
		{
			allPredecessorsOf( result, edge.startVertex );
		}

		predecessor.color = VertexColor.Black;
	}

	public List<V> allPredecessorsOf( final V vertex )
			throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		resetColors();

		final List<V> result = new LinkedList<V>();
		final List<E> incommingEdges = incommingOf( vertex );

		for( final E edge : incommingEdges )
		{
			allPredecessorsOf( result, edge.startVertex );
		}

		return result;
	}

	public void clear()
	{
		entryPoint = null;
		exitPoint = null;
		edges.clear();
		vertices.clear();
		adjacency.clear();
	}

	public boolean contains( final E edge )
	{
		return edges.contains( edge );
	}

	public boolean contains( final V vertex )
	{
		return vertices.contains( vertex );
	}

	public boolean containsEdge( final V startVertex, final V endVertex )
	{
		if( !contains( startVertex ) )
		{
			return false;
		}

		if( !contains( endVertex ) )
		{
			return false;
		}

		for( final E edge : adjacency.get( startVertex ) )
		{
			if( edge.endVertex.equals( endVertex ) )
			{
				return true;
			}
		}

		return false;
	}

	public List<E> edgeList()
	{
		return edges;
	}

	public Set<E> edgeSet()
	{
		return new HashSet<E>( edges );
	}

	public V getEntryVertex()
	{
		return entryPoint;
	}

	public V getExitVertex()
	{
		return exitPoint;
	}

	public List<E> incommingOf( final V vertex )
			throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		final List<E> result = new LinkedList<E>();

		for( final E edge : edges )
		{
			if( edge.endVertex.equals( vertex ) )
			{
				result.add( edge );
			}
		}

		return result;
	}

	public int indegreeOf( final V vertex ) throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		int numEdges = 0;

		for( final E edge : edges )
		{
			if( edge.endVertex.equals( vertex ) )
			{
				++numEdges;
			}
		}

		return numEdges;
	}

	public int outdegreeOf( final V vertex ) throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		return adjacency.get( vertex ).size();
	}

	public List<E> outgoingOf( final V vertex )
			throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		return adjacency.get( vertex );
	}

	public List<V> predecessorsOf( final V vertex )
			throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		final List<V> result = new LinkedList<V>();
		final List<E> incommingEdges = incommingOf( vertex );

		for( final E edge : incommingEdges )
		{
			result.add( edge.startVertex );
		}

		return result;
	}

	public boolean remove( final E edge ) throws ControlFlowGraphException
	{
		if( !contains( edge.startVertex ) || !contains( edge.endVertex ) )
		{
			throw new ControlFlowGraphException( "Unknown edge." );
		}

		return adjacency.get( edge.startVertex ).remove( edge )
				&& edges.remove( edge );
	}

	public boolean remove( final V vertex ) throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		final List<E> outgoing = outgoingOf( vertex );

		while( !outgoing.isEmpty() )
		{
			remove( outgoing.get( 0 ) );
		}

		for( final E edge : incommingOf( vertex ) )
		{
			remove( edge );
		}

		adjacency.remove( vertex );

		return vertices.remove( vertex );
	}

	public void resetColors()
	{
		for( final V vertex : vertices )
		{
			vertex.color = VertexColor.White;
		}
	}

	public void setEntryPoint( final V value ) throws ControlFlowGraphException
	{
		if( value.kind != VertexKind.Entry )
		{
			throw new ControlFlowGraphException( "Vertex is not of kind Entry." );
		}

		if( null != entryPoint )
		{
			remove( entryPoint );
		}

		entryPoint = value;

		add( entryPoint );
	}

	public void setExitPoint( final V value ) throws ControlFlowGraphException
	{
		if( value.kind != VertexKind.Exit )
		{
			throw new ControlFlowGraphException( "Vertex is not of kind Exit." );
		}

		if( null != exitPoint )
		{
			remove( exitPoint );
		}

		exitPoint = value;

		add( exitPoint );
	}

	public List<V> successorsOf( final V vertex )
			throws ControlFlowGraphException
	{
		if( !contains( vertex ) )
		{
			throw new ControlFlowGraphException( "Unknown vertex." );
		}

		final List<V> result = new LinkedList<V>();
		final List<E> outgoingEdges = outgoingOf( vertex );

		for( final E edge : outgoingEdges )
		{
			result.add( edge.endVertex );
		}

		return result;
	}

	public BasicBlockGraph<V> toBlockGraph() throws ControlFlowGraphException
	{
		final BasicBlockBuilder<V, E> builder = new BasicBlockBuilder<V, E>(
				this );

		return builder.getBlockGraph();
	}

	public List<V> vertexList()
	{
		return vertices;
	}

	public Set<V> vertexSet()
	{
		return new HashSet<V>( vertices );
	}
}
