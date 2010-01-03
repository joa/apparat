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
import apparat.bytecode.{BytecodeExceptionHandler, Marker}

object Op {
	val bkpt = 0x01
    val nop = 0x02
    val `throw` = 0x03
    val getsuper = 0x04
    val setsuper = 0x05
    val dxns = 0x06
    val dxnslate = 0x07
    val kill = 0x08
    val label = 0x09

    val ifnlt = 0x0c
    val ifnle = 0x0d
    val ifngt = 0x0e
    val ifnge = 0x0f
    val jump = 0x10
    val iftrue = 0x11
    val iffalse = 0x12
    val ifeq = 0x13
    val ifne = 0x14
    val iflt = 0x15
    val ifle = 0x16
    val ifgt = 0x17
    val ifge = 0x18
    val ifstricteq = 0x19
    val ifstrictne = 0x1a
    val lookupswitch = 0x1b
    val pushwith = 0x1c
    val popscope = 0x1d
    val nextname = 0x1e
    val hasnext = 0x1f

    val pushnull = 0x20
    val pushundefined = 0x21
    val pushuninitialized = 0x22
    val nextvalue = 0x23
    val pushbyte = 0x24
    val pushshort = 0x25
    val pushtrue = 0x26
    val pushfalse = 0x27
    val pushnan = 0x28
    val pop = 0x29
    val dup = 0x2a
    val swap = 0x2b
    val pushstring = 0x2c
    val pushint = 0x2d
    val pushuint = 0x2e
    val pushdouble = 0x2f
    val pushscope = 0x30
    val pushnamespace = 0x31
    val hasnext2 = 0x32
    val pushdecimal = 0x33
 	val pushdnan = 0x34
 	
 	val li8 = 0x35
	val li16 = 0x36
	val li32 = 0x37
	val lf32 = 0x38
	val lf64 = 0x39
	val si8 = 0x3A
	val si16 = 0x3B
	val si32 = 0x3C
	val sf32 = 0x3D
	val sf64 = 0x3E

    val newfunction = 0x40
    val call = 0x41
    val construct = 0x42
    val callmethod = 0x43
    val callstatic = 0x44
    val callsuper = 0x45
    val callproperty = 0x46
    val returnvoid = 0x47
    val returnvalue = 0x48
    val constructsuper = 0x49
    val constructprop = 0x4A
    val callproplex = 0x4C
    val callsupervoid = 0x4E
    val callpropvoid = 0x4F
    val sxi1 = 0x50
    val sxi8 = 0x51
    val sxi16 = 0x52
    val applytype = 0x53

    val newobject = 0x55
    val newarray = 0x56
    val newactivation = 0x57

    val newclass = 0x58
    val getdescendants = 0x59
    val newcatch = 0x5a
    val deldescendants = 0x5b

    val findpropstrict = 0x5d
    val findproperty   = 0x5e
    val finddef        = 0x5f
    val getlex          = 0x60

    val setproperty = 0x61
    val getlocal = 0x62
    val setlocal = 0x63

    val getglobalscope = 0x64
    val getscopeobject = 0x65
    val getproperty = 0x66
    val initproperty = 0x68
    val deleteproperty = 0x6a
    val getslot = 0x6c
    val setslot = 0x6d
    
    /** @deprecated use getglobalscope+getslot */
    val getglobalslot = 0x6e

    /** @deprecated use getglobalscope+setslot */
    val setglobalslot = 0x6f


    val convert_s = 0x70
    val esc_xelem = 0x71
    val esc_xattr = 0x72
    val convert_i = 0x73
    val convert_u = 0x74
    val convert_d = 0x75
    val convert_b = 0x76
    val convert_o = 0x77
    val checkfilter = 0x78
    val convert_m = 0x79
 	val convert_m_p = 0x7a

    val coerce = 0x80
    
    /** @deprecated use OP_convert_b */
    val coerce_b        = 0x81
    val coerce_a        = 0x82
    /** @deprecated use OP_convert_i */
    val coerce_i        = 0x83
    /** @deprecated use OP_convert_d */
    val coerce_d        = 0x84
    val coerce_s        = 0x85
    val astype          = 0x86
    val astypelate      = 0x87
    /** @deprecated use OP_convert_u */
    val coerce_u        = 0x88
	val coerce_o		   = 0x89

 	val negate_p = 0x8f
    val negate = 0x90
    val increment = 0x91
    val inclocal = 0x92
    val decrement = 0x93
    val declocal = 0x94
    val typeof = 0x95
    val not = 0x96
    val bitnot = 0x97

	val increment_p = 0x9c
	val inclocal_p = 0x9d
	val decrement_p = 0x9e
	val declocal_p = 0x9f
 
    val add = 0xa0
    val subtract = 0xa1
    val multiply = 0xa2
    val divide = 0xa3
    val modulo = 0xa4
    val lshift = 0xa5
    val rshift = 0xa6
    val urshift = 0xa7
    val bitand = 0xa8
    val bitor = 0xa9
    val bitxor = 0xaa
    val equals = 0xab
    val strictequals = 0xac
    val lessthan = 0xad
    val lessequals = 0xae
    val greaterthan = 0xaf

    val greaterequals = 0xb0
    val instanceof = 0xb1
    val istype = 0xb2
    val istypelate = 0xb3
    val in = 0xb4
    // arithmetic with decimal parameters
    val add_p = 0xb5
    val subtract_p = 0xb6
    val multiply_p = 0xb7
    val divide_p = 0xb8
    val modulo_p = 0xb9

    val increment_i = 0xc0
    val decrement_i = 0xc1
    val inclocal_i = 0xc2
    val declocal_i = 0xc3
    val negate_i = 0xc4
    val add_i = 0xc5
    val subtract_i = 0xc6
    val multiply_i = 0xc7

    val getlocal0 = 0xd0
    val getlocal1 = 0xd1
    val getlocal2 = 0xd2
    val getlocal3 = 0xd3    
    val setlocal0 = 0xd4
    val setlocal1 = 0xd5
    val setlocal2 = 0xd6
    val setlocal3 = 0xd7    
    
    val debug = 0xef

    val debugline = 0xf0
    val debugfile = 0xf1
    val bkptline  = 0xf2
    val timestamp = 0xf3
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
case class CallMethod(methodIndex: Int, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow
case class CallProperty(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropLex(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropVoid(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallStatic(method: AbcMethod, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallSuper(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallSuperVoid(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CheckFilter() extends AbstractOpWithOperands(1, 1) with OpThatCanThrow
case class Coerce(typeName: AbcName) extends AbstractUnaryOp with OpThatCanThrow { require(!typeName.isRuntimeName) }
case class CoerceAny() extends AbstractUnaryOp
case class CoerceBoolean() extends AbstractUnaryOp
case class CoerceDouble() extends AbstractUnaryOp
case class CoerceInt() extends AbstractUnaryOp
case class CoerceObject() extends AbstractUnaryOp
case class CoerceString() extends AbstractUnaryOp
case class CoerceUInt() extends AbstractUnaryOp
case class Construct(numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow
case class ConstructProp(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
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
case class DefaultXMLNamespace(uri: Symbol) extends AbstractOp with OpThatCanThrow
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
case class NewCatch(exceptionHandler: BytecodeExceptionHandler) extends AbstractOpWithOperands(1, 0)
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
