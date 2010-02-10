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
}

sealed trait TaasUnit extends TaasNode {
	type T = TaasPackage
	def unit = this
	def namespaces: ListBuffer[T]
	def children = namespaces
}

case class TaasTarget(namespaces: ListBuffer[TaasPackage]) extends TaasUnit
case class TaasLibrary(namespaces: ListBuffer[TaasPackage]) extends TaasUnit
case class TaasPackage(name: Symbol, definitions: ListBuffer[TaasDefinition]) extends TaasNode with ParentUnit {
	type T = TaasDefinition
	def children = definitions
}

sealed trait TaasNamespace extends TaasElement with ParentUnit
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
}

case class TaasAnnotation(name: Symbol, namespace: TaasNamespace, properties: Map[Symbol, Symbol]) extends TaasDefinition

sealed trait TaasField extends TaasDefinition

case class TaasSlot(
		name: Symbol,
		namespace: TaasNamespace,
		isStatic: Boolean) extends TaasField

case class TaasConstant(
		name: Symbol,
		namespace: TaasNamespace,
		isStatic: Boolean) extends TaasField

case class TaasMethod(
		name: Symbol,
		namespace: TaasNamespace,
		isStatic: Boolean,
		isFinal: Boolean,
		isNative: Boolean) extends TaasDefinition

sealed trait TaasNominal extends TaasNode with TaasDefinition {
	def methods: ListBuffer[TaasMethod]
}

case class TaasInterface(
		name: Symbol,
		namespace: TaasNamespace,
		methods: ListBuffer[TaasMethod]) extends TaasNominal {
	type T = TaasMethod
	def children = methods
}

case class TaasClass(
		name: Symbol,
		namespace: TaasNamespace,
		isFinal: Boolean,
		isDynamic: Boolean,
		init: TaasMethod,
		ctor: TaasMethod,
		methods: ListBuffer[TaasMethod],
		fields: ListBuffer[TaasField]) extends TaasNominal {
	type T = TaasDefinition
	lazy val children = {
		val result: ListBuffer[T] = ListBuffer(init, ctor)
		result ++= methods
		result ++= fields
		result
	}
}