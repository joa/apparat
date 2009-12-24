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
package apparat.abc

object AbcNamespaceKind {
	val Namespace = 0x08
	val Package = 0x16
	val PackageInternal = 0x17
	val Protected = 0x18
	val Explicit = 0x19
	val StaticProtected = 0x1a
	val Private = 0x05
}

case class AbcNamespace(val kind: Int, val name: Symbol) {
	override def equals(that: Any) = {
		that match {
			case AbcNamespace(AbcNamespaceKind.Private, thatName) => false
			case AbcNamespace(thatKind, thatName) => thatKind == kind && thatName == name
			case _ => false
		}
	}
}
