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

package com.joa_ebert.apparat.taas.toolkit.livenessAnalysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.joa_ebert.apparat.abc.controlflow.BasicBlock;
import com.joa_ebert.apparat.abc.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.abc.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.VertexKind;
import com.joa_ebert.apparat.abc.controlflow.utils.DepthFirstIterator;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.expr.AbstractLocalExpr;
import com.joa_ebert.apparat.taas.expr.TDecLocal;
import com.joa_ebert.apparat.taas.expr.TIncLocal;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class LivenessAnalysis
{
	private final TaasMethod method;
	private final BasicBlockGraph<TaasVertex> graph;

	private final Map<BasicBlock<TaasVertex>, List<TaasLocal>> inSet = new HashMap<BasicBlock<TaasVertex>, List<TaasLocal>>();
	private final Map<BasicBlock<TaasVertex>, List<TaasLocal>> outSet = new HashMap<BasicBlock<TaasVertex>, List<TaasLocal>>();

	public LivenessAnalysis( final TaasMethod method )
			throws ControlFlowGraphException
	{
		this( method, method.code.toBlockGraph() );
	}

	public LivenessAnalysis( final TaasMethod method,
			final BasicBlockGraph<TaasVertex> graph )
	{
		this.method = method;
		this.graph = graph;
	}

	private void addAll( final List<TaasLocal> target,
			final List<TaasLocal> source )
	{
		for( final TaasLocal element : source )
		{
			if( !target.contains( element ) )
			{
				target.add( element );
			}
		}
	}

	private BasicBlock<TaasVertex> blockOf( final TaasVertex vertex )
	{
		for( final BasicBlock<TaasVertex> block : graph.vertexList() )
		{
			if( block.vertices().contains( vertex ) )
			{
				return block;
			}
		}

		throw new TaasException( "Vertex can not be resolved." );
	}

	private List<TaasLocal> def( final BasicBlock<TaasVertex> b )
	{
		final List<TaasLocal> result = new LinkedList<TaasLocal>();

		if( b.kind == VertexKind.Entry )
		{
			//
			// AVM+ fills local 0 always with the this or global scope.
			// Because of that we mark it defined in the entry block.
			//

			result.add( method.locals.get( 0 ) );

			final int n = method.parameters.size();

			for( int i = 0; i < n; ++i )
			{
				result.add( method.locals.get( 1 + i ) );
			}
		}

		nextLocal: for( final TaasLocal local : method.locals.getRegisterList() )
		{
			final Iterator<TaasVertex> iter = b.vertices().descendingIterator();

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( vertex.value instanceof AbstractLocalExpr )
				{
					if( ( (AbstractLocalExpr)vertex.value ).local == local )
					{
						if( !result.contains( local ) )
						{
							result.add( local );
						}

						continue nextLocal;
					}
					else if( TaasToolkit.references( vertex.value, local ) )
					{
						continue nextLocal;
					}
				}
				else if( TaasToolkit.references( vertex.value, local ) )
				{
					continue nextLocal;
				}
			}
		}

		return result;
	}

	public BasicBlockGraph<TaasVertex> getGraph()
	{
		return graph;
	}

	public List<TaasLocal> liveIn( final BasicBlock<TaasVertex> block )
	{
		return inSet.get( block );
	}

	public List<TaasLocal> liveIn( final TaasVertex vertex )
	{
		return liveIn( blockOf( vertex ) );
	}

	public List<TaasLocal> liveOut( final BasicBlock<TaasVertex> block )
	{
		return outSet.get( block );
	}

	public List<TaasLocal> liveOut( final TaasVertex vertex )
	{
		return liveOut( blockOf( vertex ) );
	}

	public void solve()
	{
		try
		{
			inSet.put( graph.getEntryVertex(), new LinkedList<TaasLocal>() );
			inSet.put( graph.getExitVertex(), new LinkedList<TaasLocal>() );

			//
			// Correct is an undefined value, but we keep it simple like that
			// and get rid of a lot of null checks.
			//

			outSet.put( graph.getExitVertex(), new LinkedList<TaasLocal>() );

			//
			// Optimize the liveness analysis by traversing in postorder.
			//

			final DepthFirstIterator<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>> iter = new DepthFirstIterator<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>>(
					graph );

			final LinkedList<BasicBlock<TaasVertex>> blockSet = new LinkedList<BasicBlock<TaasVertex>>();

			while( iter.hasNext() )
			{
				blockSet.push( iter.next() );
			}

			//
			// Remove the exit block.
			//

			blockSet.remove( graph.getExitVertex() );

			//
			// Initialize all other blocks to the empty set.
			//

			for( final BasicBlock<TaasVertex> block : blockSet )
			{
				inSet.put( block, new LinkedList<TaasLocal>() );
			}

			boolean changed;

			do
			{
				changed = false;

				for( final BasicBlock<TaasVertex> block : blockSet )
				{
					if( !outSet.containsKey( block ) )
					{
						changed = true;
					}

					final List<TaasLocal> newOutSet = unionSuccessorsOf( block );

					if( !newOutSet.equals( outSet.get( block ) ) )
					{
						changed = true;

						outSet.put( block, newOutSet );
					}

					final List<TaasLocal> useSet = use( block );
					final List<TaasLocal> defSet = def( block );
					final List<TaasLocal> newSet = new LinkedList<TaasLocal>();

					addAll( newSet, outSet.get( block ) );

					newSet.removeAll( defSet );

					addAll( useSet, newSet );

					if( useSet.size() != inSet.get( block ).size() )
					{
						changed = true;
					}
					else
					{
						changed = !useSet.equals( inSet.get( block ) )
								|| changed;
					}

					inSet.put( block, useSet );
				}
			}
			while( changed );

		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}
	}

	private List<TaasLocal> unionSuccessorsOf(
			final BasicBlock<TaasVertex> block )
			throws ControlFlowGraphException
	{
		final List<TaasLocal> result = new LinkedList<TaasLocal>();
		final List<BasicBlock<TaasVertex>> successors = graph
				.successorsOf( block );

		for( final BasicBlock<TaasVertex> successor : successors )
		{
			addAll( result, inSet.get( successor ) );
		}

		return result;
	}

	private List<TaasLocal> use( final BasicBlock<TaasVertex> b )
	{
		final List<TaasLocal> result = new LinkedList<TaasLocal>();

		nextLocal: for( final TaasLocal local : method.locals.getRegisterList() )
		{
			final Iterator<TaasVertex> iter = b.vertices().descendingIterator();

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( vertex.value instanceof AbstractLocalExpr )
				{
					if( ( (AbstractLocalExpr)vertex.value ).local == local )
					{
						if( ( vertex.value instanceof TIncLocal )
								|| ( vertex.value instanceof TDecLocal ) )
						{
							if( !result.contains( local ) )
							{
								result.add( local );
							}
						}

						continue nextLocal;
					}
					else if( TaasToolkit.references( vertex.value, local ) )
					{
						if( !result.contains( local ) )
						{
							result.add( local );
						}

						continue nextLocal;
					}
				}
				else if( TaasToolkit.references( vertex.value, local ) )
				{
					if( !result.contains( local ) )
					{
						result.add( local );
					}

					continue nextLocal;
				}
			}
		}

		return result;
	}
}
