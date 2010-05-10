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
import scala.math.max
import apparat.bytecode.analysis.{StackAnalysis, LocalCount}
import collection.immutable.SortedMap

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
		var markers = bytecode.markers

		@inline def insert(op: AbstractOp, property: AbcName, numArguments: Int) = {
			macroStack.head.klass.traits find (_.name == property) match {
				case Some(anyTrait) => {
					anyTrait match {
						case methodTrait: AbcTraitMethod => {
							if(numArguments != parameters.length) {
								error("Expected "+numArguments+" arguments, got "+parameters.length+".")
							}

							val method = methodTrait.method

							method.body match {
								case Some(body) => body.bytecode match {
									case Some(macro) => {
										parameters = parameters.reverse

										val parameterCount = method.parameters.length
										val newLocals = body.localCount - parameterCount - 1
										val replacement = (macro.ops.slice(2, macro.ops.length - 1) map {
											//
											// Shift all local variables that are not parameters.
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
											// Prohibit use of "this".
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
											// Map all parameters to local registers.
											//
											case GetLocal(x) => parameters(x - 1) match {
												case getLocal: GetLocal => getLocal.copy()
												case other => error("Unexpected "+other+".")
											}
											case SetLocal(x) => SetLocal(registerOf(parameters(x - 1)))
											case DecLocal(x) => DecLocal(registerOf(parameters(x - 1)))
											case DecLocalInt(x) => DecLocalInt(registerOf(parameters(x - 1)))
											case IncLocal(x) => IncLocal(registerOf(parameters(x - 1)))
											case IncLocalInt(x) => IncLocalInt(registerOf(parameters(x - 1)))
											case Kill(x) => Kill(registerOf(parameters(x - 1)))
											case Debug(kind, name, x, extra) => Debug(kind, name, registerOf(parameters(x - 1)), extra)

											case other => other
										}) ::: List(Nop()) ::: ((0 until newLocals) map { register => Kill(localCount + register) } toList)

										//
										// Clean up
										//
										parameters = Nil
										localCount += newLocals
										replacements += op -> (replacement map {
											case Jump(marker) => Jump(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfEqual(marker) => IfEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfFalse(marker) => IfFalse(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfGreaterEqual(marker) => IfGreaterEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfGreaterThan(marker) => IfGreaterThan(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfLessEqual(marker) => IfLessEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfLessThan(marker) => IfLessThan(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfNotGreaterEqual(marker) => IfNotGreaterEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfNotGreaterThan(marker) => {
												println("index: " + (macro.ops indexOf marker.op.get))
												IfNotGreaterThan(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											}
											case IfNotLessEqual(marker) => IfNotLessEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfNotLessThan(marker) => IfNotLessThan(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfNotEqual(marker) => IfNotEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfStrictEqual(marker) => IfStrictEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case IfStrictNotEqual(marker) => IfStrictNotEqual(markers mark replacement((macro.ops indexOf marker.op.get) - 2))
											case LookupSwitch(defaultCase, cases) => {
												LookupSwitch(markers mark replacement((macro.ops indexOf defaultCase.op.get) - 2), cases map {
													käse => markers mark replacement((macro.ops indexOf käse.op.get) - 2)//the reward is cheese!												})
												})
											}
											case other => other
										})
									}
									case None => error("Bytecode is not loaded.")
								}
								case None => error("Method body is not defined.")
							}

							macroStack = macroStack.tail
							balance -= 1
							true
						}
						case _ => error("Unexpected trait "+anyTrait)
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
			case CallPropVoid(property, numArguments) if balance > 0 => if(insert(op, property, numArguments)) {
				modified = true
			}
			case CallProperty(property, numArguments) if balance > 0 => {
				if(insert(op, property, numArguments)) {
					removePop = true
					modified = true
				}
			}
			case p: AbstractPushOp if balance > 0 => {
				parameters = p :: parameters
				removes = p :: removes
			}
			case g: GetLocal if balance > 0 => {
				parameters = g :: parameters
				removes = g :: removes
			}
			case x if balance > 0 => error("Unexpected operation "+x)
			case _ =>
		}
		
		if(modified) {
			removes foreach { bytecode remove _ }
			replacements.iterator foreach { x => bytecode.replace(x._1, x._2) }

			bytecode.body match {
				case Some(body) => {
					val (operandStack, scopeStack) = StackAnalysis(bytecode)
					body.localCount = localCount
					body.maxStack = operandStack
					body.maxScopeDepth = body.initScopeDepth + scopeStack
				}
				case None =>
			}

			expand(bytecode)
		} else {
			bytecode
		}
	}
}