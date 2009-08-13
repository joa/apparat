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

package com.joa_ebert.apparat.tests.controlflow;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.joa_ebert.apparat.controlflow.BasicBlock;
import com.joa_ebert.apparat.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.EntryVertex;
import com.joa_ebert.apparat.controlflow.ExitVertex;
import com.joa_ebert.apparat.controlflow.Vertex;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.controlflow.export.DOTExporter;
import com.joa_ebert.apparat.controlflow.utils.BasicBlockBuilder;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class BasicBlockTest
{
	private final Map<Integer, Vertex> map = new TreeMap<Integer, Vertex>();

	@Test
	public void testBlockBuilder() throws Exception
	{
		final ControlFlowGraph<Vertex, Edge<Vertex>> cfg = new ControlFlowGraph<Vertex, Edge<Vertex>>();

		final Vertex entry = new EntryVertex();
		final Vertex exit = new ExitVertex();

		cfg.setEntryPoint( entry );
		cfg.setExitPoint( exit );
		cfg.add( V( 1 ) );
		cfg.add( V( 2 ) );
		cfg.add( V( 3 ) );

		cfg.add( new Edge<Vertex>( entry, V( 1 ), EdgeKind.True ) );
		cfg.add( new Edge<Vertex>( entry, V( 2 ), EdgeKind.False ) );
		cfg.add( new Edge<Vertex>( V( 1 ), V( 3 ), EdgeKind.Default ) );
		cfg.add( new Edge<Vertex>( V( 2 ), V( 3 ), EdgeKind.Default ) );
		cfg.add( new Edge<Vertex>( V( 3 ), exit, EdgeKind.Default ) );

		final BasicBlockBuilder<Vertex, Edge<Vertex>> blockBuilder = new BasicBlockBuilder<Vertex, Edge<Vertex>>(
				cfg );

		@SuppressWarnings( "unused" )
		final BasicBlockGraph<Vertex> blockGraph = blockBuilder.getBlockGraph();

		@SuppressWarnings( "unused" )
		final DOTExporter<BasicBlock<Vertex>, Edge<BasicBlock<Vertex>>> exporter = new DOTExporter<BasicBlock<Vertex>, Edge<BasicBlock<Vertex>>>(
				new BasicBlock.LabelProvider<Vertex>() );
		@SuppressWarnings( "unused" )
		final DOTExporter<Vertex, Edge<Vertex>> exporter2 = new DOTExporter<Vertex, Edge<Vertex>>();

		// exporter.export( System.out, blockGraph );
		// exporter2.export( System.out, cfg );
	}

	private Vertex V( final int index )
	{
		if( map.containsKey( index ) )
		{
			return map.get( index );
		}

		final Vertex result = new Vertex( VertexKind.Default )
		{
			@Override
			public String toString()
			{
				return Integer.toString( index );
			}
		};

		map.put( index, result );

		return result;
	}
}
