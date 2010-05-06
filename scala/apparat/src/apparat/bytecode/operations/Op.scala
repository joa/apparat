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
import apparat.bytecode._

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
	val findproperty = 0x5e
	val finddef = 0x5f
	val getlex = 0x60

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

	/**@deprecated use getglobalscope+getslot */
	val getglobalslot = 0x6e

	/**@deprecated use getglobalscope+setslot */
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

	/**@deprecated use OP_convert_b */
	val coerce_b = 0x81
	val coerce_a = 0x82

	/**@deprecated use OP_convert_i */
	val coerce_i = 0x83

	/**@deprecated use OP_convert_d */
	val coerce_d = 0x84
	val coerce_s = 0x85
	val astype = 0x86
	val astypelate = 0x87

	/**@deprecated use OP_convert_u */
	val coerce_u = 0x88
	val coerce_o = 0x89

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
	val bkptline = 0xf2
	val timestamp = 0xf3

	def copyConditionalOp(op: AbstractOp, marker: Marker) = {
		op match {
			case op: IfEqual => op.copy(marker)
			case op: IfFalse => op.copy(marker)
			case op: IfGreaterEqual => op.copy(marker)
			case op: IfGreaterThan => op.copy(marker)
			case op: IfLessEqual => op.copy(marker)
			case op: IfLessThan => op.copy(marker)
			case op: IfNotGreaterEqual => op.copy(marker)
			case op: IfNotGreaterThan => op.copy(marker)
			case op: IfNotLessEqual => op.copy(marker)
			case op: IfNotLessThan => op.copy(marker)
			case op: IfNotEqual => op.copy(marker)
			case op: IfStrictEqual => op.copy(marker)
			case op: IfStrictNotEqual => op.copy(marker)
			case op: IfTrue => op.copy(marker)
			case op: Jump => op.copy(marker)
			case _ => error(op + " is not a conditional operation")
		}
	}

	def invertCopyConditionalOp(op: AbstractOp, marker: Marker) = {
		op match {
			case op: IfEqual => IfNotEqual(marker)
			case op: IfFalse => IfTrue(marker)
			case op: IfGreaterEqual => IfLessThan(marker)
			case op: IfGreaterThan => IfLessEqual(marker)
			case op: IfLessEqual => IfGreaterThan(marker)
			case op: IfLessThan => IfGreaterEqual(marker)
			case op: IfNotGreaterEqual => IfGreaterEqual(marker)
			case op: IfNotGreaterThan => IfGreaterThan(marker)
			case op: IfNotLessEqual => IfLessEqual(marker)
			case op: IfNotLessThan => IfLessThan(marker)
			case op: IfNotEqual => IfEqual(marker)
			case op: IfStrictEqual => IfStrictNotEqual(marker)
			case op: IfStrictNotEqual => IfStrictEqual(marker)
			case op: IfTrue => IfFalse(marker)
			case op: Jump => op.copy(marker)
			case _ => error(op + " is not a conditional operation")
		}
	}

}

sealed abstract class AbstractOp extends OpCode with Product {
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

	def ~==(that: AbstractOp): Boolean = {
		if (opCode == that.opCode && productArity == that.productArity) {
			var i = 0
			val n = productArity

			while(i < n) {
				if(this.productElement(i) != that.productElement(i)) {
					return false
				}

				i += 1
			}
			
			true
		} else {
			false
		}
	}
}

trait OpCode {
	def opCode: Int
}

trait DebugOp

trait AlchemyOp

trait OpThatCanThrow extends AbstractOp {final override def canThrow = true}

trait OpThatControlsFlow extends AbstractOp {final override def controlsFlow = true}

trait OpThatReturns extends OpThatControlsFlow

trait OpWithRegister {def register: Int}

trait OpWithSlot {def slot: Int}

trait OpWithMethod {def method: AbcMethod}

trait OpWithMarker {def marker: Marker}

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
sealed abstract class AbstractConditionalBinaryOp extends AbstractConditionalOp(0, 2)
sealed abstract class AbstractConditionalUnaryOp extends AbstractConditionalOp(0, 1)
sealed abstract class AbstractPushOp extends AbstractOpWithOperands(1, 0)
sealed abstract class AbstractAlchemySetOp extends AbstractOpWithOperands(0, 2) with AlchemyOp
sealed abstract class AbstractAlchemyGetOp extends AbstractOpWithOperands(1, 1) with AlchemyOp

case class Add() extends AbstractBinaryOp {final override def opCode = Op.add}
case class AddInt() extends AbstractBinaryOp {final override def opCode = Op.add_i}
case class ApplyType(numArguments: Int) extends AbstractUnaryOp with OpWithArguments with OpThatCanThrow {final override def opCode = Op.applytype}
case class AsType(typeName: AbcName) extends AbstractUnaryOp {
	require(!typeName.isRuntimeName)
	final override def opCode = Op.astype
}
case class AsTypeLate() extends AbstractOpWithOperands(1, 2) {final override def opCode = Op.astypelate}
case class BitAnd() extends AbstractBinaryOp {final override def opCode = Op.bitand}
case class BitNot() extends AbstractUnaryOp {final override def opCode = Op.bitnot}
case class BitOr() extends AbstractBinaryOp {final override def opCode = Op.bitor}
case class BitXor() extends AbstractBinaryOp {final override def opCode = Op.bitxor}
case class Breakpoint() extends AbstractOp with DebugOp {final override def opCode = Op.bkpt}
case class BreakpointLine() extends AbstractOp with DebugOp {final override def opCode = Op.bkptline}
case class Call(numArguments: Int) extends AbstractOpWithOperands(1, 2) with OpWithArguments with OpThatCanThrow {final override def opCode = Op.call}
case class CallMethod(methodIndex: Int, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow {final override def opCode = Op.callmethod}
case class CallProperty(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow {final override def opCode = Op.callproperty}
case class CallPropLex(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow {final override def opCode = Op.callproplex}
case class CallPropVoid(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow {final override def opCode = Op.callpropvoid}
case class CallStatic(method: AbcMethod, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow {final override def opCode = Op.callstatic}
case class CallSuper(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow {final override def opCode = Op.callsuper}
case class CallSuperVoid(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow {final override def opCode = Op.callsupervoid}
case class CheckFilter() extends AbstractOpWithOperands(1, 1) with OpThatCanThrow {final override def opCode = Op.checkfilter}
case class Coerce(typeName: AbcName) extends AbstractUnaryOp with OpThatCanThrow {
	require(!typeName.isRuntimeName)
	final override def opCode = Op.coerce
}
case class CoerceAny() extends AbstractUnaryOp {final override def opCode = Op.coerce_a}
case class CoerceBoolean() extends AbstractUnaryOp {final override def opCode = Op.coerce_b}
case class CoerceDouble() extends AbstractUnaryOp {final override def opCode = Op.coerce_d}
case class CoerceInt() extends AbstractUnaryOp {final override def opCode = Op.coerce_i}
case class CoerceObject() extends AbstractUnaryOp {final override def opCode = Op.coerce_o}
case class CoerceString() extends AbstractUnaryOp {final override def opCode = Op.coerce_s}
case class CoerceUInt() extends AbstractUnaryOp {final override def opCode = Op.coerce_u}
case class Construct(numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpThatCanThrow {final override def opCode = Op.construct}
case class ConstructProp(property: AbcName, numArguments: Int) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow {final override def opCode = Op.constructprop}
case class ConstructSuper(numArguments: Int) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpThatCanThrow {final override def opCode = Op.constructsuper}
case class ConvertBoolean() extends AbstractUnaryOp {final override def opCode = Op.convert_b}
case class ConvertInt() extends AbstractUnaryOp {final override def opCode = Op.convert_i}
case class ConvertDouble() extends AbstractUnaryOp {final override def opCode = Op.convert_d}
case class ConvertObject() extends AbstractUnaryOp with OpThatCanThrow {final override def opCode = Op.convert_o}
case class ConvertString() extends AbstractUnaryOp {final override def opCode = Op.convert_s}
case class ConvertUInt() extends AbstractUnaryOp {final override def opCode = Op.convert_u}
case class Debug(kind: Int, name: Symbol, register: Int, extra: Int) extends AbstractOp with OpWithRegister with DebugOp {final override def opCode = Op.debug}
case class DebugFile(file: Symbol) extends AbstractOp with DebugOp {final override def opCode = Op.debugfile}
case class DebugLine(line: Int) extends AbstractOp with DebugOp {final override def opCode = Op.debugline}
case class DecLocal(register: Int) extends AbstractOp with OpWithRegister {final override def opCode = Op.declocal}
case class DecLocalInt(register: Int) extends AbstractOp with OpWithRegister {final override def opCode = Op.declocal_i}
case class Decrement() extends AbstractUnaryOp {final override def opCode = Op.decrement}
case class DecrementInt() extends AbstractUnaryOp {final override def opCode = Op.decrement_i}
case class DeleteProperty(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.deleteproperty}
case class Divide() extends AbstractBinaryOp {final override def opCode = Op.divide}
case class Dup() extends AbstractOpWithOperands(2, 1) {final override def opCode = Op.dup}
case class DefaultXMLNamespace(uri: Symbol) extends AbstractOp with OpThatCanThrow {final override def opCode = Op.dxns}
case class DefaultXMLNamespaceLate() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow {final override def opCode = Op.dxnslate}
case class Equals() extends AbstractBinaryOp {final override def opCode = Op.equals}
case class EscapeXMLAttribute() extends AbstractOpWithOperands(1, 1) {final override def opCode = Op.esc_xattr}
case class EscapeXMLElement() extends AbstractOpWithOperands(1, 1) {final override def opCode = Op.esc_xelem}
case class FindProperty(property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty {final override def opCode = Op.findproperty}
case class FindPropStrict(property: AbcName) extends AbstractOpWithOperands(1, 0) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.findpropstrict}
case class GetDescendants(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.getdescendants}
case class GetGlobalScope() extends AbstractOpWithOperands(1, 0) {final override def opCode = Op.getglobalscope}
case class GetGlobalSlot(slot: Int) extends AbstractOpWithOperands(1, 0) with OpWithSlot {final override def opCode = Op.getglobalslot}
case class GetLex(typeName: AbcName) extends AbstractOpWithOperands(1, 0) with OpThatCanThrow {
	require(!typeName.isRuntimeName)
	final override def opCode = Op.getlex
}
case class GetLocal(register: Int) extends AbstractOpWithOperands(1, 0) with OpWithRegister {
	final override def opCode = register match {
		case 0 => Op.getlocal0
		case 1 => Op.getlocal1
		case 2 => Op.getlocal2
		case 3 => Op.getlocal3
		case _ => Op.getlocal
	}
}
case class GetProperty(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty {final override def opCode = Op.getproperty}
case class GetScopeObject(index: Int) extends AbstractOpWithOperands(1, 0) {final override def opCode = Op.getscopeobject}
case class GetSlot(slot: Int) extends AbstractOpWithOperands(1, 1) with OpWithSlot with OpThatCanThrow {final override def opCode = Op.getslot}
case class GetSuper(property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.getsuper}
case class GreaterEquals() extends AbstractBinaryOp {final override def opCode = Op.greaterequals}
case class GreaterThan() extends AbstractBinaryOp {final override def opCode = Op.greaterthan}
case class HasNext() extends AbstractOpWithOperands(1, 2) {final override def opCode = Op.hasnext}
case class HasNext2(objectRegister: Int, indexRegister: Int) extends AbstractOpWithOperands(1, 0) {final override def opCode = Op.hasnext2} //TODO uses two local registers?!!
case class IfEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifeq}
case class IfFalse(marker: Marker) extends AbstractConditionalUnaryOp {final override def opCode = Op.iffalse}
case class IfGreaterEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifge}
case class IfGreaterThan(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifgt}
case class IfLessEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifle}
case class IfLessThan(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.iflt}
case class IfNotGreaterEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifnge}
case class IfNotGreaterThan(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifngt}
case class IfNotLessEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifnle}
case class IfNotLessThan(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifnlt}
case class IfNotEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifne}
case class IfStrictEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifstricteq}
case class IfStrictNotEqual(marker: Marker) extends AbstractConditionalBinaryOp {final override def opCode = Op.ifstrictne}
case class IfTrue(marker: Marker) extends AbstractConditionalUnaryOp {final override def opCode = Op.iftrue}
case class In() extends AbstractOpWithOperands(1, 2) {final override def opCode = Op.in}
case class IncLocal(register: Int) extends AbstractOp with OpWithRegister {final override def opCode = Op.inclocal}
case class IncLocalInt(register: Int) extends AbstractOp with OpWithRegister {final override def opCode = Op.inclocal_i}
case class Increment() extends AbstractUnaryOp {final override def opCode = Op.increment}
case class IncrementInt() extends AbstractUnaryOp {final override def opCode = Op.increment_i}
case class InitProperty(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.initproperty}
case class InstanceOf() extends AbstractBinaryOp with OpThatCanThrow {final override def opCode = Op.instanceof}
case class IsType(typeName: AbcName) extends AbstractOpWithOperands(1, 1) {
	require(!typeName.isRuntimeName)
	final override def opCode = Op.istype
}
case class IsTypeLate() extends AbstractOpWithOperands(1, 2) with OpThatCanThrow {final override def opCode = Op.istypelate}
case class Jump(marker: Marker) extends AbstractOp with OpWithMarker with OpThatControlsFlow {final override def opCode = Op.jump}
case class Kill(register: Int) extends AbstractOp with OpWithRegister {final override def opCode = Op.kill}
case class Label() extends AbstractOp {final override def opCode = Op.label}
case class LessEquals() extends AbstractBinaryOp {final override def opCode = Op.lessequals}
case class LessThan() extends AbstractBinaryOp {final override def opCode = Op.lessthan}
case class LookupSwitch(defaultCase: Marker, cases: Array[Marker]) extends AbstractOpWithOperands(0, 1) with OpThatControlsFlow {final override def opCode = Op.lookupswitch}
case class ShiftLeft() extends AbstractBinaryOp {final override def opCode = Op.lshift}
case class Modulo() extends AbstractBinaryOp {final override def opCode = Op.modulo}
case class Multiply() extends AbstractBinaryOp {final override def opCode = Op.multiply}
case class MultiplyInt() extends AbstractBinaryOp {final override def opCode = Op.multiply_i}
case class Negate() extends AbstractUnaryOp {final override def opCode = Op.negate}
case class NegateInt() extends AbstractUnaryOp {final override def opCode = Op.negate_i}
case class NewActivation() extends AbstractOpWithOperands(1, 0) {final override def opCode = Op.newactivation}
case class NewArray(numArguments: Int) extends AbstractOpWithOperands(1, 0) with OpWithArguments {final override def opCode = Op.newarray}
case class NewCatch(exceptionHandler: BytecodeExceptionHandler) extends AbstractOpWithOperands(1, 0) {final override def opCode = Op.newcatch}
case class NewClass(nominalType: AbcNominalType) extends AbstractOpWithOperands(1, 1) {final override def opCode = Op.newclass}
case class NewFunction(function: AbcMethod) extends AbstractOpWithOperands(1, 0) {final override def opCode = Op.newfunction}
case class NewObject(numArguments: Int) extends AbstractOpWithOperands(1, numArguments * 2) {final override def opCode = Op.newobject}
case class NextName() extends AbstractOpWithOperands(1, 2) {final override def opCode = Op.nextname}
case class NextValue() extends AbstractOpWithOperands(1, 2) {final override def opCode = Op.nextvalue}
case class Nop() extends AbstractOp {final override def opCode = Op.nop}
case class Not() extends AbstractUnaryOp {final override def opCode = Op.not}
case class Pop() extends AbstractOpWithOperands(0, 1) {final override def opCode = Op.pop}
case class PopScope() extends AbstractOpWithScopes(0, 1) {final override def opCode = Op.popscope}
case class PushByte(value: Int) extends AbstractPushOp {final override def opCode = Op.pushbyte}
case class PushDouble(value: Double) extends AbstractPushOp {final override def opCode = Op.pushdouble}
case class PushFalse() extends AbstractPushOp {final override def opCode = Op.pushfalse}
case class PushInt(value: Int) extends AbstractPushOp {final override def opCode = Op.pushint}
case class PushNamespace(value: AbcNamespace) extends AbstractPushOp {final override def opCode = Op.pushnamespace}
case class PushNaN() extends AbstractPushOp {final override def opCode = Op.pushnan}
case class PushNull() extends AbstractPushOp {final override def opCode = Op.pushnull}
case class PushScope() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow {
	override def popOperands = 1

	final override def opCode = Op.pushscope
}
case class PushShort(value: Int) extends AbstractPushOp {final override def opCode = Op.pushshort}
case class PushString(value: Symbol) extends AbstractPushOp {final override def opCode = Op.pushstring}
case class PushTrue() extends AbstractPushOp {final override def opCode = Op.pushtrue}
case class PushUInt(value: Long) extends AbstractPushOp {final override def opCode = Op.pushuint}
case class PushUndefined() extends AbstractPushOp {final override def opCode = Op.pushundefined}
case class PushWith() extends AbstractOpWithScopes(1, 0) with OpThatCanThrow {
	override def popOperands = 1

	final override def opCode = Op.pushwith
}
case class ReturnValue() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatReturns {final override def opCode = Op.returnvalue}
case class ReturnVoid() extends AbstractOp with OpThatReturns {final override def opCode = Op.returnvoid}
case class ShiftRight() extends AbstractBinaryOp {final override def opCode = Op.rshift}
case class SetLocal(register: Int) extends AbstractOpWithOperands(0, 1) with OpWithRegister {
	final override def opCode = register match {
		case 0 => Op.setlocal0
		case 1 => Op.setlocal1
		case 2 => Op.setlocal2
		case 3 => Op.setlocal3
		case _ => Op.setlocal
	}
}
case class SetGlobalSlot(slot: Int) extends AbstractOpWithOperands(0, 1) with OpWithSlot {final override def opCode = Op.setglobalslot}
case class SetProperty(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.setproperty}
case class SetSlot(slot: Int) extends AbstractOpWithOperands(0, 2) with OpWithSlot with OpThatCanThrow {final override def opCode = Op.setslot}
case class SetSuper(property: AbcName) extends AbstractOpWithOperands(0, 2) with OpWithProperty with OpThatCanThrow {final override def opCode = Op.setsuper}
case class StrictEquals() extends AbstractBinaryOp {final override def opCode = Op.strictequals}
case class Subtract() extends AbstractBinaryOp {final override def opCode = Op.subtract}
case class SubtractInt() extends AbstractBinaryOp {final override def opCode = Op.subtract_i}
case class Swap() extends AbstractOpWithOperands(2, 2) {final override def opCode = Op.swap}
case class Throw() extends AbstractOpWithOperands(0, 1) with OpThatCanThrow with OpThatControlsFlow {final override def opCode = Op.`throw`}
case class TypeOf() extends AbstractOpWithOperands(1, 1) {final override def opCode = Op.typeof}
case class ShiftRightUnsigned() extends AbstractBinaryOp {final override def opCode = Op.urshift}
case class SetByte() extends AbstractAlchemySetOp {final override def opCode = Op.si8}
case class SetShort() extends AbstractAlchemySetOp {final override def opCode = Op.si16}
case class SetInt() extends AbstractAlchemySetOp {final override def opCode = Op.si32}
case class SetFloat() extends AbstractAlchemySetOp {final override def opCode = Op.sf32}
case class SetDouble() extends AbstractAlchemySetOp {final override def opCode = Op.sf64}
case class GetByte() extends AbstractAlchemyGetOp {final override def opCode = Op.li8}
case class GetShort() extends AbstractAlchemyGetOp {final override def opCode = Op.li16}
case class GetInt() extends AbstractAlchemyGetOp {final override def opCode = Op.li32}
case class GetFloat() extends AbstractAlchemyGetOp {final override def opCode = Op.lf32}
case class GetDouble() extends AbstractAlchemyGetOp {final override def opCode = Op.lf64}
case class Sign1() extends AbstractUnaryOp with AlchemyOp {final override def opCode = Op.sxi1}
case class Sign8() extends AbstractUnaryOp with AlchemyOp {final override def opCode = Op.sxi8}
case class Sign16() extends AbstractUnaryOp with AlchemyOp {final override def opCode = Op.sxi16}
