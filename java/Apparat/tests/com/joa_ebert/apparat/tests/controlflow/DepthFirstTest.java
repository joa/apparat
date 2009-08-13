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

import org.junit.Assert;
import org.junit.Test;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.EntryVertex;
import com.joa_ebert.apparat.controlflow.ExitVertex;
import com.joa_ebert.apparat.controlflow.Vertex;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.controlflow.utils.DepthFirstIterator;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class DepthFirstTest
{
	@Test
	public void testDepthFirst() throws Exception
	{
		final ControlFlowGraph<Vertex, Edge<Vertex>> cfg = new ControlFlowGraph<Vertex, Edge<Vertex>>();

		final Vertex entry = new EntryVertex();
		final Vertex between = new Vertex( VertexKind.Default )
		{
		};

		final Vertex exit = new ExitVertex();

		cfg.setEntryPoint( entry );
		cfg.add( between );
		cfg.setExitPoint( exit );

		cfg.add( new Edge<Vertex>( entry, between ) );
		cfg.add( new Edge<Vertex>( between, exit ) );

		int i = 0;

		final DepthFirstIterator<Vertex, Edge<Vertex>> iter = new DepthFirstIterator<Vertex, Edge<Vertex>>(
				cfg );

		while( iter.hasNext() )
		{
			switch( i )
			{
				case 0:
					Assert.assertEquals( exit, iter.next() );
					break;

				case 1:
					Assert.assertEquals( between, iter.next() );
					break;

				case 2:
					Assert.assertEquals( entry, iter.next() );
					break;
			}

			++i;
		}

		Assert.assertEquals( 3, i );
	}
}
