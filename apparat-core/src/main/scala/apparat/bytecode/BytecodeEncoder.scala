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
package apparat.bytecode

import apparat.abc.{Abc, AbcExceptionHandler, AbcName, AbcOutputStream, AbcOutputUtil}
import collection.immutable.TreeMap
import java.io.{ByteArrayOutputStream => JByteArrayOutputStream}
import operations._

object BytecodeEncoder {
	def apply(bytecode: Bytecode)(implicit abc: Abc) = {
		val byteArrayOutputStream = new JByteArrayOutputStream()
		val output = new AbcOutputStream(byteArrayOutputStream)
		val cpool = abc.cpool
		var patches = TreeMap[Int, AbstractOp]()
		val markers = bytecode.markers

		@inline def u08(value: Int) = output writeU08 value
		@inline def s08(value: Int) = output writeS08 value
		@inline def s24(value: Int) = output writeS24 value
		@inline def u30(value: Int) = output writeU30 value
		@inline def s30(value: Int) = output writeS30 value
		@inline def name(value: AbcName) = u30(cpool indexOf value)
		@inline def string(value: Symbol) = u30(cpool indexOf value)
		@inline def position = output.position
		@inline def patch(op: AbstractOp) = {
			patches = patches + ((position - 1) -> op)
			s24(0)
		}
		@inline def writeOp(op: AbstractOp) = {
			markers(op) match {
				case Some(marker) => marker.position = position
				case None =>
			}
			if (op.opCode<256) u08(op.opCode)
			op match {
				case Add() | AddInt() =>
				case ApplyType(numArguments) => u30(numArguments)
				case AsType(typeName) => name(typeName)
				case AsTypeLate() =>
				case BitAnd() | BitNot() | BitOr() | BitXor() =>
				case Breakpoint() | BreakpointLine() =>
				case BytecodeOp(bytes) => bytes.foreach(u08(_))
				case Call(numArguments) => u30(numArguments)
				case CallMethod(index, numArguments) => {
					u30(index)
					u30(numArguments)
				}
				case CallProperty(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case CallPropLex(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case CallPropVoid(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case CallStatic(method, numArguments) => {
					u30(abc.methods indexOf method)
					u30(numArguments)
				}
				case CallSuper(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case CallSuperVoid(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case CheckFilter() =>
				case Coerce(typeName) => name(typeName)
				case CoerceAny() | CoerceBoolean() | CoerceDouble() | CoerceInt() | CoerceObject() | CoerceString() | CoerceUInt() =>
				case Construct(numArguments) => u30(numArguments)
				case ConstructProp(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case ConstructSuper(numArguments) => u30(numArguments)
				case ConvertBoolean() | ConvertDouble() | ConvertInt() | ConvertObject() | ConvertString() | ConvertUInt() =>
			 	case Debug(kind, name, register, extra) => {
					 u08(kind)
					 string(name)
					 u08(register)
					 u30(extra)
				}
				case DebugFile(file) => string(file)
				case DebugLine(line) => u30(line)
				case DecLocal(register) => u30(register)
				case DecLocalInt(register) => u30(register)
				case Decrement() | DecrementInt() =>
				case DeleteProperty(property) => name(property)
				case Divide() =>
				case Dup() =>
				case DefaultXMLNamespace(uri) => string(uri)
				case DefaultXMLNamespaceLate() =>
				case Equals() =>
				case EscapeXMLAttribute() =>
				case EscapeXMLElement() =>
				case FindProperty(property) => name(property)
				case FindPropStrict(property) => name(property)
				case GetDescendants(property) => name(property)
				case GetGlobalScope() =>
				case GetGlobalSlot(slot) => u30(slot)
				case GetLex(typeName) => name(typeName)
				case GetLocal(register: Int) => if(register > 3) u30(register)//note: keep the if and do not put it in the case
				case GetProperty(property) => name(property)
				case GetScopeObject(index) => u08(index)
				case GetSlot(slot) => u30(slot)
				case GetSuper(property) => name(property)
				case GreaterEquals() | GreaterThan() =>
				case HasNext() =>
				case HasNext2(objectRegister, indexRegister) => {
					u30(objectRegister)
					u30(indexRegister)
				}
				case IfEqual(marker) => patch(op)
				case IfFalse(marker) => patch(op)
				case IfGreaterEqual(marker) => patch(op)
				case IfGreaterThan(marker) => patch(op)
				case IfLessEqual(marker) => patch(op)
				case IfLessThan(marker) => patch(op)
				case IfNotEqual(marker) => patch(op)
				case IfNotGreaterEqual(marker) => patch(op)
				case IfNotGreaterThan(marker) => patch(op)
				case IfNotLessEqual(marker) => patch(op)
				case IfNotLessThan(marker) => patch(op)
				case IfStrictEqual(marker) => patch(op)
				case IfStrictNotEqual(marker) => patch(op)
				case IfTrue(marker) => patch(op)
				case In() =>
				case IncLocal(register) => u30(register)
				case IncLocalInt(register) => u30(register)
				case Increment() | IncrementInt() =>
				case InitProperty(property) => name(property)
				case InstanceOf() =>
				case IsType(typeName) => name(typeName)
				case IsTypeLate() =>
				case Jump(marker) => patch(op)
				case Kill(register) => u30(register)
				case Label() =>
				case LessEquals() | LessThan() =>
				case LookupSwitch(defaultCase, cases) => {
					patch(op)
					u30(cases.length - 1)
					(0 until cases.length) foreach { i => s24(0) }
				}
				case ShiftLeft() | Modulo() | Multiply() | MultiplyInt() | Negate() | NegateInt() =>
				case NewActivation() =>
				case NewArray(numArguments) => u30(numArguments)
				case NewCatch(exceptionHandler) => u30(bytecode.exceptions indexOf exceptionHandler)
				case NewClass(nominalType) => u30(abc.types indexOf nominalType)
				case NewFunction(function) => u30(abc.methods indexOf function)
				case NewObject(numArguments) => u30(numArguments)
				case NextName() | NextValue() | Nop() | Not() | Pop() | PopScope() =>
				case PushByte(value) => s08(value) // pushbyte is signed
				case PushDouble(value) => u30(cpool indexOf value)
				case PushFalse() =>
				case PushInt(value) => u30(cpool indexOf value)
				case PushNamespace(value) => u30(cpool indexOf value)
				case PushNaN() | PushNull() | PushScope() =>
				case PushShort(value) => s30(value) // pushshort is signed
				case PushString(value) => string(value)
				case PushTrue() =>
				case PushUInt(value) => u30(cpool indexOf value)
				case PushUndefined() | PushWith() =>
				case ReturnValue() | ReturnVoid() =>
				case ShiftRight() =>
				case SetLocal(register) => if(register > 3) u30(register)
				case SetGlobalSlot(slot) => u30(slot)
				case SetProperty(property) => name(property)
				case SetSlot(slot) => u30(slot)
				case SetSuper(property) => name(property)
				case StrictEquals() | Subtract() | SubtractInt() | Swap() =>
				case Throw() | TypeOf() | ShiftRightUnsigned() =>
				case SetByte() | SetShort() | SetInt() | SetFloat() | SetDouble() =>
				case GetByte() | GetShort() | GetInt() | GetFloat() | GetDouble() =>
				case Sign1() | Sign8() | Sign16() =>
			}
		}
		try {
			bytecode.ops foreach writeOp

			output.close()

			val buffer = byteArrayOutputStream.toByteArray

			for((position, op) <- patches) {
				op match {
					case LookupSwitch(defaultCase, cases) => {
						val offset = position + 4 + AbcOutputUtil.lengthOf(cases.length - 1)

						AbcOutputUtil.writeS24(buffer, position + 1, defaultCase.position - position)

						for(i <- 0 until cases.length)
							AbcOutputUtil.writeS24(buffer, offset + i * 3, cases(i).position - position );
					}
					case opWithMarker: OpWithMarker => AbcOutputUtil.writeS24(buffer, position + 1, opWithMarker.marker.position - (position + 4))
					case other => error("Unexpected operation " + other)
				}
			}

			val exceptions = bytecode.exceptions map {
				handler => {
					new AbcExceptionHandler(
						handler.from.position,
						handler.to.position,
						handler.target.position,
						handler.typeName, handler.varName)
				}
			}

			(buffer, exceptions)
		}
		finally {
			try { output.close() } catch { case _ =>  }
		}
	}
}