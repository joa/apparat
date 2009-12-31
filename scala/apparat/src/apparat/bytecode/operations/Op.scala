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

final case class Add() extends AbstractBinaryOp
final case class AddDouble() extends AbstractBinaryOp
final case class AddInt() extends AbstractBinaryOp
final case class ApplyType(numArguments: Int) extends AbstractUnaryOp with OpWithArguments with OpThatCanThrow
final case class AsType(typeName: AbcName) extends AbstractUnaryOp { require(!typeName.isRuntimeName) }
final case class AsTypeLate() extends AbstractOpWithOperands(1, 2)
final case class BitAnd() extends AbstractBinaryOp
final case class BitNot() extends AbstractUnaryOp
final case class BitOr() extends AbstractBinaryOp
final case class BitXor() extends AbstractBinaryOp
final case class Breakpoint() extends AbstractOp with DebugOp
final case class BreakpointLine() extends AbstractOp with DebugOp
final case class Call(numArguments: Int) extends AbstractOpWithOperands(1, 2) with OpWithArguments with OpThatCanThrow
final case class CallMethod(numArguments: Int, method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
final case class CallProperty(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallPropLex(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallPropVoid(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallStatic(numArguments: Int, method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
final case class CallSuper(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallSuperVoid(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CheckFilter() extends AbstractOpWithOperands(1, 1) with OpThatCanThrow
final case class Coerce(typeName: AbcName) extends AbstractUnaryOp with OpThatCanThrow { require(!typeName.isRuntimeName) }
final case class CoerceAny() extends AbstractUnaryOp
final case class CoerceString() extends AbstractUnaryOp
final case class Construct(numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow
final case class ConstructProp(numArguments: Int, property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class ConstructSuper(numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpThatCanThrow
final case class ConvertBoolean() extends AbstractUnaryOp
final case class ConvertInt() extends AbstractUnaryOp
final case class ConvertDouble() extends AbstractUnaryOp
final case class ConvertObject() extends AbstractUnaryOp with OpThatCanThrow
final case class ConvertUInt() extends AbstractUnaryOp
final case class ConvertString() extends AbstractUnaryOp
final case class Debug(kind: Int, name: Symbol, register: Int, extra: Int) extends AbstractOp with OpWithRegister with DebugOp
final case class DebugFile(file: Symbol) extends AbstractOp with DebugOp
final case class DebugLine(line: Int) extends AbstractOp with DebugOp
final case class DecLocal(register: Int) extends AbstractOp with OpWithRegister
final case class DecLocalInt(register: Int) extends AbstractOp with OpWithRegister
final case class Decrement() extends AbstractUnaryOp
final case class DecrementInt() extends AbstractUnaryOp
final case class DeleteProperty(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
final case class Divide() extends AbstractBinaryOp
final case class Dup() extends AbstractOpWithOperands(2, 1)
final case class DefaultXMLNamespace() extends AbstractOp with OpThatCanThrow
final case class DefaultXMLNamespaceLate() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow
final case class Equals() extends AbstractBinaryOp
final case class EscapeXMLAttribute() extends AbstractOpWithOperands(1, 1)
final case class EscapeXMLElement() extends AbstractOpWithOperands(1, 1)
final case class FindProperty(property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty
final case class FindPropStrict(property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty with OpThatCanThrow
final case class GetDescendants(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
final case class GetGlobalScope() extends AbstractOpWithOperands(1, 0)
final case class GetGlobalSlot(slot: Int) extends AbstractOpWithOperands(1, 0) with OpWithSlot
final case class GetLex(typeName: AbcName) extends AbstractOpWithOperands(1, 0) with OpThatCanThrow { require(!typeName.isRuntimeName) }
final case class GetLocal(register: Int) extends AbstractOpWithOperands(1, 0) with OpWithRegister
final case class GetProperty(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty
final case class GetScopeObject(index: Int) extends AbstractOpWithOperands(1, 0)
final case class GetSlot(slot: Int) extends AbstractOpWithOperands(1, 1) with OpWithSlot with OpThatCanThrow
final case class GetSuper(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
final case class GreaterEquals() extends AbstractBinaryOp
final case class GreaterThan() extends AbstractBinaryOp
final case class HasNext() extends AbstractOpWithOperands(1, 2)
final case class HasNext2(objectRegister: Int, indexRegister: Int) extends AbstractOpWithOperands(1, 0)//TODO uses two local registers?!!
final case class IfEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfFalse(marker: Marker) extends AbstractConditionalUnaryOp
final case class IfGreaterEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfGreaterThan(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfLessEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfLessThan(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotGreaterEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotGreaterThan(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotLessEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotLessThan(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfStrictEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfStrictNotEqual(marker: Marker) extends AbstractConditionalBinaryOp
final case class IfTrue(marker: Marker) extends AbstractConditionalUnaryOp
final case class In() extends AbstractOpWithOperands(1, 2)
final case class IncLocal(register: Int) extends AbstractOp with OpWithRegister
final case class IncLocalInt(register: Int) extends AbstractOp with OpWithRegister
final case class Increment() extends AbstractUnaryOp
final case class IncrementInt() extends AbstractUnaryOp
final case class InitProperty(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
final case class InstanceOf() extends AbstractBinaryOp with OpThatCanThrow
final case class IsType(typeName: AbcName) extends AbstractOpWithOperands(1, 1) { require(!typeName.isRuntimeName) }
final case class IsTypeLate() extends AbstractOpWithOperands(1, 2) with OpThatCanThrow
final case class Jump(marker: Marker) extends AbstractOp with OpWithMarker with OpThatControlsFlow
final case class Kill(register: Int) extends AbstractOp with OpWithRegister
final case class Label() extends AbstractOp
final case class LessEquals() extends AbstractBinaryOp
final case class LessThan() extends AbstractBinaryOp
final case class LookupSwitch(defaultCase: Marker, cases: Array[Marker]) extends AbstractOpWithOperands(0, 1) with OpThatControlsFlow
final case class ShiftLeft() extends AbstractBinaryOp
final case class Modulo() extends AbstractBinaryOp
final case class Multiply() extends AbstractBinaryOp
final case class MultiplyInt() extends AbstractBinaryOp
final case class Negate() extends AbstractUnaryOp
final case class NegateInt() extends AbstractUnaryOp
final case class NewActivation() extends AbstractOpWithOperands(1, 0)
final case class NewArray(numArguments: Int) extends AbstractOpWithOperands(1, 0) with OpWithArguments
final case class NewCatch(exceptionHandler: AbcExceptionHandler) extends AbstractOpWithOperands(1, 0)
final case class NewClass(nominalType: AbcNominalType) extends AbstractOpWithOperands(1, 1)
final case class NewFunction(function: AbcMethod) extends AbstractOpWithOperands(1, 0)
final case class NewObject(numArguments: Int) extends AbstractOpWithOperands(1, numArguments * 2)
final case class NextName() extends AbstractOpWithOperands(1, 2)
final case class NextValue() extends AbstractOpWithOperands(1, 2)
final case class Nop() extends AbstractOp
final case class Not() extends AbstractUnaryOp
final case class Pop() extends AbstractOpWithOperands(0, 1)
final case class PopScope() extends AbstractOpWithScopes(0, 1)
final case class PushByte(value: Int) extends AbstractPushOp
final case class PushDouble(value: Double) extends AbstractPushOp
final case class PushFalse() extends AbstractPushOp
final case class PushInt(value: Int) extends AbstractPushOp
final case class PushNamespace(value: AbcNamespace) extends AbstractPushOp
final case class PushNaN() extends AbstractPushOp
final case class PushNull() extends AbstractPushOp
final case class PushScope() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
final case class PushShort(value: Int) extends AbstractPushOp
final case class PushString(value: Symbol) extends AbstractPushOp
final case class PushTrue() extends AbstractPushOp
final case class PushUInt(value: Long) extends AbstractPushOp
final case class PushUndefined() extends AbstractPushOp
final case class PushWith() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
final case class ReturnValue() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
final case class ReturnVoid() extends AbstractOp with OpThatControlsFlow
final case class ShiftRight() extends AbstractBinaryOp
final case class SetLocal(register: Int) extends AbstractOpWithOperands(0, 1) with OpWithRegister
final case class SetGlobalSlot(slot: Int) extends AbstractOpWithOperands(0, 1) with OpWithSlot
final case class SetProperty(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
final case class SetSlot(slot: Int) extends AbstractOpWithOperands(0, 2) with OpWithSlot with OpThatCanThrow
final case class SetSuper(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
final case class StrictEquals() extends AbstractBinaryOp
final case class Subtract() extends AbstractBinaryOp
final case class SubtractInt() extends AbstractBinaryOp
final case class Swap() extends AbstractOpWithOperands(2, 2)
final case class Throw() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
final case class TypeOf() extends AbstractOpWithOperands(1, 1)
final case class ShiftRightUnsigned() extends AbstractBinaryOp
final case class SetByte() extends AbstractAlchemySetOp
final case class SetShort() extends AbstractAlchemySetOp
final case class SetInt() extends AbstractAlchemySetOp
final case class SetFloat() extends AbstractAlchemySetOp
final case class SetDouble() extends AbstractAlchemySetOp
final case class GetByte() extends AbstractAlchemyGetOp
final case class GetShort() extends AbstractAlchemyGetOp
final case class GetInt() extends AbstractAlchemyGetOp
final case class GetFloat() extends AbstractAlchemyGetOp
final case class GetDouble() extends AbstractAlchemyGetOp
final case class Sign1() extends AbstractUnaryOp with AlchemyOp
final case class Sign8() extends AbstractUnaryOp with AlchemyOp
final case class Sign16() extends AbstractUnaryOp with AlchemyOp
