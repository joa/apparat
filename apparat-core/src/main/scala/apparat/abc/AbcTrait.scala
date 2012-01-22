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

object AbcTraitKind {
	val Slot = 0
	val Method = 1
	val Getter = 2
	val Setter = 3
	val Class = 4
	val Function = 5
	val Const = 6
}

sealed abstract class AbcTrait(val kind: Int, val name: AbcQName,
							   val metadata: Option[Array[AbcMetadata]]) {
	def accept(visitor: AbcVisitor) = visitor visit this
}

sealed abstract class AbcTraitAnyMethod(override val kind: Int, override val name: AbcQName,
										override val metadata: Option[Array[AbcMetadata]], val dispId: Int,
										val method: AbcMethod, val isFinal: Boolean, val isOverride: Boolean) extends AbcTrait(kind, name, metadata) {
	method.anonymous = false
}

sealed abstract class AbcTraitAnySlot(override val kind: Int, override val name: AbcQName,
									  override val metadata: Option[Array[AbcMetadata]], val index: Int, val typeName: AbcName,
									  val valueType: Option[Int], val value: Option[Any]) extends AbcTrait(kind, name, metadata)

case class AbcTraitGetter(override val name: AbcQName, override val dispId: Int,
						  override val method: AbcMethod, override val isFinal: Boolean, override val isOverride: Boolean,
						  override val metadata: Option[Array[AbcMetadata]]) extends AbcTraitAnyMethod(AbcTraitKind.Getter, name, metadata, dispId, method, isFinal, isOverride)

case class AbcTraitSetter(override val name: AbcQName, override val dispId: Int,
						  override val method: AbcMethod, override val isFinal: Boolean, override val isOverride: Boolean,
						  override val metadata: Option[Array[AbcMetadata]]) extends AbcTraitAnyMethod(AbcTraitKind.Setter, name, metadata, dispId, method, isFinal, isOverride)

case class AbcTraitMethod(override val name: AbcQName, override val dispId: Int,
						  override val method: AbcMethod, override val isFinal: Boolean, override val isOverride: Boolean,
						  override val metadata: Option[Array[AbcMetadata]]) extends AbcTraitAnyMethod(AbcTraitKind.Method, name, metadata, dispId, method, isFinal, isOverride)

case class AbcTraitSlot(override val name: AbcQName, override val index: Int,
						override val typeName: AbcName, override val valueType: Option[Int],
						override val value: Option[Any],
						override val metadata: Option[Array[AbcMetadata]]) extends AbcTraitAnySlot(AbcTraitKind.Slot, name, metadata, index, typeName, valueType, value)

case class AbcTraitConst(override val name: AbcQName, override val index: Int,
						 override val typeName: AbcName, override val valueType: Option[Int],
						 override val value: Option[Any],
						 override val metadata: Option[Array[AbcMetadata]]) extends AbcTraitAnySlot(AbcTraitKind.Const, name, metadata, index, typeName, valueType, value)

case class AbcTraitClass(override val name: AbcQName, val index: Int,
						 val nominalType: AbcNominalType,
						 override val metadata: Option[Array[AbcMetadata]]) extends AbcTrait(AbcTraitKind.Class, name, metadata)

case class AbcTraitFunction(override val name: AbcQName, val index: Int, val function: AbcMethod,
							override val metadata: Option[Array[AbcMetadata]]) extends AbcTrait(AbcTraitKind.Function, name, metadata)
