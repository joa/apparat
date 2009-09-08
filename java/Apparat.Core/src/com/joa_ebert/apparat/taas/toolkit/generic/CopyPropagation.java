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

package com.joa_ebert.apparat.taas.toolkit.generic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.BasicBlock;
import com.joa_ebert.apparat.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
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
public final class CopyPropagation implements ITaasTool
{
	private static final boolean DEBUG = false;

	private static final Logger LOG = ( DEBUG ) ? Logger
			.getLogger( CopyPropagation.class.getName() ) : null;

	private boolean cp( final AbcEnvironment environment,
			final TaasMethod method ) throws ControlFlowGraphException
	{
		if( DEBUG )
		{
			LOG.entering( "CopyPropagation", "cp" );
			LOG.info( "Begin CopyPropagation..." );
		}

		boolean changed = false;

		final LivenessAnalysis la = new LivenessAnalysis( method );
		final BasicBlockGraph<TaasVertex> graph = la.getGraph();

		la.solve();

		for( final BasicBlock<TaasVertex> block : graph.vertexList() )
		{
			final List<TaasLocal> liveOut = la.liveOut( block );
			final List<TaasVertex> vertices = block.vertices();

			final List<TSetLocal> locals = new LinkedList<TSetLocal>();

			for( final TaasVertex vertex : vertices )
			{
				if( VertexKind.Default != vertex.kind )
				{
					continue;
				}

				final TaasValue value = vertex.value;

				if( value instanceof TSetLocal )
				{
					final TSetLocal setLocal = (TSetLocal)value;

					if( !setLocal.hasSideEffects()
							&& !liveOut.contains( setLocal.local ) )
					{
						if( DEBUG )
						{
							LOG.info( "Candidate: " + setLocal );
						}

						locals.add( setLocal );
					}
				}
			}

			nextLocal: for( final TSetLocal setLocal : locals )
			{
				if( DEBUG )
				{
					LOG.info( "Processing " + setLocal );
				}

				if( setLocal.value instanceof TaasConstant )
				{
					if( DEBUG )
					{
						LOG.info( "Constant assignment: x = const" );
					}

					//
					// Constant assignment:
					//
					// x = const
					//

					final ListIterator<TaasVertex> iter = vertices
							.listIterator();

					final List<TaasVertex> verticesToRemove = new LinkedList<TaasVertex>();

					boolean startPropagation = false;

					while( iter.hasNext() )
					{
						final TaasVertex vertex = iter.next();

						if( VertexKind.Default != vertex.kind )
						{
							continue;
						}

						final TaasValue value = vertex.value;

						if( value == setLocal )
						{
							if( DEBUG )
							{
								LOG.info( "Start propagation at " + value );
							}

							startPropagation = true;
							verticesToRemove.add( vertex );
							continue;
						}
						else if( value instanceof AbstractLocalExpr
								&& ( (AbstractLocalExpr)value ).local
										.getIndex() == setLocal.local
										.getIndex() )
						{
							if( DEBUG )
							{
								LOG.info( "Stop propagation at " + value );
							}

							continue nextLocal;
						}
						else if( TaasToolkit.references( value, setLocal.local ) )
						{
							if( startPropagation )
							{
								if( DEBUG )
								{
									LOG.info( "Propagating copy into " + value );
								}

								TaasToolkit.replace( value, setLocal.local,
										setLocal.value.dup() );

								changed = true;
							}
						}
					}

					for( final TaasVertex vertex : verticesToRemove )
					{
						if( DEBUG )
						{
							LOG.info( "Removing vertex " + vertex );
						}

						TaasToolkit.remove( method, vertex );
						changed = true;
					}
				}
				else if( setLocal.value instanceof TaasLocal )
				{
					if( DEBUG )
					{
						LOG.info( "Copy assignment: x = y" );
					}

					//
					// Copy assignment:
					//
					// x = y;
					//

					final TaasLocal copy = (TaasLocal)setLocal.value;
					final ListIterator<TaasVertex> iter = vertices
							.listIterator();

					if( DEBUG )
					{
						LOG.info( "Copy is " + copy );
					}

					final List<TaasVertex> verticesToRemove = new LinkedList<TaasVertex>();

					boolean startPropagation = false;

					while( iter.hasNext() )
					{
						final TaasVertex vertex = iter.next();

						if( VertexKind.Default != vertex.kind )
						{
							continue;
						}

						final TaasValue value = vertex.value;

						if( value == setLocal )
						{
							if( DEBUG )
							{
								LOG.info( "Start propagation at " + value );
							}

							startPropagation = true;
							verticesToRemove.add( vertex );
							continue;
						}
						else if( value instanceof AbstractLocalExpr
								&& ( ( (AbstractLocalExpr)value ).local
										.getIndex() == setLocal.local
										.getIndex() || ( (AbstractLocalExpr)value ).local
										.getIndex() == copy.getIndex() ) )
						{
							//
							// || ( (AbstractLocalExpr)value ).local
							// .getIndex() == copy.getIndex() )
							//

							if( DEBUG )
							{
								LOG.info( "Stop propagation at " + value );
							}

							continue nextLocal;
						}
						else if( TaasToolkit.references( value, setLocal.local ) )
						{
							if( startPropagation )
							{
								if( DEBUG )
								{
									LOG.info( "Propagating copy into " + value );
								}

								TaasToolkit.replace( value, setLocal.local,
										setLocal.value );

								changed = true;
							}
						}
					}

					for( final TaasVertex vertex : verticesToRemove )
					{
						if( DEBUG )
						{
							LOG.info( "Removing vertex " + vertex );
						}

						TaasToolkit.remove( method, vertex );
						changed = true;
					}
				}
				else
				{
					if( DEBUG )
					{
						LOG.info( "Other assignment: x = expr" );
					}

					//
					// Other assignment:
					//
					// x = 2 * y;
					// x = Math.random();
					// .
					// .
					// .
					// etc.
					//

					ListIterator<TaasVertex> iter = vertices.listIterator();

					int uses = 0;

					boolean startPropagation = false;

					while( iter.hasNext() )
					{
						final TaasVertex vertex = iter.next();

						if( VertexKind.Default != vertex.kind )
						{
							continue;
						}

						final TaasValue value = vertex.value;

						if( value == setLocal )
						{
							startPropagation = true;
							continue;
						}
						else if( value instanceof AbstractLocalExpr
								&& ( (AbstractLocalExpr)value ).local
										.getIndex() == setLocal.local
										.getIndex() )
						{
							continue nextLocal;
						}
						else
						{
							if( startPropagation )
							{
								uses += TaasToolkit.numReferences( value,
										setLocal.local );
							}
						}
					}

					if( DEBUG )
					{
						LOG.info( "Value uses: " + uses );
					}

					if( 1 == uses )
					{
						startPropagation = false;

						iter = vertices.listIterator();

						final List<TaasVertex> verticesToRemove = new LinkedList<TaasVertex>();

						final LinkedList<TaasLocal> sideEffects = new LinkedList<TaasLocal>();

						TaasToolkit.searchAll( setLocal, TaasLocal.class, true,
								sideEffects );

						while( iter.hasNext() )
						{
							final TaasVertex vertex = iter.next();

							if( VertexKind.Default != vertex.kind )
							{
								continue;
							}

							final TaasValue value = vertex.value;

							if( value == setLocal )
							{
								if( DEBUG )
								{
									LOG.info( "Start propagation at " + value );
								}

								startPropagation = true;
								verticesToRemove.add( vertex );
								continue;
							}
							else if( value instanceof AbstractLocalExpr )
							{
								if( ( (AbstractLocalExpr)value ).local
										.getIndex() == setLocal.local
										.getIndex() )
								{
									if( DEBUG )
									{
										LOG.info( "Stop propagation at "
												+ value );
									}

									continue nextLocal;
								}

								if( value instanceof TSetLocal )
								{
									final TSetLocal setLocal2 = (TSetLocal)value;

									for( final TaasLocal local : sideEffects )
									{
										if( setLocal2.local.getIndex() == local
												.getIndex() )
										{
											if( startPropagation
													&& TaasToolkit.references(
															value,
															setLocal.local ) )
											{
												if( DEBUG )
												{
													LOG
															.info( "Propagating value into "
																	+ value );
												}

												TaasToolkit.replace( value,
														setLocal.local,
														setLocal.value );

												changed = true;
											}

											if( startPropagation )
											{
												if( DEBUG )
												{
													LOG
															.info( "Stop propagation at "
																	+ value );
												}

												continue nextLocal;
											}
										}
									}

									if( startPropagation
											&& TaasToolkit.references( value,
													setLocal.local ) )
									{
										if( DEBUG )
										{
											LOG.info( "Propagating value into "
													+ value );
										}

										TaasToolkit.replace( value,
												setLocal.local, setLocal.value );

										changed = true;
									}
								}
								else if( TaasToolkit.references( value,
										setLocal.local ) )
								{
									if( startPropagation )
									{
										if( DEBUG )
										{
											LOG.info( "Propagating value into "
													+ value );
										}

										TaasToolkit.replace( value,
												setLocal.local, setLocal.value );

										changed = true;
									}
								}
							}
							else if( TaasToolkit.references( value,
									setLocal.local ) )
							{
								if( startPropagation )
								{
									if( DEBUG )
									{
										LOG.info( "Propagating value into "
												+ value );
									}

									TaasToolkit.replace( value, setLocal.local,
											setLocal.value );

									changed = true;
								}
							}
						}

						for( final TaasVertex vertex : verticesToRemove )
						{
							if( DEBUG )
							{
								LOG.info( "Removing vertex " + vertex );
							}

							TaasToolkit.remove( method, vertex );
							changed = true;
						}
					}
				}
			}
		}

		if( DEBUG )
		{
			LOG.info( "Changed: " + changed );
			LOG.exiting( "CopyPropagation", "cp" );
		}

		return changed;
	}

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = false;

		try
		{
			changed = cp( environment, method );
		}
		catch( final ControlFlowGraphException exception )
		{
			throw new TaasException( exception );
		}

		try
		{
			final LinkedList<TaasVertex> list = method.code.vertexList();
			final LinkedList<TaasVertex> removes = new LinkedList<TaasVertex>();
			final Iterator<TaasVertex> iter = list.listIterator();

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( VertexKind.Default != vertex.kind )
				{
					continue;
				}

				final TaasValue value = vertex.value;

				if( value instanceof TaasConstant && value.isConstant() )
				{
					removes.add( vertex );
				}
				else if( value instanceof TaasLocal )
				{
					removes.add( vertex );
				}
				else
				{
					final Iterator<TaasVertex> refIter = list
							.descendingIterator();

					boolean canRemove = false;

					while( refIter.hasNext() )
					{
						final TaasVertex refVert = refIter.next();
						final TaasValue refValue = refVert.value;

						if( refValue == value )
						{
							break;
						}
						else if( TaasToolkit.references( refValue, value ) )
						{
							canRemove = true;
							break;
						}
					}

					if( canRemove )
					{
						removes.add( vertex );
					}
				}
			}

			if( !changed )
			{
				changed = !removes.isEmpty();
			}

			for( final TaasVertex vertex : removes )
			{
				TaasToolkit.remove( method, vertex );
			}
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS && changed )
		{
			TaasToolkit.debug( "CopyPropagation", method );
		}

		return changed;
	}
}
