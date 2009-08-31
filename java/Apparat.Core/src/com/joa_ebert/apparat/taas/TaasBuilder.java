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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.Parameter;
import com.joa_ebert.apparat.abc.Script;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Marker;
import com.joa_ebert.apparat.abc.bytecode.MarkerManager;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodeVertex;
import com.joa_ebert.apparat.abc.bytecode.analysis.ControlFlowGraphBuilder;
import com.joa_ebert.apparat.abc.bytecode.analysis.IInterpreter;
import com.joa_ebert.apparat.abc.bytecode.operations.*;
import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.constants.TaasBoolean;
import com.joa_ebert.apparat.taas.constants.TaasGlobalScope;
import com.joa_ebert.apparat.taas.constants.TaasInstance;
import com.joa_ebert.apparat.taas.constants.TaasInt;
import com.joa_ebert.apparat.taas.constants.TaasMultiname;
import com.joa_ebert.apparat.taas.constants.TaasNamespace;
import com.joa_ebert.apparat.taas.constants.TaasNull;
import com.joa_ebert.apparat.taas.constants.TaasNumber;
import com.joa_ebert.apparat.taas.constants.TaasString;
import com.joa_ebert.apparat.taas.constants.TaasUInt;
import com.joa_ebert.apparat.taas.constants.TaasUndefined;
import com.joa_ebert.apparat.taas.expr.AbstractCallExpr;
import com.joa_ebert.apparat.taas.expr.AbstractLocalExpr;
import com.joa_ebert.apparat.taas.expr.AbstractReturnExpr;
import com.joa_ebert.apparat.taas.expr.TApplyType;
import com.joa_ebert.apparat.taas.expr.TDefaultXmlNamespace;
import com.joa_ebert.apparat.taas.expr.TDeleteProperty;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.expr.TInitProperty;
import com.joa_ebert.apparat.taas.expr.TLookupSwitch;
import com.joa_ebert.apparat.taas.expr.TSetProperty;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.toolkit.constantFolding.ConstantFolding;
import com.joa_ebert.apparat.taas.toolkit.copyPropagation.CopyPropagation;
import com.joa_ebert.apparat.taas.toolkit.deadCodeElimination.DeadCodeElimination;
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

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasBuilder implements IInterpreter
{
	private static final Taas TAAS = new Taas();

	private TaasCode code;
	private TaasTyper typer;

	private TaasStack operandStack;
	private TaasStack scopeStack;
	private TaasRegisters localRegisters;

	private ControlFlowGraph<BytecodeVertex, Edge<BytecodeVertex>> bytecodeGraph;
	private MarkerManager markers;

	private final Map<BytecodeVertex, TaasStack> operandStackAtMerge = new LinkedHashMap<BytecodeVertex, TaasStack>();
	private final Map<BytecodeVertex, TaasStack> scopeStackAtMerge = new LinkedHashMap<BytecodeVertex, TaasStack>();
	private final List<BytecodeVertex> visitedVertices = new LinkedList<BytecodeVertex>();
	private final Map<Marker, TaasVertex> visitedMarkers = new LinkedHashMap<Marker, TaasVertex>();
	private final Map<BytecodeVertex, TaasVertex> bytecodeToVertex = new LinkedHashMap<BytecodeVertex, TaasVertex>();

	public TaasMethod build( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		//
		// Initialize typer, graphs, etc.
		//

		initialize( environment, bytecode );

		//
		// Interpret given method.
		//

		interpret( environment, bytecode );

		//
		// Build parameter list.
		//

		final ArrayList<TaasParameter> parameters = new ArrayList<TaasParameter>(
				bytecode.methodBody.method.parameters.size() );

		for( final Parameter oldParameter : bytecode.methodBody.method.parameters )
		{
			final TaasParameter newParameter = new TaasParameter();

			newParameter.name = oldParameter.name;
			newParameter.type = typer.typeOf( oldParameter.type );

			parameters.add( newParameter );

			if( oldParameter.isOptional )
			{
				TODO();
			}
		}

		//
		// Create result.
		// 

		final TaasMethod result = new TaasMethod( typer, localRegisters, code,
				parameters );

		//
		// Perform these steps as outlined in
		// "Jimple: Simplifying Java Bytecode for Analyses and Transformation"
		// by Raja Vallee-Rai and Laurie J. Hendren
		//

		final CopyPropagation copyPropagation = new CopyPropagation();
		final ConstantFolding constantFolding = new ConstantFolding();
		final DeadCodeElimination deadCodeElimination = new DeadCodeElimination();
		// final SsaBuilder ssaBuilder = new SsaBuilder();

		boolean changed;

		do
		{
			changed = false;

			changed = copyPropagation.manipulate( environment, result )
					|| changed;

			changed = constantFolding.manipulate( environment, result )
					|| changed;

			changed = deadCodeElimination.manipulate( environment, result )
					|| changed;
		}
		while( changed );

		// ssaBuilder.manipulate( result );

		//
		// Cleanup dead vertices.
		//

		cleanup( result );

		//
		// Clear all maps and references.
		//

		reset();

		return result;
	}

	private void build( final BytecodeVertex vertex, final EdgeKind kind,
			final int operandSize, final int scopeSize ) throws TaasException
	{
		//
		// The code does some edge handling and we do some edge handling here.
		// Therefore we have to share the same kind for the next edge.
		//

		code.setNextKind( kind );

		//
		// We may have evaluated a vertex already because of loops.
		//

		if( isVisited( vertex ) )
		{
			//
			// Yes, stop here.
			//

			code.connectIfNeccessary( bytecodeToVertex.get( vertex ) );

			contributeStack( vertex, operandSize, scopeSize, code.getLastEdge() );

			return;
		}

		if( vertex.kind == VertexKind.Entry )
		{
			try
			{
				code.setEntryPoint( TaasVertex.ENTRY_POINT );
				code.setLastInserted( TaasVertex.ENTRY_POINT );

				markVisited( vertex );
			}
			catch( final ControlFlowGraphException e )
			{
				throw new TaasException( e );
			}

			build( nextOf( vertex ), operandSize, scopeSize );
		}
		else if( vertex.kind == VertexKind.Exit )
		{
			try
			{
				code.setExitPoint( TaasVertex.EXIT_POINT );
				code.add( new TaasEdge( code.getLastInserted(), code
						.getExitVertex(), code.getNextKind() ) );
				code.setLastInserted( code.getExitVertex() );

				markVisited( vertex );
			}
			catch( final ControlFlowGraphException e )
			{
				throw new TaasException( e );
			}

			//
			// We do not continue building here of course.
			//
		}
		else if( vertex.kind == VertexKind.Default )
		{
			BytecodeVertex currentVertex = vertex;

			do
			{
				if( isVisited( currentVertex ) )
				{
					//
					// Already visited, this means here occurred a CF merge.
					//

					contributeStack( currentVertex, operandSize, scopeSize,
							code.getLastEdge() );

					//
					// Stop later once we created the edge using the marker
					// lookup.
					//
				}
				else
				{
					markVisited( currentVertex );
				}

				final AbstractOperation op = currentVertex.getOperation();
				final int opcode = op.code;

				if( markers.hasMarkerFor( op ) )
				{
					//
					// This operation is marked, we may have already evaluated
					// this path.
					//

					final Marker marker = markers.getMarkerFor( op );

					if( visitedMarkers.containsKey( marker ) )
					{
						//
						// This path has already been evaluated. Just add an
						// edge from the last position to the current to
						// keep the flow correct.
						//

						try
						{
							code.add( new TaasEdge( code.getLastInserted(),
									visitedMarkers.get( marker ), code
											.getNextKind() ) );
						}
						catch( final ControlFlowGraphException e )
						{
							throw new TaasException( e );
						}

						//
						// Now stop.
						//

						return;
					}
				}

				transformStack( currentVertex, operandSize, scopeSize, code
						.getLastEdge() );

				switch( opcode )
				{
					case Op.IfTrue:
					case Op.IfFalse:
					case Op.IfEqual:
					case Op.IfNotEqual:
					case Op.IfLessThan:
					case Op.IfLessEqual:
					case Op.IfGreaterThan:
					case Op.IfGreaterEqual:
					case Op.IfStrictEqual:
					case Op.IfStrictNotEqual:
					case Op.IfNotLessThan:
					case Op.IfNotLessEqual:
					case Op.IfNotGreaterThan:
					case Op.IfNotGreaterEqual:
					{
						onIf( (AbstractConditionalJump)op );

						final TaasVertex lastInserted = code.getLastInserted();
						final TaasEdge lastEdge = code.getLastEdge();

						List<Edge<BytecodeVertex>> edges = null;

						try
						{
							edges = bytecodeGraph.outgoingOf( currentVertex );
						}
						catch( final ControlFlowGraphException e )
						{
							throw new TaasException( e );
						}

						for( final Edge<BytecodeVertex> edge : edges )
						{
							if( edge.kind == EdgeKind.True
									|| edge.kind == EdgeKind.False )
							{
								final TaasFrame backup = new TaasFrame(
										operandStack, scopeStack,
										localRegisters );

								code.setLastInserted( lastInserted );
								code.setLastEdge( lastEdge );

								build( edge.endVertex, edge.kind, operandStack
										.size(), scopeStack.size() );

								operandStack = backup.operandStack;
								scopeStack = backup.scopeStack;
								localRegisters = backup.localRegisters;
							}
						}

						return;
					}
						// /////////////////////////////////////////////////////
					case Op.LookupSwitch:
					{
						onLookupSwitch( (LookupSwitch)op );

						final TaasVertex lastInserted2 = code.getLastInserted();
						final TaasEdge lastEdge2 = code.getLastEdge();

						List<Edge<BytecodeVertex>> edges2 = null;

						try
						{
							edges2 = bytecodeGraph.outgoingOf( currentVertex );
						}
						catch( final ControlFlowGraphException e )
						{
							throw new TaasException( e );
						}

						for( final Edge<BytecodeVertex> edge : edges2 )
						{
							if( edge.kind == EdgeKind.DefaultCase
									|| edge.kind == EdgeKind.Case )
							{
								final TaasFrame backup = new TaasFrame(
										operandStack, scopeStack,
										localRegisters );

								code.setLastInserted( lastInserted2 );
								code.setLastEdge( lastEdge2 );

								build( edge.endVertex, edge.kind, operandStack
										.size(), scopeStack.size() );

								operandStack = backup.operandStack;
								scopeStack = backup.scopeStack;
								localRegisters = backup.localRegisters;
							}
						}

						return;
					}
						// /////////////////////////////////////////////////////
					case Op.Jump:
						onJump( (Jump)op );
						code.setNextKind( EdgeKind.Jump );
						break;
					case Op.Breakpoint:
						onBreakpoint( (Breakpoint)op );
						break;
					case Op.Nop:
						onNop( (Nop)op );
						break;
					case Op.Throw:
						onThrow( (Throw)op );
						break;
					case Op.GetSuper:
						onGetSuper( (GetSuper)op );
						break;
					case Op.SetSuper:
						onSetSuper( (SetSuper)op );
						break;
					case Op.DefaultXmlNamespace:
						onDefaultXmlNamespace( (DefaultXmlNamespace)op );
						break;
					case Op.DefaultXmlNamespaceL:
						onDefaultXmlNamespaceL( (DefaultXmlNamespaceL)op );
						break;
					case Op.Kill:
						onKill( (Kill)op );
						break;
					case Op.Label:
						onLabel( (Label)op );
						break;
					case Op.PushWith:
						onPushWith( (PushWith)op );
						break;
					case Op.PopScope:
						onPopScope( (PopScope)op );
						break;
					case Op.NextName:
						onNextName( (NextName)op );
						break;
					case Op.HasNext:
						onHasNext( (HasNext)op );
						break;
					case Op.PushNull:
						onPushNull( (PushNull)op );
						break;
					case Op.PushUndefined:
						onPushUndefined( (PushUndefined)op );
						break;
					case Op.NextValue:
						onNextValue( (NextValue)op );
						break;
					case Op.PushByte:
						onPushByte( (PushByte)op );
						break;
					case Op.PushShort:
						onPushShort( (PushShort)op );
						break;
					case Op.PushTrue:
						onPushTrue( (PushTrue)op );
						break;
					case Op.PushFalse:
						onPushFalse( (PushFalse)op );
						break;
					case Op.PushNaN:
						onPushNaN( (PushNaN)op );
						break;
					case Op.Pop:
						onPop( (Pop)op );
						break;
					case Op.Dup:
						onDup( (Dup)op );
						break;
					case Op.Swap:
						onSwap( (Swap)op );
						break;
					case Op.PushString:
						onPushString( (PushString)op );
						break;
					case Op.PushInt:
						onPushInt( (PushInt)op );
						break;
					case Op.PushUInt:
						onPushUInt( (PushUInt)op );
						break;
					case Op.PushDouble:
						onPushDouble( (PushDouble)op );
						break;
					case Op.PushScope:
						onPushScope( (PushScope)op );
						break;
					case Op.PushNamespace:
						onPushNamespace( (PushNamespace)op );
						break;
					case Op.HasNext2:
						onHasNext2( (HasNext2)op );
						break;
					case Op.NewFunction:
						onNewFunction( (NewFunction)op );
						break;
					case Op.Call:
						onCall( (Call)op );
						break;
					case Op.Construct:
						onConstruct( (Construct)op );
						break;
					case Op.CallMethod:
						onCallMethod( (CallMethod)op );
						break;
					case Op.CallStatic:
						onCallStatic( (CallStatic)op );
						break;
					case Op.CallSuper:
						onCallSuper( (CallSuper)op );
						break;
					case Op.CallProperty:
						onCallProperty( (CallProperty)op );
						break;
					case Op.ReturnVoid:
						onReturnVoid( (ReturnVoid)op );
						break;
					case Op.ReturnValue:
						onReturnValue( (ReturnValue)op );
						break;
					case Op.ConstructSuper:
						onConstructSuper( (ConstructSuper)op );
						break;
					case Op.ConstructProp:
						onConstructProp( (ConstructProp)op );
						break;
					// case Op.CallSuperId:
					// onCallSuperId( (CallSuperId)op ); break;
					case Op.CallPropLex:
						onCallPropLex( (CallPropLex)op );
						break;
					// case Op.CallInterface:
					// onCallInterface( (CallInterface)op ); break;
					case Op.CallSuperVoid:
						onCallSuperVoid( (CallSuperVoid)op );
						break;
					case Op.CallPropVoid:
						onCallPropVoid( (CallPropVoid)op );
						break;
					case Op.ApplyType:
						onApplyType( (ApplyType)op );
						break;
					case Op.NewObject:
						onNewObject( (NewObject)op );
						break;
					case Op.NewArray:
						onNewArray( (NewArray)op );
						break;
					case Op.NewActivation:
						onNewActivation( (NewActivation)op );
						break;
					case Op.NewClass:
						onNewClass( (NewClass)op );
						break;
					case Op.GetDescendants:
						onGetDescendants( (GetDescendants)op );
						break;
					case Op.NewCatch:
						onNewCatch( (NewCatch)op );
						break;
					case Op.FindPropStrict:
						onFindPropStrict( (FindPropStrict)op );
						break;
					case Op.FindProperty:
						onFindProperty( (FindProperty)op );
						break;
					// case Op.FindDef:
					// onFindDef( (FindDef)op ); break;
					case Op.GetLex:
						onGetLex( (GetLex)op );
						break;
					case Op.SetProperty:
						onSetProperty( (SetProperty)op );
						break;
					case Op.GetLocal:
						onGetLocal( (GetLocal)op );
						break;
					case Op.SetLocal:
						onSetLocal( (SetLocal)op );
						break;
					case Op.GetGlobalScope:
						onGetGlobalScope( (GetGlobalScope)op );
						break;
					case Op.GetScopeObject:
						onGetScopeObject( (GetScopeObject)op );
						break;
					case Op.GetProperty:
						onGetProperty( (GetProperty)op );
						break;
					case Op.GetPropertyLate:
						onGetPropertyLate( (GetPropertyLate)op );
						break;
					case Op.InitProperty:
						onInitProperty( (InitProperty)op );
						break;
					case Op.SetPropertyLate:
						onSetPropertyLate( (SetPropertyLate)op );
						break;
					case Op.DeleteProperty:
						onDeleteProperty( (DeleteProperty)op );
						break;
					case Op.DeletePropertyLate:
						onDeletePropertyLate( (DeletePropertyLate)op );
						break;
					case Op.GetSlot:
						onGetSlot( (GetSlot)op );
						break;
					case Op.SetSlot:
						onSetSlot( (SetSlot)op );
						break;
					case Op.GetGlobalSlot:
						onGetGlobalSlot( (GetGlobalSlot)op );
						break;
					case Op.SetGlobalSlot:
						onSetGlobalSlot( (SetGlobalSlot)op );
						break;
					case Op.ConvertString:
						onConvertString( (ConvertString)op );
						break;
					case Op.EscXmlElem:
						onEscXmlElem( (EscXmlElem)op );
						break;
					case Op.EscXmlAttr:
						onEscXmlAttr( (EscXmlAttr)op );
						break;
					case Op.ConvertInt:
						onConvertInt( (ConvertInt)op );
						break;
					case Op.ConvertUInt:
						onConvertUInt( (ConvertUInt)op );
						break;
					case Op.ConvertDouble:
						onConvertDouble( (ConvertDouble)op );
						break;
					case Op.ConvertBoolean:
						onConvertBoolean( (ConvertBoolean)op );
						break;
					case Op.ConvertObject:
						onConvertObject( (ConvertObject)op );
						break;
					case Op.CheckFilter:
						onCheckFilter( (CheckFilter)op );
						break;
					case Op.Coerce:
						onCoerce( (Coerce)op );
						break;
					case Op.CoerceBoolean:
						onCoerceBoolean( (CoerceBoolean)op );
						break;
					case Op.CoerceAny:
						onCoerceAny( (CoerceAny)op );
						break;
					case Op.CoerceInt:
						onCoerceInt( (CoerceInt)op );
						break;
					case Op.CoerceDouble:
						onCoerceDouble( (CoerceDouble)op );
						break;
					case Op.CoerceString:
						onCoerceString( (CoerceString)op );
						break;
					case Op.AsType:
						onAsType( (AsType)op );
						break;
					case Op.AsTypeLate:
						onAsTypeLate( (AsTypeLate)op );
						break;
					case Op.CoerceUInt:
						onCoerceUInt( (CoerceUInt)op );
						break;
					case Op.CoerceObject:
						onCoerceObject( (CoerceObject)op );
						break;
					case Op.Negate:
						onNegate( (Negate)op );
						break;
					case Op.Increment:
						onIncrement( (Increment)op );
						break;
					case Op.IncLocal:
						onIncLocal( (IncLocal)op );
						break;
					case Op.Decrement:
						onDecrement( (Decrement)op );
						break;
					case Op.DecLocal:
						onDecLocal( (DecLocal)op );
						break;
					case Op.TypeOf:
						onTypeOf( (TypeOf)op );
						break;
					case Op.Not:
						onNot( (Not)op );
						break;
					case Op.BitNot:
						onBitNot( (BitNot)op );
						break;
					case Op.Concat:
						onConcat( (Concat)op );
						break;
					case Op.AddDouble:
						onAddDouble( (AddDouble)op );
						break;
					case Op.Add:
						onAdd( (Add)op );
						break;
					case Op.Subtract:
						onSubtract( (Subtract)op );
						break;
					case Op.Multiply:
						onMultiply( (Multiply)op );
						break;
					case Op.Divide:
						onDivide( (Divide)op );
						break;
					case Op.Modulo:
						onModulo( (Modulo)op );
						break;
					case Op.ShiftLeft:
						onShiftLeft( (ShiftLeft)op );
						break;
					case Op.ShiftRight:
						onShiftRight( (ShiftRight)op );
						break;
					case Op.ShiftRightUnsigned:
						onShiftRightUnsigned( (ShiftRightUnsigned)op );
						break;
					case Op.BitAnd:
						onBitAnd( (BitAnd)op );
						break;
					case Op.BitOr:
						onBitOr( (BitOr)op );
						break;
					case Op.BitXor:
						onBitXor( (BitXor)op );
						break;
					case Op.Equals:
						onEquals( (Equals)op );
						break;
					case Op.StrictEquals:
						onStrictEquals( (StrictEquals)op );
						break;
					case Op.LessThan:
						onLessThan( (LessThan)op );
						break;
					case Op.LessEquals:
						onLessEquals( (LessEquals)op );
						break;
					case Op.GreaterThan:
						onGreaterThan( (GreaterThan)op );
						break;
					case Op.GreaterEquals:
						onGreaterEquals( (GreaterEquals)op );
						break;
					case Op.InstanceOf:
						onInstanceOf( (InstanceOf)op );
						break;
					case Op.IsType:
						onIsType( (IsType)op );
						break;
					case Op.IsTypeLate:
						onIsTypeLate( (IsTypeLate)op );
						break;
					case Op.In:
						onIn( (In)op );
						break;
					case Op.IncrementInt:
						onIncrementInt( (IncrementInt)op );
						break;
					case Op.DecrementInt:
						onDecrementInt( (DecrementInt)op );
						break;
					case Op.IncLocalInt:
						onIncLocalInt( (IncLocalInt)op );
						break;
					case Op.DecLocalInt:
						onDecLocalInt( (DecLocalInt)op );
						break;
					case Op.NegateInt:
						onNegateInt( (NegateInt)op );
						break;
					case Op.AddInt:
						onAddInt( (AddInt)op );
						break;
					case Op.SubtractInt:
						onSubtractInt( (SubtractInt)op );
						break;
					case Op.MultiplyInt:
						onMultiplyInt( (MultiplyInt)op );
						break;
					case Op.GetLocal0:
						onGetLocal0( (GetLocal0)op );
						break;
					case Op.GetLocal1:
						onGetLocal1( (GetLocal1)op );
						break;
					case Op.GetLocal2:
						onGetLocal2( (GetLocal2)op );
						break;
					case Op.GetLocal3:
						onGetLocal3( (GetLocal3)op );
						break;
					case Op.SetLocal0:
						onSetLocal0( (SetLocal0)op );
						break;
					case Op.SetLocal1:
						onSetLocal1( (SetLocal1)op );
						break;
					case Op.SetLocal2:
						onSetLocal2( (SetLocal2)op );
						break;
					case Op.SetLocal3:
						onSetLocal3( (SetLocal3)op );
						break;
					case Op.Debug:
						onDebug( (Debug)op );
						break;
					case Op.DebugLine:
						onDebugLine( (DebugLine)op );
						break;
					case Op.DebugFile:
						onDebugFile( (DebugFile)op );
						break;
					case Op.BreakpointLine:
						onBreakpointLine( (BreakpointLine)op );
						break;
					// case Op.SetByte:
					// onSetByte( (SetByte)op );
					// break;
					// case Op.SetShort:
					// onSetShort( (SetShort)op );
					// break;
					// case Op.SetInt:
					// onSetInt( (SetInt)op );
					// break;
					// case Op.SetFloat:
					// onSetFloat( (SetFloat)op );
					// break;
					// case Op.SetDouble:
					// onSetDouble( (SetDouble)op );
					// break;
					// case Op.GetByte:
					// onGetByte( (GetByte)op );
					// break;
					// case Op.GetShort:
					// onGetShort( (GetShort)op );
					// break;
					// case Op.GetInt:
					// onGetInt( (GetInt)op );
					// break;
					// case Op.GetFloat:
					// onGetFloat( (GetFloat)op );
					// break;
					// case Op.GetDouble:
					// onGetDouble( (GetDouble)op );
					// break;
					// case Op.Sign1:
					// onSign1( (Sign1)op );
					// break;
					// case Op.Sign8:
					// onSign8( (Sign8)op );
					// break;
					// case Op.Sign16:
					// onSign16( (Sign16)op );
					// break;
				}

				//
				// If an operation is marked it is possible that it might be
				// visited more often. Therefore we will put the operation
				// in the visitedMarkers map if it has a marker.
				//

				if( markers.hasMarkerFor( op ) )
				{
					visitedMarkers.put( markers.getMarkerFor( op ), code
							.getLastInserted() );
				}

				bytecodeToVertex.put( currentVertex, code.getLastInserted() );

				currentVertex = nextOf( currentVertex );
			}
			while( null != currentVertex
					&& currentVertex.kind == VertexKind.Default );

			if( null != currentVertex )
			{
				switch( currentVertex.kind )
				{
					case Default:
					case Entry:
						throw new TaasException( "Not reachable by definition." );

					case Exit:
						build( currentVertex, operandSize, scopeSize );
						break;
				}
			}
		}
		else
		{
			throw new TaasException( "Not reachable by definition." );
		}
	}

	private void build( final BytecodeVertex vertex, final int operandSize,
			final int scopeSize ) throws TaasException
	{
		build( vertex, EdgeKind.Default, operandSize, scopeSize );
	}

	/**
	 * @param method
	 */
	private void cleanup( final TaasMethod method )
	{
		final TaasCode code = method.code;
		final List<TaasVertex> vertices = code.vertexList();
		final List<TaasVertex> removes = new LinkedList<TaasVertex>();

		for( final TaasVertex vertex : vertices )
		{
			if( VertexKind.Default != vertex.kind )
			{
				continue;
			}

			boolean remove = true;

			final TaasValue value = vertex.value;

			if( value instanceof AbstractCallExpr )
			{
				remove = value instanceof TApplyType;
			}
			else if( value instanceof AbstractLocalExpr )
			{
				remove = false;
			}
			else if( value instanceof AbstractReturnExpr )
			{
				remove = false;
			}
			else if( value instanceof TDefaultXmlNamespace )
			{
				remove = false;
			}
			else if( value instanceof TDeleteProperty )
			{
				remove = false;
			}
			// else if( value instanceof TEnterScope )
			// {
			// remove = true;
			// }
			// else if( value instanceof TLeaveScope )
			// {
			// remove = true;
			// }
			// else if( value instanceof TNewClass )
			// {
			// remove = true;
			// }
			// else if( value instanceof TGetProperty )
			// {
			// // disscuss this ...
			// // remove = false;
			// }
			else if( value instanceof TInitProperty )
			{
				remove = false;
			}
			else if( value instanceof TSetProperty )
			{
				remove = false;
			}
			else if( value instanceof TIf )
			{
				remove = false;
			}
			else if( value instanceof TLookupSwitch )
			{
				remove = false;
			}

			if( remove )
			{
				removes.add( vertex );
			}
		}

		try
		{
			for( final TaasVertex vertex : removes )
			{
				TaasToolkit.remove( method, vertex );
			}
		}
		catch( final ControlFlowGraphException exception )
		{
			throw new TaasException( exception );
		}
	}

	private TaasMultiname constant( final AbstractMultiname value )
	{
		TaasNamespace namespace;
		TaasValue name;

		switch( value.kind )
		{
			case RTQName:
				namespace = (TaasNamespace)operandStack.pop();
				return new TaasMultiname( value, namespace, null );
			case RTQNameL:
				namespace = (TaasNamespace)operandStack.pop();
				name = operandStack.pop();
				return new TaasMultiname( value, namespace, name );
			case MultinameL:
				name = operandStack.pop();
				return new TaasMultiname( value, null, name );
			default:
				return new TaasMultiname( value );
		}
	}

	private TaasNumber constant( final Double value )
	{
		return new TaasNumber( value );
	}

	private TaasInt constant( final Integer value )
	{
		return new TaasInt( value );
	}

	private TaasUInt constant( final Long value )
	{
		return new TaasUInt( value );
	}

	private TaasNamespace constant( final Namespace value )
	{
		return new TaasNamespace( value );
	}

	private TaasString constant( final String value )
	{
		return new TaasString( value );
	}

	/**
	 * Contributes the stack delta to the stack at the control flow merge.
	 * 
	 * @param currentVertex
	 *            The merge vertex.
	 * @param operandSize
	 *            The initial operand stack size.
	 * @param scopeSize
	 *            The initial scope stack size.
	 */
	private void contributeStack( final BytecodeVertex currentVertex,
			final int operandSize, final int scopeSize,
			final TaasEdge previousEdge )
	{
		int delta = operandStack.size() - operandSize;

		if( delta > 0 )
		{
			if( !operandStackAtMerge.containsKey( currentVertex ) )
			{
				//
				// If we have visited this vertex before but did not fix the
				// operand stack then we did something completely wrong.
				//

				throw new TaasException( "Internal error." );
			}

			final int i = operandStack.size();
			final TaasStack mergeStack = operandStackAtMerge
					.get( currentVertex );

			do
			{
				final TaasValue stackValue = mergeStack.get( i - delta );

				if( stackValue instanceof TaasPhi )
				{
					( (TaasPhi)stackValue ).add( operandStack.get( i - delta ),
							previousEdge );
				}
				else
				{
					//
					// Stack depth is not even. This should have resulted in a
					// verifier error.
					//

					throw new TaasException( "Stack depth is not even." );
				}
			}
			while( --delta > 0 );
		}
		else if( delta == 0 )
		{
			return;
		}
		else
		{
			return;
			// throw new TaasException( "Invalid code at "
			// + currentVertex.toString() + " with previous edge "
			// + previousEdge.toString() + "." );
		}
	}

	public TaasCode getCode()
	{
		return code;
	}

	protected TIf.Operator ifOperatorOf( final int code )
	{
		switch( code )
		{
			case Op.IfEqual:
				return TIf.Operator.Equal;
			case Op.IfFalse:
				return TIf.Operator.False;
			case Op.IfGreaterEqual:
				return TIf.Operator.GreaterEqual;
			case Op.IfGreaterThan:
				return TIf.Operator.GreaterThan;
			case Op.IfLessEqual:
				return TIf.Operator.LessEqual;
			case Op.IfLessThan:
				return TIf.Operator.LessThan;
			case Op.IfNotEqual:
				return TIf.Operator.NotEqual;
			case Op.IfNotGreaterEqual:
				return TIf.Operator.NotGreaterEqual;
			case Op.IfNotGreaterThan:
				return TIf.Operator.NotGreaterThan;
			case Op.IfNotLessEqual:
				return TIf.Operator.NotLessEqual;
			case Op.IfNotLessThan:
				return TIf.Operator.NotLessThan;
			case Op.IfStrictEqual:
				return TIf.Operator.StrictEqual;
			case Op.IfStrictNotEqual:
				return TIf.Operator.StrictNotEqual;
			case Op.IfTrue:
				return TIf.Operator.True;
			default:
				throw new TaasException( "Illegal opcode "
						+ Op.codeToString( code ) );
		}
	}

	private void initialize( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		visitedVertices.clear();
		visitedMarkers.clear();
		operandStackAtMerge.clear();
		scopeStackAtMerge.clear();
		bytecodeToVertex.clear();

		operandStack = new TaasStack( bytecode.methodBody.maxStack );
		scopeStack = new TaasStack( bytecode.methodBody.maxScopeDepth
				- bytecode.methodBody.initScopeDepth );

		localRegisters = new TaasRegisters( bytecode.methodBody.localCount );
		code = new TaasCode();
		typer = new TaasTyper( environment );
	}

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		if( localRegisters.numRegisters() != 0 )
		{
			final Object scope = environment
					.scopeOf( bytecode.methodBody.method );

			if( null == scope )
			{
				throw new TaasException(
						"Scope of method is unknown or does not exist." );
			}

			if( ( scope instanceof Class ) || ( scope instanceof Script ) )
			{
				localAt( 0 ).setValue( TaasGlobalScope.INSTANCE );
			}
			else if( scope instanceof Instance )
			{
				localAt( 0 ).setValue(
						new TaasInstance( new TaasMultiname(
								( (Instance)scope ).name ) ) );
			}
			else
			{
				throw new TaasException( "Unknown scope type." );
			}

			//
			// Type local variables:
			//

			final Method method = bytecode.methodBody.method;
			int localIndex = 1;

			for( final Parameter parameter : method.parameters )
			{
				localAt( localIndex++ ).typeAs(
						typer.toNativeType( parameter.type ) );
			}
		}

		try
		{
			final ControlFlowGraphBuilder graphBuilder = new ControlFlowGraphBuilder();
			graphBuilder.interpret( environment, bytecode );

			// this.bytecode = bytecode;
			markers = bytecode.markers;
			bytecodeGraph = graphBuilder.getGraph();
			//
			// final DOTExporter<BytecodeVertex, Edge<BytecodeVertex>> export =
			// new DOTExporter<BytecodeVertex, Edge<BytecodeVertex>>(
			// new BytecodeVertex.LabelProvider() );
			// export.export( System.out, bytecodeGraph );

			build( bytecodeGraph.getEntryVertex(), 0, 0 );
		}
		catch( final TaasException ex )
		{
			System.err.println( "####TAAS#ERROR#LOG#########################" );
			System.err.println( localRegisters.debug() + "\n" );
			System.err.println( operandStack.debug() + "\n" );
			System.err.println( code.debug() );
			System.err.println( "###########################################" );

			throw ex;
		}
	}

	private boolean isVisited( final BytecodeVertex vertex )
	{
		return visitedVertices.contains( vertex );
	}

	private TaasLocal localAt( final int register )
	{
		return localRegisters.get( register );
	}

	private void markVisited( final BytecodeVertex vertex )
	{
		visitedVertices.add( vertex );
	}

	private BytecodeVertex nextOf( final BytecodeVertex vertex )
			throws TaasException
	{
		try
		{
			//
			// Search for the first occurrence of a default edge ignoring
			// throw.
			//

			final List<Edge<BytecodeVertex>> edges = bytecodeGraph
					.outgoingOf( vertex );

			for( final Edge<BytecodeVertex> edge : edges )
			{
				if( edge.kind == EdgeKind.Default || edge.kind == EdgeKind.Jump
						|| edge.kind == EdgeKind.Return )
				{
					if( edge.kind != EdgeKind.Default )
					{
						code.setNextKind( edge.kind );
					}

					return edge.endVertex;
				}
			}

			return null;
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}
	}

	protected void onAdd( final Add operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.add( lhs, rhs ) ) );
	}

	protected void onAddDouble( final AddDouble operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.add( lhs, rhs ) ) );
	}

	protected void onAddInt( final AddInt operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.add( lhs, rhs ) ) );
	}

	protected void onApplyType( final ApplyType operation )
	{
		final TaasValue[] parameters = parameters( operation.typeSize );
		final TaasValue object = operandStack.pop();

		code.add( operandStack.push( TAAS.applyType( object, parameters ) ) );
	}

	protected void onAsType( final AsType operation )
	{
		code.add( operandStack.push( TAAS.asType( operandStack.pop(),
				constant( operation.type ) ) ) );
	}

	protected void onAsTypeLate( final AsTypeLate operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.asType( lhs, rhs ) ) );
	}

	protected void onBitAnd( final BitAnd operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.bitAnd( lhs, rhs ) ) );
	}

	protected void onBitNot( final BitNot operation )
	{
		code.add( operandStack.push( TAAS.bitNot( operandStack.pop() ) ) );
	}

	protected void onBitOr( final BitOr operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.bitOr( lhs, rhs ) ) );
	}

	protected void onBitXor( final BitXor operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.bitXor( lhs, rhs ) ) );
	}

	protected void onBreakpoint( final Breakpoint operation )
	{
		// IGNORED
	}

	protected void onBreakpointLine( final BreakpointLine operation )
	{
		// IGNORED
	}

	protected void onCall( final Call operation )
	{
		TODO();
	}

	protected void onCallMethod( final CallMethod operation )
	{
		TODO();
	}

	protected void onCallProperty( final CallProperty operation )
	{
		final TaasValue[] parameters = parameters( operation.numArguments );
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( operandStack.push( TAAS.callProperty( object, property,
				parameters, typer.typeOf( object, property ) ) ) );
	}

	protected void onCallPropLex( final CallPropLex operation )
	{
		TODO();
	}

	protected void onCallPropVoid( final CallPropVoid operation )
	{
		final TaasValue[] parameters = parameters( operation.numArguments );
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( TAAS.callProperty( object, property, parameters ) );
	}

	protected void onCallStatic( final CallStatic operation )
	{
		TODO();
	}

	protected void onCallSuper( final CallSuper operation )
	{
		TODO();
	}

	protected void onCallSuperVoid( final CallSuperVoid operation )
	{
		TODO();
	}

	protected void onCheckFilter( final CheckFilter operation )
	{
		code.add( TAAS.checkFilter( operandStack.peek() ) );
	}

	protected void onCoerce( final Coerce operation )
	{
		final TaasType rhs = new MultinameType( operation.type );
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.coerce( lhs, rhs ) ) );
	}

	protected void onCoerceAny( final CoerceAny operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				AnyType.INSTANCE ) ) );
	}

	protected void onCoerceBoolean( final CoerceBoolean operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				BooleanType.INSTANCE ) ) );
	}

	protected void onCoerceDouble( final CoerceDouble operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				NumberType.INSTANCE ) ) );
	}

	protected void onCoerceInt( final CoerceInt operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				IntType.INSTANCE ) ) );
	}

	protected void onCoerceObject( final CoerceObject operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				ObjectType.INSTANCE ) ) );
	}

	protected void onCoerceString( final CoerceString operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				StringType.INSTANCE ) ) );
	}

	protected void onCoerceUInt( final CoerceUInt operation )
	{
		code.add( operandStack.push( TAAS.coerce( operandStack.pop(),
				UIntType.INSTANCE ) ) );
	}

	protected void onConcat( final Concat operation )
	{
		UNDOCUMENTED();
	}

	protected void onConstruct( final Construct operation )
	{
		final TaasValue[] parameters = parameters( operation.numArguments );
		final TaasValue object = operandStack.pop();

		code.add( operandStack.push( TAAS.construct( object, parameters ) ) );
	}

	protected void onConstructProp( final ConstructProp operation )
	{
		final TaasValue[] parameters = parameters( operation.numArguments );
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( operandStack.push( TAAS.constructProperty( object, property,
				parameters ) ) );
	}

	protected void onConstructSuper( final ConstructSuper operation )
	{
		final TaasValue[] parameters = parameters( operation.numArguments );
		final TaasValue object = operandStack.pop();

		code.add( TAAS.constructSuper( object, parameters ) );
	}

	protected void onConvertBoolean( final ConvertBoolean operation )
	{
		code.add( operandStack.push( TAAS.convert( operandStack.pop(),
				BooleanType.INSTANCE ) ) );
	}

	protected void onConvertDouble( final ConvertDouble operation )
	{
		code.add( operandStack.push( TAAS.convert( operandStack.pop(),
				NumberType.INSTANCE ) ) );
	}

	protected void onConvertInt( final ConvertInt operation )
	{
		code.add( operandStack.push( TAAS.convert( operandStack.pop(),
				IntType.INSTANCE ) ) );
	}

	protected void onConvertObject( final ConvertObject operation )
	{
		code.add( operandStack.push( TAAS.convert( operandStack.pop(),
				ObjectType.INSTANCE ) ) );
	}

	protected void onConvertString( final ConvertString operation )
	{
		code.add( operandStack.push( TAAS.convert( operandStack.pop(),
				StringType.INSTANCE ) ) );
	}

	protected void onConvertUInt( final ConvertUInt operation )
	{
		code.add( operandStack.push( TAAS.convert( operandStack.pop(),
				UIntType.INSTANCE ) ) );
	}

	protected void onDebug( final Debug operation )
	{
		// IGNORED
	}

	protected void onDebugFile( final DebugFile operation )
	{
		// IGNORED
	}

	protected void onDebugLine( final DebugLine operation )
	{
		// IGNORED
	}

	protected void onDecLocal( final DecLocal operation )
	{
		code.add( TAAS.decLocal( localAt( operation.register ) ) );
	}

	protected void onDecLocalInt( final DecLocalInt operation )
	{
		code.add( TAAS.decLocal( localAt( operation.register ) ) );
	}

	protected void onDecrement( final Decrement operation )
	{
		code.add( operandStack.push( TAAS.decrement( operandStack.pop() ) ) );
	}

	protected void onDecrementInt( final DecrementInt operation )
	{
		code.add( operandStack.push( TAAS.decrement( operandStack.pop() ) ) );
	}

	protected void onDefaultXmlNamespace( final DefaultXmlNamespace operation )
	{
		code.add( TAAS.defaultXmlNamespace( constant( operation.uri ) ) );
	}

	protected void onDefaultXmlNamespaceL( final DefaultXmlNamespaceL operation )
	{
		code.add( TAAS.defaultXmlNamespace( operandStack.pop() ) );
	}

	protected void onDeleteProperty( final DeleteProperty operation )
	{
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( TAAS.deleteProperty( object, property ) );
	}

	protected void onDeletePropertyLate( final DeletePropertyLate operation )
	{
		UNDOCUMENTED();
	}

	protected void onDivide( final Divide operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.divide( lhs, rhs ) ) );
	}

	protected void onDup( final Dup operation )
	{
		final TaasValue value = operandStack.pop();

		operandStack.push( value );
		operandStack.push( value );
	}

	protected void onEquals( final Equals operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.equals( lhs, rhs ) ) );
	}

	protected void onEscXmlAttr( final EscXmlAttr operation )
	{
		code.add( operandStack.push( TAAS.escapeXmlAttribute( operandStack
				.pop() ) ) );
	}

	protected void onEscXmlElem( final EscXmlElem operation )
	{
		code.add( operandStack
				.push( TAAS.escapeXmlElement( operandStack.pop() ) ) );
	}

	protected void onFindProperty( final FindProperty operation )
	{
		final TaasMultiname property = constant( operation.property );

		code.add( operandStack.push( TAAS.findProperty( property ) ) );
	}

	protected void onFindPropStrict( final FindPropStrict operation )
	{
		final TaasMultiname property = constant( operation.property );

		code.add( operandStack.push( TAAS.findPropertyStrict( property ) ) );
	}

	protected void onGetDescendants( final GetDescendants operation )
	{
		TODO();
	}

	protected void onGetGlobalScope( final GetGlobalScope operation )
	{
		TODO();
	}

	protected void onGetGlobalSlot( final GetGlobalSlot operation )
	{
		TODO();
	}

	protected void onGetLex( final GetLex operation )
	{
		final TaasMultiname name = constant( operation.property );

		code.add( operandStack.push( TAAS.getLex( name, typer.typeOf(
				scopeStack, name ) ) ) );
	}

	protected void onGetLocal( final GetLocal operation )
	{
		code.add( operandStack.push( localAt( operation.register ) ) );
	}

	protected void onGetLocal0( final GetLocal0 operation )
	{
		code.add( operandStack.push( localAt( 0 ) ) );
	}

	protected void onGetLocal1( final GetLocal1 operation )
	{
		code.add( operandStack.push( localAt( 1 ) ) );
	}

	protected void onGetLocal2( final GetLocal2 operation )
	{
		code.add( operandStack.push( localAt( 2 ) ) );
	}

	protected void onGetLocal3( final GetLocal3 operation )
	{
		code.add( operandStack.push( localAt( 3 ) ) );
	}

	protected void onGetProperty( final GetProperty operation )
	{
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( operandStack.push( TAAS.getProperty( object, property, typer
				.typeOf( object, property ) ) ) );
	}

	protected void onGetPropertyLate( final GetPropertyLate operation )
	{
		TODO();
	}

	protected void onGetScopeObject( final GetScopeObject operation )
	{
		code.add( operandStack.push( TAAS.getScopeObject( scopeStack
				.get( operation.scopeIndex ) ) ) );
	}

	protected void onGetSlot( final GetSlot operation )
	{
		TODO();
	}

	protected void onGetSuper( final GetSuper operation )
	{
		TODO();
	}

	protected void onGreaterEquals( final GreaterEquals operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.greaterEquals( lhs, rhs ) ) );
	}

	protected void onGreaterThan( final GreaterThan operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.greaterThan( lhs, rhs ) ) );
	}

	protected void onHasNext( final HasNext operation )
	{
		TODO();
	}

	protected void onHasNext2( final HasNext2 operation )
	{
		TODO();
	}

	protected void onIf( final AbstractConditionalJump conditionalJump )
	{
		TaasValue lhs = null;
		TaasValue rhs = null;

		switch( conditionalJump.code )
		{
			case Op.IfFalse:
			case Op.IfTrue:
				lhs = operandStack.pop();
				break;

			default:
				rhs = operandStack.pop();
				lhs = operandStack.pop();
				break;
		}

		code.add( TAAS.if$( lhs, rhs, ifOperatorOf( conditionalJump.code ) ) );
	}

	protected void onIn( final In operation )
	{
		TODO();
	}

	protected void onIncLocal( final IncLocal operation )
	{
		code.add( TAAS.incLocal( localAt( operation.register ) ) );
	}

	protected void onIncLocalInt( final IncLocalInt operation )
	{
		code.add( TAAS.incLocal( localAt( operation.register ) ) );
	}

	protected void onIncrement( final Increment operation )
	{
		code.add( operandStack.push( TAAS.increment( operandStack.pop() ) ) );
	}

	protected void onIncrementInt( final IncrementInt operation )
	{
		code.add( operandStack.push( TAAS.increment( operandStack.pop() ) ) );
	}

	protected void onInitProperty( final InitProperty operation )
	{
		final TaasValue value = operandStack.pop();
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( TAAS.initProperty( object, property, value ) );
	}

	protected void onInstanceOf( final InstanceOf operation )
	{
		TODO();
	}

	protected void onIsType( final IsType operation )
	{
		TODO();
	}

	protected void onIsTypeLate( final IsTypeLate operation )
	{
		TODO();
	}

	protected void onJump( final Jump operation )
	{
		code.add( TAAS.jump() );
	}

	protected void onKill( final Kill operation )
	{
		code.add( TAAS.kill( localAt( operation.register ) ) );
	}

	protected void onLabel( final Label operation )
	{
		// IGNORED
	}

	protected void onLessEquals( final LessEquals operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.lessEquals( lhs, rhs ) ) );
	}

	protected void onLessThan( final LessThan operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.lessThan( lhs, rhs ) ) );
	}

	protected void onLookupSwitch( final LookupSwitch operation )
	{
		code.add( TAAS.lookupSwitch( operandStack.pop() ) );
	}

	protected void onModulo( final Modulo operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.modulo( lhs, rhs ) ) );
	}

	protected void onMultiply( final Multiply operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.multiply( lhs, rhs ) ) );
	}

	protected void onMultiplyInt( final MultiplyInt operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.multiply( lhs, rhs ) ) );
	}

	protected void onNegate( final Negate operation )
	{
		code.add( operandStack.push( TAAS.negate( operandStack.pop() ) ) );
	}

	protected void onNegateInt( final NegateInt operation )
	{
		code.add( operandStack.push( TAAS.negate( operandStack.pop() ) ) );
	}

	protected void onNewActivation( final NewActivation operation )
	{
		TODO();
	}

	protected void onNewArray( final NewArray operation )
	{
		TODO();
	}

	protected void onNewCatch( final NewCatch operation )
	{
		TODO();
	}

	protected void onNewClass( final NewClass operation )
	{
		code.add( operandStack.push( TAAS.newClass( operandStack.pop(),
				operation.klass ) ) );
	}

	protected void onNewFunction( final NewFunction operation )
	{
		TODO();
	}

	protected void onNewObject( final NewObject operation )
	{
		final TaasObject result = new TaasObject();

		int numProperties = operation.numProperties;

		while( --numProperties > -1 )
		{
			final TaasValue name = operandStack.pop();
			final TaasValue value = operandStack.pop();

			if( !name.getType().isType( StringType.INSTANCE ) )
			{
				throw new TaasException( "Property has to be of StringType." );
			}

			if( !( name instanceof TaasString ) )
			{
				throw new TaasException(
						"Property is not an instance of TaasString." );
			}

			result.put( (TaasString)name, value );
		}

		code.add( operandStack.push( result ) );
	}

	protected void onNextName( final NextName operation )
	{
		TODO();
	}

	protected void onNextValue( final NextValue operation )
	{
		TODO();
	}

	protected void onNop( final Nop operation )
	{
		// IGNORED
	}

	protected void onNot( final Not operation )
	{
		code.add( operandStack.push( TAAS.not( operandStack.pop() ) ) );
	}

	protected void onPop( final Pop operation )
	{
		operandStack.pop();
	}

	protected void onPopScope( final PopScope operation )
	{
		scopeStack.pop();

		code.add( TAAS.leaveScope() );
	}

	protected void onPushByte( final PushByte operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushDouble( final PushDouble operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushFalse( final PushFalse operation )
	{
		code.add( operandStack.push( TaasBoolean.FALSE ) );
	}

	protected void onPushInt( final PushInt operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushNamespace( final PushNamespace operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushNaN( final PushNaN operation )
	{
		code.add( operandStack.push( TaasNumber.NaN ) );
	}

	protected void onPushNull( final PushNull operation )
	{
		code.add( operandStack.push( TaasNull.INSTANCE ) );
	}

	protected void onPushScope( final PushScope operation )
	{
		final TaasValue value = operandStack.pop();

		if( value.isType( UndefinedType.INSTANCE )
				|| value.isType( NullType.INSTANCE ) )
		{
			throw new TaasException(
					"Can not use 'undefined' or 'null' as scope value." );
		}

		scopeStack.push( value );

		code.add( TAAS.enterScope( value ) );
	}

	protected void onPushShort( final PushShort operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushString( final PushString operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushTrue( final PushTrue operation )
	{
		code.add( operandStack.push( TaasBoolean.TRUE ) );
	}

	protected void onPushUInt( final PushUInt operation )
	{
		code.add( operandStack.push( constant( operation.value ) ) );
	}

	protected void onPushUndefined( final PushUndefined operation )
	{
		code.add( operandStack.push( TaasUndefined.INSTANCE ) );
	}

	protected void onPushWith( final PushWith operation )
	{
		final TaasValue value = operandStack.pop();

		if( value.isType( UndefinedType.INSTANCE )
				|| value.isType( NullType.INSTANCE ) )
		{
			throw new TaasException(
					"Can not use 'undefined' or 'null' as scope value." );
		}

		// TODO add operation to change scope
		// TODO mark with scope
		scopeStack.push( value );
	}

	protected void onReturnValue( final ReturnValue operation )
	{
		code.add( TAAS.return$( operandStack.pop() ) );
	}

	protected void onReturnVoid( final ReturnVoid operation )
	{
		code.add( TAAS.return$() );
	}

	protected void onSetGlobalSlot( final SetGlobalSlot operation )
	{

	}

	protected void onSetLocal( final SetLocal operation )
	{
		final TaasLocal local = localAt( operation.register );
		final TaasValue value = operandStack.pop();

		if( !local.isTyped() )
		{
			local.typeAs( value.getType() );
		}

		code.add( TAAS.setLocal( local, value ) );
	}

	protected void onSetLocal0( final SetLocal0 operation )
	{
		final TaasLocal local = localAt( 0 );
		final TaasValue value = operandStack.pop();

		if( !local.isTyped() )
		{
			local.typeAs( value.getType() );
		}

		code.add( TAAS.setLocal( local, value ) );
	}

	protected void onSetLocal1( final SetLocal1 operation )
	{
		final TaasLocal local = localAt( 1 );
		final TaasValue value = operandStack.pop();

		if( !local.isTyped() )
		{
			local.typeAs( value.getType() );
		}

		code.add( TAAS.setLocal( local, value ) );
	}

	protected void onSetLocal2( final SetLocal2 operation )
	{
		final TaasLocal local = localAt( 2 );
		final TaasValue value = operandStack.pop();

		if( !local.isTyped() )
		{
			local.typeAs( value.getType() );
		}

		code.add( TAAS.setLocal( local, value ) );
	}

	protected void onSetLocal3( final SetLocal3 operation )
	{
		final TaasLocal local = localAt( 3 );
		final TaasValue value = operandStack.pop();

		if( !local.isTyped() )
		{
			local.typeAs( value.getType() );
		}

		code.add( TAAS.setLocal( local, value ) );
	}

	protected void onSetProperty( final SetProperty operation )
	{
		final TaasValue value = operandStack.pop();
		final TaasMultiname property = constant( operation.property );
		final TaasValue object = operandStack.pop();

		code.add( TAAS.setProperty( object, property, value ) );
	}

	protected void onSetPropertyLate( final SetPropertyLate operation )
	{
		UNDOCUMENTED();
	}

	protected void onSetSlot( final SetSlot operation )
	{
		TODO();
	}

	protected void onSetSuper( final SetSuper operation )
	{
		TODO();
	}

	protected void onShiftLeft( final ShiftLeft operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.shiftLeft( lhs, rhs ) ) );
	}

	protected void onShiftRight( final ShiftRight operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.shiftRight( lhs, rhs ) ) );
	}

	protected void onShiftRightUnsigned( final ShiftRightUnsigned operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.shiftRightUnsigned( lhs, rhs ) ) );
	}

	protected void onStrictEquals( final StrictEquals operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.strictEquals( lhs, rhs ) ) );
	}

	protected void onSubtract( final Subtract operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.subtract( lhs, rhs ) ) );
	}

	protected void onSubtractInt( final SubtractInt operation )
	{
		final TaasValue rhs = operandStack.pop();
		final TaasValue lhs = operandStack.pop();

		code.add( operandStack.push( TAAS.subtract( lhs, rhs ) ) );
	}

	protected void onSwap( final Swap operation )
	{
		// TODO is this correct?

		final TaasValue a = operandStack.pop();
		final TaasValue b = operandStack.pop();

		operandStack.push( a );
		operandStack.push( b );
	}

	protected void onThrow( final Throw operation )
	{
		code.add( TAAS.throw$( operandStack.pop() ) );
	}

	protected void onTypeOf( final TypeOf operation )
	{
		code.add( operandStack.push( TAAS.typeOf( operandStack.pop() ) ) );
	}

	private TaasValue[] parameters( final int numParameters )
	{
		final TaasValue[] result = new TaasValue[ numParameters ];

		int i = numParameters;

		while( --i > -1 )
		{
			result[ i ] = operandStack.pop();
		}

		return result;
	}

	private void reset()
	{
		markers = null;
		bytecodeGraph = null;

		visitedVertices.clear();
		visitedMarkers.clear();
		operandStackAtMerge.clear();
		scopeStackAtMerge.clear();

		operandStack = null;
		scopeStack = null;
		localRegisters = null;
	}

	private void TODO()
	{
		throw new TaasException( "TODO" );
	}

	/**
	 * Safe cast from TaasValue to TaasType.
	 * 
	 * @param value
	 *            The value to cast as TaasType.
	 * @return The value casted as TaasType.
	 */
	// private TaasType toType( final TaasValue value )
	// {
	// if( value instanceof TaasType )
	// {
	// return (TaasType)value;
	// }
	// else
	// {
	// throw new TaasException( "Can not convert from " + value
	// + " to TaasType." );
	// }
	// }
	/**
	 * Looks up the stack delta at the current vertex and transforms all
	 * additional values to boxes.
	 * 
	 * @param currentVertex
	 *            The current vertex.
	 * @param operandSize
	 *            The initial operand stack size.
	 * @param scopeSize
	 *            The initial scope stack size.
	 */
	private void transformStack( final BytecodeVertex currentVertex,
			final int operandSize, final int scopeSize,
			final TaasEdge previousEdge )
	{
		try
		{
			//
			// Scope stack is ignored since it will never be part of the
			// operand stack.
			//

			if( bytecodeGraph.indegreeOf( currentVertex ) > 1 )
			{
				int delta = operandStack.size() - operandSize;

				if( delta > 0 )
				{
					final int i = operandStack.size();

					do
					{
						operandStack.set( i - delta, new TaasPhi( operandStack
								.get( i - delta ), previousEdge ) );
					}
					while( --delta > 0 );

					//
					// Freeze the stack at a CF merge.
					//

					operandStackAtMerge.put( currentVertex, operandStack
							.clone() );

					scopeStackAtMerge.put( currentVertex, scopeStack.clone() );
				}
				else if( delta == 0 )
				{
					return;
				}
				else
				{
					// throw new TaasException( "Invalid code." );
					return;
				}
			}
		}
		catch( final ControlFlowGraphException cfgEx )
		{
			throw new TaasException( cfgEx );
		}
	}

	private void UNDOCUMENTED()
	{
		throw new TaasException( "Undocumented operation." );
	}
}
