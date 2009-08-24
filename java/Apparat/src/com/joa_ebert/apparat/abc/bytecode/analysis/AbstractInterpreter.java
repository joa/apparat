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

import java.util.Iterator;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.*;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class AbstractInterpreter implements IInterpreter
{
	protected AbcEnvironment env;
	protected Bytecode bytecode;

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		this.env = environment;
		this.bytecode = bytecode;

		final Iterator<AbstractOperation> iter = bytecode.listIterator();

		while( iter.hasNext() )
		{
			final AbstractOperation op = iter.next();
			final int code = op.code;

			switch( code )
			{
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
				case Op.IfNotLessThan:
					onIfNotLessThan( (IfNotLessThan)op );
					break;
				case Op.IfNotLessEqual:
					onIfNotLessEqual( (IfNotLessEqual)op );
					break;
				case Op.IfNotGreaterThan:
					onIfNotGreaterThan( (IfNotGreaterThan)op );
					break;
				case Op.IfNotGreaterEqual:
					onIfNotGreaterEqual( (IfNotGreaterEqual)op );
					break;
				case Op.Jump:
					onJump( (Jump)op );
					break;
				case Op.IfTrue:
					onIfTrue( (IfTrue)op );
					break;
				case Op.IfFalse:
					onIfFalse( (IfFalse)op );
					break;
				case Op.IfEqual:
					onIfEqual( (IfEqual)op );
					break;
				case Op.IfNotEqual:
					onIfNotEqual( (IfNotEqual)op );
					break;
				case Op.IfLessThan:
					onIfLessThan( (IfLessThan)op );
					break;
				case Op.IfLessEqual:
					onIfLessEqual( (IfLessEqual)op );
					break;
				case Op.IfGreaterThan:
					onIfGreaterThan( (IfGreaterThan)op );
					break;
				case Op.IfGreaterEqual:
					onIfGreaterEqual( (IfGreaterEqual)op );
					break;
				case Op.IfStrictEqual:
					onIfStrictEqual( (IfStrictEqual)op );
					break;
				case Op.IfStrictNotEqual:
					onIfStrictNotEqual( (IfStrictNotEqual)op );
					break;
				case Op.LookupSwitch:
					onLookupSwitch( (LookupSwitch)op );
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
				case Op.SetByte:
					onSetByte( (SetByte)op );
					break;
				case Op.SetShort:
					onSetShort( (SetShort)op );
					break;
				case Op.SetInt:
					onSetInt( (SetInt)op );
					break;
				case Op.SetFloat:
					onSetFloat( (SetFloat)op );
					break;
				case Op.SetDouble:
					onSetDouble( (SetDouble)op );
					break;
				case Op.GetByte:
					onGetByte( (GetByte)op );
					break;
				case Op.GetShort:
					onGetShort( (GetShort)op );
					break;
				case Op.GetInt:
					onGetInt( (GetInt)op );
					break;
				case Op.GetFloat:
					onGetFloat( (GetFloat)op );
					break;
				case Op.GetDouble:
					onGetDouble( (GetDouble)op );
					break;
				case Op.Sign1:
					onSign1( (Sign1)op );
					break;
				case Op.Sign8:
					onSign8( (Sign8)op );
					break;
				case Op.Sign16:
					onSign16( (Sign16)op );
					break;
			}
		}

		this.env = null;
		this.bytecode = null;
	}

	protected abstract void onAdd( final Add operation );

	protected abstract void onAddDouble( final AddDouble operation );

	protected abstract void onAddInt( final AddInt operation );

	protected abstract void onApplyType( ApplyType operation );

	protected abstract void onAsType( final AsType operation );

	protected abstract void onAsTypeLate( final AsTypeLate operation );

	protected abstract void onBitAnd( final BitAnd operation );

	protected abstract void onBitNot( final BitNot operation );

	protected abstract void onBitOr( final BitOr operation );

	protected abstract void onBitXor( final BitXor operation );

	protected abstract void onBreakpoint( final Breakpoint operation );

	protected abstract void onBreakpointLine( final BreakpointLine operation );

	protected abstract void onCall( final Call operation );

	protected abstract void onCallMethod( final CallMethod operation );

	// protected abstract void onCallInterface( final CallInterface operation );

	protected abstract void onCallProperty( final CallProperty operation );

	protected abstract void onCallPropLex( final CallPropLex operation );

	protected abstract void onCallPropVoid( final CallPropVoid operation );

	protected abstract void onCallStatic( final CallStatic operation );

	protected abstract void onCallSuper( final CallSuper operation );

	protected abstract void onCallSuperVoid( final CallSuperVoid operation );

	// protected abstract void onCallSuperId( final CallSuperId operation );

	protected abstract void onCheckFilter( final CheckFilter operation );

	protected abstract void onCoerce( final Coerce operation );

	protected abstract void onCoerceAny( final CoerceAny operation );

	protected abstract void onCoerceBoolean( final CoerceBoolean operation );

	protected abstract void onCoerceDouble( final CoerceDouble operation );

	protected abstract void onCoerceInt( final CoerceInt operation );

	protected abstract void onCoerceObject( final CoerceObject operation );

	protected abstract void onCoerceString( final CoerceString operation );

	protected abstract void onCoerceUInt( final CoerceUInt operation );

	protected abstract void onConcat( final Concat operation );

	protected abstract void onConstruct( final Construct operation );

	protected abstract void onConstructProp( final ConstructProp operation );

	protected abstract void onConstructSuper( final ConstructSuper operation );

	protected abstract void onConvertBoolean( final ConvertBoolean operation );

	protected abstract void onConvertDouble( final ConvertDouble operation );

	protected abstract void onConvertInt( final ConvertInt operation );

	protected abstract void onConvertObject( final ConvertObject operation );

	protected abstract void onConvertString( final ConvertString operation );

	protected abstract void onConvertUInt( final ConvertUInt operation );

	protected abstract void onDebug( final Debug operation );

	protected abstract void onDebugFile( final DebugFile operation );

	protected abstract void onDebugLine( final DebugLine operation );

	protected abstract void onDecLocal( final DecLocal operation );

	protected abstract void onDecLocalInt( final DecLocalInt operation );

	protected abstract void onDecrement( final Decrement operation );

	protected abstract void onDecrementInt( final DecrementInt operation );

	protected abstract void onDefaultXmlNamespace(
			final DefaultXmlNamespace operation );

	protected abstract void onDefaultXmlNamespaceL(
			final DefaultXmlNamespaceL operation );

	protected abstract void onDeleteProperty( final DeleteProperty operation );

	protected abstract void onDeletePropertyLate(
			final DeletePropertyLate operation );

	protected abstract void onDivide( final Divide operation );

	protected abstract void onDup( final Dup operation );

	protected abstract void onEquals( final Equals operation );

	protected abstract void onEscXmlAttr( final EscXmlAttr operation );

	protected abstract void onEscXmlElem( final EscXmlElem operation );

	protected abstract void onFindProperty( final FindProperty operation );

	// protected abstract void onFindDef( final FindDef operation );

	protected abstract void onFindPropStrict( final FindPropStrict operation );

	protected abstract void onGetByte( final GetByte operation );

	protected abstract void onGetDescendants( final GetDescendants operation );

	protected abstract void onGetDouble( final GetDouble operation );

	protected abstract void onGetFloat( final GetFloat operation );

	protected abstract void onGetGlobalScope( final GetGlobalScope operation );

	protected abstract void onGetGlobalSlot( final GetGlobalSlot operation );

	protected abstract void onGetInt( final GetInt operation );

	protected abstract void onGetLex( final GetLex operation );

	protected abstract void onGetLocal( final GetLocal operation );

	protected abstract void onGetLocal0( final GetLocal0 operation );

	protected abstract void onGetLocal1( final GetLocal1 operation );

	protected abstract void onGetLocal2( final GetLocal2 operation );

	protected abstract void onGetLocal3( final GetLocal3 operation );

	protected abstract void onGetProperty( final GetProperty operation );

	protected abstract void onGetPropertyLate( final GetPropertyLate operation );

	protected abstract void onGetScopeObject( final GetScopeObject operation );

	protected abstract void onGetShort( final GetShort operation );

	protected abstract void onGetSlot( final GetSlot operation );

	protected abstract void onGetSuper( final GetSuper operation );

	protected abstract void onGreaterEquals( final GreaterEquals operation );

	protected abstract void onGreaterThan( final GreaterThan operation );

	protected abstract void onHasNext( final HasNext operation );

	protected abstract void onHasNext2( final HasNext2 operation );

	protected abstract void onIfEqual( final IfEqual operation );

	protected abstract void onIfFalse( final IfFalse operation );

	protected abstract void onIfGreaterEqual( final IfGreaterEqual operation );

	protected abstract void onIfGreaterThan( final IfGreaterThan operation );

	protected abstract void onIfLessEqual( final IfLessEqual operation );

	protected abstract void onIfLessThan( final IfLessThan operation );

	protected abstract void onIfNotEqual( final IfNotEqual operation );

	protected abstract void onIfNotGreaterEqual(
			final IfNotGreaterEqual operation );

	protected abstract void onIfNotGreaterThan( final IfNotGreaterThan operation );

	protected abstract void onIfNotLessEqual( final IfNotLessEqual operation );

	protected abstract void onIfNotLessThan( final IfNotLessThan operation );

	protected abstract void onIfStrictEqual( final IfStrictEqual operation );

	protected abstract void onIfStrictNotEqual( final IfStrictNotEqual operation );

	protected abstract void onIfTrue( final IfTrue operation );

	protected abstract void onIn( final In operation );

	protected abstract void onIncLocal( final IncLocal operation );

	protected abstract void onIncLocalInt( final IncLocalInt operation );

	protected abstract void onIncrement( final Increment operation );

	protected abstract void onIncrementInt( final IncrementInt operation );

	protected abstract void onInitProperty( final InitProperty operation );

	protected abstract void onInstanceOf( final InstanceOf operation );

	protected abstract void onIsType( final IsType operation );

	protected abstract void onIsTypeLate( final IsTypeLate operation );

	protected abstract void onJump( final Jump operation );

	protected abstract void onKill( final Kill operation );

	protected abstract void onLabel( final Label operation );

	protected abstract void onLessEquals( final LessEquals operation );

	protected abstract void onLessThan( final LessThan operation );

	protected abstract void onLookupSwitch( final LookupSwitch operation );

	protected abstract void onModulo( final Modulo operation );

	protected abstract void onMultiply( final Multiply operation );

	protected abstract void onMultiplyInt( final MultiplyInt operation );

	protected abstract void onNegate( final Negate operation );

	protected abstract void onNegateInt( final NegateInt operation );

	protected abstract void onNewActivation( final NewActivation operation );

	protected abstract void onNewArray( final NewArray operation );

	protected abstract void onNewCatch( final NewCatch operation );

	protected abstract void onNewClass( final NewClass operation );

	protected abstract void onNewFunction( final NewFunction operation );

	protected abstract void onNewObject( final NewObject operation );

	protected abstract void onNextName( final NextName operation );

	protected abstract void onNextValue( final NextValue operation );

	protected abstract void onNop( final Nop operation );

	protected abstract void onNot( final Not operation );

	protected abstract void onPop( final Pop operation );

	protected abstract void onPopScope( final PopScope operation );

	protected abstract void onPushByte( final PushByte operation );

	protected abstract void onPushDouble( final PushDouble operation );

	protected abstract void onPushFalse( final PushFalse operation );

	protected abstract void onPushInt( final PushInt operation );

	protected abstract void onPushNamespace( final PushNamespace operation );

	protected abstract void onPushNaN( final PushNaN operation );

	protected abstract void onPushNull( final PushNull operation );

	protected abstract void onPushScope( final PushScope operation );

	protected abstract void onPushShort( final PushShort operation );

	protected abstract void onPushString( final PushString operation );

	protected abstract void onPushTrue( final PushTrue operation );

	protected abstract void onPushUInt( final PushUInt operation );

	protected abstract void onPushUndefined( final PushUndefined operation );

	protected abstract void onPushWith( final PushWith operation );

	protected abstract void onReturnValue( final ReturnValue operation );

	protected abstract void onReturnVoid( final ReturnVoid operation );

	protected abstract void onSetByte( final SetByte operation );

	protected abstract void onSetDouble( final SetDouble operation );

	protected abstract void onSetFloat( final SetFloat operation );

	protected abstract void onSetGlobalSlot( final SetGlobalSlot operation );

	protected abstract void onSetInt( final SetInt operation );

	protected abstract void onSetLocal( final SetLocal operation );

	protected abstract void onSetLocal0( final SetLocal0 operation );

	protected abstract void onSetLocal1( final SetLocal1 operation );

	protected abstract void onSetLocal2( final SetLocal2 operation );

	protected abstract void onSetLocal3( final SetLocal3 operation );

	protected abstract void onSetProperty( final SetProperty operation );

	protected abstract void onSetPropertyLate( final SetPropertyLate operation );

	protected abstract void onSetShort( final SetShort operation );

	protected abstract void onSetSlot( final SetSlot operation );

	protected abstract void onSetSuper( final SetSuper operation );

	protected abstract void onShiftLeft( final ShiftLeft operation );

	protected abstract void onShiftRight( final ShiftRight operation );

	protected abstract void onShiftRightUnsigned(
			final ShiftRightUnsigned operation );

	protected abstract void onSign1( final Sign1 operation );

	protected abstract void onSign16( final Sign16 operation );

	protected abstract void onSign8( final Sign8 operation );

	protected abstract void onStrictEquals( final StrictEquals operation );

	protected abstract void onSubtract( final Subtract operation );

	protected abstract void onSubtractInt( final SubtractInt operation );

	protected abstract void onSwap( final Swap operation );

	protected abstract void onThrow( final Throw operation );

	protected abstract void onTypeOf( final TypeOf operation );
}
