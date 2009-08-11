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

package com.joa_ebert.apparat.abc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class NamespaceSet implements Set<Namespace>
{
	private final ArrayList<Namespace> set;

	public NamespaceSet()
	{
		this( 0 );
	}

	public NamespaceSet( final int capacity )
	{
		set = new ArrayList<Namespace>( capacity );
	}

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );

		for( final Namespace namespace : set )
		{
			namespace.accept( context, visitor );
		}
	}

	public boolean add( final Namespace namespace )
	{
		return set.add( namespace );
	}

	public boolean addAll( final Collection<? extends Namespace> collection )
	{
		return set.addAll( collection );
	}

	public void clear()
	{
		set.clear();
	}

	public boolean contains( final Object obj )
	{
		return set.contains( obj );
	}

	public boolean containsAll( final Collection<?> collection )
	{
		return set.containsAll( collection );
	}

	public boolean equals( final NamespaceSet other )
	{
		if( other.size() != size() )
		{
			return false;
		}

		final int n = size();

		for( int i = 0; i < n; ++i )
		{
			if( !get( i ).equals( other.get( i ) ) )
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof NamespaceSet )
		{
			return equals( (NamespaceSet)other );
		}

		return false;
	}

	public Namespace get( final int index )
	{
		return set.get( index );
	}

	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	public Iterator<Namespace> iterator()
	{
		return set.iterator();
	}

	public boolean remove( final Object obj )
	{
		return set.remove( obj );
	}

	public boolean removeAll( final Collection<?> collection )
	{
		return set.removeAll( collection );
	}

	public boolean retainAll( final Collection<?> collection )
	{
		return set.retainAll( collection );
	}

	public int size()
	{
		return set.size();
	}

	public Object[] toArray()
	{
		return set.toArray();
	}

	public <T> T[] toArray( final T[] array )
	{
		return set.toArray( array );
	}
}
