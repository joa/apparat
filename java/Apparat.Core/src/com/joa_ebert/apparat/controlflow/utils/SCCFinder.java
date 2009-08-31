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

	public List<SCComponent<V>> find( final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		index = 0;
		S = new Stack<V>();
		final List<SCComponent<V>> components = new LinkedList<SCComponent<V>>();

		for( final V v : graph.vertexList() )
		{
			v.index = -1;
			v.lowlink = -1;
		}

		for( final V v : graph.vertexList() )
		{
			if( -1 == v.index )
			{
				tarjan( v, graph, components );
			}
		}

		return components;
	}

	private void tarjan( final V v, final ControlFlowGraph<V, E> G,
			final List<SCComponent<V>> components )
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
			V v2 = null;

			LinkedList<V> list = null;

			do
			{
				v2 = S.pop();

				if( v != v2 && list == null )
				{
					list = new LinkedList<V>();
				}

				if( null != list )
				{
					list.add( v2 );
				}
			}
			while( v != v2 );

			if( null != list )
			{
				components.add( new SCComponent<V>( list ) );
			}
		}
	}
}
