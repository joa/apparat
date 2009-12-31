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
package apparat.bytecode.operations

import apparat.abc._
import apparat.bytecode.Marker

object Op

sealed abstract class AbstractOp {
	def canThrow = false
	def controlsFlow = false

	def pushOperands: Int = 0
	def popOperands: Int = 0
	def pushScopes: Int = 0
	def popScopes: Int = 0

	def operandDelta = pushOperands - popOperands
	def scopeDelta = pushScopes - popScopes

	override def equals(that: Any) = that match {
		case abstractOp: AbstractOp => abstractOp eq this
		case _ => false
	}
}

trait DebugOp

trait AlchemyOp

trait OpThatCanThrow extends AbstractOp { final override def canThrow = true }

trait OpThatControlsFlow extends AbstractOp { final override def controlsFlow = true }

trait OpWithRegister { def register: Int }

trait OpWithSlot { def slot: Int }

trait OpWithMethod { def method: AbcMethod }

trait OpWithMarker { def marker: Marker }

trait OpWithArguments extends AbstractOp {
	def numArguments: Int
	override def popOperands = super.popOperands + numArguments
}

trait OpWithProperty extends AbstractOp {
	def property: AbcName
	override def popOperands = super.popOperands + (property match {
		case AbcRTQNameL | AbcRTQNameLA => 2
		case AbcRTQName(_) | AbcRTQNameA(_) => 1
		case _ => 0
	})
}

sealed abstract class AbstractOpWithOperands(numPush: Int, numPop: Int) extends AbstractOp {
	override def pushOperands: Int = numPush
	override def popOperands: Int = numPop
}

sealed abstract class AbstractOpWithScopes(numPush: Int, numPop: Int) extends AbstractOp {
	override def pushScopes: Int = numPush
	override def popScopes: Int = numPop
}

sealed abstract class AbstractBinaryOp extends AbstractOpWithOperands(1, 2)
sealed abstract class AbstractUnaryOp extends AbstractOpWithOperands(1, 1)
sealed abstract class AbstractConditionalOp(numPush: Int, numPop: Int) extends AbstractOpWithOperands(numPush, numPop) with OpWithMarker with OpThatControlsFlow
sealed abstract class AbstractConditionalBinaryOp extends AbstractConditionalOp( 0, 2)
sealed abstract class AbstractConditionalUnaryOp extends AbstractConditionalOp(0, 1)
sealed abstract class AbstractPushOp extends AbstractOpWithOperands(1, 0)
sealed abstract class AbstractAlchemySetOp extends AbstractOpWithOperands(0, 2) with AlchemyOp
sealed abstract class AbstractAlchemyGetOp extends AbstractOpWithOperands(1, 1) with AlchemyOp

case class Add() extends AbstractBinaryOp
case class AddDouble() extends AbstractBinaryOp
case class AddInt() extends AbstractBinaryOp
case class ApplyType(numArguments: Int) extends AbstractUnaryOp with OpWithArguments with OpThatCanThrow
case class AsType(typeName: AbcName) extends AbstractUnaryOp { require(!typeName.isRuntimeName) }
case class AsTypeLate() extends AbstractOpWithOperands(1, 2)
case class BitAnd() extends AbstractBinaryOp
case class BitNot() extends AbstractUnaryOp
case class BitOr() extends AbstractBinaryOp
case class BitXor() extends AbstractBinaryOp
case class Breakpoint() extends AbstractOp with DebugOp
case class BreakpointLine() extends AbstractOp with DebugOp
case class Call(numArguments: Int) extends AbstractOpWithOperands(1, 2) with OpWithArguments with OpThatCanThrow
case class CallMethod(numArguments: Int, method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallProperty(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropLex(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropVoid(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallStatic(numArguments: Int, method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallSuper(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallSuperVoid(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CheckFilter() extends AbstractOpWithOperands(1, 1) with OpThatCanThrow
case class Coerce(typeName: AbcName) extends AbstractUnaryOp with OpThatCanThrow { require(!typeName.isRuntimeName) }
case class CoerceAny() extends AbstractUnaryOp
case class CoerceString() extends AbstractUnaryOp
case class Construct(numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow
case class ConstructProp(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class ConstructSuper(numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpThatCanThrow
case class ConvertBoolean() extends AbstractUnaryOp
case class ConvertInt() extends AbstractUnaryOp
case class ConvertDouble() extends AbstractUnaryOp
case class ConvertObject() extends AbstractUnaryOp with OpThatCanThrow
case class ConvertUInt() extends AbstractUnaryOp
case class ConvertString() extends AbstractUnaryOp
case class Debug(kind: Int, name: Symbol, register: Int, extra: Int) extends AbstractOp with OpWithRegister with DebugOp
case class DebugFile(file: Symbol) extends AbstractOp with DebugOp
case class DebugLine(line: Int) extends AbstractOp with DebugOp
case class DecLocal(register: Int) extends AbstractOp with OpWithRegister
case class DecLocalInt(register: Int) extends AbstractOp with OpWithRegister
case class Decrement() extends AbstractUnaryOp
case class DecrementInt() extends AbstractUnaryOp
case class DeleteProperty(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
case class Divide() extends AbstractBinaryOp
case class Dup() extends AbstractOpWithOperands(2, 1)
case class DefaultXMLNamespace() extends AbstractOp with OpThatCanThrow
case class DefaultXMLNamespaceLate() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow
case class Equals() extends AbstractBinaryOp
case class EscapeXMLAttribute() extends AbstractOpWithOperands(1, 1)
case class EscapeXMLElement() extends AbstractOpWithOperands(1, 1)
case class FindProperty(property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty
case class FindPropStrict(property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty with OpThatCanThrow
case class GetDescendants(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
case class GetGlobalScope() extends AbstractOpWithOperands(1, 0)
case class GetGlobalSlot(slot: Int) extends AbstractOpWithOperands(1, 0) with OpWithSlot
case class GetLex(typeName: AbcName) extends AbstractOpWithOperands(1, 0) with OpThatCanThrow { require(!typeName.isRuntimeName) }
case class GetLocal(register: Int) extends AbstractOpWithOperands(1, 0) with OpWithRegister
case class GetProperty(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty
case class GetScopeObject(index: Int) extends AbstractOpWithOperands(1, 0)
case class GetSlot(slot: Int) extends AbstractOpWithOperands(1, 1) with OpWithSlot with OpThatCanThrow
case class GetSuper(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
case class GreaterEquals() extends AbstractBinaryOp
case class GreaterThan() extends AbstractBinaryOp
case class HasNext() extends AbstractOpWithOperands(1, 2)
case class HasNext2(objectRegister: Int, indexRegister: Int) extends AbstractOpWithOperands(1, 0)//TODO uses two local registers?!!
case class IfEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfFalse(marker: Marker) extends AbstractConditionalUnaryOp
case class IfGreaterEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfGreaterThan(marker: Marker) extends AbstractConditionalBinaryOp
case class IfLessEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfLessThan(marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotGreaterEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotGreaterThan(marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotLessEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotLessThan(marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfStrictEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfStrictNotEqual(marker: Marker) extends AbstractConditionalBinaryOp
case class IfTrue(marker: Marker) extends AbstractConditionalUnaryOp
case class In() extends AbstractOpWithOperands(1, 2)
case class IncLocal(register: Int) extends AbstractOp with OpWithRegister
case class IncLocalInt(register: Int) extends AbstractOp with OpWithRegister
case class Increment() extends AbstractUnaryOp
case class IncrementInt() extends AbstractUnaryOp
case class InitProperty(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
case class InstanceOf() extends AbstractBinaryOp with OpThatCanThrow
case class IsType(typeName: AbcName) extends AbstractOpWithOperands(1, 1) { require(!typeName.isRuntimeName) }
case class IsTypeLate() extends AbstractOpWithOperands(1, 2) with OpThatCanThrow
case class Jump(marker: Marker) extends AbstractOp with OpWithMarker with OpThatControlsFlow
case class Kill(register: Int) extends AbstractOp with OpWithRegister
case class Label() extends AbstractOp
case class LessEquals() extends AbstractBinaryOp
case class LessThan() extends AbstractBinaryOp
case class LookupSwitch(defaultCase: Marker, cases: Array[Marker]) extends AbstractOpWithOperands(0, 1) with OpThatControlsFlow
case class ShiftLeft() extends AbstractBinaryOp
case class Modulo() extends AbstractBinaryOp
case class Multiply() extends AbstractBinaryOp
case class MultiplyInt() extends AbstractBinaryOp
case class Negate() extends AbstractUnaryOp
case class NegateInt() extends AbstractUnaryOp
case class NewActivation() extends AbstractOpWithOperands(1, 0)
case class NewArray(numArguments: Int) extends AbstractOpWithOperands(1, 0) with OpWithArguments
case class NewCatch(exceptionHandler: AbcExceptionHandler) extends AbstractOpWithOperands(1, 0)
case class NewClass(nominalType: AbcNominalType) extends AbstractOpWithOperands(1, 1)
case class NewFunction(function: AbcMethod) extends AbstractOpWithOperands(1, 0)
case class NewObject(numArguments: Int) extends AbstractOpWithOperands(1, numArguments * 2)
case class NextName() extends AbstractOpWithOperands(1, 2)
case class NextValue() extends AbstractOpWithOperands(1, 2)
case class Nop() extends AbstractOp
case class Not() extends AbstractUnaryOp
case class Pop() extends AbstractOpWithOperands(0, 1)
case class PopScope() extends AbstractOpWithScopes(0, 1)
case class PushByte(value: Int) extends AbstractPushOp
case class PushDouble(value: Double) extends AbstractPushOp
case class PushFalse() extends AbstractPushOp
case class PushInt(value: Int) extends AbstractPushOp
case class PushNamespace(value: AbcNamespace) extends AbstractPushOp
case class PushNaN() extends AbstractPushOp
case class PushNull() extends AbstractPushOp
case class PushScope() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
case class PushShort(value: Int) extends AbstractPushOp
case class PushString(value: Symbol) extends AbstractPushOp
case class PushTrue() extends AbstractPushOp
case class PushUInt(value: Long) extends AbstractPushOp
case class PushUndefined() extends AbstractPushOp
case class PushWith() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
case class ReturnValue() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
case class ReturnVoid() extends AbstractOp with OpThatControlsFlow
case class ShiftRight() extends AbstractBinaryOp
case class SetLocal(register: Int) extends AbstractOpWithOperands(0, 1) with OpWithRegister
case class SetGlobalSlot(slot: Int) extends AbstractOpWithOperands(0, 1) with OpWithSlot
case class SetProperty(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
case class SetSlot(slot: Int) extends AbstractOpWithOperands(0, 2) with OpWithSlot with OpThatCanThrow
case class SetSuper(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
case class StrictEquals() extends AbstractBinaryOp
case class Subtract() extends AbstractBinaryOp
case class SubtractInt() extends AbstractBinaryOp
case class Swap() extends AbstractOpWithOperands(2, 2)
case class Throw() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
case class TypeOf() extends AbstractOpWithOperands(1, 1)
case class ShiftRightUnsigned() extends AbstractBinaryOp
case class SetByte() extends AbstractAlchemySetOp
case class SetShort() extends AbstractAlchemySetOp
case class SetInt() extends AbstractAlchemySetOp
case class SetFloat() extends AbstractAlchemySetOp
case class SetDouble() extends AbstractAlchemySetOp
case class GetByte() extends AbstractAlchemyGetOp
case class GetShort() extends AbstractAlchemyGetOp
case class GetInt() extends AbstractAlchemyGetOp
case class GetFloat() extends AbstractAlchemyGetOp
case class GetDouble() extends AbstractAlchemyGetOp
case class Sign1() extends AbstractUnaryOp with AlchemyOp
case class Sign8() extends AbstractUnaryOp with AlchemyOp
case class Sign16() extends AbstractUnaryOp with AlchemyOp
