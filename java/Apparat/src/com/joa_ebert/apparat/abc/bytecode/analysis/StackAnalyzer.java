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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.ApplyType;
import com.joa_ebert.apparat.abc.bytecode.operations.Call;
import com.joa_ebert.apparat.abc.bytecode.operations.CallMethod;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropLex;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.CallProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.CallStatic;
import com.joa_ebert.apparat.abc.bytecode.operations.CallSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.CallSuperVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.Construct;
import com.joa_ebert.apparat.abc.bytecode.operations.ConstructProp;
import com.joa_ebert.apparat.abc.bytecode.operations.ConstructSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.DeleteProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.FindPropStrict;
import com.joa_ebert.apparat.abc.bytecode.operations.FindProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetDescendants;
import com.joa_ebert.apparat.abc.bytecode.operations.GetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.InitProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.NewArray;
import com.joa_ebert.apparat.abc.bytecode.operations.NewObject;
import com.joa_ebert.apparat.abc.bytecode.operations.SetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.SetSuper;
import com.joa_ebert.apparat.controlflow.BasicBlock;
import com.joa_ebert.apparat.controlflow.BasicBlockGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.Path;
import com.joa_ebert.apparat.controlflow.VertexKind;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class StackAnalyzer
{
	private static <E> List<E> cloneList( final List<E> list )
	{
		final List<E> result = new LinkedList<E>();

		for( final E element : list )
		{
			result.add( element );
		}

		return result;
	}

	/**
	 * Computes the maximum stack and maximum scope depth for given bytecode.
	 * 
	 * The values are not completely accurate because only the depth of each
	 * basic block is analyzed and not a complete path. This might result in a
	 * slightly higher stack depth but is much faster instead of evaluating all
	 * paths.
	 * 
	 * @param environment
	 *            The current environment.
	 * @param bytecode
	 *            The bytecode to analyze.
	 * @return An array of results. The first index is the stack, the second the
	 *         scope depth.
	 * @throws ControlFlowGraphException
	 *             If any control flow error occurs.
	 */
	public int[] analyze( final AbcEnvironment environment,
			final Bytecode bytecode ) throws ControlFlowGraphException
	{
		//
		// Build the graph structures.
		//

		final ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();

		cfgBuilder.interpret( environment, bytecode );

		final ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> controlFlowGraph = cfgBuilder
				.getGraph();

		final BasicBlockGraph<BytecodeVertex> blockGraph = controlFlowGraph
				.toBlockGraph();

		//
		// We will store the maximum stack and scope depth of each
		// basic block in a hash map.
		//

		final HashMap<BasicBlock<BytecodeVertex>, Integer> stackDepths = new LinkedHashMap<BasicBlock<BytecodeVertex>, Integer>();
		final HashMap<BasicBlock<BytecodeVertex>, Integer> scopeDepths = new LinkedHashMap<BasicBlock<BytecodeVertex>, Integer>();

		//
		// We will have to figure out the delta of each basic block.
		//

		for( final BasicBlock<BytecodeVertex> basicBlock : blockGraph
				.vertexList() )
		{
			int stack = 0;
			int maxStack = 0;

			int scopeDepth = 0;
			int maxScopeDepth = 0;

			final ListIterator<BytecodeVertex> iter = basicBlock.vertices()
					.listIterator();

			while( iter.hasNext() )
			{
				final BytecodeVertex vertex = iter.next();

				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				final AbstractOperation operation = vertex.getOperation();
				final int code = operation.code;

				stack = modifyOperandStack( stack, operation, code );
				scopeDepth = modifyScopeStack( scopeDepth, code );

				if( stack > maxStack )
				{
					maxStack = stack;
				}

				if( scopeDepth > maxScopeDepth )
				{
					maxScopeDepth = scopeDepth;
				}
			}

			stackDepths.put( basicBlock, maxStack );
			scopeDepths.put( basicBlock, maxScopeDepth );
		}

		//
		// Now the graph of basic blocks gets reduced.
		//

		final List<BasicBlock<BytecodeVertex>> blocksToMerge = new LinkedList<BasicBlock<BytecodeVertex>>();
		final List<BasicBlock<BytecodeVertex>> blocksToRemove = new LinkedList<BasicBlock<BytecodeVertex>>();

		loop: while( true )
		{
			search: for( final BasicBlock<BytecodeVertex> basicBlock : blockGraph
					.vertexList() )
			{

				final int outDegree = blockGraph.outdegreeOf( basicBlock );

				if( 0 == outDegree )
				{
					//
					// The vertex is a sink. Most probably because this is
					// either a fully reduced graph and this vertex left
					// is either the entry or it can be the exit.
					//
					// For now the entry and exit will never be removed.
					//

					if( 0 == blockGraph.indegreeOf( basicBlock ) )
					{
						//
						// The block is dead. We can remove it.
						//

						if( basicBlock.kind != VertexKind.Default )
						{
							continue search;
						}

						blocksToRemove.add( basicBlock );
						break search;
					}
				}
				else if( 1 == outDegree )
				{
					//
					// Exactly one block is following the current.
					//

					final Edge<BasicBlock<BytecodeVertex>> edge = blockGraph
							.outgoingOf( basicBlock ).get( 0 );
					final BasicBlock<BytecodeVertex> nextBlock = edge.endVertex;

					if( nextBlock == basicBlock )
					{
						//
						// We have found a loop. We are save to remove the
						// edge.
						//

						blockGraph.remove( edge );
						continue loop;
					}
					else if( 1 == blockGraph.indegreeOf( nextBlock ) )
					{
						//
						// The next basic block has only the current block
						// as its predecessor.
						//
						// We are safe to remove the following block while
						// we have to add the operand and scope stack values.
						//
						// The next block will not be removed. It will be merged
						// into the current. We will patch all edges later.
						//

						if( nextBlock.kind != VertexKind.Default )
						{
							continue search;
						}

						final int maxStack0 = stackDepths.get( basicBlock );
						final int maxScopeDepth0 = scopeDepths.get( basicBlock );
						final int maxStack1 = stackDepths.get( nextBlock );
						final int maxScopeDepth1 = scopeDepths.get( nextBlock );

						stackDepths.put( basicBlock, maxStack0 + maxStack1 );
						stackDepths.remove( nextBlock );

						scopeDepths.put( basicBlock, maxScopeDepth0
								+ maxScopeDepth1 );
						scopeDepths.remove( nextBlock );

						blocksToMerge.add( nextBlock );
						break search;
					}
				}
				else
				{
					//
					// More than one edge is outgoing. Still no surprise.

					//

					final List<Edge<BasicBlock<BytecodeVertex>>> outgoingOf = blockGraph
							.outgoingOf( basicBlock );

					if( outgoingOf.size() == 2 )
					{
						//
						// We have only two vertices to compare.
						// This is a special case.
						//
						// Imagine the following:
						//
						// A -> B
						// A -> C
						// B -> C
						//
						// This is a simple if statement. We can remove the
						// edge A -> C once we figured out who is B and C.
						//
						//

						final BasicBlock<BytecodeVertex> block0 = outgoingOf
								.get( 0 ).endVertex;

						final BasicBlock<BytecodeVertex> block1 = outgoingOf
								.get( 1 ).endVertex;

						if( blockGraph.indegreeOf( block0 ) == 1
								&& blockGraph.indegreeOf( block1 ) == 1 )
						{
							if( blockGraph.outdegreeOf( block0 ) == 1
									&& blockGraph.outgoingOf( block0 ).get( 0 ).endVertex == block1 )
							{
								blockGraph.remove( outgoingOf.get( 1 ) );
								continue loop;
							}
							else if( blockGraph.outdegreeOf( block1 ) == 1
									&& blockGraph.outgoingOf( block1 ).get( 0 ).endVertex == block0 )
							{
								blockGraph.remove( outgoingOf.get( 0 ) );
								continue loop;
							}
							else
							{
								//
								// This is not a simple if but maybe an if-else
								// construct like this:
								// 
								// A -> B
								// A -> C
								// B -> D
								// C -> D
								//
								// We are safe to remove either B or C
								// completely once we figured out who has the
								// higher stack depth. We will keep only the
								// vertex with the highest depth.
								//

								final List<Edge<BasicBlock<BytecodeVertex>>> outgoingOf0 = blockGraph
										.outgoingOf( block0 );
								final List<Edge<BasicBlock<BytecodeVertex>>> outgoingOf1 = blockGraph
										.outgoingOf( block1 );

								if( outgoingOf0.size() == 1
										&& outgoingOf1.size() == 1 )
								{
									if( outgoingOf0.get( 0 ).endVertex == outgoingOf1
											.get( 0 ).endVertex )
									{
										final int stack0 = stackDepths
												.get( block0 );
										final int scopeDepth0 = scopeDepths
												.get( block0 );

										final int stack1 = stackDepths
												.get( block1 );
										final int scopeDepth1 = scopeDepths
												.get( block1 );

										if( stack0 >= stack1
												&& scopeDepth0 >= scopeDepth1 )
										{
											blocksToRemove.add( block1 );
											break search;
										}
										else if( stack1 >= stack0
												&& scopeDepth1 >= scopeDepth0 )
										{
											blocksToRemove.add( block0 );
											break search;
										}
									}
								}
							}
						}
					}
					else
					{
						//
						// More than two edges. This is a switch construct or
						// something else.
						//
						// We are safe to keep only the best path if all blocks
						// have the same merge point.
						// 
						// E.g.:
						//
						// A -> B0
						// A -> B1
						// A -> ..
						// A -> Bn
						// B0 -> C
						// B1 -> C
						// .. -> C
						// Bn -> C
						//
						// In that case we search for the block Bn which has
						// the highest operand and scope stack.
						// All the other vertices can be ignored.
						//

						boolean isReducible = true;
						BasicBlock<BytecodeVertex> mergeBlock = null;

						int maxStack = -1;
						int maxScopeDepth = -1;

						BasicBlock<BytecodeVertex> highestStack = null;
						BasicBlock<BytecodeVertex> highestScope = null;

						for( final Edge<BasicBlock<BytecodeVertex>> edge : outgoingOf )
						{
							final BasicBlock<BytecodeVertex> nextBlock = edge.endVertex;

							final int stack = stackDepths.get( nextBlock );
							final int scopeDepth = scopeDepths.get( nextBlock );

							if( stack > maxStack )
							{
								highestStack = nextBlock;
								maxStack = stack;
							}

							if( scopeDepth > maxScopeDepth )
							{
								highestScope = nextBlock;
								maxScopeDepth = scopeDepth;
							}

							if( 1 != blockGraph.indegreeOf( nextBlock )
									|| 1 != blockGraph.outdegreeOf( nextBlock ) )
							{
								//
								// This is not a reducible case since one of
								// the blocks is reached from somewhere else.
								//

								isReducible = false;
								break;
							}
							else
							{
								final List<Edge<BasicBlock<BytecodeVertex>>> outgoingOf2 = blockGraph
										.outgoingOf( nextBlock );

								if( null == mergeBlock )
								{
									mergeBlock = outgoingOf2.get( 0 ).endVertex;
								}
								else
								{
									if( mergeBlock != outgoingOf2.get( 0 ).endVertex )
									{
										//
										// Not all blocks merge at the same
										// position so we are not safe.
										//

										isReducible = false;
										break;
									}
								}
							}
						}

						if( isReducible && highestScope == highestStack )
						{
							for( final Edge<BasicBlock<BytecodeVertex>> edge : outgoingOf )
							{
								final BasicBlock<BytecodeVertex> nextBlock = edge.endVertex;

								if( nextBlock != highestStack )
								{
									blocksToRemove.add( nextBlock );
								}
							}

							if( !blocksToRemove.isEmpty() )
							{
								break search;
							}
						}
					}
				}
			}

			if( !blocksToMerge.isEmpty() || !blocksToRemove.isEmpty() )
			{
				for( final BasicBlock<BytecodeVertex> blockToRemove : blocksToRemove )
				{
					blockGraph.remove( blockToRemove );
				}

				for( final BasicBlock<BytecodeVertex> blockToMerge : blocksToMerge )
				{
					//
					// Here we merge all paths like A -> B -> C and patch all
					// edges that B had so that A owns them now.
					//

					final BasicBlock<BytecodeVertex> predecessor = blockGraph
							.incommingOf( blockToMerge ).get( 0 ).startVertex;
					final List<Edge<BasicBlock<BytecodeVertex>>> edgesToKeep = cloneList( blockGraph
							.outgoingOf( blockToMerge ) );

					blockGraph.remove( blockToMerge );

					for( final Edge<BasicBlock<BytecodeVertex>> edge : edgesToKeep )
					{
						if( !blockGraph.containsEdge( predecessor,
								edge.endVertex ) )
						{
							blockGraph
									.add( new Edge<BasicBlock<BytecodeVertex>>(
											predecessor, edge.endVertex ) );
						}
					}
				}

				blocksToRemove.clear();
				blocksToMerge.clear();
			}
			else
			{
				break;
			}
		}

		//
		// Now that the graph is simplified we can evaluate all possible paths.
		// This is still critical.
		//

		final LinkedList<Integer> stackResults = new LinkedList<Integer>();
		final LinkedList<Integer> scopeResults = new LinkedList<Integer>();

		walk(
				0,
				0,
				blockGraph.getEntryVertex(),
				new Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>>(),
				stackDepths, scopeDepths, stackResults, scopeResults,
				blockGraph );

		int maxStack = 0;
		int maxScopeDepth = 0;

		for( final Integer stack : stackResults )
		{
			if( stack > maxStack )
			{
				maxStack = stack;
			}
		}

		for( final Integer scopeDepth : scopeResults )
		{
			if( scopeDepth > maxScopeDepth )
			{
				maxScopeDepth = scopeDepth;
			}
		}

		return new int[] {
				maxStack, maxScopeDepth
		};
	}

	private int deltaOf( final AbstractMultiname name )
	{
		return 0;
	}

	@Deprecated
	public int getMaxScope(
			final Path<BytecodeVertex, Edge<BytecodeVertex>> path )
	{
		final Iterator<BytecodeVertex> iter = path.listIterator();

		int scopeDepth = 0;
		int maxScopeDepth = 0;

		while( iter.hasNext() )
		{
			final BytecodeVertex vertex = iter.next();

			if( vertex.kind != VertexKind.Default )
			{
				continue;
			}

			final AbstractOperation operation = vertex.getOperation();
			final int code = operation.code;

			scopeDepth = modifyScopeStack( scopeDepth, code );

			if( scopeDepth > maxScopeDepth )
			{
				maxScopeDepth = scopeDepth;
			}
		}

		return maxScopeDepth;
	}

	@Deprecated
	public int getMaxScope2(
			final Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>> path )
	{
		final Iterator<BasicBlock<BytecodeVertex>> iter = path.listIterator();

		int scopeDepth = 0;
		int maxScopeDepth = 0;

		while( iter.hasNext() )
		{
			final BasicBlock<BytecodeVertex> basicBlock = iter.next();
			final ListIterator<BytecodeVertex> vertexIter = basicBlock
					.vertices().listIterator();

			while( vertexIter.hasNext() )
			{
				final BytecodeVertex vertex = vertexIter.next();

				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				final AbstractOperation operation = vertex.getOperation();
				final int code = operation.code;

				scopeDepth = modifyScopeStack( scopeDepth, code );

				if( scopeDepth > maxScopeDepth )
				{
					maxScopeDepth = scopeDepth;
				}
			}
		}

		return maxScopeDepth;
	}

	@Deprecated
	public int getMaxStack( final BytecodeVertex startVertex,
			final BytecodeVertex endVertex,
			final Path<BytecodeVertex, Edge<BytecodeVertex>> path )
	{
		final Iterator<BytecodeVertex> iter = path.listIterator( path
				.indexOf( startVertex ) );

		int stack = 0;
		int maxStack = 0;

		while( iter.hasNext() )
		{
			final BytecodeVertex vertex = iter.next();

			if( vertex.kind != VertexKind.Default )
			{
				continue;
			}

			final AbstractOperation operation = vertex.getOperation();
			final int code = operation.code;

			stack = modifyOperandStack( stack, operation, code );

			if( stack > maxStack )
			{
				maxStack = stack;
			}

			if( vertex.equals( endVertex ) )
			{
				break;
			}
		}

		return maxStack;
	}

	@Deprecated
	public int getMaxStack2(
			final Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>> path )
	{
		final Iterator<BasicBlock<BytecodeVertex>> iter = path.listIterator();

		int stack = 0;
		int maxStack = 0;

		while( iter.hasNext() )
		{
			final BasicBlock<BytecodeVertex> basicBlock = iter.next();
			final ListIterator<BytecodeVertex> vertexIter = basicBlock
					.vertices().listIterator();

			while( vertexIter.hasNext() )
			{
				final BytecodeVertex vertex = vertexIter.next();

				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				final AbstractOperation operation = vertex.getOperation();
				final int code = operation.code;

				stack = modifyOperandStack( stack, operation, code );

				if( stack > maxStack )
				{
					maxStack = stack;
				}

			}
		}

		return maxStack;
	}

	private int modifyOperandStack( int stack,
			final AbstractOperation operation, final int code )
	{
		switch( code )
		{
			case Op.ApplyType:
				stack -= ( (ApplyType)operation ).typeSize;
				break;

			case Op.IfEqual:
			case Op.IfGreaterEqual:
			case Op.IfGreaterThan:
			case Op.IfLessEqual:
			case Op.IfLessThan:
			case Op.IfNotEqual:
			case Op.IfNotGreaterEqual:
			case Op.IfNotGreaterThan:
			case Op.IfNotLessEqual:
			case Op.IfNotLessThan:
			case Op.IfStrictEqual:
			case Op.IfStrictNotEqual:
			case Op.SetSlot:
			case Op.SetByte:
			case Op.SetShort:
			case Op.SetInt:
			case Op.SetFloat:
			case Op.SetDouble:
				stack -= 2;
				break;

			case Op.Add:
			case Op.AddDouble:
			case Op.AddInt:
			case Op.AsTypeLate:
			case Op.BitAnd:
			case Op.BitOr:
			case Op.BitXor:
			case Op.Divide:
			case Op.DefaultXmlNamespaceL:
			case Op.Equals:
			case Op.GreaterEquals:
			case Op.GreaterThan:
			case Op.HasNext:
			case Op.IfFalse:
			case Op.IfTrue:
			case Op.In:
			case Op.InstanceOf:
			case Op.IsTypeLate:
			case Op.LessEquals:
			case Op.LessThan:
			case Op.LookupSwitch:
			case Op.Modulo:
			case Op.Multiply:
			case Op.MultiplyInt:
			case Op.NextName:
			case Op.NextValue:
			case Op.Pop:
			case Op.PushScope:
			case Op.PushWith:
			case Op.ReturnValue:
			case Op.SetGlobalSlot:
			case Op.SetLocal:
			case Op.SetLocal0:
			case Op.SetLocal1:
			case Op.SetLocal2:
			case Op.SetLocal3:
			case Op.ShiftLeft:
			case Op.ShiftRight:
			case Op.ShiftRightUnsigned:
			case Op.StrictEquals:
			case Op.Subtract:
			case Op.SubtractInt:
			case Op.Throw:
				--stack;
				break;

			case Op.Dup:
			case Op.GetGlobalScope:
			case Op.GetGlobalSlot:
			case Op.GetLex:
			case Op.GetLocal:
			case Op.GetLocal0:
			case Op.GetLocal1:
			case Op.GetLocal2:
			case Op.GetLocal3:
			case Op.GetScopeObject:
			case Op.HasNext2:
			case Op.NewActivation:
			case Op.NewCatch:
			case Op.NewFunction:
			case Op.PushByte:
			case Op.PushDouble:
			case Op.PushFalse:
			case Op.PushInt:
			case Op.PushNamespace:
			case Op.PushNaN:
			case Op.PushNull:
			case Op.PushShort:
			case Op.PushString:
			case Op.PushTrue:
			case Op.PushUInt:
			case Op.PushUndefined:
				++stack;
				break;

			case Op.Call:
				final Call call = (Call)operation;
				stack -= call.numArguments + 1;
				break;

			// case Op.CallInterface
			case Op.CallMethod:
				final CallMethod callMethod = (CallMethod)operation;
				stack -= callMethod.numArguments;
				break;

			case Op.CallProperty:
				final CallProperty callProperty = (CallProperty)operation;
				stack -= callProperty.numArguments
						+ deltaOf( callProperty.property );
				break;

			case Op.CallPropLex:
				final CallPropLex callPropLex = (CallPropLex)operation;
				stack -= callPropLex.numArguments
						+ deltaOf( callPropLex.property );
				break;

			case Op.CallPropVoid:
				final CallPropVoid callPropVoid = (CallPropVoid)operation;
				stack -= callPropVoid.numArguments
						+ deltaOf( callPropVoid.property ) + 1;
				break;

			case Op.CallStatic:
				final CallStatic callStatic = (CallStatic)operation;
				stack -= callStatic.numArguments;
				break;

			case Op.CallSuper:
				final CallSuper callSuper = (CallSuper)operation;
				stack -= callSuper.numArguments + deltaOf( callSuper.name );
				break;

			// case Op.CallSuperId:
			case Op.CallSuperVoid:
				final CallSuperVoid callSuperVoid = (CallSuperVoid)operation;
				stack -= callSuperVoid.numArguments
						+ deltaOf( callSuperVoid.name ) + 1;
				break;

			case Op.Construct:
				stack -= ( (Construct)operation ).numArguments;
				break;

			case Op.ConstructProp:
				final ConstructProp constructProp = (ConstructProp)operation;
				stack -= constructProp.numArguments
						+ deltaOf( constructProp.property );
				break;

			case Op.ConstructSuper:
				stack -= ( (ConstructSuper)operation ).numArguments + 1;
				break;

			case Op.DeleteProperty:
				stack -= deltaOf( ( (DeleteProperty)operation ).property );
				break;

			case Op.FindProperty:
				stack -= deltaOf( ( (FindProperty)operation ).property );
				++stack;
				break;

			case Op.FindPropStrict:
				stack -= deltaOf( ( (FindPropStrict)operation ).property );
				++stack;
				break;

			case Op.GetDescendants:
				stack -= deltaOf( ( (GetDescendants)operation ).name );
				break;

			case Op.GetProperty:
				stack -= deltaOf( ( (GetProperty)operation ).property );
				break;

			case Op.GetSuper:
				stack -= deltaOf( ( (GetSuper)operation ).property );
				break;

			case Op.InitProperty:
				stack -= deltaOf( ( (InitProperty)operation ).property ) + 2;
				break;

			case Op.NewArray:
				stack -= ( (NewArray)operation ).numArguments - 1;
				break;

			case Op.NewObject:
				stack -= ( ( (NewObject)operation ).numProperties * 2 ) - 1;
				break;

			case Op.SetProperty:
				final SetProperty setProperty = (SetProperty)operation;
				stack -= deltaOf( setProperty.property ) + 2;
				break;

			case Op.SetSuper:
				final SetSuper setSuper = (SetSuper)operation;
				stack -= deltaOf( setSuper.property ) + 2;
				break;
		}

		return stack;
	}

	private int modifyScopeStack( int scopeDepth, final int code )
	{
		switch( code )
		{
			case Op.PushScope:
			case Op.PushWith:
				++scopeDepth;
				break;

			case Op.PopScope:
				--scopeDepth;
				break;
		}

		return scopeDepth;
	}

	private void walk(
			int stackDepth,
			int scopeDepth,
			final BasicBlock<BytecodeVertex> vertex,
			final Path<BasicBlock<BytecodeVertex>, Edge<BasicBlock<BytecodeVertex>>> path,
			final HashMap<BasicBlock<BytecodeVertex>, Integer> stackDepths,
			final HashMap<BasicBlock<BytecodeVertex>, Integer> scopeDepths,
			final LinkedList<Integer> stackResults,
			final LinkedList<Integer> scopeResults,
			final BasicBlockGraph<BytecodeVertex> graph )
			throws ControlFlowGraphException
	{
		if( path.contains( vertex ) )
		{
			//
			// Loops are added even if they do not reach the exit point.
			//

			stackResults.add( stackDepth );
			scopeResults.add( scopeDepth );

			return;
		}

		path.add( vertex );

		stackDepth += stackDepths.get( vertex );
		scopeDepth += scopeDepths.get( vertex );

		final List<Edge<BasicBlock<BytecodeVertex>>> outgoingEdges = graph
				.outgoingOf( vertex );

		if( 1 == outgoingEdges.size() )
		{
			//
			// Only one possible path available. Continue walking.
			//

			walk( stackDepth, scopeDepth, outgoingEdges.get( 0 ).endVertex,
					path, stackDepths, scopeDepths, stackResults, scopeResults,
					graph );
		}
		else if( !outgoingEdges.isEmpty() )
		{
			//
			// Create a new path for each possibility.
			//

			for( final Edge<BasicBlock<BytecodeVertex>> edge : outgoingEdges )
			{
				walk( stackDepth, scopeDepth, edge.endVertex, path,
						stackDepths, scopeDepths, stackResults, scopeResults,
						graph );
			}
		}
		else
		{
			stackResults.add( stackDepth );
			scopeResults.add( scopeDepth );
		}
	}
}
