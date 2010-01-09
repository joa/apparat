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

import collection.mutable.ListBuffer
import operations._
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import annotation.tailrec
import apparat.abc._
import collection.immutable.{SortedMap, TreeMap}
import apparat.utils.{IndentingPrintWriter, Dumpable}

object Bytecode {
	def fromMethod(method: AbcMethod)(implicit abc: Abc) = method.body match {
		case Some(body) => fromBody(body)
		case None => None
	}

	def fromBody(body: AbcMethodBody)(implicit abc: Abc) = {
		val input = new AbcInputStream(new ByteArrayInputStream(body.code))
		val cpool = abc.cpool
		val markers = new MarkerManager()
		val exceptions = body.exceptions map { handler => new BytecodeExceptionHandler(markers.putMarkerAt(handler.from), markers.putMarkerAt(handler.to), markers.putMarkerAt(handler.target), handler.typeName, handler.varName) }
		@inline def u08 = input.readU08()
		@inline def s24 = input.readS24()
		@inline def u30 = input.readU30()
		@inline def name = cpool.names(u30)
		@inline def string = cpool.strings(u30)
		@inline def numArguments = u30
		@inline def register = u30
		@inline def slot = u30
		@inline def property = name
		@inline def marker(implicit position: Int) = markers putMarkerAt (position + 0x04 + s24)
		@inline def readOp(implicit position: Int): AbstractOp = input.readU08() match {
			case Op.add => Add()
			case Op.add_i => AddInt()
			case Op.add_p => error("add_p")
			case Op.applytype => ApplyType(numArguments)
			case Op.astype => AsType(name)
			case Op.astypelate => AsTypeLate()
			case Op.bitand => BitAnd()
			case Op.bitnot => BitNot()
			case Op.bitor => BitOr()
			case Op.bitxor => BitXor()
			case Op.bkpt => Breakpoint()
			case Op.bkptline => BreakpointLine()
			case Op.call => Call(numArguments)
			case Op.callmethod => CallMethod(u30, numArguments)
			case Op.callproperty => CallProperty(property, numArguments)
			case Op.callproplex => CallPropLex(property, numArguments)
			case Op.callpropvoid => CallPropVoid(property, numArguments)
			case Op.callstatic => CallStatic(abc methods u30, numArguments)
			case Op.callsuper => CallSuper(property, numArguments)
			case Op.callsupervoid => CallSuperVoid(property, numArguments)
			case Op.checkfilter => CheckFilter()
			case Op.coerce => Coerce(name)
			case Op.coerce_a => CoerceAny()
			case Op.coerce_b => CoerceBoolean()
			case Op.coerce_d => CoerceDouble()
			case Op.coerce_i => CoerceInt()
			case Op.coerce_o => CoerceObject()
			case Op.coerce_s => CoerceString()
			case Op.coerce_u => CoerceUInt()
			case Op.construct => Construct(numArguments)
			case Op.constructprop => ConstructProp(property, numArguments)
			case Op.constructsuper => ConstructSuper(numArguments)
			case Op.convert_b => ConvertBoolean()
			case Op.convert_d => ConvertDouble()
			case Op.convert_i => ConvertInt()
			case Op.convert_m => error("convert_m")
			case Op.convert_m_p => error("convet_m_p")
			case Op.convert_o => ConvertObject()
			case Op.convert_u => ConvertUInt()
			case Op.debug => Debug(u08, string, u08, u30)
			case Op.debugfile => DebugFile(string)
			case Op.debugline => DebugLine(u30)
			case Op.declocal => DecLocal(register)
			case Op.declocal_i => DecLocalInt(register)
			case Op.declocal_p => error("declocal_p")
			case Op.decrement => Decrement()
			case Op.decrement_i => DecrementInt()
			case Op.decrement_p => error("decrement_p")
			case Op.deldescendants => error("deldescendants")
			case Op.deleteproperty => DeleteProperty(property)
			case Op.divide => Divide()
			case Op.divide_p => error("divide_p")
			case Op.dup => Dup()
			case Op.dxns => DefaultXMLNamespace(string)
			case Op.dxnslate => DefaultXMLNamespaceLate()
			case Op.equals => Equals()
			case Op.esc_xattr => EscapeXMLAttribute()
			case Op.esc_xelem => EscapeXMLElement()
			case Op.finddef => error("finddef")
			case Op.findproperty => FindProperty(property)
			case Op.findpropstrict => FindPropStrict(property)
			case Op.getdescendants => GetDescendants(property)
			case Op.getglobalscope => GetGlobalScope()
			case Op.getglobalslot => GetGlobalSlot(slot)
			case Op.getlex => GetLex(name)
			case Op.getlocal => GetLocal(register)
			case Op.getlocal0 => GetLocal(0)
			case Op.getlocal1 => GetLocal(1)
			case Op.getlocal2 => GetLocal(2)
			case Op.getlocal3 => GetLocal(3)
			case Op.getproperty => GetProperty(property)
			case Op.getscopeobject => GetScopeObject(u08)
			case Op.getslot => GetSlot(slot)
			case Op.getsuper => GetSuper(property)
			case Op.greaterequals => GreaterEquals()
			case Op.greaterthan => GreaterThan()
			case Op.hasnext => HasNext()
			case Op.hasnext2 => HasNext2(register, register)
			case Op.ifeq => IfEqual(marker)
			case Op.iffalse => IfFalse(marker)
			case Op.ifge => IfGreaterEqual(marker)
			case Op.ifgt => IfGreaterThan(marker)
			case Op.ifle => IfLessEqual(marker)
			case Op.iflt => IfLessThan(marker)
			case Op.ifne => IfNotEqual(marker)
			case Op.ifnge => IfNotGreaterEqual(marker)
			case Op.ifngt => IfNotGreaterThan(marker)
			case Op.ifnle => IfNotLessEqual(marker)
			case Op.ifnlt => IfNotLessThan(marker)
			case Op.ifstricteq => IfStrictEqual(marker)
			case Op.ifstrictne => IfStrictNotEqual(marker)
			case Op.iftrue => IfTrue(marker)
			case Op.in => In()
			case Op.inclocal => IncLocal(register)
			case Op.inclocal_i => IncLocalInt(register)
			case Op.inclocal_p => error("inclocal_p")
			case Op.increment => Increment()
			case Op.increment_i => IncrementInt()
			case Op.increment_p => error("increment_p")
			case Op.initproperty => InitProperty(property)
			case Op.instanceof => InstanceOf()
			case Op.istype => IsType(name)
			case Op.istypelate => IsTypeLate()
			case Op.jump => Jump(marker)
			case Op.kill => Kill(register)
			case Op.label => Label()
			case Op.lessequals => LessEquals()
			case Op.lessthan => LessThan()
			case Op.lf32 => GetFloat()
			case Op.lf64 => GetDouble()
			case Op.li16 => GetShort()
			case Op.li32 => GetInt()
			case Op.li8 => GetByte()
			case Op.lookupswitch => LookupSwitch(markers putMarkerAt (position + s24), Array.fill(u30 + 1) { markers putMarkerAt (position + s24) })
			case Op.lshift => ShiftLeft()
			case Op.modulo => Modulo()
			case Op.modulo_p => error("modulo_p")
			case Op.multiply => Multiply()
			case Op.multiply_i => MultiplyInt()
			case Op.multiply_p => error("multiply_p")
			case Op.negate => Negate()
			case Op.negate_i => NegateInt()
			case Op.negate_p => error("negate_p")
			case Op.newactivation => NewActivation()
			case Op.newarray => NewArray(numArguments)
			case Op.newcatch => NewCatch(exceptions(u30))
			case Op.newclass => NewClass(abc types u30)
			case Op.newfunction => NewFunction(abc methods u30)
			case Op.newobject => NewObject(numArguments)
			case Op.nextname => NextName()
			case Op.nextvalue => NextValue()
			case Op.nop => Nop()
			case Op.not => Not()
			case Op.pop => Pop()
			case Op.popscope => PopScope()
			case Op.pushbyte => PushByte(u08 match  {
				case x if (x & 0x80) != 0 => (x & 0x7f) - 0x80
				case y => y
			})
			case Op.pushdecimal => error("pushdecimal")
			case Op.pushdnan => error("pushdnan")//push decimal nan
			case Op.pushdouble => PushDouble(cpool doubles u30)
			case Op.pushfalse => PushFalse()
			case Op.pushint => PushInt(cpool ints u30)
			case Op.pushnamespace => PushNamespace(cpool namespaces u30)
			case Op.pushnan => PushNaN()
			case Op.pushnull => PushNull()
			case Op.pushscope => PushScope()
			case Op.pushshort => PushShort(u30 match {
				case x if (x & 0x20000000) != 0 => (x & 0x1fffffff) - 0x20000000
				case y => y
			})
			case Op.pushstring => PushString(string)
			case Op.pushtrue => PushTrue()
			case Op.pushuint => PushUInt(cpool uints u30)
			case Op.pushundefined => PushUndefined()
			case Op.pushuninitialized => error("pushuninitialized")
			case Op.pushwith => PushWith()
			case Op.returnvalue => ReturnValue()
			case Op.returnvoid => ReturnVoid()
			case Op.rshift => ShiftRight()
			case Op.setglobalslot => SetGlobalSlot(slot)
			case Op.setlocal => SetLocal(register)
			case Op.setlocal0 => SetLocal(0)
			case Op.setlocal1 => SetLocal(1)
			case Op.setlocal2 => SetLocal(2)
			case Op.setlocal3 => SetLocal(3)
			case Op.setproperty => SetProperty(property)
			case Op.setslot => SetSlot(slot)
			case Op.setsuper => SetSuper(property)
			case Op.sf32 => SetFloat()
			case Op.sf64 => SetDouble()
			case Op.si16 => SetShort()
			case Op.si32 => SetInt()
			case Op.si8 => SetByte()
			case Op.strictequals => StrictEquals()
			case Op.subtract => Subtract()
			case Op.subtract_i => SubtractInt()
			case Op.subtract_p => error("subtract_p")
			case Op.swap => Swap()
			case Op.sxi1 => Sign1()
			case Op.sxi16 => Sign16()
			case Op.sxi8 => Sign8()
			case Op.`throw` => Throw()
			case Op.timestamp => error("timestamp")
			case Op.typeof => TypeOf()
			case Op.urshift => ShiftRightUnsigned()
			case x => error("Unknown opcode " + x + ".")
		}

		@tailrec def build(list: ListBuffer[AbstractOp], map: SortedMap[Int, AbstractOp]): (ListBuffer[AbstractOp], SortedMap[Int, AbstractOp]) = input.available match {
			case 0 => (list, map)
			case _ => {
				val pos = input.position
				val op = readOp(pos)
				build(list += op, map + (pos -> op))
			}
		}

		try {
			val (ops, map) = build(new ListBuffer[AbstractOp](), new TreeMap[Int, AbstractOp]())
			new Bytecode(ops, markers solve map, exceptions)
		}
		finally {
			try { input.close() } catch { case _ => {} }
		}
	}
}

class Bytecode(val ops: Seq[AbstractOp], val markers: MarkerManager, val exceptions: Array[BytecodeExceptionHandler]) extends Dumpable {
	override def dump(writer: IndentingPrintWriter) = new BytecodeDump(ops, markers, exceptions) dump writer

	def toByteArray(implicit abc: Abc) = {
		val byteArrayOutputStream = new ByteArrayOutputStream()
		val output = new AbcOutputStream(byteArrayOutputStream)
		val cpool = abc.cpool
		var patches = TreeMap[Int, AbstractOp]()
		@inline def u08(value: Int) = output writeU08 value
		@inline def s24(value: Int) = output writeS24 value
		@inline def u30(value: Int) = output writeU30 value
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
				case None => {}
			}
			u08(op.opCode)
			op match {
				case Add() | AddInt() => {}
				case ApplyType(numArguments) => u30(numArguments)
				case AsType(typeName) => name(typeName)
				case AsTypeLate() => {}
				case BitAnd() | BitNot() | BitOr() | BitXor() => {}
				case Breakpoint() | BreakpointLine() => {}
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
				case CheckFilter() => {}
				case Coerce(typeName) => name(typeName)
				case CoerceAny() | CoerceBoolean() | CoerceDouble() | CoerceInt() | CoerceObject() | CoerceString() | CoerceUInt() => {}
				case Construct(numArguments) => u30(numArguments)
				case ConstructProp(property, numArguments) => {
					name(property)
					u30(numArguments)
				}
				case ConstructSuper(numArguments) => u30(numArguments)
				case ConvertBoolean() | ConvertDouble() | ConvertInt() | ConvertObject() | ConvertString() | ConvertUInt() => {}
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
				case Decrement() | DecrementInt() => {}
				case DeleteProperty(property) => name(property)
				case Divide() => {}
				case Dup() => {}
				case DefaultXMLNamespace(uri) => string(uri)
				case DefaultXMLNamespaceLate() => {}
				case Equals() => {}
				case EscapeXMLAttribute() => {}
				case EscapeXMLElement() => {}
				case FindProperty(property) => name(property)
				case FindPropStrict(property) => name(property)
				case GetDescendants(property) => name(property)
				case GetGlobalScope() => {}
				case GetGlobalSlot(slot) => u30(slot)
				case GetLex(typeName) => name(typeName)
				case GetLocal(register: Int) => if(register > 3) u30(register)//note: keep the if and do not put it in the case
				case GetProperty(property) => name(property)
				case GetScopeObject(index) => u08(index)
				case GetSlot(slot) => u30(slot)
				case GetSuper(property) => name(property)
				case GreaterEquals() | GreaterThan() => {}
				case HasNext() => {}
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
				case In() => {}
				case IncLocal(register) => u30(register)
				case IncLocalInt(register) => u30(register)
				case Increment() | IncrementInt() => {}
				case InitProperty(property) => name(property)
				case InstanceOf() => {}
				case IsType(typeName) => name(typeName)
				case IsTypeLate() => {}
				case Jump(marker) => patch(op)
				case Kill(register) => u30(register)
				case Label() => {}
				case LessEquals() | LessThan() => {}
				case LookupSwitch(defaultCase, cases) => {
					patch(op)
					u30(cases.length - 1)
					(0 until cases.length) foreach { i => s24(0) }
				}
				case ShiftLeft() | Modulo() | Multiply() | MultiplyInt() | Negate() | NegateInt() => {}
				case NewActivation() => {}
				case NewArray(numArguments) => u30(numArguments)
				case NewCatch(exceptionHandler) => u30(exceptions indexOf exceptionHandler)
				case NewClass(nominalType) => u30(abc.types indexOf nominalType)
				case NewFunction(function) => u30(abc.methods indexOf function)
				case NewObject(numArguments) => u30(numArguments)
				case NextName() | NextValue() | Nop() | Not() | Pop() | PopScope() => {}
				case PushByte(value) => u08(value)
				case PushDouble(value) => u30(cpool indexOf value)
				case PushFalse() => {}
				case PushInt(value) => u30(cpool indexOf value)
				case PushNamespace(value) => u30(cpool indexOf value)
				case PushNaN() | PushNull() | PushScope() => {}
				case PushShort(value) => u30(value)
				case PushString(value) => string(value)
				case PushTrue() => {}
				case PushUInt(value) => u30(cpool indexOf value)
				case PushUndefined() | PushWith() => {}
				case ReturnValue() | ReturnVoid() => {}
				case ShiftRight() => {}
				case SetLocal(register) => if(register > 3) u30(register)
				case SetGlobalSlot(slot) => u30(slot)
				case SetProperty(property) => name(property)
				case SetSlot(slot) => u30(slot)
				case SetSuper(property) => name(property)
				case StrictEquals() | Subtract() | SubtractInt() | Swap() => {}
				case Throw() | TypeOf() | ShiftRightUnsigned() => {}
				case SetByte() | SetShort() | SetInt() | SetFloat() | SetDouble() => {}
				case GetByte() | GetShort() | GetInt() | GetFloat() | GetDouble() => {}
				case Sign1() | Sign8() | Sign16() => {}
			}
		}
		try {
			ops foreach writeOp

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

			buffer
		}
		finally {
			try { output.close() } catch { case _ => {} }
		}
	}
}