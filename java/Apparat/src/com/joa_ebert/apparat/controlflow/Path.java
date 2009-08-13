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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class Path<V extends Vertex, E extends Edge<V>> implements List<V>
{
	private final LinkedList<V> vertices = new LinkedList<V>();

	public void add( final int index, final V element )
	{
		vertices.add( index, element );
	}

	public boolean add( final V e )
	{
		return vertices.add( e );
	}

	public boolean addAll( final Collection<? extends V> c )
	{
		return vertices.addAll( c );
	}

	public boolean addAll( final int index, final Collection<? extends V> c )
	{
		return vertices.addAll( index, c );
	}

	public void clear()
	{
		vertices.clear();
	}

	@Override
	public Path<V, E> clone()
	{
		final Path<V, E> result = new Path<V, E>();

		for( final V vertex : this )
		{
			result.add( vertex );
		}

		return result;
	}

	public boolean contains( final Object o )
	{
		return vertices.contains( o );
	}

	public boolean containsAll( final Collection<?> c )
	{
		return vertices.containsAll( c );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof Path )
		{
			try
			{
				return equals( (Path<V, E>)other );
			}
			catch( final Throwable t )
			{
				return false;
			}
		}

		return false;
	}

	public boolean equals( final Path<V, E> other )
	{
		if( size() != other.size() )
		{
			return false;
		}

		int n = size();

		while( --n > -1 )
		{
			if( !get( n ).equals( other.get( n ) ) )
			{
				return false;
			}
		}

		return true;
	}

	public V get( final int index )
	{
		return vertices.get( index );
	}

	public int indexOf( final Object o )
	{
		return vertices.indexOf( o );
	}

	public boolean isEmpty()
	{
		return vertices.isEmpty();
	}

	public Iterator<V> iterator()
	{
		return vertices.iterator();
	}

	public int lastIndexOf( final Object o )
	{
		return vertices.lastIndexOf( o );
	}

	public ListIterator<V> listIterator()
	{
		return vertices.listIterator();
	}

	public ListIterator<V> listIterator( final int index )
	{
		return vertices.listIterator( index );
	}

	public Iterator<V> pathIterator()
	{
		return vertices.listIterator();
	}

	public V remove( final int index )
	{
		return vertices.remove( index );
	}

	public boolean remove( final Object o )
	{
		return vertices.remove( o );
	}

	public boolean removeAll( final Collection<?> c )
	{
		return vertices.removeAll( c );
	}

	public boolean retainAll( final Collection<?> c )
	{
		return vertices.retainAll( c );
	}

	public V set( final int index, final V element )
	{
		return vertices.set( index, element );
	}

	public int size()
	{
		return vertices.size();
	}

	public List<V> subList( final int fromIndex, final int toIndex )
	{
		return vertices.subList( fromIndex, toIndex );
	}

	public Object[] toArray()
	{
		return vertices.toArray();
	}

	public <T> T[] toArray( final T[] a )
	{
		return vertices.toArray( a );
	}
}
