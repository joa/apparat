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

package com.joa_ebert.apparat.abc.bytecode.permutations;

import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.analysis.AbstractPatternMatcher;
import com.joa_ebert.apparat.abc.bytecode.operations.FindPropStrict;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLex;
import com.joa_ebert.apparat.abc.bytecode.operations.GetProperty;

/**
 * @author Joa Ebert
 * 
 */
public final class GetLexPattern extends AbstractPatternMatcher implements
		IBytecodePermutation
{
	public static final int[] PATTERN = new int[] {
			Op.FindPropStrict, Op.GetProperty
	};

	private boolean modificationMade = false;

	public GetLexPattern()
	{
		super( PATTERN );
	}

	@Override
	protected void handleOccurrences( final AbcEnvironment environment,
			final Bytecode bytecode, final List<AbstractOperation> occurrences )
	{
		for( final AbstractOperation occurrence : occurrences )
		{
			final FindPropStrict findPropStrict = (FindPropStrict)occurrence;
			final GetProperty getProperty = (GetProperty)bytecode.get( bytecode
					.indexOf( findPropStrict ) + 1 );

			if( findPropStrict.property.equals( getProperty.property ) )
			{
				final GetLex getLex = new GetLex();

				getLex.property = findPropStrict.property;

				bytecode.replace( findPropStrict, getLex );
				bytecode.remove( getProperty );

				modificationMade = true;
			}
		}
	}

	@Override
	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		modificationMade = false;

		super.interpret( environment, bytecode );
	}

	public boolean modified()
	{
		return modificationMade;
	}
}
