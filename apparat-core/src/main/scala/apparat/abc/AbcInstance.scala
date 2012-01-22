/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.abc

import apparat.utils.{Dumpable, IndentingPrintWriter}

class AbcInstance(
		var name: AbcQName,
		var base: Option[AbcName],
		var isSealed: Boolean,
		var isFinal: Boolean,
		var isInterface: Boolean,
		var nonNullable: Boolean,
		var protectedNs: Option[AbcNamespace],
		var interfaces: Array[AbcName],
		var init: AbcMethod,
		var traits: Array[AbcTrait]
		) extends Dumpable with HasTraits {
	init.anonymous = false

	lazy val privateNs: AbcNamespace =  traits find { _.name.namespace.kind == AbcNamespaceKind.Private } match {
		case Some(t) => t.name.namespace
		case None => AbcNamespace(AbcNamespaceKind.Private, Symbol(""))
	}

	def accept(visitor: AbcVisitor) = {
		visitor visit this
		traits foreach (_ accept visitor)
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Instance:"
		writer withIndent {
			writer <= "Name: " + name
			base match {
				case Some(base) => writer <= "Base: " + base
				case None =>
			}
			writer <= "Is Sealed: " + isSealed
			writer <= "Is Final: " + isFinal
			writer <= "Is Interface " + isInterface
			writer <= "Nullable: " + (!nonNullable)
			protectedNs match {
				case Some(ns) => writer <= "Protected Namespace: " + ns
				case None =>
			}
			writer <= "Interfaces: "
			writer <<< interfaces
			init dump writer
			dumpTraits(writer)

		}
	}

	override def toString = "[AbcInstance name: " + name + "]"
}
