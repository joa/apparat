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

import TaasVisibility._

/**
 * @author Joa Ebert
 */
sealed trait TaasTree

sealed trait TaasParent extends TaasTree

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
	type T <: TaasElement
	def children: List[T]
	override def setParent(parent: TaasParent) = {
		super.setParent(parent)
		children foreach (_ setParent this)
	}
}

sealed trait TaasRoot extends TaasTree with TaasParent {
	def children: List[TaasNode]
	def init(): this.type = {
		children foreach (_ setParent this)
		this
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

case class TaasAST(units: List[TaasUnit]) extends TaasRoot {
	type T = TaasUnit
	def children = units
}

sealed trait TaasUnit extends TaasNode {
	type T = TaasNamespace
	def unit = this
	def namespaces: List[T]
	def children = namespaces
}

case class TaasTarget(namespaces: List[TaasNamespace]) extends TaasUnit
case class TaasLibrary(namespaces: List[TaasNamespace]) extends TaasUnit
case class TaasNamespace(name: String, definitions: List[TaasDefinition]) extends TaasNode with ParentUnit {
	type T = TaasDefinition
	def children = definitions
}

sealed trait TaasDefinition extends TaasElement with ParentUnit {
	def name: String
	def visibility: TaasVisibility
}

case class TaasField(
		name: String,
		visibility: TaasVisibility) extends TaasDefinition

case class TaasMethod(
		name: String,
		visibility: TaasVisibility) extends TaasDefinition

sealed trait TaasNominal extends TaasNode with TaasDefinition {
	def methods: List[TaasMethod]
}

case class TaasInterface(
		name: String,
		visibility: TaasVisibility,
		methods: List[TaasMethod]) extends TaasNode with TaasDefinition {
	type T = TaasMethod
	def children = methods
}

case class TaasClass(
		name: String,
		visibility: TaasVisibility,
		isFinal: Boolean,
		isDynamic: Boolean,
		methods: List[TaasMethod],
		fields: List[TaasField]) extends TaasNode with TaasDefinition {
	type T = TaasDefinition
	lazy val children = methods ::: fields
}