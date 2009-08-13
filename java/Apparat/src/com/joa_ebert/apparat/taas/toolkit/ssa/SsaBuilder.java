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

package com.joa_ebert.apparat.taas.toolkit.ssa;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.joa_ebert.apparat.abc.controlflow.BasicBlock;
import com.joa_ebert.apparat.abc.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.VertexKind;
import com.joa_ebert.apparat.abc.controlflow.utils.DepthFirstIterator;
import com.joa_ebert.apparat.abc.controlflow.utils.Dominance;
import com.joa_ebert.apparat.taas.Taas;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasPhi;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.expr.AbstractLocalExpr;
import com.joa_ebert.apparat.taas.expr.TSetLocal;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.toolkit.livenessAnalysis.LivenessAnalysis;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class SsaBuilder implements ITaasTool
{
	private static final boolean DEBUG = true;

	private static final Taas TAAS = new Taas();

	private LivenessAnalysis livenessAnalysis;

	private BasicBlockGraph<TaasVertex> basicBlocks;

	private Dominance<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>> dominanceAnalysis;

	private Map<BasicBlock<TaasVertex>, List<TaasLocal>> localDefinitions;

	// private Map<BasicBlock<TaasVertex>, List<TaasLocal>> phiInsertions;

	private TaasMethod method;

	private DepthFirstIterator<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>> blockIterator()
	{
		try
		{
			return new DepthFirstIterator<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>>(
					basicBlocks );
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}
	}

	private void createBasicBlocks()
	{
		try
		{
			basicBlocks = method.code.toBlockGraph();
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}
	}

	private TaasVertex createPhi( final TaasLocal local, final TaasEdge edge )
	{
		return new TaasVertex( TAAS
				.setLocal( local, new TaasPhi( local, edge ) ) );
	}

	private TaasEdge edgeBetween( final BasicBlock<TaasVertex> source,
			final BasicBlock<TaasVertex> target )
	{
		final Iterator<TaasVertex> sourceIter = source.vertices()
				.descendingIterator();

		while( sourceIter.hasNext() )
		{
			final TaasVertex sourceVertex = sourceIter.next();

			final Iterator<TaasVertex> targetIter = target.vertices()
					.listIterator();

			while( targetIter.hasNext() )
			{
				final TaasVertex targetVertex = targetIter.next();

				try
				{
					for( final TaasEdge edge : method.code
							.outgoingOf( sourceVertex ) )
					{
						if( edge.endVertex == targetVertex )
						{
							return edge;
						}
					}
				}
				catch( final ControlFlowGraphException exception )
				{
					throw new TaasException( exception );
				}
			}
		}

		throw new TaasException( "Unreachable by definition." );
	}

	private void insertPhi( final TaasLocal local,
			final BasicBlock<TaasVertex> source,
			final BasicBlock<TaasVertex> target )
	{
		final TaasEdge phiEdge = edgeBetween( source, target );

		if( DEBUG )
		{
			System.out.println( "Insert phi for " + local.toString() + " into "
					+ target.toString() );

			System.out.println( "Using edge " + phiEdge.toString() );
		}

		for( final TaasVertex vertex : target.vertices() )
		{
			if( vertex.kind != VertexKind.Default )
			{
				continue;
			}

			if( vertex.value instanceof TSetLocal )
			{
				final TSetLocal tSetLocal = (TSetLocal)vertex.value;

				if( tSetLocal.value instanceof TaasPhi )
				{
					final TaasPhi existingPhi = (TaasPhi)tSetLocal.value;

					if( existingPhi.contains( local ) )
					{
						if( DEBUG )
						{
							System.out
									.println( "Phi has already been inserted for "
											+ local.toString() );
						}

						existingPhi.add( local, phiEdge );

						return;
					}
				}
				else
				{
					break;
				}
			}
			else
			{
				break;
			}
		}

		final TaasVertex phiVertex = createPhi( local, phiEdge );

		TaasToolkit.insertBefore( method, target.vertices().getFirst(),
				phiVertex );

		target.vertices().addFirst( phiVertex );
	}

	private void insertPhiFunctions()
	{
		//
		// TODO iterate over localDefinitions
		//

		for( final BasicBlock<TaasVertex> currentBlock : basicBlocks
				.vertexList() )
		{
			final List<TaasLocal> blockDefinitions = localDefinitions
					.get( currentBlock );

			if( DEBUG )
			{
				System.out.println( "Block: " + currentBlock.toString() );
			}

			if( blockDefinitions.isEmpty() )
			{
				if( DEBUG )
				{
					System.out.println( "No definitions." );
				}

				continue;
			}

			if( DEBUG )
			{
				System.out.println( "Defines:" + blockDefinitions.toString() );
			}

			final List<BasicBlock<TaasVertex>> dominanceFrontierSet = dominanceAnalysis
					.frontierOf( currentBlock );

			if( dominanceFrontierSet.isEmpty() )
			{
				if( DEBUG )
				{
					System.out.println( "Dominance frontier set is empty." );
				}

				continue;
			}

			if( DEBUG )
			{
				System.out.println( "Dominance frontier set: "
						+ dominanceFrontierSet.toString() );
			}

			for( final BasicBlock<TaasVertex> nextBlock : dominanceFrontierSet )
			{
				final List<TaasLocal> liveLocals = livenessAnalysis
						.liveIn( nextBlock );

				if( DEBUG )
				{
					System.out.println( "Dominance frontier: "
							+ nextBlock.toString() );
					System.out.println( "LiveIn: " + liveLocals.toString() );
				}

				for( final TaasLocal local : blockDefinitions )
				{
					if( liveLocals.contains( local ) )
					{
						insertPhi( local, currentBlock, nextBlock );
					}
				}
			}
		}
	}

	public boolean manipulate( final TaasMethod method )
	{
		this.method = method;

		if( DEBUG )
		{
			System.out.println( "Code before:" );
			System.out.println( method.code.debug() );
		}

		createBasicBlocks();

		solveDominanceFrontiers();
		solveLiveness();
		solveDefinitions();

		insertPhiFunctions();

		renameVariables();

		if( DEBUG )
		{
			System.out.println( "Code after:" );
			System.out.println( method.code.debug() );
		}

		return false;
	}

	private void renameVariables()
	{
		final DepthFirstIterator<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>> iter = blockIterator();
		final Map<TaasLocal, Integer> subscriptMap = new LinkedHashMap<TaasLocal, Integer>(
				method.locals.numRegisters() );

		final int n = method.parameters.size();

		for( final TaasLocal local : method.locals.getRegisterList() )
		{
			if( local.getIndex() <= n )
			{
				subscriptMap.put( local, 0 );
			}
			else
			{
				subscriptMap.put( local, -1 );
			}
		}

		iter.reverse();

		while( iter.hasNext() )
		{
			final BasicBlock<TaasVertex> basicBlock = iter.next();
			final List<TaasVertex> vertices = basicBlock.vertices();

			for( final TaasVertex vertex : vertices )
			{
				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				if( vertex.value instanceof AbstractLocalExpr )
				{
					final AbstractLocalExpr localExpr = (AbstractLocalExpr)vertex.value;
					final TaasLocal oldLocal = localExpr.local;
					final int newSubscript = subscriptMap.get( oldLocal ) + 1;
					final TaasLocal newLocal = oldLocal
							.newVersion( newSubscript );

					subscriptMap.put( oldLocal, newSubscript );

					localExpr.local = newLocal;

					//
					// Replace
					//

					repalcePhi( basicBlock, oldLocal, newLocal );

					// contributePhi( basicBlock, oldLocal, newLocal );

					replaceUses( basicBlock, vertices.listIterator( vertices
							.indexOf( vertex ) ), oldLocal, newLocal );
				}
			}
		}
	}

	private void repalcePhi( final BasicBlock<TaasVertex> basicBlock,
			final TaasLocal oldLocal, final TaasLocal newLocal )
	{
		//
		// 1.) Rename uses in phi functions.
		// 2.) Contribute to phi functions.
		//

		final List<BasicBlock<TaasVertex>> dominanceFrontierSet = dominanceAnalysis
				.frontierOf( basicBlock );

		if( !dominanceFrontierSet.isEmpty() )
		{
			for( final BasicBlock<TaasVertex> nextBlock : dominanceFrontierSet )
			{
				if( !livenessAnalysis.liveIn( nextBlock ).contains( oldLocal ) )
				{
					continue;
				}

				final TaasEdge phiEdge = edgeBetween( basicBlock, nextBlock );

				for( final TaasVertex vertex : nextBlock.vertices() )
				{
					if( vertex.value instanceof TSetLocal )
					{
						final TSetLocal setLocal = (TSetLocal)vertex.value;

						if( ( setLocal.local.getIndex() == oldLocal.getIndex() )
								&& ( setLocal.value instanceof TaasPhi ) )
						{
							final TaasPhi phi = (TaasPhi)setLocal.value;

							if( phi.contains( oldLocal ) )
							{
								for( final TaasPhi.Element element : phi.values )
								{
									if( element.value.equals( oldLocal )
											&& element.edge.equals( phiEdge ) )
									{
										System.out
												.println( "Replacing phi element "
														+ oldLocal.toString()
														+ " with "
														+ newLocal.toString() );

										element.value = newLocal;
									}
								}
							}
							else
							{
							}
						}
					}
					else
					{
						break;
					}
				}
			}
		}
	}

	private void replaceUses( final BasicBlock<TaasVertex> basicBlock,
			final Iterator<TaasVertex> iter, final TaasLocal oldLocal,
			final TaasLocal newLocal )
	{
		//
		// Rename uses in basic block.
		//

		while( iter.hasNext() )
		{
			final TaasVertex vertex = iter.next();

			if( vertex.kind != VertexKind.Default )
			{
				continue;
			}

			final TaasValue value = vertex.value;

			if( value instanceof AbstractLocalExpr )
			{
				final AbstractLocalExpr localExpr = (AbstractLocalExpr)value;

				if( localExpr.local.equals( newLocal ) )
				{
					//
					// We do not want to override the uses of a local when we
					// define a new local.
					//

					continue;
				}
				else if( localExpr.local.getIndex() == newLocal.getIndex()
						&& localExpr.local.getSubscript() >= newLocal
								.getSubscript() )
				{
					//
					// A newer version already exists.
					//

					return;
				}
			}

			if( TaasToolkit.references( value, oldLocal ) )
			{
				if( DEBUG )
				{
					System.out.println( "Replacing use of "
							+ oldLocal.toString() + " in " + value.toString()
							+ " with " + newLocal.toString() );
				}

				TaasToolkit.replace( value, oldLocal, newLocal );
			}
		}
	}

	private void solveDefinitions()
	{
		localDefinitions = new LinkedHashMap<BasicBlock<TaasVertex>, List<TaasLocal>>();

		for( final BasicBlock<TaasVertex> basicBlock : basicBlocks.vertexList() )
		{
			final List<TaasLocal> localsList = new LinkedList<TaasLocal>();

			for( final TaasVertex vertex : basicBlock.vertices() )
			{
				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				if( vertex.value instanceof AbstractLocalExpr )
				{
					final AbstractLocalExpr localExpr = (AbstractLocalExpr)vertex.value;

					localsList.add( localExpr.local );
				}
			}

			localDefinitions.put( basicBlock, localsList );
		}
	}

	private void solveDominanceFrontiers()
	{
		try
		{
			dominanceAnalysis = new Dominance<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>>(
					basicBlocks );

			dominanceAnalysis.solve();
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}
	}

	private void solveLiveness()
	{
		livenessAnalysis = new LivenessAnalysis( method, basicBlocks );
		livenessAnalysis.solve();
	}
}
