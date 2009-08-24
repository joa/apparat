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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.controlflow.BasicBlock;
import com.joa_ebert.apparat.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.Path;
import com.joa_ebert.apparat.controlflow.utils.BasicBlockBuilder;
import com.joa_ebert.apparat.controlflow.utils.PathBuilder;

/**
 * 
 * @author Joa Ebert
 * 
 */
@SuppressWarnings( "deprecation" )
public class BytecodeAnalysis
{
	@Deprecated
	private static final boolean USE_BASIC_BLOCKS = true;

	private static final boolean DEBUG = false;
	private static final Logger LOGGER = ( DEBUG ) ? Logger
			.getLogger( BytecodeAnalysis.class.getName() ) : null;

	private final Bytecode bytecode;
	private final AbcEnvironment environment;

	@Deprecated
	private ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> graph;

	@Deprecated
	private BasicBlockGraph<BytecodeVertex> basicBlocks;

	@Deprecated
	private List<Path<BytecodeVertex, Edge<BytecodeVertex>>> paths;

	@Deprecated
	private List<Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>>> blockPaths;

	private int maxStack = -1;
	private int localCount = -1;
	private int maxScopeDepth = -1;

	public BytecodeAnalysis( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		this.environment = environment;
		this.bytecode = bytecode;
	}

	@Deprecated
	private BasicBlockGraph<BytecodeVertex> getBasicBlocks()
	{
		if( null == basicBlocks )
		{
			try
			{
				final BasicBlockBuilder<BytecodeVertex, Edge<BytecodeVertex>> blockBuilder = new BasicBlockBuilder<BytecodeVertex, Edge<BytecodeVertex>>(
						getGraph() );

				basicBlocks = blockBuilder.getBlockGraph();
			}
			catch( final ControlFlowGraphException cfgException )
			{
				throw new RuntimeException( cfgException );
			}
		}

		return basicBlocks;
	}

	@Deprecated
	private List<Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>>> getBlockPaths()
	{
		if( null == paths )
		{
			final PathBuilder<BytecodeVertex, Edge<BytecodeVertex>> pathBuilder = new PathBuilder<BytecodeVertex, Edge<BytecodeVertex>>();

			try
			{
				blockPaths = pathBuilder.getBasicBlockPaths( getBasicBlocks() );

			}
			catch( final ControlFlowGraphException e )
			{
				e.printStackTrace();

				blockPaths = new LinkedList<Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>>>();
			}
		}

		return blockPaths;
	}

	@Deprecated
	private ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> getGraph()
	{
		if( null == graph )
		{
			final ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder();

			builder.interpret( environment, bytecode );

			graph = builder.getGraph();
		}

		return graph;
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

	@Deprecated
	public int getMaxScopeDepth()
	{
		if( -1 == maxScopeDepth )
		{
			final StackAnalyzer stackAnalyzer = new StackAnalyzer();

			if( USE_BASIC_BLOCKS )
			{
				final List<Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>>> paths = getBlockPaths();

				for( final Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>> path : paths )
				{
					final int depth = stackAnalyzer.getMaxScope2( path );

					if( depth > maxScopeDepth )
					{
						maxScopeDepth = depth;
					}
				}
			}
			else
			{
				final List<Path<BytecodeVertex, Edge<BytecodeVertex>>> paths = getPaths();

				for( final Path<BytecodeVertex, Edge<BytecodeVertex>> path : paths )
				{
					final int depth = stackAnalyzer.getMaxScope( path );

					if( depth > maxScopeDepth )
					{
						maxScopeDepth = depth;
					}
				}
			}
		}

		return maxScopeDepth;
	}

	@Deprecated
	public int getMaxStack()
	{
		if( -1 == maxStack )
		{
			final StackAnalyzer stackAnalyzer = new StackAnalyzer();

			if( USE_BASIC_BLOCKS )
			{
				if( USE_BASIC_BLOCKS )
				{
					final List<Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>>> paths = getBlockPaths();

					for( final Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>> path : paths )
					{
						final int depth = stackAnalyzer.getMaxStack2( path );

						if( depth > maxStack )
						{
							maxStack = depth;
						}
					}
				}
			}
			else
			{
				final List<Path<BytecodeVertex, Edge<BytecodeVertex>>> paths = getPaths();

				for( final Path<BytecodeVertex, Edge<BytecodeVertex>> path : paths )
				{
					final int depth = stackAnalyzer.getMaxStack( path.get( 0 ),
							path.get( path.size() - 1 ), path );

					if( depth > maxStack )
					{
						maxStack = depth;
					}
				}
			}
		}

		return maxStack;
	}

	@Deprecated
	private List<Path<BytecodeVertex, Edge<BytecodeVertex>>> getPaths()
	{
		if( null == paths )
		{
			final PathBuilder<BytecodeVertex, Edge<BytecodeVertex>> pathBuilder = new PathBuilder<BytecodeVertex, Edge<BytecodeVertex>>();

			try
			{
				paths = pathBuilder.getPaths( getGraph() );
			}
			catch( final ControlFlowGraphException e )
			{
				e.printStackTrace();

				paths = new LinkedList<Path<BytecodeVertex, Edge<BytecodeVertex>>>();
			}
		}

		return paths;
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
