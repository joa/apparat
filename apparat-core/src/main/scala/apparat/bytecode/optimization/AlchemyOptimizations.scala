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
	private val public = AbcNamespace(22, Symbol(""))
	val _mr32 = AbcQName('_mr32, public)
	val _mru16 = AbcQName('_mru16, public)      
	val _mrs16 = AbcQName('_mrs16, public)
	val _mru8 = AbcQName('_mru8, public)
	val _mrs8 = AbcQName('_mrs8, public)
	val _mrf = AbcQName('_mrf, public)
	val _mrd = AbcQName('_mrd, public)
	val _mw32 = AbcQName('_mw32, public)
	val _mw16 = AbcQName('_mw16, public)
	val _mw8 = AbcQName('_mw8, public)
	val _mwd = AbcQName('_mwd, public)
	val _mrf = AbcQName('_mwf, public)
	
	override def apply(bytecode: Bytecode): Boolean = {
		var result = List.empty[AbstractOp]
		var modified = false

		for(op <- bytecode.ops) {
			if(op.opCode == Op.callproperty) {
				op match {
					// We ignore the object for the original code. The original code was something
					// like mstate._mr32(address), therefore we insert an extra Pop() to get rid
					// of mstate.
					case CallProperty(_mr32, 1) => result = Pop() :: GetInt() :: result; modified = true
					case CallProperty(_mru16, 1) => result = Pop() :: GetShort() :: result; modified = true
					case CallProperty(_mrs16, 1) => result = Pop() :: GetShort() :: result; modified = true
					case CallProperty(_mru8, 1) => result = Pop() :: GetByte() :: result; modified = true
					case CallProperty(_mrs8, 1) => result = Pop() :: GetByte() :: result; modified = true
					case CallProperty(_mrf, 1) => result = Pop() :: GetFloat() :: result; modified = true
					case CallProperty(_mrd, 1) => result = Pop() :: GetDouble() :: result; modified = true
					// ASC inserts an extra Pop() after CallProperty() since those methods
					// are typed void. Because of this we do not have to add an extra Pop()
					case CallProperty(_mw32, 2) => result = WriteInt() :: result; modified = true
					case CallProperty(_mw16, 2) => result = WriteShort() :: result; modified = true
					case CallProperty(_mw8, 2) => result = WriteByte() :: result; modified = true
					case CallProperty(_mwd, 2) => result = WriteDouble() :: result; modified = true
					case CallProperty(_mwf, 2) => result = WriteFloat() :: result; modified = true
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