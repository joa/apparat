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

/**
 * @author Joa Ebert
 */
class MacroExpansion(abcs: List[Abc]) {
	lazy val macros: Map[AbcName, AbcNominalType] = {
		Map((for(abc <- abcs; nominal <- abc.types if nominal.inst.name.name.name endsWith "Macro") yield (nominal.inst.name -> nominal)):_*)
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

							method.body match {//TODO transfer markers
								case Some(body) => body.bytecode match {
									case Some(bytecode) => {
										parameters = parameters.reverse
										val parameterCount = method.parameters.length
										val replacement = bytecode.ops.slice(2, bytecode.length - 1) map {
											//TODO add all OpWithRegister
											case GetLocal(x) if x > parameterCount => GetLocal(localCount + x)
											case SetLocal(x) if x > parameterCount => SetLocal(localCount + x)
											case other => other
										} map {
											//TODO add all OpWithRegister
											case GetLocal(0) => error("Illegal GetLocal(0).")
											case GetLocal(x) if x <= parameterCount => {
												parameters(x - 1)
											}
											case SetLocal(x) if x != 0 && x <= parameterCount => {
												parameters(x - 1) match {
													case GetLocal(y) => SetLocal(y)
													case other => Pop()
												}
											}
											case other => other
										}
										body.localCount
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