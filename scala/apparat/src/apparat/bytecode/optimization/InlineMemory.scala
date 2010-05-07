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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.bytecode.optimization

import apparat.abc.{AbcQName, AbcNamespaceKind}
import apparat.bytecode.Bytecode
import apparat.bytecode.operations._

/**
 * @author Joa Ebert
 */
object InlineMemory extends (Bytecode => Unit) {
	private val namespace0 = AbcNamesapce(AbcNamespaceKind.Package, Symbol("apparat.memory"))
	private val namespace1 = AbcNamesapce(AbcNamespaceKind.Package, Symbol("com.joa_ebert.apparat.memory"))
	private val memory0 = AbcQName(namespace0, 'Memory)
	private val memory1 = AbcQName(namespace1, 'Memory)

	override def apply(bytecode: Bytecode): Unit = {
		var removes = List.empty[AbstractOp]
		var replacements = Map.empty[AbstractOp, AbstractOp]
		var modified = false
		var removePop = false
		var balance = 0

		for(op <- bytecode.ops) op match {
			case Pop() if removePop => {
				removes = op :: removes
				removePop = false
			}
			case GetLex(`type`) if `type` == memory0 || `type` == memory1 => {
				removes = op :: removes
				balance += 1
			}
			case CallPropVoid(property, numArguments) if balance > 0 => property match {
				case AbcQName(name, _) => {
					(name match {
						case 'writeByte => Some(SetByte())
						case 'writeShort => Some(SetShort())
						case 'writeInt => Some(SetInt())
						case 'writeFloat => Some(SetFloat())
						case 'writeDouble => Some(SetDouble())
						case 'select => {
							removes = removes.init
							None
						}
						case _ => None
					}) match {
						case Some(replacement) => {
							balance -= 1
							replacements += op -> replacement
							modified = true
						}
						case None =>
					}
				}
				case _ =>
			}
			case CallProperty(property, numArguments) if balance > 0 => property match {
				case AbcQName(name, _) => {
					(name match {
						case 'readUnsignedByte => GetByte()
						case 'readUnsignedShort => GetShort()
						case 'readInt => GetInt()
						case 'readFloat => GetFloat()
						case 'readDouble => GetDouble()
						case 'signExtend1 => Sign1()
						case 'signExtend8 => Sign8()
						case 'signExtend16 => Sign16()
						case 'writeByte => {
							removePop = true
							Some(SetByte())
						}
						case 'writeShort => {
							removePop = true
							Some(SetShort())
						}
						case 'writeInt => {
							removePop = true
							Some(SetInt())
						}
						case 'writeFloat => {
							removePop = true
							Some(SetFloat())
						}
						case 'writeDouble => {
							removePop = true
							Some(SetDouble())
						}
						case 'select => {
							removes = removes.init
							None
						}
						case _ => None
					}) match {
						case Some(replacement) => {
							balance -= 1
							replacements += op -> replacement
							modified = true
						}
						case None =>
					}
				}
				case _ =>
			}
		}

		if(modified) {
			removes foreach { bytecode remove _ }
			replacements.elements foreach { bytecode replace _ }
		}
	}
}