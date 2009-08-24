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

import com.joa_ebert.apparat.abc.bytecode.operations.*;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class NullInterpreter extends AbstractInterpreter
{
	@Override
	public void onAdd( final Add operation )
	{
	}

	@Override
	public void onAddDouble( final AddDouble operation )
	{
	}

	@Override
	public void onAddInt( final AddInt operation )
	{
	}

	@Override
	protected void onApplyType( final ApplyType operation )
	{
	}

	@Override
	public void onAsType( final AsType operation )
	{
	}

	@Override
	public void onAsTypeLate( final AsTypeLate operation )
	{
	}

	@Override
	public void onBitAnd( final BitAnd operation )
	{
	}

	@Override
	public void onBitNot( final BitNot operation )
	{
	}

	@Override
	public void onBitOr( final BitOr operation )
	{
	}

	@Override
	public void onBitXor( final BitXor operation )
	{
	}

	@Override
	public void onBreakpoint( final Breakpoint operation )
	{
	}

	@Override
	public void onBreakpointLine( final BreakpointLine operation )
	{
	}

	@Override
	public void onCall( final Call operation )
	{
	}

	@Override
	public void onCallMethod( final CallMethod operation )
	{
	}

	@Override
	public void onCallProperty( final CallProperty operation )
	{
	}

	@Override
	public void onCallPropLex( final CallPropLex operation )
	{
	}

	@Override
	public void onCallPropVoid( final CallPropVoid operation )
	{
	}

	@Override
	public void onCallStatic( final CallStatic operation )
	{
	}

	@Override
	public void onCallSuper( final CallSuper operation )
	{
	}

	@Override
	public void onCallSuperVoid( final CallSuperVoid operation )
	{
	}

	@Override
	public void onCheckFilter( final CheckFilter operation )
	{
	}

	@Override
	public void onCoerce( final Coerce operation )
	{
	}

	@Override
	public void onCoerceAny( final CoerceAny operation )
	{
	}

	@Override
	public void onCoerceBoolean( final CoerceBoolean operation )
	{
	}

	@Override
	public void onCoerceDouble( final CoerceDouble operation )
	{
	}

	@Override
	public void onCoerceInt( final CoerceInt operation )
	{
	}

	@Override
	public void onCoerceObject( final CoerceObject operation )
	{
	}

	@Override
	public void onCoerceString( final CoerceString operation )
	{
	}

	@Override
	public void onCoerceUInt( final CoerceUInt operation )
	{
	}

	@Override
	public void onConcat( final Concat operation )
	{
	}

	@Override
	public void onConstruct( final Construct operation )
	{
	}

	@Override
	public void onConstructProp( final ConstructProp operation )
	{
	}

	@Override
	public void onConstructSuper( final ConstructSuper operation )
	{
	}

	@Override
	public void onConvertBoolean( final ConvertBoolean operation )
	{
	}

	@Override
	public void onConvertDouble( final ConvertDouble operation )
	{
	}

	@Override
	public void onConvertInt( final ConvertInt operation )
	{
	}

	@Override
	public void onConvertObject( final ConvertObject operation )
	{
	}

	@Override
	public void onConvertString( final ConvertString operation )
	{
	}

	@Override
	public void onConvertUInt( final ConvertUInt operation )
	{
	}

	@Override
	public void onDebug( final Debug operation )
	{
	}

	@Override
	public void onDebugFile( final DebugFile operation )
	{
	}

	@Override
	public void onDebugLine( final DebugLine operation )
	{
	}

	@Override
	public void onDecLocal( final DecLocal operation )
	{
	}

	@Override
	public void onDecLocalInt( final DecLocalInt operation )
	{
	}

	@Override
	public void onDecrement( final Decrement operation )
	{
	}

	@Override
	public void onDecrementInt( final DecrementInt operation )
	{
	}

	@Override
	public void onDefaultXmlNamespace( final DefaultXmlNamespace operation )
	{
	}

	@Override
	public void onDefaultXmlNamespaceL( final DefaultXmlNamespaceL operation )
	{
	}

	@Override
	public void onDeleteProperty( final DeleteProperty operation )
	{
	}

	@Override
	public void onDeletePropertyLate( final DeletePropertyLate operation )
	{
	}

	@Override
	public void onDivide( final Divide operation )
	{
	}

	@Override
	public void onDup( final Dup operation )
	{
	}

	@Override
	public void onEquals( final Equals operation )
	{
	}

	@Override
	public void onEscXmlAttr( final EscXmlAttr operation )
	{
	}

	@Override
	public void onEscXmlElem( final EscXmlElem operation )
	{
	}

	@Override
	public void onFindProperty( final FindProperty operation )
	{
	}

	@Override
	public void onFindPropStrict( final FindPropStrict operation )
	{
	}

	@Override
	protected void onGetByte( final GetByte operation )
	{
	}

	@Override
	public void onGetDescendants( final GetDescendants operation )
	{
	}

	@Override
	protected void onGetDouble( final GetDouble operation )
	{
	}

	@Override
	protected void onGetFloat( final GetFloat operation )
	{
	}

	@Override
	public void onGetGlobalScope( final GetGlobalScope operation )
	{
	}

	@Override
	public void onGetGlobalSlot( final GetGlobalSlot operation )
	{
	}

	@Override
	protected void onGetInt( final GetInt operation )
	{
	}

	@Override
	public void onGetLex( final GetLex operation )
	{
	}

	@Override
	public void onGetLocal( final GetLocal operation )
	{
	}

	@Override
	public void onGetLocal0( final GetLocal0 operation )
	{
	}

	@Override
	public void onGetLocal1( final GetLocal1 operation )
	{
	}

	@Override
	public void onGetLocal2( final GetLocal2 operation )
	{
	}

	@Override
	public void onGetLocal3( final GetLocal3 operation )
	{
	}

	@Override
	public void onGetProperty( final GetProperty operation )
	{
	}

	@Override
	public void onGetPropertyLate( final GetPropertyLate operation )
	{
	}

	@Override
	public void onGetScopeObject( final GetScopeObject operation )
	{
	}

	@Override
	protected void onGetShort( final GetShort operation )
	{
	}

	@Override
	public void onGetSlot( final GetSlot operation )
	{
	}

	@Override
	public void onGetSuper( final GetSuper operation )
	{
	}

	@Override
	public void onGreaterEquals( final GreaterEquals operation )
	{
	}

	@Override
	public void onGreaterThan( final GreaterThan operation )
	{
	}

	@Override
	public void onHasNext( final HasNext operation )
	{
	}

	@Override
	public void onHasNext2( final HasNext2 operation )
	{
	}

	@Override
	public void onIfEqual( final IfEqual operation )
	{
	}

	@Override
	public void onIfFalse( final IfFalse operation )
	{
	}

	@Override
	public void onIfGreaterEqual( final IfGreaterEqual operation )
	{
	}

	@Override
	public void onIfGreaterThan( final IfGreaterThan operation )
	{
	}

	@Override
	public void onIfLessEqual( final IfLessEqual operation )
	{
	}

	@Override
	public void onIfLessThan( final IfLessThan operation )
	{
	}

	@Override
	public void onIfNotEqual( final IfNotEqual operation )
	{
	}

	@Override
	public void onIfNotGreaterEqual( final IfNotGreaterEqual operation )
	{
	}

	@Override
	public void onIfNotGreaterThan( final IfNotGreaterThan operation )
	{
	}

	@Override
	public void onIfNotLessEqual( final IfNotLessEqual operation )
	{
	}

	@Override
	public void onIfNotLessThan( final IfNotLessThan operation )
	{
	}

	@Override
	public void onIfStrictEqual( final IfStrictEqual operation )
	{
	}

	@Override
	public void onIfStrictNotEqual( final IfStrictNotEqual operation )
	{
	}

	@Override
	public void onIfTrue( final IfTrue operation )
	{
	}

	@Override
	public void onIn( final In operation )
	{
	}

	@Override
	public void onIncLocal( final IncLocal operation )
	{
	}

	@Override
	public void onIncLocalInt( final IncLocalInt operation )
	{
	}

	@Override
	public void onIncrement( final Increment operation )
	{
	}

	@Override
	public void onIncrementInt( final IncrementInt operation )
	{
	}

	@Override
	public void onInitProperty( final InitProperty operation )
	{
	}

	@Override
	public void onInstanceOf( final InstanceOf operation )
	{
	}

	@Override
	public void onIsType( final IsType operation )
	{
	}

	@Override
	public void onIsTypeLate( final IsTypeLate operation )
	{
	}

	@Override
	public void onJump( final Jump operation )
	{
	}

	@Override
	public void onKill( final Kill operation )
	{
	}

	@Override
	public void onLabel( final Label operation )
	{
	}

	@Override
	public void onLessEquals( final LessEquals operation )
	{
	}

	@Override
	public void onLessThan( final LessThan operation )
	{
	}

	@Override
	public void onLookupSwitch( final LookupSwitch operation )
	{
	}

	@Override
	public void onModulo( final Modulo operation )
	{
	}

	@Override
	public void onMultiply( final Multiply operation )
	{
	}

	@Override
	public void onMultiplyInt( final MultiplyInt operation )
	{
	}

	@Override
	public void onNegate( final Negate operation )
	{
	}

	@Override
	public void onNegateInt( final NegateInt operation )
	{
	}

	@Override
	public void onNewActivation( final NewActivation operation )
	{
	}

	@Override
	public void onNewArray( final NewArray operation )
	{
	}

	@Override
	public void onNewCatch( final NewCatch operation )
	{
	}

	@Override
	public void onNewClass( final NewClass operation )
	{
	}

	@Override
	public void onNewFunction( final NewFunction operation )
	{
	}

	@Override
	public void onNewObject( final NewObject operation )
	{
	}

	@Override
	public void onNextName( final NextName operation )
	{
	}

	@Override
	public void onNextValue( final NextValue operation )
	{
	}

	@Override
	public void onNop( final Nop operation )
	{
	}

	@Override
	public void onNot( final Not operation )
	{
	}

	@Override
	public void onPop( final Pop operation )
	{
	}

	@Override
	public void onPopScope( final PopScope operation )
	{
	}

	@Override
	public void onPushByte( final PushByte operation )
	{
	}

	@Override
	public void onPushDouble( final PushDouble operation )
	{
	}

	@Override
	public void onPushFalse( final PushFalse operation )
	{
	}

	@Override
	public void onPushInt( final PushInt operation )
	{
	}

	@Override
	public void onPushNamespace( final PushNamespace operation )
	{
	}

	@Override
	public void onPushNaN( final PushNaN operation )
	{
	}

	@Override
	public void onPushNull( final PushNull operation )
	{
	}

	@Override
	public void onPushScope( final PushScope operation )
	{
	}

	@Override
	public void onPushShort( final PushShort operation )
	{
	}

	@Override
	public void onPushString( final PushString operation )
	{
	}

	@Override
	public void onPushTrue( final PushTrue operation )
	{
	}

	@Override
	public void onPushUInt( final PushUInt operation )
	{
	}

	@Override
	public void onPushUndefined( final PushUndefined operation )
	{
	}

	@Override
	public void onPushWith( final PushWith operation )
	{
	}

	@Override
	public void onReturnValue( final ReturnValue operation )
	{
	}

	@Override
	public void onReturnVoid( final ReturnVoid operation )
	{
	}

	@Override
	protected void onSetByte( final SetByte operation )
	{
	}

	@Override
	protected void onSetDouble( final SetDouble operation )
	{
	}

	@Override
	protected void onSetFloat( final SetFloat operation )
	{
	}

	@Override
	public void onSetGlobalSlot( final SetGlobalSlot operation )
	{
	}

	@Override
	protected void onSetInt( final SetInt operation )
	{
	}

	@Override
	public void onSetLocal( final SetLocal operation )
	{
	}

	@Override
	public void onSetLocal0( final SetLocal0 operation )
	{
	}

	@Override
	public void onSetLocal1( final SetLocal1 operation )
	{
	}

	@Override
	public void onSetLocal2( final SetLocal2 operation )
	{
	}

	@Override
	public void onSetLocal3( final SetLocal3 operation )
	{
	}

	@Override
	public void onSetProperty( final SetProperty operation )
	{
	}

	@Override
	public void onSetPropertyLate( final SetPropertyLate operation )
	{
	}

	@Override
	protected void onSetShort( final SetShort operation )
	{
	}

	@Override
	public void onSetSlot( final SetSlot operation )
	{
	}

	@Override
	public void onSetSuper( final SetSuper operation )
	{
	}

	@Override
	public void onShiftLeft( final ShiftLeft operation )
	{
	}

	@Override
	public void onShiftRight( final ShiftRight operation )
	{
	}

	@Override
	public void onShiftRightUnsigned( final ShiftRightUnsigned operation )
	{
	}

	@Override
	protected void onSign1( final Sign1 operation )
	{
	}

	@Override
	protected void onSign16( final Sign16 operation )
	{
	}

	@Override
	protected void onSign8( final Sign8 operation )
	{
	}

	@Override
	public void onStrictEquals( final StrictEquals operation )
	{
	}

	@Override
	public void onSubtract( final Subtract operation )
	{
	}

	@Override
	public void onSubtractInt( final SubtractInt operation )
	{
	}

	@Override
	public void onSwap( final Swap operation )
	{
	}

	@Override
	public void onThrow( final Throw operation )
	{
	}

	@Override
	public void onTypeOf( final TypeOf operation )
	{
	}
}
