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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.joa_ebert.apparat.controlflow.export.AbstractVertexLabelProvider;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class BasicBlock<V extends Vertex> extends Vertex
{
	public static final class LabelProvider<V extends Vertex> extends
			AbstractVertexLabelProvider<BasicBlock<V>>
	{
		private final Map<BasicBlock<V>, Integer> map = new LinkedHashMap<BasicBlock<V>, Integer>();
		private int i = 0;

		@Override
		public String toString( final BasicBlock<V> vertex )
		{
			switch( vertex.kind )
			{
				case Default:
					int index;

					if( map.containsKey( vertex ) )
					{
						index = map.get( vertex );
					}
					else
					{
						index = i++;

						map.put( vertex, index );
					}

					return Integer.toString( index ) + " (size: "
							+ Integer.toString( vertex.vertices().size() )
							+ ")";

				case Entry:
					return "[[Entry]]";

				case Exit:
					return "[[Exit]]";
			}

			return null;
		}

	}

	public static <V extends Vertex> BasicBlock<V> createEntry()
	{
		return new BasicBlock<V>( VertexKind.Entry );
	}

	public static <V extends Vertex> BasicBlock<V> createExit()
	{
		return new BasicBlock<V>( VertexKind.Exit );
	}

	private final LinkedList<V> vertices;

	public BasicBlock()
	{
		this( VertexKind.Default );
	}

	protected BasicBlock( final VertexKind kind )
	{
		super( kind );

		vertices = new LinkedList<V>();
	}

	@Override
	public String toString()
	{
		return "[BasicBlock " + kind.toString() + ", vertices: "
				+ vertices.toString() + "]";
	}

	public LinkedList<V> vertices()
	{
		return vertices;
	}
}
