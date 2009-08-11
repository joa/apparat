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

import org.junit.Assert;
import org.junit.Test;

import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.EntryVertex;
import com.joa_ebert.apparat.abc.controlflow.ExitVertex;
import com.joa_ebert.apparat.abc.controlflow.Vertex;
import com.joa_ebert.apparat.abc.controlflow.VertexKind;
import com.joa_ebert.apparat.abc.controlflow.utils.Dominance;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class DominanceTest
{
	private final Map<Integer, Vertex> map = new TreeMap<Integer, Vertex>();

	@Test
	public void testDominance() throws Exception
	{
		final ControlFlowGraph<Vertex, Edge<Vertex>> cfg = new ControlFlowGraph<Vertex, Edge<Vertex>>();

		final Vertex entry = new EntryVertex();
		final Vertex exit = new ExitVertex();

		cfg.setEntryPoint( entry );
		cfg.setExitPoint( exit );
		cfg.add( V( 1 ) );
		cfg.add( V( 2 ) );
		cfg.add( V( 3 ) );

		cfg.add( new Edge<Vertex>( entry, V( 1 ) ) );
		cfg.add( new Edge<Vertex>( entry, V( 2 ) ) );
		cfg.add( new Edge<Vertex>( V( 1 ), V( 3 ) ) );
		cfg.add( new Edge<Vertex>( V( 2 ), V( 3 ) ) );
		cfg.add( new Edge<Vertex>( V( 3 ), exit ) );

		final Dominance<Vertex, Edge<Vertex>> dominance = new Dominance<Vertex, Edge<Vertex>>(
				cfg );

		dominance.solve();

		Assert.assertEquals( 1, dominance.frontierOf( V( 1 ) ).size() );
		Assert.assertEquals( 1, dominance.frontierOf( V( 2 ) ).size() );
		Assert.assertEquals( V( 3 ), dominance.frontierOf( V( 1 ) ).get( 0 ) );
		Assert.assertEquals( V( 3 ), dominance.frontierOf( V( 2 ) ).get( 0 ) );
	}

	@Test
	public void testLoops() throws Exception
	{
		final ControlFlowGraph<Vertex, Edge<Vertex>> cfg = new ControlFlowGraph<Vertex, Edge<Vertex>>();

		final Vertex entry = new EntryVertex();
		final Vertex exit = new ExitVertex();

		cfg.setEntryPoint( entry );
		cfg.setExitPoint( exit );
		cfg.add( V( 1 ) );
		cfg.add( V( 2 ) );
		cfg.add( V( 3 ) );

		cfg.add( new Edge<Vertex>( entry, V( 1 ) ) );
		cfg.add( new Edge<Vertex>( V( 1 ), V( 2 ) ) );
		cfg.add( new Edge<Vertex>( V( 2 ), exit ) );
		cfg.add( new Edge<Vertex>( V( 2 ), V( 3 ) ) );
		cfg.add( new Edge<Vertex>( V( 3 ), V( 2 ) ) );

		final Dominance<Vertex, Edge<Vertex>> dominance = new Dominance<Vertex, Edge<Vertex>>(
				cfg );

		dominance.solve();
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
