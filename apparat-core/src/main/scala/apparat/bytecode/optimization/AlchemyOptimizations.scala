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
import apparat.log.SimpleLog

/**
 * @author Joa Ebert
 */
object AlchemyOptimizations extends (Bytecode => Boolean) with SimpleLog {
	private val `public` = AbcNamespace(22, Symbol(""))
	private val readInt = AbcQName('readInt, `public`)
	private val position = AbcQName('position, `public`)
	private val ds = AbcQName('ds, `public`)

	override def apply(bytecode: Bytecode): Boolean = {
		var result = List.empty[AbstractOp]
		var modified = false

		for(op <- bytecode.ops) {
			if(op.opCode == Op.callproperty) {
				op match {
					// We ignore the object for the original code. The original code was something
					// like mstate._mr32(address), therefore we insert an extra Pop() to get rid
					// of mstate.
					case CallProperty(AbcQName('_mr32, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: GetInt() :: result; modified = true
					case CallProperty(AbcQName('_mru16, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: GetShort() :: result; modified = true
					case CallProperty(AbcQName('_mrs16, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: Sign16() :: GetShort() :: result; modified = true
					case CallProperty(AbcQName('_mru8, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: GetByte() :: result; modified = true
					case CallProperty(AbcQName('_mrs8, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: Sign8() :: GetByte() :: result; modified = true
					case CallProperty(AbcQName('_mrf, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: GetFloat() :: result; modified = true
					case CallProperty(AbcQName('_mrd, AbcNamespace(22, Symbol(""))), 1) => result = Pop() :: Swap() :: GetDouble() :: result; modified = true
					// ASC inserts an extra Pop() after CallProperty() since those methods
					// are typed void. Because of this we do not have to add an extra Pop()
					case CallProperty(AbcQName('_mw32, AbcNamespace(22, Symbol(""))), 2) => result = SetInt() :: Swap() :: result; modified = true
					case CallProperty(AbcQName('_mw16, AbcNamespace(22, Symbol(""))), 2) => result = SetShort() :: Swap() :: result; modified = true
					case CallProperty(AbcQName('_mw8, AbcNamespace(22, Symbol(""))), 2) => result = SetByte() :: Swap() :: result; modified = true
					case CallProperty(AbcQName('_mwd, AbcNamespace(22, Symbol(""))), 2) => result = SetDouble() :: Swap() :: result; modified = true
					case CallProperty(AbcQName('_mwf, AbcNamespace(22, Symbol(""))), 2) => result = SetFloat() :: Swap() :: result; modified = true
					case o => result = o :: result
				}
			} else {
				result = op :: result
			}
		}

		if(modified) {
			bytecode.ops = result.reverse
		}

		modified || alchemyPeepholes(bytecode)
	}

	private def alchemyPeepholes(bytecode: Bytecode): Boolean = {
		var source = bytecode.ops
		var target = List.empty[AbstractOp]
		val n = source.length
		var modified = false
		val markers = bytecode.markers
		var i = 0

		@inline def nextOp(): Unit = {
			i += 1
			source = source.tail
		}

		/*
		  +1|-0  GetGlobalScope()
		  +1|-1  GetSlot(32)
		  +1|-1  GetProperty(AbcQName('ebp,AbcNamespace(22,')))
		  +1|-0  PushByte(-48)
		  +1|-2  Add()
		  +1|-1  ConvertInt()
		 */
		
		while(i < n) {
			val op = source.head
			val opCode = op.opCode
			if(Op.getglobalscope == opCode) {
				/*
					GetGlobalScope()
					GetSlot(x)
					GetProperty(AbcQName('ds,AbcNamespace(22,')))
					GetLocal(y)
					SetProperty(AbcQName('position,AbcNamespace(22,')))
					GetGlobalScope()
					GetSlot(x)
					GetProperty(AbcQName('ds,AbcNamespace(22,')))
					CallProperty(AbcQName('readInt,AbcNamespace(22,')),0) | readFloat | readDouble | readUnsignedInt | readByte
					ConvertInt()

					->

					GetLocal(y)
					GetInt()
				*/
				var tail = source.tail
				val op2 = tail.head
				if(Op.getslot == op2.opCode) {
					nextOp()
					tail = tail.tail
					val op3 = tail.head
					if(Op.getproperty == op3.opCode && op3.asInstanceOf[GetProperty].property == ds) {
						nextOp()
						tail = tail.tail
						val op4 = tail.head//<- address pointer
						val opCode4 = op4.opCode
						if(opCode4 == Op.getlocal ||
							opCode4 == Op.getlocal0 ||
							opCode4 == Op.getlocal1 ||
							opCode4 == Op.getlocal2 ||
							opCode4 == Op.getlocal3) {
							nextOp()
							tail = tail.tail
							val op5 = tail.head
							if(Op.setproperty == op5.opCode && op5.asInstanceOf[SetProperty].property == position) {
								nextOp()
								tail = tail.tail
								val op6 = tail.head
								if(Op.getglobalscope == op6.opCode) {
									nextOp()
									tail = tail.tail
									val op7 = tail.head
									if(Op.getslot == op7.opCode) {
										nextOp()
										tail = tail.tail
										val op8 = tail.head
										if(Op.getproperty == op8.opCode && op8.asInstanceOf[GetProperty].property == ds) {
											nextOp()
											tail = tail.tail
											val op9 = tail.head
											if(Op.callproperty == op9.opCode &&
													op9.asInstanceOf[CallProperty].property == readInt) {
												nextOp()
												tail = tail.tail
												val op10 = tail.head
												if(Op.convert_i == op10.opCode) {
													nextOp()
													target = GetInt() :: op4 :: target
													modified = true
												} else {
													target = op9 :: op8 :: op7 :: op6 :: op5 :: op4 :: op3 :: op2 :: op :: target
												}
											} else {
												target = op8 :: op7 :: op6 :: op5 :: op4 :: op3 :: op2 :: op :: target
											}
										} else {
											target = op7 :: op6 :: op5 :: op4 :: op3 :: op2 :: op :: target
										}
									} else {
										target = op6 :: op5 :: op4 :: op3 :: op2 :: op :: target
									}
								} else {
									target = op5 :: op4 :: op3 :: op2 :: op :: target
								}
							} else {
								target = op4 :: op3 :: op2 :: op :: target
							}
						} else {
							target = op3 :: op2 :: op :: target
						}
					} else {
						target = op2 :: op :: target
					}
				} else {
					target = op :: target
				}
			} else {
				target = op :: target
			}
			nextOp()
		}

		if(modified) {
			bytecode.ops = target.reverse
		}

		modified
	}
}