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
package apparat.bytecode.optimization

import apparat.bytecode.Bytecode
import apparat.bytecode.operations._
import apparat.abc.{AbcTraitMethod, AbcName, AbcNominalType, Abc}
import apparat.bytecode.analysis.LocalCount
import scala.math.max

/**
 * @author Joa Ebert
 */
class MacroExpansion(abcs: List[Abc]) {
	lazy val macros: Map[AbcName, AbcNominalType] = {
		Map((for(abc <- abcs; nominal <- abc.types if nominal.inst.name.name.name endsWith "Macro") yield (nominal.inst.name -> nominal)):_*)
	}

	@inline private def registerOf(op: AbstractOp): Int = op match {
		case opWithRegister: OpWithRegister => opWithRegister.register
		case _ => error("Unexpected "+op+".")
	}

	def expand(bytecode: Bytecode): Bytecode = {
		var modified = false
		var balance = 0
		var removes = List.empty[AbstractOp]
		var removePop = false
		var macroStack = List.empty[AbcNominalType]
		var parameters = List.empty[AbstractOp]
		var replacements = Map.empty[AbstractOp, List[AbstractOp]]
		var localCount = LocalCount(bytecode)

		@inline def insert(op: AbstractOp, property: AbcName, numArguments: Int) = {
			macroStack.head.klass.traits find (_.name == property) match {
				case Some(anyTrait) => {
					anyTrait match {
						case methodTrait: AbcTraitMethod => {
							if(numArguments != parameters.length) {
								error("Expected "+numArguments+" arguments, got "+parameters.length+".")
							}

							val method = methodTrait.method

							method.body match {//TODO transfer markers by index (+-2)
								case Some(body) => body.bytecode match {
									case Some(bytecode) => {
										parameters = parameters.reverse
										val parameterCount = method.parameters.length
										val newLocals = body.localCount - parameterCount - 1
										val replacement = bytecode.ops.slice(2, bytecode.length - 1) map {
											//
											// Shift all local variables that are not parameters
											//
											case GetLocal(x) if x > parameterCount => GetLocal(localCount + x - parameterCount - 1)
											case SetLocal(x) if x > parameterCount => SetLocal(localCount + x - parameterCount - 1)
											case DecLocal(x) if x > parameterCount => DecLocal(localCount + x - parameterCount - 1)
											case DecLocalInt(x) if x > parameterCount => DecLocalInt(localCount + x - parameterCount - 1)
											case IncLocal(x) if x > parameterCount => IncLocal(localCount + x - parameterCount - 1)
											case IncLocalInt(x) if x > parameterCount => IncLocalInt(localCount + x - parameterCount - 1)
											case Kill(x) if x > parameterCount => Kill(localCount + x - parameterCount - 1)
											case Debug(kind, name, x, extra) if x > parameterCount => Debug(kind, name, localCount + x - parameterCount - 1, extra)

											//
											// Prohibit use of "this" register
											//
											case GetLocal(0) => error("Illegal GetLocal(0).")
											case SetLocal(0) => error("Illegal SetLocal(0).")
											case DecLocal(0) => error("Illegal DecLocal(0).")
											case DecLocalInt(0) => error("Illegal DecLocalInt(0).")
											case IncLocal(0) => error("Illegal IncLocal(0).")
											case IncLocalInt(0) => error("Illegal IncLocalInt(0).")
											case Kill(0) => error("Illegal Kill(0).")
											case Debug(_, _, 0, _) => error("Illegal Debug(.., 0, ..)")

											//
											// Map all parameters to current function
											//
											case GetLocal(x) => parameters(x - 1)
											case SetLocal(x) => SetLocal(registerOf(parameters(x - 1)))
											case DecLocal(x) => DecLocal(registerOf(parameters(x - 1)))
											case DecLocalInt(x) => DecLocalInt(registerOf(parameters(x - 1)))
											case IncLocal(x) => IncLocal(registerOf(parameters(x - 1)))
											case IncLocalInt(x) => IncLocalInt(registerOf(parameters(x - 1)))
											case Kill(x) => Kill(registerOf(parameters(x - 1)))
											case Debug(kind, name, x, extra) => Debug(kind, name, registerOf(parameters(x - 1)), extra)

											case other => other
										} ::: ((0 until newLocals) foreach { register => Kill(localCount + register) })

										localCount += newLocals
									}
									case None => error("Bytecode is not loaded.")
								}
								case None => error("Method body is not defined.")
							}

							macroStack = macroStack.tail
							balance -= 1
							true
						}
						case _ => error("Unexpected trait "+t)
					}
				}
				case None => false
			}
		}
		
		for(op <- bytecode.ops) op match {
			case Pop() if removePop => {
				removes = op :: removes
				removePop = false
			}
			case GetLex(name) if macros contains name => {
				removes = op :: removes
				macroStack = macros(name) :: macroStack
				balance += 1
			}
			case CallPropVoid(property, numArguments) if balance > 0 => insert(op, property, numArguments)
			case CallProperty(property, numArguments) if balance > 0 => {
				if(insert(op, property, numArguments)) {
					removePop = true
				}
			}
			case p: AbstractPushOp if balance > 0 => parameters = p :: parameters
			case g: GetLocal if balance > 0 => parameters = g :: parameters
			case x if balance > 0 => error("Unexpected operation "+x)
			case _ =>
		}
		
		if(modified) {
			expand(bytecode)
		} else {
			bytecode
		}
	}
}