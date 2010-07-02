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

import apparat.bytecode.Bytecode
import apparat.abc.{AbcQName, AbcNamespace}
import apparat.bytecode.operations._

/**
 * @author Joa Ebert
 */
object AlchemyOptimizations extends (Bytecode => Boolean) {
	override def apply(bytecode: Bytecode): Boolean = {
		var result = List.empty[AbstractOp]
		var modified = false

		for(op <- bytecode.ops) {
			if(op.opCode == Op.callproperty) {
				op match {
					// We ignore the object for the original code. The original code was something
					// like mstate._mr32(address), therefore we insert an extra Pop() to get rid
					// of mstate.
					case CallProperty(AbcQName('_mr32, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetInt() :: result; modified = true
					case CallProperty(AbcQName('_mru16, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetShort() :: result; modified = true
					case CallProperty(AbcQName('_mrs16, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetShort() :: result; modified = true
					case CallProperty(AbcQName('_mru8, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetByte() :: result; modified = true
					case CallProperty(AbcQName('_mrs8, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetByte() :: result; modified = true
					case CallProperty(AbcQName('_mrf, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetFloat() :: result; modified = true
					case CallProperty(AbcQName('_mrd, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: GetDouble() :: result; modified = true
					// ASC inserts an extra Pop() after CallProperty() since those methods
					// are typed void. Because of this we do not have to add an extra Pop()
					case CallProperty(AbcQName('_mw32, AbcNamespace(22, Symbol(""))), 2) => result = SetInt() :: result; modified = true
					case CallProperty(AbcQName('_mw16, AbcNamespace(22, Symbol(""))), 2) => result = SetShort() :: result; modified = true
					case CallProperty(AbcQName('_mw8, AbcNamespace(22, Symbol(""))), 2) => result = SetByte() :: result; modified = true
					case CallProperty(AbcQName('_mwd, AbcNamespace(22, Symbol(""))), 2) => result = SetDouble() :: result; modified = true
					case CallProperty(AbcQName('_mwf, AbcNamespace(22, Symbol(""))), 2) => result = SetFloat() :: result; modified = true
					case o => result = o :: result
				}
			} else {
				result = op :: result
			}
		}

		if(modified) {
			bytecode.ops = result.reverse
		}

		modified
	}
}