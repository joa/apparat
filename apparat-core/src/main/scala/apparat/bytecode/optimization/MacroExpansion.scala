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

import apparat.abc._
import apparat.bytecode.Bytecode
import apparat.bytecode.operations._
import apparat.bytecode.analysis.{StackAnalysis, LocalCount}
import apparat.log.SimpleLog
import scala.annotation.tailrec

/**
 * @author Joa Ebert
 */
class MacroExpansion(abcs: List[Abc]) extends SimpleLog {
	lazy val nsInline = AbcNamespace(AbcNamespaceKind.Package, Symbol("apparat.inline"))
	lazy val byRef = AbcQName('__byRef, nsInline)
	lazy val apparatMacro = AbcQName('Macro, nsInline)
	lazy val voidName = AbcQName('void, AbcNamespace(AbcNamespaceKind.Package, Symbol("")))
	lazy val macros: Map[AbcName, AbcNominalType] = {
		Map((for(abc <- abcs; nominal <- abc.types if ((nominal.inst.base getOrElse AbcConstantPool.EMPTY_NAME) == apparatMacro) && !nominal.inst.isInterface) yield (nominal.inst.name -> nominal)):_*)
	}

	def validate() = {
		for(nominal <- macros.valuesIterator) {
			if(nominal.inst.traits.length != 1) error("No instance members are allowed.")
			if(!nominal.inst.isSealed) error("Macro must not be a dynamic class.")
			for(t <- nominal.klass.traits) {
				t match {
					case AbcTraitMethod(_, _, method, _, _, _) => {
						if(!method.body.isDefined) error("Method body is not defined.")
						if(method.hasOptionalParameters) error("Macro may not have any optional parameters.")
						if(method.needsActivation) error("Macro may not require an activation scope.")
						if(method.needsRest) error("Macro may not use rest parameters.")
						if(method.setsDXNS) error("Macro may not change the default XML namespace.")
						if(method.returnType != voidName) error("Macro must return void.")
						if(method.body.get.exceptions.length != 0) error("Macro may not throw any exception.")
						if(method.body.get.traits != 0) error("Macro may not use constant variables or throw any exceptions.")
					}
					case other => error("Only static methods are allowed.")
				}
			}
		}
	}

	@inline private def registerOf(op: AbstractOp): Int = op match {
		case opWithRegister: OpWithRegister => opWithRegister.register
		case _ => error("Unexpected "+op+".")
	}

	@tailrec final def expand(bytecode: Bytecode, haveBeenModified:Boolean=false): Boolean = {
		var modified = false
		var balance = 0
		var byRefBalance = 0
		var byRefParameters = List.empty[AbstractOp]
		var byRefMap = Map.empty[AbstractOp, List[AbstractOp]]
		var byRefReplacementsMap = Map.empty[AbstractOp, List[AbstractOp]]
		var removes = List.empty[AbstractOp]
		var removePop = false
		var macroStack = List.empty[AbcNominalType]
		var parameters = List.empty[AbstractOp]
		var replacements = Map.empty[AbstractOp, List[AbstractOp]]
		var localCount = LocalCount(bytecode)
		var markers = bytecode.markers
		val debugFile = bytecode.ops find (_.opCode == Op.debugfile)

		def reverseSearchForStackDepthIndex(stack:List[AbstractOp], from:Int, toDepth:Int):Int = {
			@tailrec def loop(from:Int, currentDepth:Int):Int = {
				if (currentDepth != 0) {
					val op = stack.view(from)
					loop(from-1, currentDepth + op.pushOperands - op.popOperands)
				} else from + 1
			}
			loop(from, -toDepth)
		}
		
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
										val oldDebugFile = macro.ops.find (_.opCode == Op.debugfile)
										val delta = -macro.ops.indexWhere(_.opCode == Op.pushscope) - 1
										var replacement = (macro.ops.slice(macro.ops.indexWhere(_.opCode == Op.pushscope) + 1, macro.ops.length - 1) map {
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
											case Debug(_, _, 0, _) => Nop()

											//
											// Map all parameters to local registers.
											//
											case GetLocal(x) => parameters(x - 1) match {
												case getLocal: GetLocal => getLocal.copy()
												case nop@Nop() => byRefMap.get(parameters(x - 1)) match {
													case Some(ops) => {
														if (ops.size == 1) {
															ops.head match {
																case GetLocal(y) => GetLocal(y)
																case e@_ => error("Unexpected "+e+".")
															}
														} else {
															val ret = Nop()
															val newOps=ops.dropRight(1).map(_.opCopy()) ::: List(ops.last match{case GetProperty(aName)=>GetProperty(aName);case x@_=> error("Unexpexted " + x)})
															byRefReplacementsMap += (ret -> newOps)
															ret
														}
													}
													case _ => error("Unexpected "+nop+".")
												}
												case other => error("Unexpected "+other+".")
											}
											case SetLocal(x) => {
												byRefMap.get(parameters(x - 1)) match {
													case Some(ops) => {
														if (ops.size == 1) {
															ops.head match {
																case GetLocal(y) => SetLocal(y)
																case e@_ => error("Unexpected "+e+".")
															}
														} else {
															val ret=Nop()
															val newOps=ops.dropRight(1).map(_.opCopy()) ::: List(ops.last match{case GetProperty(aName)=>SetProperty(aName);case x@_=> error("Unexpexted " + x)})
															byRefReplacementsMap += (ret -> newOps)
															ret
														}
													}
													case _ => SetLocal(registerOf(parameters(x - 1)))
												}
											}
											case DecLocal(x) => DecLocal(registerOf(parameters(x - 1)))
											case DecLocalInt(x) => DecLocalInt(registerOf(parameters(x - 1)))
											case IncLocal(x) => IncLocal(registerOf(parameters(x - 1)))
											case IncLocalInt(x) => IncLocalInt(registerOf(parameters(x - 1)))
											case Kill(x) => Kill(registerOf(parameters(x - 1)))
											case Debug(kind, name, x, extra) if parameters(x - 1).opCode != Op.nop => Debug(kind, name, registerOf(parameters(x - 1)), extra)

											case other => other.opCopy()
										}) ::: List(Nop()) ::: (List.tabulate(newLocals) { register => Kill(localCount + register) })

										//
										// Switch debug file back into place.
										//

										/*debugFile match {
											case Some(debugFile) => oldDebugFile match {
												case Some(oldDebugFile) => (oldDebugFile.opCopy() :: replacement) ::: List(debugFile.opCopy())
												case None => replacement
											}
											case None => replacement
										}*/

										//
										// Clean up
										//
										parameters = Nil
										localCount += newLocals

										replacement = (replacement map {
											//
											// Patch all markers.
											//
											case branch:Jump => {
												val newOp = Jump(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfEqual => {
												val newOp = IfEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfTrue => {
												val newOp = IfTrue(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfFalse => {
												val newOp = IfFalse(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfGreaterEqual =>  {
												val newOp = IfGreaterEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfGreaterThan =>  {
												val newOp = IfGreaterThan(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfLessEqual =>  {
												val newOp = IfLessEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfLessThan =>  {
												val newOp = IfLessThan(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfNotGreaterEqual =>  {
												val newOp = IfNotGreaterEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfNotGreaterThan =>  {
												val newOp = IfNotGreaterThan(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfNotLessEqual =>  {
												val newOp = IfNotLessEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfNotLessThan =>  {
												val newOp = IfNotLessThan(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfNotEqual =>  {
												val newOp = IfNotEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfStrictEqual =>  {
												val newOp = IfStrictEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case branch:IfStrictNotEqual =>  {
												val newOp = IfStrictNotEqual(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
												markers.forwardMarker(branch, newOp)
												newOp
											}
											case lookup:LookupSwitch => {
												val newOp = LookupSwitch(markers mark replacement((macro.ops indexOf lookup.defaultCase.op.get) + delta), lookup.cases map {
													`case` => markers mark replacement((macro.ops indexOf `case`.op.get) + delta)//the reward is cheese!
												})
												markers.forwardMarker(lookup, newOp)
												newOp
											}
											case other => other
										})
										
										// patch byRef
										for ((op, ops) <- byRefReplacementsMap) {
											ops.last match {
												case GetProperty(_) => replacement = replacement.patch(replacement.indexWhere( _ == op), ops, 1)
												case SetProperty(_) => {
													val endIndex = replacement.indexWhere( _ == op)
													val startIndex = reverseSearchForStackDepthIndex(replacement, endIndex, 1)
													val (h,t) = ops.splitAt(ops.size - 1)
													replacement = replacement.patch(endIndex, t, 1)
													val valueOp = replacement(startIndex)
													replacement = replacement.patch(startIndex, h ::: List(valueOp), 1)
												}
												case _=>
											}
										}

										replacements += op -> replacement
									}
									case None => log.error("Bytecode of %s is not loaded.", property)
								}
								case None => log.error("Method body of %s is not defined.", property)
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
			case Pop() if removePop && byRefBalance==0 => {
				removes = op :: removes
				removePop = false
			}
			case GetLex(name) if macros.contains(name)  && byRefBalance==0 => {
				removes = op :: removes
				macroStack = macros(name) :: macroStack
				balance += 1
			}
			case CallPropVoid(property, numArguments) if byRefBalance > 0  && property==byRef => {
				byRefBalance -= 1
				removes = op :: removes
				var nop=Nop()
				parameters = nop :: parameters
				byRefMap += (nop -> byRefParameters.reverse)
				byRefParameters = Nil
			}
			case CallPropVoid(property, numArguments) if balance > 0  && byRefBalance==0 => {
				if(insert(op, property, numArguments)) {
					modified = true
				} else {
					error("Unexpected "+CallPropVoid(property, numArguments))
				}
			}
			case CallProperty(property, numArguments) if byRefBalance > 0  && property==byRef => {
				removes = op :: removes
				byRefBalance -= 1
				removePop = true
				var nop=Nop()
				parameters = nop :: parameters
				byRefMap += (nop -> byRefParameters.reverse)
				byRefParameters = Nil
			}
			case CallProperty(property, numArguments) if balance > 0  && byRefBalance==0 => {
				if(insert(op, property, numArguments)) {
					removePop = true
					modified = true
				} else {
					error("Unexpected "+CallPropVoid(property, numArguments))
				}
			}
			case g: GetLocal if balance > 0  && byRefBalance==0 => {
				parameters = g :: parameters
				removes = g :: removes
			}
			case FindPropStrict(aName) if balance>0 && aName == byRef => {
				if (byRefBalance>0) error("__byRef can't be nested")
				byRefBalance += 1
				removes = op :: removes
			}
			case DebugLine(line) => // skip DebugLine op
			case x if byRefBalance > 0 => {
				byRefParameters = x :: byRefParameters
				removes = op :: removes
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
				case None => log.warning("Bytecode body missing. Cannot adjust stack/locals.")
			}

			expand(bytecode, true)
		} else {
			haveBeenModified
		}
	}
}