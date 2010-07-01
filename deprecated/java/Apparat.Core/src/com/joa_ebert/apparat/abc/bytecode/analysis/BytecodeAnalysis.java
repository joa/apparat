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

import java.util.logging.Logger;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class BytecodeAnalysis
{
	private static final boolean DEBUG = false;
	private static final Logger LOGGER = ( DEBUG ) ? Logger
			.getLogger( BytecodeAnalysis.class.getName() ) : null;

	private final Bytecode bytecode;
	private final AbcEnvironment environment;

	private int maxStack = -1;
	private int localCount = -1;
	private int maxScopeDepth = -1;

	public BytecodeAnalysis( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		this.environment = environment;
		this.bytecode = bytecode;
	}

	public int getInitScopeDepth()
	{
		return 0;
	}

	public int getLocalCount()
	{
		if( -1 == localCount )
		{
			final LocalCountAnalyzer localCountAnalyzer = new LocalCountAnalyzer();

			localCount = localCountAnalyzer.getLocalCount( bytecode );
		}

		return localCount;
	}

	private void performAnalysis()
	{
		if( -1 == maxStack && -1 == maxScopeDepth )
		{
			try
			{
				final int[] result = new StackAnalyzer().analyze( environment,
						bytecode );

				maxStack = result[ 0 ];
				maxScopeDepth = result[ 1 ];
			}
			catch( final ControlFlowGraphException e )
			{
				e.printStackTrace();

				maxStack = maxScopeDepth = 0;
			}
		}
	}

	public void updateAll()
	{
		updateMaxStack();
		updateMaxScopeDepth();
		updateInitScopeDepth();
		updateLocalCount();
	}

	public void updateInitScopeDepth()
	{
		if( DEBUG )
		{
			LOGGER.info( "Changing InitScopeDepth from "
					+ bytecode.methodBody.initScopeDepth + " to "
					+ getInitScopeDepth() );
		}

		bytecode.methodBody.initScopeDepth = getInitScopeDepth();
	}

	public void updateLocalCount()
	{
		if( DEBUG )
		{
			LOGGER
					.info( "Changing LocalCount from "
							+ bytecode.methodBody.localCount + " to "
							+ getLocalCount() );
		}

		bytecode.methodBody.localCount = getLocalCount();
	}

	public void updateMaxScopeDepth()
	{
		performAnalysis();

		if( DEBUG )
		{
			LOGGER.info( "Changing MaxScopeDepth from "
					+ bytecode.methodBody.maxScopeDepth + " to "
					+ maxScopeDepth );
		}

		bytecode.methodBody.maxScopeDepth = maxScopeDepth;

		// if( DEBUG )
		// {
		// LOGGER.info( "Changing MaxScopeDepth from "
		// + bytecode.methodBody.maxScopeDepth + " to "
		// + getMaxScopeDepth() );
		// }
		//
		// bytecode.methodBody.maxScopeDepth = getMaxScopeDepth();
	}

	public void updateMaxStack()
	{
		performAnalysis();

		if( DEBUG )
		{
			LOGGER.info( "Changing MaxStack from "
					+ bytecode.methodBody.maxStack + " to " + maxStack );
		}

		bytecode.methodBody.maxStack = maxStack;

		// if( DEBUG )
		// {
		// LOGGER.info( "Changing MaxStack from "
		// + bytecode.methodBody.maxStack + " to " + getMaxStack() );
		// }
		//
		// bytecode.methodBody.maxStack = getMaxStack();
	}
}
