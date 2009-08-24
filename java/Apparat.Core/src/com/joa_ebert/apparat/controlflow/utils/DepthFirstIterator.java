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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.Vertex;
import com.joa_ebert.apparat.controlflow.VertexColor;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class DepthFirstIterator<V extends Vertex, E extends Edge<V>>
		implements Iterator<V>
{
	private final LinkedList<V> vertexList = new LinkedList<V>();

	public DepthFirstIterator( final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		this( graph, graph.getEntryVertex() );
	}

	public DepthFirstIterator( final ControlFlowGraph<V, E> graph,
			final V startVertex ) throws ControlFlowGraphException
	{
		graph.resetColors();

		dft( startVertex, graph );
	}

	private void dft( final V vertex, final ControlFlowGraph<V, E> graph )
			throws ControlFlowGraphException
	{
		vertex.color = VertexColor.Gray;

		final List<E> outgoingEdges = graph.outgoingOf( vertex );

		for( final E edge : outgoingEdges )
		{
			if( edge.endVertex.color == VertexColor.White )
			{
				dft( edge.endVertex, graph );
			}
		}

		vertex.color = VertexColor.Black;

		vertexList.addLast( vertex );
	}

	public boolean hasNext()
	{
		return !vertexList.isEmpty();
	}

	public V next()
	{
		return vertexList.pollFirst();
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public void reverse()
	{
		Collections.reverse( vertexList );
	}
}
