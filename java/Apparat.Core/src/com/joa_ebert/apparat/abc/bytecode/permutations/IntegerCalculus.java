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
import com.joa_ebert.apparat.abc.bytecode.operations.AddInt;
import com.joa_ebert.apparat.abc.bytecode.operations.MultiplyInt;
import com.joa_ebert.apparat.abc.bytecode.operations.SubtractInt;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class IntegerCalculus extends AbstractPatternMatcher implements
		IBytecodePermutation
{
	//
	//
	// ActionScript:
	// var a: int = 0;
	// var b: int = 0;
	// var c: int = a + b
	//
	// ASC:
	// (Add | Subtract | Multiply)
	// ConvertInt
	//
	// Optimized:
	// (AddInt | SubtractInt | MultiplyInt)
	// 

	//
	// NOTE: This permutation can introduce serious bugs in your code.
	// This is not type safe.
	//
	// For instance imagine this case:
	//
	// PushDouble 1.9
	// PushDouble 1.9
	// Add
	// ConvertInt
	//
	// This will result in Add(1.9, 1.9) = 3.8 => ConvertInt(3.8) = 3
	//
	// The same with this permutation applied:
	//
	// PushDouble 1.9
	// PushDouble 1.9
	// AddInt
	//
	// This will result in AddInt(1.9, 1.9) = 2
	//
	// Now the same with Multiply(1.9, 1000) can yield an even bigger error.
	//

	public static final int PATTERN[][] = new int[][] {
			new int[] {
					Op.Add, Op.Subtract, Op.Multiply
			}, new int[] {
				Op.ConvertInt
			}
	};

	private boolean modificationMade = false;

	public IntegerCalculus()
	{
		super( PATTERN );
	}

	private AbstractOperation convertedType( final int code )
	{
		switch( code )
		{
			case Op.Add:
				return new AddInt();
			case Op.Subtract:
				return new SubtractInt();
			case Op.Multiply:
				return new MultiplyInt();

			default:
				return null;
		}
	}

	@Override
	protected void handleOccurrences( final AbcEnvironment environment,
			final Bytecode bytecode, final List<AbstractOperation> occurrences )
	{
		for( final AbstractOperation occurrence : occurrences )
		{
			final int indexOf = bytecode.indexOf( occurrence );

			bytecode.remove( indexOf + 1 );
			bytecode.replace( occurrence, convertedType( occurrence.code ) );

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
