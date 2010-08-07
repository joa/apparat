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
trait AbstractAbcConstantPoolBuilder { self: AbcVisitor =>
	protected var addNaN: Boolean = false

	def createPool: AbcConstantPool

	def reset(): Unit = addNaN = false

	protected def addValueToPool(value: Abc): Unit = value accept this
	protected def addValueToPool(value: Int): Unit
	protected def addValueToPool(value: Long): Unit
	protected def addValueToPool(value: Double): Unit
	protected def addValueToPool(value: Symbol): Unit
	protected def addValueToPool(value: AbcNamespace): Unit
	protected def addValueToPool(value: AbcNSSet): Unit
	protected def addValueToPool(value: AbcName): Unit

	final def add(value: Abc): Unit = addValueToPool(value)

	final def add(value: Int): Unit = addValueToPool(value)

	final def add(value: Long): Unit = addValueToPool(value)

	final def add(value: Double): Unit = if(!value.isNaN) addValueToPool(value)

	final def add(value: Symbol): Unit = if(value != AbcConstantPool.EMPTY_STRING) addValueToPool(value)

	final def add(value: AbcNamespace): Unit = {
		add(value.name)
		if(value != AbcConstantPool.EMPTY_NAMESPACE) addValueToPool(value)
	}

	final def add(value: AbcNSSet): Unit  = {
		value.set foreach add
		if(value != AbcConstantPool.EMPTY_NSSET) addValueToPool(value)
	}

	final def add(value: AbcName): Unit = {
		value match {
			case AbcQName(name, namespace) => {
				add(name)
				add(namespace)
			}
			case AbcQNameA(name, namespace) => {
				add(name)
				add(namespace)
			}
			case AbcRTQName(name) => add(name)
			case AbcRTQNameA(name) => add(name)
			case AbcRTQNameL | AbcRTQNameLA =>
			case AbcMultiname(name, nsset) => {
				add(name)
				add(nsset)
			}
			case AbcMultinameA(name, nsset) => {
				add(name)
				add(nsset)
			}
			case AbcMultinameL(nsset) => add(nsset)
			case AbcMultinameLA(nsset) => add(nsset)
			case AbcTypename(name, parameters) => {
				add(name)
				parameters foreach add
			}
		}

		if(value != AbcConstantPool.EMPTY_NAME) addValueToPool(value)
	}

	final def add(kind: Int, value: Any): Unit = kind match {
		case AbcConstantType.Int => add(value.asInstanceOf[Int])
		case AbcConstantType.UInt => add(value.asInstanceOf[Long])
		case AbcConstantType.Double => {
			// Thank you. http://lampsvn.epfl.ch/trac/scala/ticket/3291
			val double = value.asInstanceOf[Double]
			if(double.isNaN) {
				addNaN = true
			} else {
				add(value.asInstanceOf[Double])
			}
		}
		case AbcConstantType.Utf8 => add(value.asInstanceOf[Symbol])
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
				AbcConstantType.PrivateNamespace => add(value.asInstanceOf[AbcNamespace])
	}

	// Visitor implementation

	override def visit(value: AbcConstantPool): Unit = {}

	override def visit(value: AbcExceptionHandler): Unit = {
		add(value.typeName)
		add(value.varName)
	}

	override def visit(value: AbcInstance): Unit = {
		add(value.name)

		value.base match {
			case Some(base) => add(base)
			case None =>
		}

		value.protectedNs match {
			case Some(namespace) => add(namespace)
			case None =>
		}

		value.interfaces foreach add
	}

	override def visit(value: AbcMetadata): Unit = {
		add(value.name)

		for((key, value) <- value.attributes) {
			add(key)
			add(value)
		}
	}

	override def visit(value: AbcMethod): Unit = {
		add(value.returnType)
		add(value.name)
	}

	override def visit(value: AbcMethodBody): Unit = {
		value.bytecode match {
			case Some(bytecode) => {
				bytecode.ops foreach {
					case Add() | AddInt() =>
					case ApplyType(_) =>
					case AsType(typeName) => add(typeName)
					case AsTypeLate() =>
					case BitAnd() | BitNot() | BitOr() | BitXor() =>
					case Breakpoint() | BreakpointLine() =>
					case Call(_) =>
					case CallMethod(_, _) =>
					case CallProperty(property, _) => add(property)
					case CallPropLex(property, _) => add(property)
					case CallPropVoid(property, _) => add(property)
					case CallStatic(_, _) =>
					case CallSuper(property, _) => add(property)
					case CallSuperVoid(property, _) => add(property)
					case CheckFilter() =>
					case Coerce(typeName) => add(typeName)
					case CoerceAny() | CoerceBoolean() | CoerceDouble() | CoerceInt() | CoerceObject() | CoerceString() | CoerceUInt() =>
					case Construct(_) =>
					case ConstructProp(property, _) => add(property)
					case ConstructSuper(_) =>
					case ConvertBoolean() | ConvertDouble() | ConvertInt() | ConvertObject() | ConvertString() | ConvertUInt() =>
					case Debug(_, name, _, _) => add(name)
					case DebugFile(file) => add(file)
					case DebugLine(_) =>
					case DecLocal(_) =>
					case DecLocalInt(_) =>
					case Decrement() | DecrementInt() =>
					case DeleteProperty(property) => add(property)
					case Divide() =>
					case Dup() =>
					case DefaultXMLNamespace(uri) => add(uri)
					case DefaultXMLNamespaceLate() =>
					case Equals() =>
					case EscapeXMLAttribute() =>
					case EscapeXMLElement() =>
					case FindProperty(property) => add(property)
					case FindPropStrict(property) => add(property)
					case GetDescendants(property) => add(property)
					case GetGlobalScope() =>
					case GetGlobalSlot(_) =>
					case GetLex(typeName) => add(typeName)
					case GetLocal(_) =>
					case GetProperty(property) => add(property)
					case GetScopeObject(_) =>
					case GetSlot(_) =>
					case GetSuper(property) => add(property)
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
					case InitProperty(property) => add(property)
					case InstanceOf() =>
					case IsType(typeName) => add(typeName)
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
					case PushDouble(value) => add(value)
					case PushFalse() =>
					case PushInt(value) => add(value)
					case PushNamespace(value) => add(value)
					case PushNaN() | PushNull() | PushScope() =>
					case PushShort(_) =>
					case PushString(value) => add(value)
					case PushTrue() =>
					case PushUInt(value) => add(value)
					case PushUndefined() | PushWith() =>
					case ReturnValue() | ReturnVoid() =>
					case ShiftRight() =>
					case SetLocal(_) =>
					case SetGlobalSlot(_) =>
					case SetProperty(property) => add(property)
					case SetSlot(_) =>
					case SetSuper(property) => add(property)
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
		add(value.typeName)

		value.name match {
			case Some(name) => add(name)
			case None =>
		}

		if(value.optional) {
			if(value.optionalType.isDefined && value.optionalVal.isDefined) {
				add(value.optionalType.get, value.optionalVal.get)
			} else {
				error("Optional parameter that is not properly defined.")
			}
		}
	}

	override def visit(value: AbcTrait): Unit = {
		add(value.name)

		value match {
			case slot: AbcTraitAnySlot => {
				add(slot.typeName)
				slot.value match {
					case Some(value) => {
						if(slot.value.isDefined && slot.valueType.isDefined) {
							add(slot.valueType.get, slot.value.get)
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