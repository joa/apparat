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

package com.joa_ebert.apparat.abc.bytecode.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class AbstractPatternMatcher implements IInterpreter
{
	private final int[][] pattern;

	protected AbstractPatternMatcher( final int[] pattern )
	{
		this.pattern = new int[ pattern.length ][ 1 ];

		for( int i = 0, n = pattern.length; i < n; ++i )
		{
			this.pattern[ i ][ 0 ] = pattern[ i ];
		}
	}

	protected AbstractPatternMatcher( final int[][] pattern )
	{
		this.pattern = pattern;
	}

	protected abstract void handleOccurrences(
			final AbcEnvironment environment, final Bytecode bytecode,
			final List<AbstractOperation> occurrences );

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		final Iterator<AbstractOperation> iter = bytecode.listIterator();
		final LinkedList<AbstractOperation> occurrences = new LinkedList<AbstractOperation>();

		int patternPosition = 0;

		while( iter.hasNext() )
		{
			final AbstractOperation operation = iter.next();
			boolean matchFound = false;

			for( final int codeToMatch : pattern[ patternPosition ] )
			{
				if( operation.code == codeToMatch )
				{
					matchFound = true;
					break;
				}
			}

			if( matchFound )
			{
				++patternPosition;

				if( 1 == patternPosition )
				{
					occurrences.push( operation );
				}

				if( patternPosition == pattern.length )
				{
					patternPosition = 0;
				}
			}
			else
			{
				if( 0 != patternPosition )
				{
					occurrences.pop();
				}

				patternPosition = 0;
			}
		}

		if( !occurrences.isEmpty() )
		{
			handleOccurrences( environment, bytecode, occurrences );
		}
	}
}
