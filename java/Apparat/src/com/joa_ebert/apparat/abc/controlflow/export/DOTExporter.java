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

package com.joa_ebert.apparat.abc.controlflow.export;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.Vertex;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class DOTExporter<V extends Vertex, E extends Edge<V>> implements
		IControlFlowGraphExporter<V, E>
{
	private final AbstractVertexLabelProvider<V> vertexLabelProvider;
	private final AbstractEdgeLabelProvider<V, E> edgeLabelProvider;
	private final Map<V, Integer> vertexIndexMap = new HashMap<V, Integer>();
	private int vertexIndex;

	public DOTExporter()
	{
		this( new IntegerLabelProvider<V>(), null );
	}

	public DOTExporter( final AbstractVertexLabelProvider<V> vertexLabelProvider )
	{
		this( vertexLabelProvider, new DefaultEdgeLabelProvider<V, E>() );
	}

	public DOTExporter(
			final AbstractVertexLabelProvider<V> vertexLabelProvider,
			final AbstractEdgeLabelProvider<V, E> edgeLabelProvider )
	{
		this.vertexLabelProvider = vertexLabelProvider;
		this.edgeLabelProvider = edgeLabelProvider;
	}

	private String escape( final String value )
	{
		return value.replace( "\"", "\\\"" );
	}

	public void export( final OutputStream output,
			final ControlFlowGraph<V, E> graph )
	{
		export( new PrintWriter( output ), graph );
	}

	public void export( final PrintWriter writer,
			final ControlFlowGraph<V, E> graph )
	{
		vertexIndex = 0;
		vertexIndexMap.clear();

		writer.write( "digraph G {\n" );

		for( final V vertex : graph.vertexList() )
		{
			if( !vertexIndexMap.containsKey( vertex ) )
			{
				vertexIndexMap.put( vertex, vertexIndex++ );
			}

			writer.write( "\t"
					+ Integer.toString( vertexIndexMap.get( vertex ) )
					+ " [label = \""
					+ escape( vertexLabelProvider.toString( vertex ) )
					+ "\"];\n" );
		}

		final boolean hasEdgeLabels = null != edgeLabelProvider;

		for( final E edge : graph.edgeList() )
		{
			writer.write( "\t"
					+ Integer.toString( vertexIndexMap.get( edge.startVertex ) )
					+ " -> "
					+ Integer.toString( vertexIndexMap.get( edge.endVertex ) ) );

			if( hasEdgeLabels )
			{
				final String label = edgeLabelProvider.toString( edge );

				if( null != label && label.length() > 0 )
				{
					writer.write( " [label = \""
							+ escape( edgeLabelProvider.toString( edge ) )
							+ "\"]" );
				}
			}

			writer.write( ";\n" );
		}

		writer.write( "}\n" );
		writer.flush();
	}
}
