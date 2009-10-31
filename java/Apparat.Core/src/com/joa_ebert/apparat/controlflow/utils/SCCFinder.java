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
import java.util.Stack;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.Vertex;

/**
 * @author Joa Ebert
 * 
 */
public class SCCFinder<V extends Vertex, E extends Edge<V>>
{
	private int index;
	private Stack<V> S;

	public List<SCComponent<V, E>> find( final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		return find( graph, graph.vertexList() );
	}

	public List<SCComponent<V, E>> find( final ControlFlowGraph<V, E> graph,
			final List<V> vertices ) throws ControlFlowGraphException
	{
		final List<SCComponent<V, E>> components = new LinkedList<SCComponent<V, E>>();

		reset( graph );

		for( final V v : vertices )
		{
			if( -1 == v.index )
			{
				tarjan( v, graph, components );
			}
		}

		return components;
	}

	public List<SCComponent<V, E>> find( final ControlFlowGraph<V, E> graph,
			final V start ) throws ControlFlowGraphException
	{
		final List<SCComponent<V, E>> components = new LinkedList<SCComponent<V, E>>();

		reset( graph );
		tarjan( start, graph, components );

		return components;
	}

	private void reset( final ControlFlowGraph<V, E> graph )
	{
		index = 0;
		S = new Stack<V>();

		for( final V v : graph.vertexList() )
		{
			v.index = -1;
			v.lowlink = -1;
		}
	}

	private void tarjan( final V v, final ControlFlowGraph<V, E> G,
			final List<SCComponent<V, E>> components )
			throws ControlFlowGraphException
	{
		v.index = index;
		v.lowlink = index++;

		S.push( v );

		for( final E e : G.outgoingOf( v ) )
		{
			final V v2 = e.endVertex;

			if( -1 == v2.index )
			{
				tarjan( v2, G, components );
				v.lowlink = Math.min( v.lowlink, v2.lowlink );
			}
			else if( S.contains( v2 ) )
			{
				v.lowlink = Math.min( v.lowlink, v2.index );
			}
		}

		if( v.index == v.lowlink )
		{
			if( !S.isEmpty() )
			{
				V v2 = null;

				final LinkedList<V> list = new LinkedList<V>();

				do
				{
					v2 = S.pop();
					list.add( v2 );
				}
				while( v != v2 );

				if( list.size() > 1 )
				{
					components.add( new SCComponent<V, E>( list, G ) );
				}
			}
		}
	}
}
