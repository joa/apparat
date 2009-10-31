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
import com.joa_ebert.apparat.abc.bytecode.operations.IncLocalInt;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class IncrementIntPattern extends AbstractPatternMatcher implements
		IBytecodePermutation
{
	//
	//
	// ActionScript:
	// while( ++n > -1 ) { [...] }
	//
	// ASC:
	// GetLocal [R]
	// IncrementInt
	// Dup
	// ConvertInt
	// SetLocal [R]
	//
	// Optimized:
	// IncLocalInt [R]
	// GetLocal [R]
	// 

	public static final int[][] PATTERN = new int[][] {
			new int[] {
					Op.GetLocal,
					Op.GetLocal0,
					Op.GetLocal1,
					Op.GetLocal2,
					Op.GetLocal3
			},
			new int[] {
				Op.IncrementInt
			},
			new int[] {
				Op.Dup
			},
			new int[] {
				Op.ConvertInt
			},
			new int[] {
					Op.SetLocal,
					Op.SetLocal0,
					Op.SetLocal1,
					Op.SetLocal2,
					Op.SetLocal3
			}
	};

	private boolean modificationMade = false;

	public IncrementIntPattern()
	{
		super( PATTERN );
	}

	@Override
	protected void handleOccurrences( final AbcEnvironment environment,
			final Bytecode bytecode, final List<AbstractOperation> occurrences )
	{
		for( final AbstractOperation occurrence : occurrences )
		{
			final AbstractOperation getLocal = occurrence;
			final IncLocalInt decLocalInt = new IncLocalInt();
			final int register = Op.getLocalRegister( getLocal );
			final int indexOf = bytecode.indexOf( getLocal );

			for( int i = 0; i < 4; ++i )
			{
				bytecode.remove( indexOf + 1 );
			}

			bytecode.replace( getLocal, decLocalInt );
			bytecode.add( bytecode.indexOf( decLocalInt ) + 1, getLocal );

			decLocalInt.register = register;

			modificationMade = true;
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
