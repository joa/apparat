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
import com.joa_ebert.apparat.abc.MultinameKind;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceKind;
import com.joa_ebert.apparat.abc.NamespaceSet;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.analysis.AbstractPatternMatcher;
import com.joa_ebert.apparat.abc.bytecode.operations.GetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.SetProperty;
import com.joa_ebert.apparat.abc.multinames.MultinameL;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class NamespaceSetCleanup extends AbstractPatternMatcher implements
		IBytecodePermutation
{
	//
	//
	// ActionScript:
	// arrayType[index] = value;
	//
	// ASC:
	// GetProperty MultinameL(NamespaceSet(Name0, Name1, ... NameN))
	//
	// Optimized:
	// GetProperty MultinameL(NamespaceSet(PackageNamespace("")))
	// 

	public static final int[][] PATTERN = new int[][] {
			new int[] {
					Op.PushByte, Op.PushShort, Op.PushInt
			}, new int[] {
					Op.GetProperty, Op.SetProperty
			}
	};

	private boolean modificationMade = false;

	public NamespaceSetCleanup()
	{
		super( PATTERN );
	}

	private void cleanup( final MultinameL multiname )
	{
		final NamespaceSet publicNamespaceSet = new NamespaceSet();
		final Namespace publicNamespace = new Namespace();

		publicNamespace.kind = NamespaceKind.PackageNamespace;
		publicNamespace.name = "";

		publicNamespaceSet.add( publicNamespace );

		multiname.namespaceSet = publicNamespaceSet;

	}

	@Override
	protected void handleOccurrences( final AbcEnvironment environment,
			final Bytecode bytecode, final List<AbstractOperation> occurrences )
	{
		for( final AbstractOperation occurrence : occurrences )
		{
			final AbstractOperation next = bytecode.get( bytecode
					.indexOf( occurrence ) + 1 );

			if( next.code == Op.GetProperty )
			{
				final GetProperty getProperty = (GetProperty)next;

				if( getProperty.property.kind == MultinameKind.MultinameL )
				{
					final MultinameL multiname = (MultinameL)getProperty.property;

					if( multiname.namespaceSet.size() != 1 )
					{
						cleanup( multiname );
						modificationMade = true;
					}
				}
			}
			else if( next.code == Op.SetProperty )
			{
				final SetProperty setProperty = (SetProperty)next;

				if( setProperty.property.kind == MultinameKind.MultinameL )
				{
					final MultinameL multiname = (MultinameL)setProperty.property;

					if( multiname.namespaceSet.size() != 1 )
					{
						cleanup( multiname );
						modificationMade = true;
					}
				}
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
