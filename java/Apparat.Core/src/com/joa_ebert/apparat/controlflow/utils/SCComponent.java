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

package com.joa_ebert.apparat.controlflow.utils;

import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.Vertex;

/**
 * @author Joa Ebert
 * 
 */
public class SCComponent<V extends Vertex, E extends Edge<V>>
{
	private final ControlFlowGraph<V, E> graph;

	public List<V> vertices;

	public SCComponent( final ControlFlowGraph<V, E> graph )
	{
		this( new LinkedList<V>(), graph );
	}

	public SCComponent( final List<V> vertices,
			final ControlFlowGraph<V, E> graph )
	{
		this.vertices = vertices;
		this.graph = graph;
	}

	public boolean canSearchSubcomponents() throws ControlFlowGraphException
	{
		return( null != getEntry() );
	}

	public boolean contains( final E edge )
	{
		return contains( edge.startVertex ) && contains( edge.endVertex );
	}

	public boolean contains( final V vertex )
	{
		return vertices.contains( vertex );
	}

	public V getEntry() throws ControlFlowGraphException
	{
		V entry = null;

		for( final V v : vertices )
		{
			final List<E> incommingOf = graph.incommingOf( v );

			for( final E e : incommingOf )
			{
				if( !contains( e.startVertex ) )
				{
					if( null != entry )
					{
						return null;
					}

					entry = v;
				}
			}
		}

		return entry;
	}

	public ControlFlowGraph<V, E> getGraph()
	{
		return graph;
	}

	public List<SCComponent<V, E>> subcomponents()
			throws ControlFlowGraphException
	{
		return subcomponents( getEntry() );
	}

	public List<SCComponent<V, E>> subcomponents( final V entry )
			throws ControlFlowGraphException
	{
		final ControlFlowGraph<V, E> subgraph = new ControlFlowGraph<V, E>();
		final SCCFinder<V, E> sccFinder = new SCCFinder<V, E>();

		if( null == entry )
		{
			throw new ControlFlowGraphException( "Cannot build subcomponent." );
		}

		for( final V v : vertices )
		{
			subgraph.add( v );
		}

		for( final V v : vertices )
		{
			final List<E> outgoingOf = graph.outgoingOf( v );

			for( final E e : outgoingOf )
			{
				if( e.endVertex != entry && contains( e ) )
				{
					subgraph.add( e );
				}
			}
		}

		return sccFinder.find( subgraph );
	}
}
