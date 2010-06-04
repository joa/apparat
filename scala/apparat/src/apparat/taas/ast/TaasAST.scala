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
package apparat.taas.ast

import collection.mutable.ListBuffer
import apparat.utils.{IndentingPrintWriter, Dumpable}
import apparat.taas.graph.TaasGraph

/**
 * @author Joa Ebert
 */
sealed trait TaasTree extends Dumpable {
	override def dump(writer: IndentingPrintWriter) = {
		writer <= toString
		this match {
			case parent: TaasParent => {
				writer withIndent { parent.children foreach (_ dump writer) }
			}
			case _ =>
		}
	}

	def accept(visitor: TaasVisitor)
}

sealed trait TaasParent extends TaasTree {
	type T <: TaasElement
	def children: ListBuffer[T]
}

sealed trait TaasElement extends TaasTree {
	private var _parent: Option[TaasParent] = None
	lazy val ast: TaasAST = parent match {
		case Some(parent) => parent match {
			case ast: TaasAST => ast
			case node: TaasNode => node.ast
		}
		case None => error("TaasTree not initialized.")
	}
	def setParent(parent: TaasParent) = _parent = Some(parent)
	def parent: Option[TaasParent] = _parent
	def unit: TaasUnit
}

sealed trait TaasNode extends TaasElement with TaasParent {
	override def setParent(parent: TaasParent) = {
		super.setParent(parent)
		children foreach (_ setParent this)
	}
}

trait ParentUnit {
	self: TaasElement =>
	lazy val unit = parent match {
		case Some(parent) => parent match {
			case node: TaasNode => node.unit
			case ast: TaasAST => error("TaasAST unexpected.")
		}
		case None => error("TaasTree not initialized.")
	}
}

case class TaasAST(units: ListBuffer[TaasUnit]) extends TaasTree with TaasParent {
	type T = TaasUnit
	def children = units
	def init(): this.type = {
		children foreach (_ setParent this)
		this
	}

	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

sealed trait TaasUnit extends TaasNode {
	type T = TaasPackage
	def unit = this
	def packages: ListBuffer[T]
	def children = packages
}

case class TaasTarget(packages: ListBuffer[TaasPackage]) extends TaasUnit {
	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

case class TaasLibrary(packages: ListBuffer[TaasPackage]) extends TaasUnit {
	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

case class TaasPackage(name: Symbol, definitions: ListBuffer[TaasDefinition]) extends TaasNode with ParentUnit {
	type T = TaasDefinition
	def children = definitions
	
	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

sealed trait TaasNamespace
case object TaasPublic extends TaasNamespace
case object TaasInternal extends TaasNamespace
case object TaasProtected extends TaasNamespace
case object TaasPrivate extends TaasNamespace
case class TaasExplicit(namespace: Symbol) extends TaasNamespace

sealed trait TaasDefinition extends TaasElement with ParentUnit {
	private var _annotations = ListBuffer.empty[TaasAnnotation]
	def setAnnotations(annotations: ListBuffer[TaasAnnotation]) = _annotations = annotations
	def name: Symbol
	def namespace: TaasNamespace
	def annotations: ListBuffer[TaasAnnotation] = _annotations

	lazy val qualifiedName: String = {
		parent match {
			case Some(parent) => {
				parent match {
					case pckg: TaasPackage => {
						val pckgName = pckg.name.name
						
						if(pckgName.length > 0) {
							pckgName + "." + name.name
						} else {
							name.name
						}
					}
					case definition: TaasDefinition => {
						definition.qualifiedName + "." + name.name
					}
					case _ => name.name
				}
			}
			case None => name.name
		}
	}
}

case class TaasAnnotation(name: Symbol, namespace: TaasNamespace, properties: Map[Symbol, Symbol]) extends TaasDefinition {
	override def accept(visitor: TaasVisitor) = visitor visit this
}

trait TaasTyped {
	def `type`: TaasType
}

sealed trait TaasField extends TaasDefinition with TaasTyped {
	def isStatic: Boolean
}

case class TaasSlot(
		name: Symbol,
		namespace: TaasNamespace,
		`type`: TaasType,
		isStatic: Boolean) extends TaasField {
	override def accept(visitor: TaasVisitor) = visitor visit this
}

case class TaasConstant(
		name: Symbol,
		namespace: TaasNamespace,
		`type`: TaasType,
		isStatic: Boolean) extends TaasField {
	override def accept(visitor: TaasVisitor) = visitor visit this
}

case class TaasMethod(
		name: Symbol,
		namespace: TaasNamespace,
		`type`: TaasType,
		parameters: ListBuffer[TaasParameter],
		isStatic: Boolean,
		isFinal: Boolean,
		isNative: Boolean, code: Option[TaasCode]) extends TaasNode with TaasDefinition with TaasTyped {
	type T = TaasParameter
	def children = parameters

	override def dump(writer: IndentingPrintWriter) = {
		writer <= toString
		code match {
			case Some(code) => writer withIndent {
				code dump writer
			}
			case None =>
		}
	}

	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

abstract class TaasCode extends Dumpable {
	def graph: TaasGraph

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "TaasCode"
		writer withIndent {
			graph dump writer
		}
	}
}

case class TaasParameter(
		`type`: TaasType,
		defaultValue: Option[Any]) extends TaasElement with ParentUnit with TaasTyped {
	override def accept(visitor: TaasVisitor) = visitor visit this
}

sealed trait TaasNominal extends TaasNode with TaasDefinition {
	def base: Option[TaasType]
	def methods: ListBuffer[TaasMethod]
	def interfaces: ListBuffer[TaasType]
}

case class TaasFunction(name: Symbol, namespace: TaasNamespace, method: TaasMethod) extends TaasNominal {
	type T = TaasMethod
	val interfaces = ListBuffer.empty[TaasType]
	val methods = ListBuffer(method)
	val children = methods
	val base = Some(TaasFunctionType)

	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

case class TaasInterface(
		name: Symbol,
		namespace: TaasNamespace,
		base: Option[TaasType],
		methods: ListBuffer[TaasMethod],
		interfaces: ListBuffer[TaasType]) extends TaasNominal {
	type T = TaasMethod
	def children = methods

	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

case class TaasClass(
		name: Symbol,
		namespace: TaasNamespace,
		isFinal: Boolean,
		isDynamic: Boolean,
		init: TaasMethod,
		ctor: TaasMethod,
		base: Option[TaasType],
		methods: ListBuffer[TaasMethod],
		fields: ListBuffer[TaasField],
		interfaces: ListBuffer[TaasType]) extends TaasNominal {
	type T = TaasDefinition
	lazy val children = {
		val result: ListBuffer[T] = ListBuffer(init, ctor)
		result ++= methods
		result ++= fields
		result
	}

	override def accept(visitor: TaasVisitor) = {
		visitor visit this
		children foreach { _ accept visitor }
	}
}

//
// You are here.
//

sealed abstract class TaasBinop(val string: String) {
	override def toString = string
}
sealed abstract class TaasUnop(val string: String) {
	override def toString = string
}

case object TOp_Nothing extends TaasUnop("")

// Arithmetic operations
case object TOp_+ extends TaasBinop("+")
case object TOp_- extends TaasBinop("-")
case object TOp_* extends TaasBinop("*")
case object TOp_/ extends TaasBinop("/")
case object TOp_% extends TaasBinop("%")

// Binary operations
case object TOp_&   extends TaasBinop("&")
case object TOp_^   extends TaasBinop("^")
case object TOp_|   extends TaasBinop("|")
case object TOp_<<  extends TaasBinop("<<")
case object TOp_>>  extends TaasBinop(">>")
case object TOp_>>> extends TaasBinop(">>>")
case object TOp_~   extends TaasUnop("~")

// Boolean operations
case object TOp_==  extends TaasBinop("==")
case object TOp_>=  extends TaasBinop(">=")
case object TOp_>   extends TaasBinop(">")
case object TOp_<=  extends TaasBinop("<=")
case object TOp_<   extends TaasBinop("<")
case object TOp_!=  extends TaasBinop("!=")
case object TOp_!>= extends TaasBinop("!>=")
case object TOp_!>  extends TaasBinop("!>")
case object TOp_!<= extends TaasBinop("!<=")
case object TOp_!<  extends TaasBinop("!<")
case object TOp_=== extends TaasBinop("===")
case object TOp_!== extends TaasBinop("!==")
case object TOp_!   extends TaasUnop("!")
case object TOp_true extends TaasUnop("true ==")
case object TOp_false extends TaasUnop("false ==")

case class TConvert(`type`: TaasType) extends TaasUnop("(convert "+`type`+")")
case class TCoerce(`type`: TaasType) extends TaasUnop("("+`type`+")")

sealed trait TExpr {
	def defines(index: Int): Boolean
	def uses(index: Int): Boolean
}

sealed trait TValue extends TExpr with TaasTyped {
	override def defines(index: Int) = false
	override def uses(index: Int) = false
	def matches(index: Int) = false
}

sealed trait TSideEffect
sealed trait TArgumentList {
	def arguments: List[TValue]
	def argumentMatches(index: Int): Boolean = {
		var p = arguments
		while(p.nonEmpty) {
			if(p.head matches index) {
				return true
			}
			p = p.tail
		}
		false
	}
}

case object TVoid extends TValue { override def `type` = TaasVoidType }
case class TInt(value: Int) extends TValue { override def `type` = TaasIntType }
case class TLong(value: Long) extends TValue { override def `type` = TaasLongType }
case class TBool(value: Boolean) extends TValue { override def `type` = TaasBooleanType }
case class TString(value: Symbol) extends TValue { override def `type` = TaasStringType }
case class TDouble(value: Double) extends TValue { override def `type` = TaasDoubleType }
case class TClass(value: TaasType) extends TValue { override def `type` = value }
case class TInstance(value: TaasType) extends TValue { override def `type` = value }
case class TLexical(value: TaasDefinition) extends TValue {
	override def `type` = value match {
		case klass: TaasClass => TaasNominalTypeInstance(klass)
		case interface: TaasInterface => TaasNominalTypeInstance(interface)
		case field: TaasField => field.`type`
		case method: TaasMethod => method.`type`
		case function: TaasFunction => TaasNominalTypeInstance(function)
		case _ => error("Unexpected lexical "+value+".")
	}
}
case class TReg(index: Int) extends TValue {
	private var _type: TaasType = TaasAnyType

	def typeAs(`type`: TaasType) = _type = `type`

	override def `type` = _type
	override def toString = "r"+index
	override def matches(index: Int) = this.index == index
}

//result = op operand1
case class T2(op: TaasUnop, rhs: TValue, result: TReg) extends TExpr {
	result typeAs (op match {
		case TOp_Nothing => rhs.`type`
		case TOp_~ => TaasIntType
		case TOp_! | TOp_true | TOp_false => TaasBooleanType
		case TConvert(t) => t
		case TCoerce(t) => t
	})

	override def toString = result.toString+" = "+op.toString+rhs.toString
	override def defines(index: Int) = result.index == index
	override def uses(index: Int) = rhs matches index
}

//result = operand1 op operand2
case class T3(op: TaasBinop, lhs: TValue, rhs: TValue, result: TReg) extends TExpr {
	result typeAs TaasType.widen(lhs.`type`, rhs.`type`)

	override def toString = result.toString+" = "+lhs.toString+" "+op.toString+" "+rhs.toString
	override def defines(index: Int) = result.index == index
	override def uses(index: Int) = (lhs matches index) || (rhs matches index)
}

//branch if: op rhs
case class TIf1(op: TaasUnop, rhs: TValue) extends TExpr {
	override def toString = "if("+op.toString+" "+rhs.toString+")"
	override def defines(index: Int) = false
	override def uses(index: Int) = rhs matches index
}

//branch if: lhs op rhs
case class TIf2(op: TaasBinop, lhs: TValue, rhs: TValue) extends TExpr {
	override def toString = "if("+lhs.toString+" "+op.toString+" "+rhs.toString+")"
	override def defines(index: Int) = false
	override def uses(index: Int) = (lhs matches index) || (rhs matches index)
}

case class TJump() extends TExpr {
	override def defines(index: Int) = false
	override def uses(index: Int) = false
}

case class TNop() extends TExpr {
	override def defines(index: Int) = false
	override def uses(index: Int) = false
}

case class TReturn(value: TValue) extends TExpr with TSideEffect {
	override def toString = "return "+value.toString
	override def defines(index: Int) = false
	override def uses(index: Int) = value matches index
}

case class TConstruct(`object`: TValue, arguments: List[TValue]) extends TExpr with TSideEffect with TArgumentList {
	override def defines(index: Int) = false
	override def uses(index: Int) = (`object` matches index) || argumentMatches(index)
}

case class TSuper(base: TValue, arguments: List[TValue]) extends TExpr with TSideEffect with TArgumentList {
	override def defines(index: Int) = false
	override def uses(index: Int) = (base matches index) || argumentMatches(index)
}

case class TCall(`this`: TValue, method: TaasMethod, arguments: List[TValue], result: Option[TReg]) extends TExpr with TSideEffect with TArgumentList {
	override def defines(index: Int) = if(result.isDefined) { result.get.index == index } else { false }
	override def uses(index: Int) = (`this` matches index) || argumentMatches(index)
}

case class TLoad(`object`: TValue, field: TaasField, result: TReg) extends TExpr {
	result typeAs field.`type`
	override def defines(index: Int) = result.index == index
	override def uses(index: Int) = `object` matches index
}

case class TStore(`object`: TValue, field: TaasField, value: TValue) extends TExpr {
	override def defines(index: Int) = false
	override def uses(index: Int) = (`object` matches index) || (value matches index)
}