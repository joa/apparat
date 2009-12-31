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

object Op {
	def byteOf(op: AbstractOp): Byte = op match {
		case _ => error("Unknown operation " + op)
	}
}

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

trait OpThatCanThrow extends AbstractOp { final override def canThrow = true }

trait OpThatControlsFlow extends AbstractOp { final override def controlsFlow = true }

trait OpWithRegister {
	self: AbstractOp =>
	def register: Int
}

trait OpWithSlot {
	self: AbstractOp =>
	def slot: Int
}

trait DebugOp {
	self: AbstractOp =>
}

trait OpWithMethod {
	self: AbstractOp =>
	def method: AbcMethod
}

trait OpWithMarker {
	self: AbstractOp =>
	def marker: Marker
}

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

case class Add() extends AbstractBinaryOp
case class AddDouble() extends AbstractBinaryOp
case class AddInt() extends AbstractBinaryOp
case class ApplyType(val numArguments: Int) extends AbstractUnaryOp with OpWithArguments with OpThatCanThrow
case class AsType(val typeName: AbcName) extends AbstractUnaryOp { require(!typeName.isRuntimeName) }
case class AsTypeLate() extends AbstractOpWithOperands(1, 2)
case class BitAnd() extends AbstractBinaryOp
case class BitNot() extends AbstractUnaryOp
case class BitOr() extends AbstractBinaryOp
case class BitXor() extends AbstractBinaryOp
case class Breakpoint() extends AbstractOp with DebugOp
case class BreakpointLine() extends AbstractOp with DebugOp
case class Call(val numArguments: Int) extends AbstractOpWithOperands(1, 2) with OpWithArguments with OpThatCanThrow
case class CallMethod(val numArguments: Int, val method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallProperty(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropLex(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropVoid(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallStatic(val numArguments: Int, val method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallSuper(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallSuperVoid(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CheckFilter() extends AbstractOpWithOperands(1, 1) with OpThatCanThrow
case class Coerce(val typeName: AbcName) extends AbstractUnaryOp with OpThatCanThrow { require(!typeName.isRuntimeName) }
case class CoerceAny() extends AbstractUnaryOp
case class CoerceString() extends AbstractUnaryOp
case class Construct(val numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow
case class ConstructProp(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class ConstructSuper(val numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpThatCanThrow
case class ConvertBoolean() extends AbstractUnaryOp
case class ConvertInt() extends AbstractUnaryOp
case class ConvertDouble() extends AbstractUnaryOp
case class ConvertObject() extends AbstractUnaryOp with OpThatCanThrow
case class ConvertUInt() extends AbstractUnaryOp
case class ConvertString() extends AbstractUnaryOp
case class Debug(val kind: Int, val name: Symbol, val register: Int, val extra: Int) extends AbstractOp with OpWithRegister with DebugOp
case class DebugFile(val file: Symbol) extends AbstractOp with DebugOp
case class DebugLine(val line: Int) extends AbstractOp with DebugOp
case class DecLocal(val register: Int) extends AbstractOp with OpWithRegister
case class DecLocalInt(val register: Int) extends AbstractOp with OpWithRegister
case class Decrement() extends AbstractUnaryOp
case class DecrementInt() extends AbstractUnaryOp
case class DeleteProperty(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
case class Divide() extends AbstractBinaryOp
case class Dup() extends AbstractOpWithOperands(2, 1)
case class DefaultXMLNamespace() extends AbstractOp with OpThatCanThrow
case class DefaultXMLNamespaceLate() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow
case class Equals() extends AbstractBinaryOp
case class EscapeXMLAttribute() extends AbstractOpWithOperands(1, 1)
case class EscapeXMLElement() extends AbstractOpWithOperands(1, 1)
case class FindProperty(val property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty
case class FindPropStrict(val property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty with OpThatCanThrow
case class GetDescendants(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
case class GetGlobalScope() extends AbstractOpWithOperands(1, 0)
case class GetGlobalSlot(val slot: Int) extends AbstractOpWithOperands(1, 0) with OpWithSlot
case class GetLex(val typeName: AbcName) extends AbstractOpWithOperands(1, 0) with OpThatCanThrow { require(!typeName.isRuntimeName) }
case class GetLocal(val register: Int) extends AbstractOpWithOperands(1, 0) with OpWithRegister
case class GetProperty(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty
case class GetScopeObject(val index: Int) extends AbstractOpWithOperands(1, 0)
case class GetSlot(val slot: Int) extends AbstractOpWithOperands(1, 1) with OpWithSlot with OpThatCanThrow
case class GetSuper(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
case class GreaterEquals() extends AbstractBinaryOp
case class GreaterThan() extends AbstractBinaryOp
case class HasNext() extends AbstractOpWithOperands(1, 2)
case class HasNext2(val objectRegister: Int, val indexRegister: Int) extends AbstractOpWithOperands(1, 0)//TODO uses two local registers?!!
case class IfEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfFalse(val marker: Marker) extends AbstractConditionalUnaryOp
case class IfGreaterEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfGreaterThan(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfLessEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfLessThan(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotGreaterEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotGreaterThan(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotLessEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotLessThan(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfNotEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfStrictEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfStrictNotEqual(val marker: Marker) extends AbstractConditionalBinaryOp
case class IfTrue(val marker: Marker) extends AbstractConditionalUnaryOp
case class In() extends AbstractOpWithOperands(1, 2)
case class IncLocal(val register: Int) extends AbstractOp with OpWithRegister
case class IncLocalInt(val register: Int) extends AbstractOp with OpWithRegister
case class Increment() extends AbstractUnaryOp
case class IncrementInt() extends AbstractUnaryOp
case class InitProperty(val property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
case class InstanceOf() extends AbstractBinaryOp with OpThatCanThrow
case class IsType(val typeName: AbcName) extends AbstractOpWithOperands(1, 1) { require(!typeName.isRuntimeName) }
case class IsTypeLate() extends AbstractOpWithOperands(1, 2) with OpThatCanThrow
case class Jump(val marker: Marker) extends AbstractOp with OpWithMarker with OpThatControlsFlow
case class Kill(val register: Int) extends AbstractOp with OpWithRegister
case class Label() extends AbstractOp
case class LessEquals() extends AbstractBinaryOp
case class LessThan() extends AbstractBinaryOp
case class LookupSwitch(val defaultCase: Marker, val cases: Array[Marker]) extends AbstractOpWithOperands(0, 1) with OpThatControlsFlow
case class ShiftLeft() extends AbstractBinaryOp
case class Modulo() extends AbstractBinaryOp
case class Multiply() extends AbstractBinaryOp
case class MultiplyInt() extends AbstractBinaryOp
case class Negate() extends AbstractUnaryOp
case class NegateInt() extends AbstractUnaryOp
case class NewActivation() extends AbstractOpWithOperands(1, 0)
case class NewArray(val numArguments: Int) extends AbstractOpWithOperands(1, 0) with OpWithArguments
case class NewCatch(val exceptionHandler: AbcExceptionHandler) extends AbstractOpWithOperands(1, 0)
case class NewClass(val nominalType: AbcNominalType) extends AbstractOpWithOperands(1, 1)
case class NewFunction(val function: AbcMethod) extends AbstractOpWithOperands(1, 0)
case class NewObject(val numArguments: Int) extends AbstractOpWithOperands(1, numArguments * 2)
case class NextName() extends AbstractOpWithOperands(1, 2)
case class NextValue() extends AbstractOpWithOperands(1, 2)
case class Nop() extends AbstractOp
case class Not() extends AbstractUnaryOp
case class Pop() extends AbstractOpWithOperands(0, 1)
case class PopScope() extends AbstractOpWithScopes(0, 1)
case class PushByte(val value: Int) extends AbstractPushOp
case class PushDouble(val value: Double) extends AbstractPushOp
case class PushFalse() extends AbstractPushOp
case class PushInt(val value: Int) extends AbstractPushOp
case class PushNamespace(val value: AbcNamespace) extends AbstractPushOp
case class PushNaN() extends AbstractPushOp
case class PushNull() extends AbstractPushOp
case class PushScope() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
case class PushShort(val value: Int) extends AbstractPushOp
case class PushString(val value: Symbol) extends AbstractPushOp
case class PushTrue() extends AbstractPushOp
case class PushUInt(val value: Long) extends AbstractPushOp
case class PushUndefined() extends AbstractPushOp
case class PushWith() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
case class ReturnValue() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
case class ReturnVoid() extends AbstractOp with OpThatControlsFlow
case class ShiftRight() extends AbstractBinaryOp
case class SetLocal(val register: Int) extends AbstractOpWithOperands(0, 1) with OpWithRegister
case class SetGlobalSlot(val slot: Int) extends AbstractOpWithOperands(0, 1) with OpWithSlot
case class SetProperty(val property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
case class SetSlot(val slot: Int) extends AbstractOpWithOperands(0, 2) with OpWithSlot with OpThatCanThrow
case class SetSuper(val property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
case class StrictEquals() extends AbstractBinaryOp
case class Subtract() extends AbstractBinaryOp
case class SubtractInt() extends AbstractBinaryOp
case class Swap() extends AbstractOpWithOperands(2, 2)
case class Throw() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
case class TypeOf() extends AbstractOpWithOperands(1, 1)
case class ShiftRightUnsigned() extends AbstractBinaryOp
