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
package apparat.abc.analysis

import apparat.abc._
import apparat.bytecode.operations._

/**
 * @author Joa Ebert
 */
trait AbstractAbcConstantPoolBuilder {
	self: AbcVisitor =>

	protected var addNaN: Boolean = false

	def createPool: AbcConstantPool

	def reset(): Unit = {
		addNaN = false
	}

	private def proxy(value: Abc): Unit = add(value)
	def add(value: Abc): Unit = value accept this

	private def proxy(value: Int): Unit = add(value)
	def add(value: Int): Unit

	private def proxy(value: Long): Unit = add(value)
	def add(value: Long): Unit

	private def proxy(value: Double): Unit = if(!value.isNaN) add(value)
	def add(value: Double): Unit

	private def proxy(value: Symbol): Unit = if(value != AbcConstantPool.EMPTY_STRING) add(value)
	def add(value: Symbol): Unit

	private def proxy(value: AbcNamespace): Unit = {
		proxy(value.name)
		if(value != AbcConstantPool.EMPTY_NAMESPACE) add(value)
	}
	def add(value: AbcNamespace): Unit

	private def proxy(value: AbcNSSet): Unit  = {
		value.set foreach proxy
		if(value != AbcConstantPool.EMPTY_NSSET) add(value)
	}
	def add(value: AbcNSSet): Unit

	private def proxy(value: AbcName): Unit = {
		value match {
			case AbcQName(name, namespace) => {
				proxy(name)
				proxy(namespace)
			}
			case AbcQNameA(name, namespace) => {
				proxy(name)
				proxy(namespace)
			}
			case AbcRTQName(name) => proxy(name)
			case AbcRTQNameA(name) => proxy(name)
			case AbcRTQNameL | AbcRTQNameLA =>
			case AbcMultiname(name, nsset) => {
				proxy(name)
				proxy(nsset)
			}
			case AbcMultinameA(name, nsset) => {
				proxy(name)
				proxy(nsset)
			}
			case AbcMultinameL(nsset) => proxy(nsset)
			case AbcMultinameLA(nsset) => proxy(nsset)
			case AbcTypename(name, parameters) => {
				proxy(name)
				parameters foreach proxy
			}
		}

		if(value != AbcConstantPool.EMPTY_NAME) add(value)
	}
	def add(value: AbcName): Unit

	private def proxy(kind: Int, value: Any): Unit = kind match {
		case AbcConstantType.Int => proxy(value.asInstanceOf[Int])
		case AbcConstantType.UInt => proxy(value.asInstanceOf[Long])
		case AbcConstantType.Double => {
			// Thank you. http://lampsvn.epfl.ch/trac/scala/ticket/3291
			val double = value.asInstanceOf[Double]
			if(double.isNaN) {
				addNaN = true
			} else {
				proxy(value.asInstanceOf[Double])
			}
		}
		case AbcConstantType.Utf8 => proxy(value.asInstanceOf[Symbol])
		case AbcConstantType.True |
				AbcConstantType.False |
				AbcConstantType.Null |
				AbcConstantType.Undefined =>
		case AbcConstantType.Namespace |
				AbcConstantType.PackageNamespace |
				AbcConstantType.InternalNamespace |
				AbcConstantType.ProtectedNamespace |
				AbcConstantType.ExplicitNamespace |
				AbcConstantType.StaticProtectedNamespace |
				AbcConstantType.PrivateNamespace => proxy(value.asInstanceOf[AbcNamespace])
	}

	override def visit(value: AbcConstantPool): Unit = {}

	override def visit(value: AbcExceptionHandler): Unit = {
		proxy(value.typeName)
		proxy(value.varName)
	}

	override def visit(value: AbcInstance): Unit = {
		proxy(value.name)

		value.base match {
			case Some(base) => proxy(base)
			case None =>
		}

		value.protectedNs match {
			case Some(namespace) => proxy(namespace)
			case None =>
		}

		value.interfaces foreach proxy
	}

	override def visit(value: AbcMetadata): Unit = {
		proxy(value.name)

		for((key, value) <- value.attributes) {
			proxy(key)
			proxy(value)
		}
	}

	override def visit(value: AbcMethod): Unit = {
		proxy(value.returnType)
		proxy(value.name)
	}

	override def visit(value: AbcMethodBody): Unit = {
		value.bytecode match {
			case Some(bytecode) => {
				bytecode.ops foreach {
					case Add() | AddInt() =>
					case ApplyType(_) =>
					case AsType(typeName) => proxy(typeName)
					case AsTypeLate() =>
					case BitAnd() | BitNot() | BitOr() | BitXor() =>
					case Breakpoint() | BreakpointLine() =>
					case Call(_) =>
					case CallMethod(_, _) =>
					case CallProperty(property, _) => proxy(property)
					case CallPropLex(property, _) => proxy(property)
					case CallPropVoid(property, _) => proxy(property)
					case CallStatic(_, _) =>
					case CallSuper(property, _) => proxy(property)
					case CallSuperVoid(property, _) => proxy(property)
					case CheckFilter() =>
					case Coerce(typeName) => proxy(typeName)
					case CoerceAny() | CoerceBoolean() | CoerceDouble() | CoerceInt() | CoerceObject() | CoerceString() | CoerceUInt() =>
					case Construct(_) =>
					case ConstructProp(property, _) => proxy(property)
					case ConstructSuper(_) =>
					case ConvertBoolean() | ConvertDouble() | ConvertInt() | ConvertObject() | ConvertString() | ConvertUInt() =>
					case Debug(_, name, _, _) => proxy(name)
					case DebugFile(file) => proxy(file)
					case DebugLine(_) =>
					case DecLocal(_) =>
					case DecLocalInt(_) =>
					case Decrement() | DecrementInt() =>
					case DeleteProperty(property) => proxy(property)
					case Divide() =>
					case Dup() =>
					case DefaultXMLNamespace(uri) => proxy(uri)
					case DefaultXMLNamespaceLate() =>
					case Equals() =>
					case EscapeXMLAttribute() =>
					case EscapeXMLElement() =>
					case FindProperty(property) => proxy(property)
					case FindPropStrict(property) => proxy(property)
					case GetDescendants(property) => proxy(property)
					case GetGlobalScope() =>
					case GetGlobalSlot(_) =>
					case GetLex(typeName) => proxy(typeName)
					case GetLocal(_) =>
					case GetProperty(property) => proxy(property)
					case GetScopeObject(_) =>
					case GetSlot(_) =>
					case GetSuper(property) => proxy(property)
					case GreaterEquals() | GreaterThan() =>
					case HasNext() =>
					case HasNext2(_, _) =>
					case IfEqual(_) =>
					case IfFalse(_) =>
					case IfGreaterEqual(_) =>
					case IfGreaterThan(_) =>
					case IfLessEqual(_) =>
					case IfLessThan(_) =>
					case IfNotEqual(_) =>
					case IfNotGreaterEqual(_) =>
					case IfNotGreaterThan(_) =>
					case IfNotLessEqual(_) =>
					case IfNotLessThan(_) =>
					case IfStrictEqual(_) =>
					case IfStrictNotEqual(_) =>
					case IfTrue(_) =>
					case In() =>
					case IncLocal(_) =>
					case IncLocalInt(_) =>
					case Increment() | IncrementInt() =>
					case InitProperty(property) => proxy(property)
					case InstanceOf() =>
					case IsType(typeName) => proxy(typeName)
					case IsTypeLate() =>
					case Jump(_) =>
					case Kill(_) =>
					case Label() =>
					case LessEquals() | LessThan() =>
					case LookupSwitch(_, _) =>
					case ShiftLeft() | Modulo() | Multiply() | MultiplyInt() | Negate() | NegateInt() =>
					case NewActivation() =>
					case NewArray(_) =>
					case NewCatch(_) =>
					case NewClass(_) =>
					case NewFunction(_) =>
					case NewObject(_) =>
					case NextName() | NextValue() | Nop() | Not() | Pop() | PopScope() =>
					case PushByte(_) =>
					case PushDouble(value) => proxy(value)
					case PushFalse() =>
					case PushInt(value) => proxy(value)
					case PushNamespace(value) => proxy(value)
					case PushNaN() | PushNull() | PushScope() =>
					case PushShort(_) =>
					case PushString(value) => proxy(value)
					case PushTrue() =>
					case PushUInt(value) => proxy(value)
					case PushUndefined() | PushWith() =>
					case ReturnValue() | ReturnVoid() =>
					case ShiftRight() =>
					case SetLocal(_) =>
					case SetGlobalSlot(_) =>
					case SetProperty(property) => proxy(property)
					case SetSlot(_) =>
					case SetSuper(property) => proxy(property)
					case StrictEquals() | Subtract() | SubtractInt() | Swap() =>
					case Throw() | TypeOf() | ShiftRightUnsigned() =>
					case SetByte() | SetShort() | SetInt() | SetFloat() | SetDouble() =>
					case GetByte() | GetShort() | GetInt() | GetFloat() | GetDouble() =>
					case Sign1() | Sign8() | Sign16() =>
				}
			}
			case None =>
		}
	}

	override def visit(value: AbcMethodParameter): Unit = {
		proxy(value.typeName)

		value.name match {
			case Some(name) => proxy(name)
			case None =>
		}

		if(value.optional) {
			if(value.optionalType.isDefined && value.optionalVal.isDefined) {
				proxy(value.optionalType.get, value.optionalVal.get)
			} else {
				error("Optional parameter that is not properly defined.")
			}
		}
	}

	override def visit(value: AbcTrait): Unit = {
		proxy(value.name)

		value match {
			case slot: AbcTraitAnySlot => {
				proxy(slot.typeName)
				slot.value match {
					case Some(value) => {
						if(slot.value.isDefined && slot.valueType.isDefined) {
							proxy(slot.valueType.get, slot.value.get)
						} else {
							error("Slot with initial value that is not properly defined.")
						}
					}
					case None =>
				}
			}
			case _ =>
		}
	}

	override def visit(value: AbcScript): Unit = {
		value.traits foreach { _ accept this }
	}
}