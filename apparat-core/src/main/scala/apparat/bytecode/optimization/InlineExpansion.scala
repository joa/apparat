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
import annotation.tailrec

/**
 * @author Joa Ebert
 */
class InlineExpansion(abcs: List[Abc]) extends SimpleLog {
	lazy val nsGlobal = AbcNamespace(AbcNamespaceKind.Package, Symbol(""))
	lazy val voidName = AbcQName('void, nsGlobal)
	lazy val uintName = AbcQName('uint, nsGlobal)
	lazy val apparatMacro = AbcQName('Inlined, AbcNamespace(AbcNamespaceKind.Package, Symbol("apparat.inline")))
	lazy val macros: Map[AbcName, AbcNominalType] = {
		Map((for(abc <- abcs; nominal <- abc.types if ((nominal.inst.base getOrElse AbcConstantPool.EMPTY_NAME) == apparatMacro) && !nominal.inst.isInterface) yield (nominal.inst.name -> nominal)):_*)
	}

	def validate() {
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
						if(method.body.get.exceptions.length != 0) error("Macro may not throw any exception.")
						if(method.body.get.traits.length != 0) error("Macro may not use constant variables or throw any exceptions.")
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

	@tailrec final def expand(bytecode: Bytecode, haveBeenModified:Boolean=false)(parentABC: Option[Abc] = None): Boolean = {
		var modified = false
		var balance = 0
		var removes = List.empty[AbstractOp]
		var removePop = false
		var macroStack = List.empty[AbcNominalType]
		var replacements = Map.empty[AbstractOp, List[AbstractOp]]
		var localCount = LocalCount(bytecode)
		var markers = bytecode.markers
		val debugFile = bytecode.ops find (_.opCode == Op.debugfile)

		def constValue2Ops(const:AbcTraitConst, value:Any):List[AbstractOp] = const.valueType.get match {
			case AbcConstantType.Double => const.typeName match {
				case aName if aName == uintName => List((value.asInstanceOf[Double].toLong match {
					case i if (i >= -128 && i <= 127 ) => PushByte(i.toInt)
					case i if (i >= -32768 && i <= 32767 ) => PushShort(i.toInt)
					case i if (i >= -2147483648 && i <= 2147483647 ) => PushInt(i.toInt)
					case i => if (i < -2147483648) PushInt((i+(1<<32)).toInt) else PushInt((i-(1<<32)+1).toInt)
				}), CoerceUInt())
				case _ => List(PushDouble(value.asInstanceOf[Double]))
			}
			case AbcConstantType.False => List(PushFalse())
			case AbcConstantType.Int => {
				value.asInstanceOf[Int] match {
					case i if (i >= -128 && i <= 127 ) => List(PushByte(i))
					case i if (i >= -32768 && i <= 32767 ) => List(PushShort(i))
					case i => List(PushInt(i))
				}
			}
			case AbcConstantType.Null => List(PushNull())
			case AbcConstantType.True => List(PushTrue())
			case AbcConstantType.UInt => List( (value.asInstanceOf[Long] match {
					case i if (i >= -128 && i <= 127 ) => PushByte(i.toInt)
					case i if (i >= -32768 && i <= 32767 ) => PushShort(i.toInt)
					case i if (i >= -2147483648 && i <= 2147483647 ) => PushInt(i.toInt)
					case i => if (i < -2147483648) PushInt((i+(1<<32)).toInt) else PushInt((i-(1<<32)+1).toInt)
				}), CoerceUInt())
			case AbcConstantType.Undefined => List(PushUndefined())
			case AbcConstantType.Utf8 => List(PushString(value.asInstanceOf[Symbol]))
			case x@_ => error("Error : ("+const+") unknown constant type " + x)
		}

		@inline def insert(op: AbstractOp, property: AbcName, numArguments: Int) = {
			macroStack.head.klass.traits find (_.name == property) match {
				case Some(anyTrait) => {
					anyTrait match {
						case methodTrait: AbcTraitMethod => {
							val method = methodTrait.method

							method.body match {
								case Some(body) => body.bytecode match {
									case Some(macro) => {
										val parameterCount = method.parameters.length
										val newLocals = body.localCount - 1
										val oldDebugFile = macro.ops.find (_.opCode == Op.debugfile)
										val gathering = Nop()
										val delta = -macro.ops.indexWhere(_.opCode == Op.pushscope) - 1 + parameterCount
										val nopReturn = macro.ops.last.isInstanceOf[OpThatReturns] && (macro.ops.count { case x: OpThatReturns => true; case _ => false } == 1)
										var replacement =
										(((parameterCount - 1) to 0 by -1) map { register => SetLocal(localCount + register) } toList) :::
										(macro.ops.slice(macro.ops.indexWhere(_.opCode == Op.pushscope) + 1, macro.ops.length) map {
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
											// Shift all local variables
											//
											case GetLocal(x) => GetLocal(localCount + x - 1)
											case SetLocal(x) => SetLocal(localCount + x - 1)
											case DecLocal(x) => DecLocal(localCount + x - 1)
											case DecLocalInt(x) => DecLocalInt(localCount + x - 1)
											case IncLocal(x) => IncLocal(localCount + x - 1)
											case IncLocalInt(x) => IncLocalInt(localCount + x - 1)
											case Kill(x) => Kill(localCount + x - 1)
											case Debug(kind, name, x, extra) => Debug(kind, name, localCount + x - 1, extra)

											//
											// Patch return statements
											//
											case ReturnValue() => if(nopReturn) Nop() else Jump(markers mark gathering)
											case ReturnVoid() => if(nopReturn) Nop() else Jump(markers mark gathering)

											case other => other.opCopy()
										}) ::: List(gathering) ::: (List.tabulate(newLocals) { register => Kill(localCount + register) })

										//
										// Clean up
										//
										localCount += newLocals

										replacement = replacement map {
											//
											// Patch all markers.
											//
											case branch:Jump if branch.marker.op.get != gathering => {
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
												val newOp = Jump(markers mark replacement((macro.ops indexOf branch.marker.op.get) + delta))
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
										}

										//
										// Switch debug file back into place.
										//

										replacement = debugFile match {
											case Some(debugFile) => oldDebugFile match {
												case Some(oldDebugFile) => List(oldDebugFile.opCopy()) ::: replacement ::: List(debugFile.opCopy())
												case None => replacement
											}
											case None => replacement
										}

										replacements += op -> replacement
									}
									case None => log.warning("Bytecode of %s is not available.", property)
								}
								case None => log.warning("Method body of %s is not defined.", property)
							}

							removePop = op.opCode == Op.callproperty && method.returnType == voidName
							macroStack = macroStack.tail
							balance -= 1
							true
						}
						case const: AbcTraitConst => {
							const.value match {
								case Some(value) => replacements += op -> constValue2Ops (const, value)
								case _ => error("Error : ("+const+") only constant int, uint, String, Number, Boolean can be inlined")
							}
							balance -= 1
							true
						}
						case _ => error("Unexpected trait "+anyTrait)
					}
				}
				case None => false
			}
		}

		val ops = bytecode.ops.view
		var slotCache = Map.empty[Int, Option[AbcTraitClass]]

		for((op, index) <- ops.zipWithIndex) op match {
			case Pop() if removePop => {
				removes = op :: removes
				removePop = false
			}
			case GetSlot(x) if (index>0) => if (ops(index-1).opCode == Op.getglobalscope) {
				{
					slotCache.getOrElse(x, null) match {
						case stc@Some(tc) => stc
						case None => None
						case _ => {
							parentABC match {
								case Some(abc) => {
									abc.scripts.flatMap(_.traits.collect{case atc:AbcTraitClass if (atc.index == x) => atc}) headOption match {
										case stc@Some(tc) if (macros.contains(tc.name)) => {
											slotCache = slotCache.updated(x, stc)
											stc
										}
										case _ => {
											slotCache = slotCache.updated(x, None)
											None
										}
									}
								}
								case _ => {
									slotCache = slotCache.updated(x, None)
									None
								}
							}
						}
					}
				} match {
					case Some(tc) => {
						removes = op :: ops(index-1) :: removes
						macroStack = macros(tc.name) :: macroStack
						balance += 1
					}
					case _ =>
				}
			}
			case GetLex(name) if macros contains name => {
				removes = op :: removes
				macroStack = macros(name) :: macroStack
				balance += 1
			}
			case CallPropVoid(property, numArguments) if balance > 0 => {
				if(insert(op, property, numArguments)) {
					modified = true
				}
			}
			case CallProperty(property, numArguments) if balance > 0 => {
				if(insert(op, property, numArguments)) {
					modified = true
				}
			}
			case GetProperty(property) if balance > 0 => {
				if (insert(op, property, 0)) {
					modified = true
				}
			}
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

			expand(bytecode, true)(parentABC)
		} else {
			haveBeenModified
		}
	}
}