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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.Vertex;

/**
 * The Dominance class is an implementation of the "Simple, Fast Dominance
 * Algorithm" as described by Keith D. Cooper et al.
 * 
 * @author Joa Ebert
 * 
 */
public final class Dominance<V extends Vertex, E extends Edge<V>>
{
	private final ControlFlowGraph<V, E> graph;

	private final Map<V, List<V>> frontiers;

	private final Map<V, V> doms;

	private final ArrayList<V> postorder;

	public Dominance( final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		this.graph = graph;

		doms = new LinkedHashMap<V, V>( graph.vertexList().size() );
		frontiers = new LinkedHashMap<V, List<V>>( graph.vertexList().size() );
		postorder = getPostorder();
	}

	public String debug()
	{
		final StringBuilder buffer = new StringBuilder( "Dominance:" );

		for( final Entry<V, V> entry : doms.entrySet() )
		{
			buffer.append( "\n" );
			buffer.append( entry.getValue().toString() );
			buffer.append( " dom " );
			buffer.append( entry.getKey() );
		}

		buffer.append( "\n\nDominanceFrontiers:" );

		for( final Entry<V, List<V>> entry : frontiers.entrySet() )
		{
			buffer.append( "\n" );
			buffer.append( entry.getKey().toString() );
			buffer.append( ": " );
			buffer.append( entry.getValue().toString() );
		}

		return buffer.toString();
	}

	public void debug( final OutputStream output )
	{
		debug( new PrintStream( output ) );
	}

	public void debug( final PrintStream output )
	{
		output.print( debug() );
		output.flush();
	}

	private void dominance() throws ControlFlowGraphException
	{
		//
		// "A Simple, Fast Dominance Algorithm"
		//
		// Keith D. Cooper et al.
		// Rice University, Houston, TX
		//
		// http://www.cs.rice.edu/~keith/EMBED/dom.pdf
		// Page 13
		//

		final V entryPoint = graph.getEntryVertex();

		doms.put( entryPoint, entryPoint );

		boolean changed;
		final ArrayList<V> reversePostorder = getReversePostorder();

		reversePostorder.remove( entryPoint );

		do
		{
			changed = false;

			for( final V b : reversePostorder )
			{
				final List<V> predecessors = graph.predecessorsOf( b );

				V newIDom = pickFirstProcessed( predecessors );

				predecessors.remove( newIDom );

				for( final V p : predecessors )
				{
					if( doms.containsKey( p ) )
					{
						newIDom = intersect( p, newIDom );
					}
				}

				if( doms.containsKey( b ) )
				{
					final V old = doms.get( b );

					if( old != newIDom )
					{
						doms.put( b, newIDom );
						changed = true;
					}
				}
				else
				{
					doms.put( b, newIDom );
					changed = true;
				}
			}
		}
		while( changed );
	}

	public List<V> frontierOf( final V vertex )
	{
		return frontiers.get( vertex );
	}

	private void frontiers() throws ControlFlowGraphException
	{
		//
		// "A Simple, Fast Dominance Algorithm"
		//
		// Keith D. Cooper et al.
		// Rice University, Houston, TX
		//
		// http://www.cs.rice.edu/~keith/EMBED/dom.pdf
		// Page 18
		//

		for( final V b : graph.vertexList() )
		{
			frontiers.put( b, new LinkedList<V>() );
		}

		for( final V b : graph.vertexList() )
		{
			final List<V> predecessors = graph.predecessorsOf( b );

			if( predecessors.size() > 1 )
			{
				for( final V p : predecessors )
				{
					V runner = p;

					while( runner != doms.get( b ) )
					{
						frontiers.get( runner ).add( b );
						runner = doms.get( runner );
					}
				}
			}
		}
	}

	private ArrayList<V> getPostorder() throws ControlFlowGraphException
	{
		final ArrayList<V> postorder = new ArrayList<V>( graph.vertexList()
				.size() );
		final DepthFirstIterator<V, E> iter = new DepthFirstIterator<V, E>(
				graph );

		while( iter.hasNext() )
		{
			postorder.add( iter.next() );
		}

		return postorder;
	}

	private ArrayList<V> getReversePostorder() throws ControlFlowGraphException
	{
		final ArrayList<V> postorder = getPostorder();

		Collections.reverse( postorder );

		return postorder;
	}

	private V intersect( final V b1, final V b2 )
	{
		V finger1 = b1;
		V finger2 = b2;

		while( finger1 != finger2 )
		{
			while( postorder.indexOf( finger1 ) < postorder.indexOf( finger2 ) )
			{
				finger1 = doms.get( finger1 );
			}

			while( postorder.indexOf( finger2 ) < postorder.indexOf( finger1 ) )
			{
				finger2 = doms.get( finger2 );
			}
		}

		return finger1;
	}

	private V pickFirstProcessed( final List<V> predecessors )
			throws ControlFlowGraphException
	{
		for( final V vertex : predecessors )
		{
			if( doms.containsKey( vertex ) )
			{
				return vertex;
			}
		}

		//
		// Unreachable since n0 will always be a processed predecessor.
		//

		throw new ControlFlowGraphException( "Unreachable by definition." );
	}

	public void solve() throws ControlFlowGraphException
	{
		dominance();
		frontiers();
	}
}
