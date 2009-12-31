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

final case class Add() extends AbstractBinaryOp
final case class AddDouble() extends AbstractBinaryOp
final case class AddInt() extends AbstractBinaryOp
final case class ApplyType(val numArguments: Int) extends AbstractUnaryOp with OpWithArguments with OpThatCanThrow
final case class AsType(val typeName: AbcName) extends AbstractUnaryOp { require(!typeName.isRuntimeName) }
final case class AsTypeLate() extends AbstractOpWithOperands(1, 2)
final case class BitAnd() extends AbstractBinaryOp
final case class BitNot() extends AbstractUnaryOp
final case class BitOr() extends AbstractBinaryOp
final case class BitXor() extends AbstractBinaryOp
final case class Breakpoint() extends AbstractOp with DebugOp
final case class BreakpointLine() extends AbstractOp with DebugOp
final case class Call(val numArguments: Int) extends AbstractOpWithOperands(1, 2) with OpWithArguments with OpThatCanThrow
final case class CallMethod(val numArguments: Int, val method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
final case class CallProperty(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallPropLex(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallPropVoid(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallStatic(val numArguments: Int, val method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
final case class CallSuper(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CallSuperVoid(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class CheckFilter() extends AbstractOpWithOperands(1, 1) with OpThatCanThrow
final case class Coerce(val typeName: AbcName) extends AbstractUnaryOp with OpThatCanThrow { require(!typeName.isRuntimeName) }
final case class CoerceAny() extends AbstractUnaryOp
final case class CoerceString() extends AbstractUnaryOp
final case class Construct(val numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow
final case class ConstructProp(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
final case class ConstructSuper(val numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpThatCanThrow
final case class ConvertBoolean() extends AbstractUnaryOp
final case class ConvertInt() extends AbstractUnaryOp
final case class ConvertDouble() extends AbstractUnaryOp
final case class ConvertObject() extends AbstractUnaryOp with OpThatCanThrow
final case class ConvertUInt() extends AbstractUnaryOp
final case class ConvertString() extends AbstractUnaryOp
final case class Debug(val kind: Int, val name: Symbol, val register: Int, val extra: Int) extends AbstractOp with OpWithRegister with DebugOp
final case class DebugFile(val file: Symbol) extends AbstractOp with DebugOp
final case class DebugLine(val line: Int) extends AbstractOp with DebugOp
final case class DecLocal(val register: Int) extends AbstractOp with OpWithRegister
final case class DecLocalInt(val register: Int) extends AbstractOp with OpWithRegister
final case class Decrement() extends AbstractUnaryOp
final case class DecrementInt() extends AbstractUnaryOp
final case class DeleteProperty(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
final case class Divide() extends AbstractBinaryOp
final case class Dup() extends AbstractOpWithOperands(2, 1)
final case class DefaultXMLNamespace() extends AbstractOp with OpThatCanThrow
final case class DefaultXMLNamespaceLate() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow
final case class Equals() extends AbstractBinaryOp
final case class EscapeXMLAttribute() extends AbstractOpWithOperands(1, 1)
final case class EscapeXMLElement() extends AbstractOpWithOperands(1, 1)
final case class FindProperty(val property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty
final case class FindPropStrict(val property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty with OpThatCanThrow
final case class GetDescendants(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
final case class GetGlobalScope() extends AbstractOpWithOperands(1, 0)
final case class GetGlobalSlot(val slot: Int) extends AbstractOpWithOperands(1, 0) with OpWithSlot
final case class GetLex(val typeName: AbcName) extends AbstractOpWithOperands(1, 0) with OpThatCanThrow { require(!typeName.isRuntimeName) }
final case class GetLocal(val register: Int) extends AbstractOpWithOperands(1, 0) with OpWithRegister
final case class GetProperty(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty
final case class GetScopeObject(val index: Int) extends AbstractOpWithOperands(1, 0)
final case class GetSlot(val slot: Int) extends AbstractOpWithOperands(1, 1) with OpWithSlot with OpThatCanThrow
final case class GetSuper(val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow
final case class GreaterEquals() extends AbstractBinaryOp
final case class GreaterThan() extends AbstractBinaryOp
final case class HasNext() extends AbstractOpWithOperands(1, 2)
final case class HasNext2(val objectRegister: Int, val indexRegister: Int) extends AbstractOpWithOperands(1, 0)//TODO uses two local registers?!!
final case class IfEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfFalse(val marker: Marker) extends AbstractConditionalUnaryOp
final case class IfGreaterEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfGreaterThan(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfLessEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfLessThan(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotGreaterEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotGreaterThan(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotLessEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotLessThan(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfNotEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfStrictEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfStrictNotEqual(val marker: Marker) extends AbstractConditionalBinaryOp
final case class IfTrue(val marker: Marker) extends AbstractConditionalUnaryOp
final case class In() extends AbstractOpWithOperands(1, 2)
final case class IncLocal(val register: Int) extends AbstractOp with OpWithRegister
final case class IncLocalInt(val register: Int) extends AbstractOp with OpWithRegister
final case class Increment() extends AbstractUnaryOp
final case class IncrementInt() extends AbstractUnaryOp
final case class InitProperty(val property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
final case class InstanceOf() extends AbstractBinaryOp with OpThatCanThrow
final case class IsType(val typeName: AbcName) extends AbstractOpWithOperands(1, 1) { require(!typeName.isRuntimeName) }
final case class IsTypeLate() extends AbstractOpWithOperands(1, 2) with OpThatCanThrow
final case class Jump(val marker: Marker) extends AbstractOp with OpWithMarker with OpThatControlsFlow
final case class Kill(val register: Int) extends AbstractOp with OpWithRegister
final case class Label() extends AbstractOp
final case class LessEquals() extends AbstractBinaryOp
final case class LessThan() extends AbstractBinaryOp
final case class LookupSwitch(val defaultCase: Marker, val cases: Array[Marker]) extends AbstractOpWithOperands(0, 1) with OpThatControlsFlow
final case class ShiftLeft() extends AbstractBinaryOp
final case class Modulo() extends AbstractBinaryOp
final case class Multiply() extends AbstractBinaryOp
final case class MultiplyInt() extends AbstractBinaryOp
final case class Negate() extends AbstractUnaryOp
final case class NegateInt() extends AbstractUnaryOp
final case class NewActivation() extends AbstractOpWithOperands(1, 0)
final case class NewArray(val numArguments: Int) extends AbstractOpWithOperands(1, 0) with OpWithArguments
final case class NewCatch(val exceptionHandler: AbcExceptionHandler) extends AbstractOpWithOperands(1, 0)
final case class NewClass(val nominalType: AbcNominalType) extends AbstractOpWithOperands(1, 1)
final case class NewFunction(val function: AbcMethod) extends AbstractOpWithOperands(1, 0)
final case class NewObject(val numArguments: Int) extends AbstractOpWithOperands(1, numArguments * 2)
final case class NextName() extends AbstractOpWithOperands(1, 2)
final case class NextValue() extends AbstractOpWithOperands(1, 2)
final case class Nop() extends AbstractOp
final case class Not() extends AbstractUnaryOp
final case class Pop() extends AbstractOpWithOperands(0, 1)
final case class PopScope() extends AbstractOpWithScopes(0, 1)
final case class PushByte(val value: Int) extends AbstractPushOp
final case class PushDouble(val value: Double) extends AbstractPushOp
final case class PushFalse() extends AbstractPushOp
final case class PushInt(val value: Int) extends AbstractPushOp
final case class PushNamespace(val value: AbcNamespace) extends AbstractPushOp
final case class PushNaN() extends AbstractPushOp
final case class PushNull() extends AbstractPushOp
final case class PushScope() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
final case class PushShort(val value: Int) extends AbstractPushOp
final case class PushString(val value: Symbol) extends AbstractPushOp
final case class PushTrue() extends AbstractPushOp
final case class PushUInt(val value: Long) extends AbstractPushOp
final case class PushUndefined() extends AbstractPushOp
final case class PushWith() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow { override def popOperands = 1 }
final case class ReturnValue() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
final case class ReturnVoid() extends AbstractOp with OpThatControlsFlow
final case class ShiftRight() extends AbstractBinaryOp
final case class SetLocal(val register: Int) extends AbstractOpWithOperands(0, 1) with OpWithRegister
final case class SetGlobalSlot(val slot: Int) extends AbstractOpWithOperands(0, 1) with OpWithSlot
final case class SetProperty(val property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
final case class SetSlot(val slot: Int) extends AbstractOpWithOperands(0, 2) with OpWithSlot with OpThatCanThrow
final case class SetSuper(val property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow
final case class StrictEquals() extends AbstractBinaryOp
final case class Subtract() extends AbstractBinaryOp
final case class SubtractInt() extends AbstractBinaryOp
final case class Swap() extends AbstractOpWithOperands(2, 2)
final case class Throw() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow
final case class TypeOf() extends AbstractOpWithOperands(1, 1)
final case class ShiftRightUnsigned() extends AbstractBinaryOp
