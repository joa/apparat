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

package com.joa_ebert.apparat.abc.bytecode;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.operations.Jump;
import com.joa_ebert.apparat.abc.bytecode.operations.LookupSwitch;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Bytecode implements List<AbstractOperation>,
		Queue<AbstractOperation>, Deque<AbstractOperation>
{
	public Abc abc;
	public MethodBody methodBody;
	public Method method;

	private final LinkedList<AbstractOperation> list = new LinkedList<AbstractOperation>();

	public final MarkerManager markers = new MarkerManager( this );

	public boolean add( final AbstractOperation operation )
	{
		return list.add( operation );
	}

	public void add( final int index, final AbstractOperation operation )
	{
		list.add( index, operation );
	}

	public boolean addAll(
			final Collection<? extends AbstractOperation> collection )
	{
		return list.addAll( collection );
	}

	public boolean addAll( final int index,
			final Collection<? extends AbstractOperation> collection )
	{
		return list.addAll( index, collection );
	}

	public void addFirst( final AbstractOperation operation )
	{
		list.addFirst( operation );
	}

	public void addLast( final AbstractOperation operation )
	{
		list.addLast( operation );
	}

	public void clear()
	{
		list.clear();
	}

	public boolean contains( final Object element )
	{
		return list.contains( element );
	}

	public boolean containsAll( final Collection<?> collection )
	{
		return list.containsAll( collection );
	}

	public Iterator<AbstractOperation> descendingIterator()
	{
		return list.descendingIterator();
	}

	public AbstractOperation element()
	{
		return list.element();
	}

	public AbstractOperation get( final int index )
	{
		return list.get( index );
	}

	public AbstractOperation getFirst()
	{
		return list.getFirst();
	}

	public AbstractOperation getLast()
	{
		return list.getLast();
	}

	public int indexOf( final AbstractOperation operation )
	{
		return list.indexOf( operation );
	}

	public int indexOf( final Marker marker )
	{
		return indexOf( markers.getOperationFor( marker ) );
	}

	public int indexOf( final Object element )
	{
		if( element instanceof AbstractOperation )
		{
			return indexOf( (AbstractOperation)element );
		}
		else if( element instanceof Marker )
		{
			return indexOf( (Marker)element );
		}
		else
		{
			return -1;
		}
	}

	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public Iterator<AbstractOperation> iterator()
	{
		return list.iterator();
	}

	public int lastIndexOf( final Object element )
	{
		return list.lastIndexOf( element );
	}

	public ListIterator<AbstractOperation> listIterator()
	{
		return list.listIterator();
	}

	public ListIterator<AbstractOperation> listIterator(
			final AbstractOperation operation )
	{
		return listIterator( indexOf( operation ) );
	}

	public ListIterator<AbstractOperation> listIterator( final int index )
	{
		return list.listIterator( index );
	}

	public ListIterator<AbstractOperation> listIterator( final Marker marker )
	{
		return listIterator( markers.getOperationFor( marker ) );
	}

	public boolean offer( final AbstractOperation operation )
	{
		return list.offer( operation );
	}

	public boolean offerFirst( final AbstractOperation operation )
	{
		return list.offerFirst( operation );
	}

	public boolean offerLast( final AbstractOperation operation )
	{
		return list.offerLast( operation );
	}

	private void patchMarker( final AbstractOperation operation )
	{
		if( markers.hasMarkerFor( operation ) )
		{
			//
			// The operation which is about to be remove has been marked.
			//

			//
			// Get the next operation which would be at the same position
			// according to the removed operation index.
			//

			final int index = indexOf( operation );
			final AbstractOperation nextOperation = ( list.size() - 1 ) == index ? list
					.get( index - 1 )
					: list.get( index + 1 );

			//
			// Remove the old marker
			//

			final Marker oldMarker = markers.getMarkerFor( operation );

			markers.removeMarkerFor( operation );

			//
			// Do we override a marker for the next operation? If yes, then
			// patch
			// all references to the new marker without overriding it.
			//
			// If not, patch the old marker to point to the next operation. The
			// reference will stay the same in that case.
			//

			if( markers.hasMarkerFor( nextOperation ) )
			{
				final Marker newMarker = markers.getMarkerFor( nextOperation );

				final Iterator<AbstractOperation> iter = listIterator();

				for( final ExceptionHandler exceptionHandler : methodBody.exceptions )
				{
					if( exceptionHandler.from == oldMarker )
					{
						exceptionHandler.from = newMarker;
					}

					if( exceptionHandler.to == oldMarker )
					{
						exceptionHandler.to = newMarker;
					}

					if( exceptionHandler.target == oldMarker )
					{
						exceptionHandler.target = newMarker;
					}
				}

				while( iter.hasNext() )
				{
					final AbstractOperation operationToPatch = iter.next();
					final int code = operationToPatch.code;

					switch( code )
					{
						case Op.IfEqual:
						case Op.IfFalse:
						case Op.IfGreaterEqual:
						case Op.IfGreaterThan:
						case Op.IfLessEqual:
						case Op.IfLessThan:
						case Op.IfNotEqual:
						case Op.IfNotGreaterEqual:
						case Op.IfNotGreaterThan:
						case Op.IfNotLessEqual:
						case Op.IfNotLessThan:
						case Op.IfStrictEqual:
						case Op.IfStrictNotEqual:
						case Op.IfTrue:
						case Op.Jump:
							final Jump jump = (Jump)operationToPatch;

							if( jump.marker == oldMarker )
							{
								jump.marker = newMarker;
							}
							break;

						case Op.LookupSwitch:
							final LookupSwitch lookupSwitch = (LookupSwitch)operationToPatch;

							if( lookupSwitch.defaultMarker == oldMarker )
							{
								lookupSwitch.defaultMarker = newMarker;
							}

							final int caseCount = lookupSwitch.caseMarkers
									.size();

							for( int i = 0; i < caseCount; ++i )
							{
								if( lookupSwitch.caseMarkers.get( i ) == oldMarker )
								{
									lookupSwitch.caseMarkers.set( i, newMarker );
								}
							}
							break;
					}
				}
			}
			else
			{
				oldMarker.setOperation( nextOperation );
				markers.addMarker( oldMarker );
			}
		}
	}

	public AbstractOperation peek()
	{
		return list.peek();
	}

	public AbstractOperation peekFirst()
	{
		return list.peekFirst();
	}

	public AbstractOperation peekLast()
	{
		return list.peekLast();
	}

	public AbstractOperation poll()
	{
		final AbstractOperation result = list.poll();

		if( null != result )
		{
			patchMarker( result );
		}

		return result;
	}

	public AbstractOperation pollFirst()
	{
		final AbstractOperation result = list.pollFirst();

		if( null != result )
		{
			patchMarker( result );
		}

		return result;
	}

	public AbstractOperation pollLast()
	{
		final AbstractOperation result = list.pollLast();

		if( null != result )
		{
			patchMarker( result );
		}

		return result;
	}

	public AbstractOperation pop()
	{
		final AbstractOperation result = list.pop();

		if( null != result )
		{
			patchMarker( result );
		}

		return result;
	}

	public void push( final AbstractOperation operation )
	{
		list.push( operation );
	}

	public AbstractOperation remove()
	{
		patchMarker( element() );

		return list.remove();
	}

	public boolean remove( final AbstractOperation operation )
	{
		if( list.contains( operation ) )
		{
			patchMarker( operation );
		}

		return list.remove( operation );
	}

	public AbstractOperation remove( final int index )
	{
		patchMarker( list.get( index ) );

		return list.remove( index );
	}

	public boolean remove( final Object element )
	{
		if( element instanceof AbstractOperation )
		{
			return remove( (AbstractOperation)element );
		}

		return list.remove( element );
	}

	public boolean removeAll( final Collection<?> collection )
	{
		// TODO patch markers
		return list.removeAll( collection );
	}

	public AbstractOperation removeFirst()
	{
		patchMarker( list.element() );

		return list.removeFirst();
	}

	public boolean removeFirstOccurrence( final AbstractOperation operation )
	{
		if( !list.isEmpty() && list.contains( operation ) )
		{
			patchMarker( list.get( list.indexOf( operation ) ) );
		}

		return list.removeFirstOccurrence( operation );
	}

	public boolean removeFirstOccurrence( final Object operation )
	{
		if( operation instanceof AbstractOperation )
		{
			return removeFirstOccurrence( (AbstractOperation)operation );
		}

		return false;
	}

	public AbstractOperation removeLast()
	{
		if( !list.isEmpty() )
		{
			patchMarker( list.peekLast() );
		}

		return list.removeLast();
	}

	public boolean removeLastOccurrence( final Object operation )
	{
		return list.removeLastOccurrence( operation );
	}

	public void replace( final AbstractOperation existing,
			final AbstractOperation replacement )
	{
		if( markers.hasMarkerFor( existing ) )
		{
			final Marker oldMarker = markers.removeMarkerFor( existing );

			oldMarker.setOperation( replacement );

			markers.addMarker( oldMarker );
		}

		add( indexOf( existing ), replacement );
		remove( existing );
	}

	public boolean retainAll( final Collection<?> collection )
	{
		// TODO patch markers
		return list.retainAll( collection );
	}

	public AbstractOperation set( final int index,
			final AbstractOperation operation )
	{
		return list.set( index, operation );
	}

	public int size()
	{
		return list.size();
	}

	public List<AbstractOperation> subList( final int fromIndex,
			final int toIndex )
	{
		return list.subList( fromIndex, toIndex );
	}

	public Object[] toArray()
	{
		return list.toArray();
	}

	public <T> T[] toArray( final T[] array )
	{
		return list.toArray( array );
	}

}
