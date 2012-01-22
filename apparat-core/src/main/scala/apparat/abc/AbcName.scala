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

object AbcNameKind {
	val QName = 0x07
	val QNameA = 0x0d
	val RTQName = 0x0f
	val RTQNameA = 0x10
	val RTQNameL = 0x11
	val RTQNameLA = 0x12
	val Multiname = 0x09
	val MultinameA = 0x0e
	val MultinameL = 0x1b
	val MultinameLA = 0x1c
	val Typename = 0x1d
}

sealed abstract class AbcName(val kind: Int){
	def isRuntimeName = {
		import AbcNameKind._

		kind match {
			case RTQName | RTQNameA => true
			case RTQNameL | RTQNameLA => true
			case _ => false
		}
	}
}

case class AbcQName(name: Symbol, namespace: AbcNamespace) extends AbcName(AbcNameKind.QName)
case class AbcQNameA(name: Symbol, namespace: AbcNamespace) extends AbcName(AbcNameKind.QNameA)
case class AbcRTQName(name: Symbol) extends AbcName(AbcNameKind.RTQName)
case class AbcRTQNameA(name: Symbol) extends AbcName(AbcNameKind.RTQNameA)
case object AbcRTQNameL extends AbcName(AbcNameKind.RTQNameL)
case object AbcRTQNameLA extends AbcName(AbcNameKind.RTQNameLA)
case class AbcMultiname(name: Symbol, nsset: AbcNSSet) extends AbcName(AbcNameKind.Multiname)
case class AbcMultinameA(name: Symbol, nsset: AbcNSSet) extends AbcName(AbcNameKind.MultinameA)
case class AbcMultinameL(nsset: AbcNSSet) extends AbcName(AbcNameKind.MultinameL)
case class AbcMultinameLA(nsset: AbcNSSet) extends AbcName(AbcNameKind.MultinameLA)
case class AbcTypename(name: AbcQName, parameters: Array[AbcName]) extends AbcName(AbcNameKind.Typename) {
	override def toString = "AbcTypename(" + name + ", [" + (parameters mkString ", ") + "])"
}
