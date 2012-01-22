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

import apparat.utils.IndentingPrintWriter

/**
 * @author Joa Ebert
 */
trait HasTraits {
	def traits: Array[AbcTrait]

	def dumpTraits(writer: IndentingPrintWriter): Unit = if(traits.length > 0) {
		def dumpMetadata(metadata: Option[Array[AbcMetadata]]) = {
			metadata match {
				case Some(metadata) => {
					writer <= "Metadata:"
					writer <<< metadata
				}
				case None =>
			}
		}
		writer <= "Traits:"
		writer withIndent {
			traits foreach {
				case anySlot: AbcTraitAnySlot => {
					writer <= (anySlot.kind match {
						case AbcTraitKind.Slot => "Trait Slot:"
						case AbcTraitKind.Const => "Trait Const:"
					})
					writer withIndent {
						writer <= "Name: " + anySlot.name
						writer <= "Index: " + anySlot.index
						writer <= "Type: " + anySlot.typeName
						anySlot.value match {
							case Some(value) => writer <= "Value: " + value
							case None =>
						}
						dumpMetadata(anySlot.metadata)
					}
				}
				case anyMethod: AbcTraitAnyMethod => {
					writer <= "Trait " + (anyMethod.kind match {
						case AbcTraitKind.Getter => "Getter:"
						case AbcTraitKind.Setter => "Setter:"
						case AbcTraitKind.Method => "Method:"
					})
					writer withIndent {
						writer <= "Name: " + anyMethod.name
						writer <= "Disp Id: " + anyMethod.dispId
						writer <= "Is Final: " + anyMethod.isFinal
						writer <= "Is Override: " + anyMethod.isOverride
						dumpMetadata(anyMethod.metadata)
						anyMethod.method dump writer
					}
				}
				case AbcTraitFunction(name, index, function, metadata) => {
					writer <= "Trait Function:"
					writer withIndent {
						writer <= "Name: " + name
						writer <= "Index: " + index
						dumpMetadata(metadata)
						function dump writer
					}
				}
				case AbcTraitClass(name, index, nominalType, metadata) => {
					writer <= "Trait Class:"
					writer withIndent {
						writer <= "Name: " + name
						writer <= "Index: " + index
						dumpMetadata(metadata)
						nominalType dump writer
					}
				}
			}
		}
	}
}
