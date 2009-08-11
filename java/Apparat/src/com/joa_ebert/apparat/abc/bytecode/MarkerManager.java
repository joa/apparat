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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.bytecode.operations.Jump;
import com.joa_ebert.apparat.abc.bytecode.operations.LookupSwitch;
import com.joa_ebert.apparat.abc.io.AbcOutputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class MarkerManager
{
	private static final boolean IS_STRICT = false;

	private final Map<AbstractOperation, Marker> markers = new LinkedHashMap<AbstractOperation, Marker>();
	private final TreeMap<Integer, Marker> unresolved = new TreeMap<Integer, Marker>();
	private final List<AbstractOperation> patches = new LinkedList<AbstractOperation>();
	private int numMarkers = 0;
	private final Bytecode code;

	public MarkerManager( final Bytecode code )
	{
		this.code = code;
	}

	public void addMarker( final Marker marker )
	{
		markers.put( marker.operation, marker );
	}

	public void applyPatches( final byte[] buffer ) throws MarkerException
	{
		for( final AbstractOperation operation : patches )
		{
			final int position = operation.position;

			if( operation instanceof Jump )
			{
				//
				// We add 1 byte to the offset because we write 1 byte after the
				// opcode.
				//
				// We add 4 bytes to the position because the delta is the value
				// after the Jump statement which consumes 4 bytes.
				//

				if( IS_STRICT )
				{
					verifyMarker( ( (Jump)operation ).marker );
				}

				AbcOutputStream.writeS24( buffer, position + 1,
						( (Jump)operation ).marker.position - ( position + 4 ) );
			}
			else if( operation instanceof LookupSwitch )
			{
				final LookupSwitch lookupSwitch = (LookupSwitch)operation;

				//
				// We add one byte here so we do not override the opcode.
				// The delta is different for LookupSwitch. It is the position
				// of the opcode BEFORE any stuff goes on.
				//

				if( IS_STRICT )
				{
					verifyMarker( lookupSwitch.defaultMarker );
				}

				AbcOutputStream.writeS24( buffer, position + 1,
						lookupSwitch.defaultMarker.position - position );

				//
				// Next we calculate the offset. It is the position plus four
				// bytes because of the three S24 bytes for default offset and
				// one byte for the opcode.
				//
				// Then we have to add the variable length of the U30 for
				// the caseCount variable.
				//

				final int caseCount = lookupSwitch.caseMarkers.size();
				final int offset = position + 4
						+ AbcOutputStream.calcU30Length( caseCount - 1 );

				for( int i = 0; i < caseCount; ++i )
				{
					verifyMarker( lookupSwitch.caseMarkers.get( i ) );

					AbcOutputStream.writeS24( buffer, offset + i * 3,
							lookupSwitch.caseMarkers.get( i ).position
									- position );
				}
			}
			else
			{
				throw new MarkerException( operation );
			}
		}

		patches.clear();
	}

	public void clear()
	{
		markers.clear();
	}

	public Marker getMarkerAt( final int position )
	{
		return unresolved.get( position );
	}

	public Marker getMarkerFor( final AbstractOperation operation )
	{
		return markers.get( operation );
	}

	public AbstractOperation getOperationFor( final Marker marker )
	{
		return marker.operation;
	}

	public boolean hasMarkerAt( final int position )
	{
		return unresolved.containsKey( position );
	}

	public boolean hasMarkerFor( final AbstractOperation operation )
	{
		return markers.containsKey( operation );
	}

	public Marker mark( final AbstractOperation operation )
	{
		if( hasMarkerFor( operation ) )
		{
			return getMarkerFor( operation );
		}

		final Marker result = new Marker( operation, numMarkers++ );

		markers.put( operation, result );

		return result;
	}

	public void patch( final AbstractOperation operation )
	{
		patches.add( operation );
	}

	public void prepareMarkers() throws MarkerException
	{
		for( final Marker marker : markers.values() )
		{
			marker.position = 0;

			if( IS_STRICT )
			{
				verifyMarker( marker );
			}
		}
	}

	public Marker putMarkerAt( final int position )
	{
		if( hasMarkerAt( position ) )
		{
			return getMarkerAt( position );
		}

		final Marker result = new Marker( numMarkers++ );

		unresolved.put( position, result );

		return result;
	}

	public Marker removeMarkerFor( final AbstractOperation operation )
	{
		return markers.remove( operation );
	}

	public void solve() throws MarkerException
	{
		if( null == unresolved )
		{
			return;
		}

		final int n = code.size();

		for( int i = 0; i < n; ++i )
		{
			final AbstractOperation current = code.get( i );

			final AbstractOperation preceeding = ( i != 0 ) ? code.get( i - 1 )
					: null;

			final Entry<Integer, Marker> markerEntry = unresolved
					.floorEntry( current.position );

			if( null == markerEntry )
			{
				continue;
			}

			final Marker marker = markerEntry.getValue();
			final int markerPosition = markerEntry.getKey();

			if( null != preceeding )
			{
				if( markerPosition > preceeding.position )
				{
					if( null != marker.operation )
					{
						throw new MarkerException( markerPosition );
					}

					marker.operation = current;
					markers.put( current, marker );
				}
			}
			else
			{
				if( null != marker.operation )
				{
					throw new MarkerException( markerPosition );
				}

				marker.operation = current;
				markers.put( current, marker );
			}
		}

		for( final Entry<Integer, Marker> marker : unresolved.entrySet() )
		{
			if( null == marker.getValue().operation )
			{
				if( IS_STRICT )
				{
					throw new MarkerException( marker.getValue(), marker
							.getKey() );
				}
				else
				{
					// Logger.getLogger( MarkerManager.class.getName()
					// ).warning(
					// "Illegal control transfer detected." );
				}
			}
		}

		unresolved.clear();
	}

	private void verifyMarker( final Marker marker ) throws MarkerException
	{
		if( null == marker )
		{
			throw new MarkerException( "Can not verify a null Marker." );
		}
		else if( null == marker.operation )
		{
			throw new MarkerException(
					"Marker got corrupted. Operation is null." );
		}
		else if( !code.contains( marker.operation ) )
		{
			throw new MarkerException( "Marked operation \""
					+ Op.codeToString( marker.operation.code )
					+ "\" does not exist in bytecode." );
		}
	}
}
