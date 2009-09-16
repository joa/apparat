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

package com.joa_ebert.apparat.taas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.AbstractTrait;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.MultinameKind;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Marker;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodeAnalysis;
import com.joa_ebert.apparat.abc.bytecode.operations.Coerce;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceAny;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceObject;
import com.joa_ebert.apparat.abc.bytecode.operations.CoerceString;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLocal0;
import com.joa_ebert.apparat.abc.bytecode.operations.Jump;
import com.joa_ebert.apparat.abc.bytecode.operations.Label;
import com.joa_ebert.apparat.abc.bytecode.operations.LookupSwitch;
import com.joa_ebert.apparat.abc.bytecode.operations.Pop;
import com.joa_ebert.apparat.abc.bytecode.operations.PushByte;
import com.joa_ebert.apparat.abc.bytecode.operations.PushFalse;
import com.joa_ebert.apparat.abc.bytecode.operations.PushNaN;
import com.joa_ebert.apparat.abc.bytecode.operations.PushNull;
import com.joa_ebert.apparat.abc.bytecode.operations.PushScope;
import com.joa_ebert.apparat.abc.bytecode.operations.PushUInt;
import com.joa_ebert.apparat.abc.bytecode.operations.PushUndefined;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.taas.expr.AbstractCallExpr;
import com.joa_ebert.apparat.taas.expr.TGetProperty;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.expr.TJump;
import com.joa_ebert.apparat.taas.expr.TLookupSwitch;
import com.joa_ebert.apparat.taas.expr.TSetLocal;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.types.AnyType;
import com.joa_ebert.apparat.taas.types.BooleanType;
import com.joa_ebert.apparat.taas.types.IntType;
import com.joa_ebert.apparat.taas.types.MultinameType;
import com.joa_ebert.apparat.taas.types.NullType;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.ObjectType;
import com.joa_ebert.apparat.taas.types.StringType;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.UIntType;
import com.joa_ebert.apparat.taas.types.UndefinedType;
import com.joa_ebert.apparat.taas.types.UnknownType;
import com.joa_ebert.apparat.taas.types.VoidType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasEmitter
{
	private static final Taas TAAS = new Taas();

	private boolean containsJumpDestination(
			final LinkedHashMap<TaasValue, List<TaasValue>> jumps,
			final TaasValue destination )
	{
		final Iterator<List<TaasValue>> iterator = jumps.values().iterator();

		while( iterator.hasNext() )
		{
			if( iterator.next().contains( destination ) )
			{
				return true;
			}
		}

		return false;
	}

	private void continueWith( final TaasVertex vertex, final TaasCode code,
			final LinkedList<TaasValue> result,
			final LinkedHashMap<TaasValue, List<TaasValue>> jumps )
			throws ControlFlowGraphException
	{
		if( result.contains( vertex.value ) )
		{
			final TJump jump = TAAS.jump();

			markJump( jump, vertex.value, jumps );

			result.add( jump );
		}
		else
		{
			order( vertex, code, result, jumps );
		}
	}

	private MethodBody createBody()
	{
		final Bytecode bytecode = new Bytecode();
		final MethodBody methodBody = new MethodBody();

		methodBody.exceptions = new LinkedList<ExceptionHandler>();
		methodBody.code = bytecode;
		methodBody.traits = new LinkedList<AbstractTrait>();

		bytecode.methodBody = methodBody;

		return methodBody;
	}

	public MethodBody emit( final AbcEnvironment environment,
			final TaasMethod method )
	{
		//
		// Assumptions for now:
		//
		// 1) We assume that we are not in SSA form.
		// 2) We assume all TaasPhi vertices have been resolved for us.
		// 3) No jumps exist (throw error otherwise!)
		// 

		//
		// Strategy:
		// 1) Convert CFG into a linked list of instructions (done)
		// 2) Insert TaasJump for non-reachable edges (done)
		// 3) Convert linked list to bytecode while solving jumps
		//
		// Once done, work on phi-nodes. Exit SSA and expand phi-nodes for non-
		// register values into predecessors.
		//

		//
		// Phase #1:
		//
		// Order the CFG and produce a list of instructions.
		// We will also create a map of jumps in this step.
		//

		final LinkedList<TaasValue> list = new LinkedList<TaasValue>();
		final LinkedHashMap<TaasValue, List<TaasValue>> jumps = new LinkedHashMap<TaasValue, List<TaasValue>>();

		try
		{
			order( method.code.getEntryVertex(), method.code, list, jumps );
		}
		catch( final ControlFlowGraphException exception )
		{
			throw new TaasException( exception );
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS )
		{
			System.out.println( "TaasEmitter:" );

			final Iterator<TaasValue> iter = list.listIterator();

			while( iter.hasNext() )
			{
				System.out.println( iter.next() );
			}
		}

		//
		// Phase #2:
		//
		// Emit bytecode. Patch back- and forward jumps.
		//

		final MethodBody result = createBody();
		final Bytecode bytecode = result.code;

		result.code.add( new GetLocal0() );
		result.code.add( new PushScope() );

		typeLocals( list, jumps, method, result.code );

		final Iterator<TaasValue> iter = list.listIterator();
		final LinkedHashMap<TaasValue, AbstractOperation> jmpSrc = new LinkedHashMap<TaasValue, AbstractOperation>();
		final LinkedHashMap<TaasValue, AbstractOperation> jmpDst = new LinkedHashMap<TaasValue, AbstractOperation>();

		while( iter.hasNext() )
		{
			final TaasValue value = iter.next();
			final boolean isJumpDestination = containsJumpDestination( jumps,
					value );

			boolean needsLabel = false;
			AbstractOperation label = null;
			int beforeEmit = -1;

			if( isJumpDestination )
			{
				//
				// Target of a jump.
				//
				// Test if the source has already been visited. If not, we have
				// a backwards jump.
				//

				for( final Entry<TaasValue, List<TaasValue>> entry : jumps
						.entrySet() )
				{
					if( entry.getValue().contains( value ) )
					{
						if( !jmpSrc.containsKey( entry.getKey() ) )
						{
							needsLabel = true;
						}
					}
				}
			}

			//
			// Insert label if we have to.
			//
			//

			if( needsLabel )
			{
				label = new Label();
				bytecode.add( label );
			}

			if( isJumpDestination && !needsLabel )
			{
				beforeEmit = bytecode.size();
			}

			value.emit( environment, result, bytecode );

			//
			// If we have a control transfer we have to fix the labels.
			//

			if( jumps.containsKey( value ) )
			{
				jmpSrc.put( value, bytecode.peekLast() );
			}

			if( isJumpDestination )
			{
				jmpDst.put( value, needsLabel ? label : bytecode
						.get( beforeEmit ) );
			}

			//
			// If we have an expression with side-effects that is dead code
			// we may not remove it.
			//
			// Although we have to remove its result.
			//

			if( value.getType() != VoidType.INSTANCE
					&& ( value instanceof AbstractCallExpr || value instanceof TGetProperty ) )
			{
				final Iterator<TaasValue> refIter = list.descendingIterator();
				boolean keepValue = false;

				while( refIter.hasNext() )
				{
					final TaasValue refValue = refIter.next();

					if( refValue == value )
					{
						break;
					}
					else if( TaasToolkit.references( refValue, value ) )
					{
						keepValue = true;
						break;
					}
				}

				if( !keepValue )
				{
					bytecode.add( new Pop() );
				}
			}
		}

		fixMarkers( jumps, jmpSrc, jmpDst, bytecode );

		//
		// Phase #3:
		//
		// Finalize bytecode by filling the missing variables like max stack,
		// scope depth and local count.
		//

		putACherryOnTheCake( environment, bytecode );

		//
		// Et voilà ...
		//

		return result;
	}

	private void fixMarkers(
			final LinkedHashMap<TaasValue, List<TaasValue>> jumps,
			final LinkedHashMap<TaasValue, AbstractOperation> jmpSrc,
			final LinkedHashMap<TaasValue, AbstractOperation> jmpDst,
			final Bytecode bytecode )
	{
		final Set<Entry<TaasValue, List<TaasValue>>> entrySet = jumps
				.entrySet();

		for( final Entry<TaasValue, List<TaasValue>> jmp : entrySet )
		{
			final TaasValue tsrc = jmp.getKey();
			final List<TaasValue> tdst = jmp.getValue();
			final AbstractOperation bsrc = jmpSrc.get( tsrc );

			if( tdst.isEmpty() )
			{
				invalidCode( "Targets of " + tsrc.toString() + " do not exist." );
			}

			if( bsrc instanceof Jump )
			{
				if( tdst.size() != 1 )
				{
					invalidCode( "TJump contains multiple destinations." );
				}

				final AbstractOperation bdst = jmpDst.get( tdst.get( 0 ) );

				( (Jump)bsrc ).marker = bytecode.markers.mark( bdst );
			}
			else if( bsrc.code == Op.LookupSwitch )
			{
				final LookupSwitch lookupSwitch = (LookupSwitch)bsrc;

				lookupSwitch.defaultMarker = bytecode.markers.mark( jmpDst
						.get( tdst.get( 0 ) ) );

				if( null == lookupSwitch.caseMarkers )
				{
					lookupSwitch.caseMarkers = new ArrayList<Marker>( tdst
							.size() - 1 );
				}

				for( int i = 1, n = tdst.size(); i < n; ++i )
				{
					lookupSwitch.caseMarkers.add( bytecode.markers.mark( jmpDst
							.get( tdst.get( i ) ) ) );
				}
			}
			else
			{
				invalidCode( "Invalid jump source " + bsrc.toString() );
			}
		}
	}

	private void invalidCode()
	{
		invalidCode( "Invalid code." );
	}

	private void invalidCode( final String message )
	{
		throw new TaasException( message );
	}

	private void invalidCode( final TaasVertex vertex )
	{
		invalidCode( "Invalid code at " + vertex + "." );
	}

	private void markJump( final TaasValue from, final TaasValue to,
			final LinkedHashMap<TaasValue, List<TaasValue>> jumps )
	{
		if( jumps.containsKey( from ) )
		{
			jumps.get( from ).add( to );
		}
		else
		{
			final List<TaasValue> list = new LinkedList<TaasValue>();

			list.add( to );

			jumps.put( from, list );
		}
	}

	private void order( final TaasVertex vertex, final TaasCode code,
			final LinkedList<TaasValue> list,
			final LinkedHashMap<TaasValue, List<TaasValue>> jumps )
			throws ControlFlowGraphException
	{
		final VertexKind kind = vertex.kind;

		if( VertexKind.Entry == kind )
		{
			//
			// The entry has only one outgoing edge which will be the
			// first vertex to evaluate.
			//

			final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

			if( 1 != outgoingEdges.size() )
			{
				invalidCode();
			}
			else
			{
				order( outgoingEdges.get( 0 ).endVertex, code, list, jumps );
			}
		}
		else if( VertexKind.Exit == kind )
		{
			//
			// We have reached the exit, stop here.
			//

			return;
		}
		else if( VertexKind.Default == kind )
		{
			final TaasValue value = vertex.value;

			list.add( value );

			if( value instanceof TJump )
			{
				throw new TaasException( "Unexpected TJump in code." );
			}
			else if( value instanceof TIf )
			{
				TaasEdge trueEdge = null;
				TaasEdge falseEdge = null;

				final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

				for( final TaasEdge edge : outgoingEdges )
				{
					final EdgeKind edgeKind = edge.kind;

					if( EdgeKind.True == edgeKind )
					{
						if( null != trueEdge )
						{
							invalidCode( vertex );
						}

						trueEdge = edge;
					}
					else if( EdgeKind.False == edgeKind )
					{
						if( null != falseEdge )
						{
							invalidCode( vertex );
						}

						falseEdge = edge;
					}
					else
					{
						invalidCode( vertex );
					}
				}

				if( null == trueEdge || null == falseEdge )
				{
					code.debug( System.err );

					invalidCode( vertex );
				}
				else
				{
					//
					// We continue with the false-branch since it will
					// fall through.
					//
					continueWith( falseEdge.endVertex, code, list, jumps );

					//
					// The true-branch is a jump we have to mark.
					//

					final TaasValue trueValue = trueEdge.endVertex.value;

					markJump( value, trueValue, jumps );

					//
					// We might have visited the true-branch already so we
					// will only continue if it has not been visited before.
					//

					if( !list.contains( trueValue ) )
					{
						continueWith( trueEdge.endVertex, code, list, jumps );
					}
				}
			}
			else if( value instanceof TLookupSwitch )
			{
				final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

				//
				// All outgoing edges are branches, we never fall through
				//

				for( final TaasEdge edge : outgoingEdges )
				{
					final EdgeKind edgeKind = edge.kind;

					if( EdgeKind.Case == edgeKind
							|| EdgeKind.DefaultCase == edgeKind )
					{
						final TaasValue caseValue = edge.endVertex.value;

						markJump( value, caseValue, jumps );

						//
						// We might have visited this branch already so we
						// will only continue if it has not been visited before.
						//

						if( !list.contains( caseValue ) )
						{
							continueWith( edge.endVertex, code, list, jumps );
						}
					}
					else
					{
						invalidCode( vertex );
					}
				}
			}
			else
			{
				TaasEdge defaultEdge = null;

				final List<TaasEdge> outgoingEdges = code.outgoingOf( vertex );

				for( final TaasEdge edge : outgoingEdges )
				{
					final EdgeKind edgeKind = edge.kind;

					if( EdgeKind.Default == edgeKind )
					{
						if( null != defaultEdge )
						{
							invalidCode();
						}

						defaultEdge = edge;
					}
				}

				if( null == defaultEdge )
				{
					TaasToolkit.debug( "Error", code );
					invalidCode( vertex );
				}

				continueWith( defaultEdge.endVertex, code, list, jumps );
			}
		}
	}

	private void putACherryOnTheCake( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		final BytecodeAnalysis analysis = new BytecodeAnalysis( environment,
				bytecode );

		analysis.updateAll();
	}

	private void typeLocals( final LinkedList<TaasValue> list,
			final LinkedHashMap<TaasValue, List<TaasValue>> jumps,
			final TaasMethod method, final Bytecode code )
	{
		final Iterator<TaasValue> iter = list.listIterator();

		final Map<TaasValue, List<TaasLocal>> typedLocals = new LinkedHashMap<TaasValue, List<TaasLocal>>();
		final LinkedList<TaasLocal> locals = new LinkedList<TaasLocal>();
		final LinkedList<TaasLocal> toType = new LinkedList<TaasLocal>();

		while( iter.hasNext() )
		{
			final TaasValue value = iter.next();

			if( containsJumpDestination( jumps, value ) )
			{
				//
				// Jump target
				//

				int targetIndex = -1;
				int sourceIndex = -1;

				for( final Entry<TaasValue, List<TaasValue>> entry : jumps
						.entrySet() )
				{
					if( entry.getValue().contains( value ) )
					{
						targetIndex = list.indexOf( value );
						sourceIndex = list.indexOf( entry.getKey() );

						break;
					}
				}

				if( -1 == targetIndex || -1 == sourceIndex )
				{
					invalidCode();
				}

				if( targetIndex < sourceIndex )
				{
					//
					// Backwards jump
					//

					typedLocals
							.put( value, new LinkedList<TaasLocal>( locals ) );
				}
			}

			if( value instanceof TSetLocal )
			{
				locals.add( ( (TSetLocal)value ).local );
			}

			if( jumps.containsKey( value ) )
			{
				//
				// Jump source
				//

				final List<TaasValue> targets = jumps.get( value );

				for( final TaasValue target : targets )
				{
					if( typedLocals.containsKey( target ) )
					{
						final List<TaasLocal> clone = new LinkedList<TaasLocal>(
								locals );

						clone.removeAll( typedLocals.get( target ) );

						toType.addAll( clone );
					}
				}
			}
		}

		final int numParameters = method.parameters.size();

		for( final TaasLocal local : method.locals.getRegisterList() )
		{
			final int index = local.getIndex();

			if( index <= numParameters )
			{
				continue;
			}

			if( !toType.contains( local ) )
			{
				continue;
			}

			final TaasType type = local.getType();

			if( type == IntType.INSTANCE )
			{
				code.add( new PushByte( 0 ) );
			}
			else if( type == UIntType.INSTANCE )
			{
				code.add( new PushUInt( 0L ) );
			}
			else if( type == NumberType.INSTANCE )
			{
				code.add( new PushNaN() );
			}
			else if( type instanceof MultinameType )
			{
				final AbstractMultiname name = ( (MultinameType)type ).multiname;
				final MultinameKind kind = name.kind;

				code.add( new PushNull() );

				if( kind != MultinameKind.QName && kind != MultinameKind.QNameA )
				{
					throw new TaasException(
							"Local is coerced to runtime type." );
				}
				else
				{
					code.add( new Coerce( ( (MultinameType)type ).multiname ) );
				}
			}
			else if( type == StringType.INSTANCE )
			{
				code.add( new PushNull() );
				code.add( new CoerceString() );
			}
			else if( type == ObjectType.INSTANCE )
			{
				code.add( new PushNull() );
				code.add( new CoerceObject() );
			}
			else if( type == AnyType.INSTANCE || type == UnknownType.INSTANCE )
			{
				code.add( new CoerceAny() );
			}
			else if( type == BooleanType.INSTANCE )
			{
				code.add( new PushFalse() );
			}
			else if( type == NullType.INSTANCE )
			{
				code.add( new PushNull() );
			}
			else if( type == UndefinedType.INSTANCE )
			{
				code.add( new PushUndefined() );
			}
			else
			{
				code.add( new PushNull() );
				code.add( new Coerce( TaasTyper.toAbcType( type ) ) );

				throw new TaasException( "Unhandled type " + type );
			}

			code.add( new SetLocal( index ) );
		}
	}
}
