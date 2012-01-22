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
package apparat.bytecode.optimization

import apparat.bytecode.Bytecode
import apparat.bytecode.operations._
import apparat.bytecode.analysis.{StackAnalysis, LocalCount}
import apparat.log.SimpleLog
import scala.annotation.tailrec
import apparat.abc._

/**
 * @author Patrick Le Clec'h
 *
 * - C like structure for as3 (the structure is mapped to alchemy memory)
 *
 * declare a Structure
 *
 * import apparat.memory.structure;
 * public final class Point extends Structure                   {
 *  // you can assign a type an a position to the field
 *  [Map(type='float', pos=0)]
 *  public var x:Number;
 *
 *  [Map(type='float', pos=1)]
 *  public var y:Number;
 * }
 *
 * then you can map it to an alchemy memory pointer
 *
 * import apparat.memory.map;
 * import apparat.memory.sizeOf;
 *
 * var pt:Point=map(intMemoryPtr, Point);
 * trace(sizeOf(Point)); // output the size of the structure (here 8)
 *
 * var x:Number=pt.x; // read the alchemy memory at intMemoryPtr address
 * pt.y=0.5; // write 0.5 to the alchemy memory at intMemoryPtr+4
 *
 * pt.next(); // advance the internal memory pointer to the sizeOf the structure
 * pt.prev(); // backward the internal memory pointer to the sizeOf the structure
 * pt.seekTo(12); // adjust the internal memory by 12 (the number will be multiply by the size of the Structure)
 *
 * x=pt.x; // read x from the new memory address
 *
 */

class MemoryHelperExpansion(abcs: List[Abc]) extends SimpleLog {
	case class FieldInfo(name: AbcName, position: Int, `type` : Symbol)

	case class StructureInfo(name: AbcQName, fields: List[FieldInfo])

	case class MemoryAlias(ptrRegister: Int, structureInfo: StructureInfo, orgPtrRegister: Int)

	case class CastRegister(register: Int, castName: AbcName)

	lazy val ApparatStructure = AbcQName('Structure, AbcNamespace(AbcNamespaceKind.Package, Symbol("apparat.memory")))

	lazy val MapName = AbcQName('map, AbcNamespace(22, Symbol("apparat.memory")))
	lazy val NextName = AbcQName('next, AbcNamespace(22, Symbol("")))
	lazy val PrevName = AbcQName('prev, AbcNamespace(22, Symbol("")))
	lazy val SeekByName = AbcQName('seekBy, AbcNamespace(22, Symbol("")))
	lazy val SeekToName = AbcQName('seekTo, AbcNamespace(22, Symbol("")))
	lazy val OffsetByName = AbcQName('offsetBy, AbcNamespace(22, Symbol("")))
	lazy val OffsetToName = AbcQName('offsetTo, AbcNamespace(22, Symbol("")))
	lazy val InternalPtrName = AbcQName('internalPtr, AbcNamespace(22, Symbol("")))
	lazy val SizeOfName = AbcQName('sizeOf, AbcNamespace(22, Symbol("apparat.memory")))
	lazy val SwapName = AbcQName('swap, AbcNamespace(22, Symbol("")))

	lazy val structures: Map[AbcName, AbcNominalType] = {
		var map = Map.empty[AbcName, AbcNominalType]
		for(abc <- abcs; nominal <- abc.types if (!nominal.inst.isInterface)) {
			val baseName = nominal.inst.base getOrElse AbcConstantPool.EMPTY_NAME
			if((baseName == ApparatStructure) || map.contains(baseName))
				map = map.updated(nominal.inst.name, nominal)
		}
		map
	}

	lazy val ANumber = AbcQName('Number, AbcNamespace(22, Symbol("")))
	lazy val AnInt = AbcQName('int, AbcNamespace(22, Symbol("")))
	lazy val AnUint = AbcQName('uint, AbcNamespace(22, Symbol("")))

	private var structureMap = Map.empty[AbcName, StructureInfo]

	def sizeOf(s: Symbol): Int = {
		s match {
			case 'byte => 1
			case 'short => 2
			case 'int => 4
			case 'uint => 4
			case 'float => 4
			case 'double => 8
			case 'sbyte => 1
			case 'sshort => 2
			case _ => error("Unknow type : " + s)
		}
	}

	def validateAndUpdateInfo() = {
		for(nominal <- structures.valuesIterator) {
			if(nominal.inst.traits.length == 0) error(nominal.name + " have no field member.")
			if(nominal.klass.traits.length != 0) error(nominal.name + " must not have methods.")
			if(!nominal.inst.isSealed) error(nominal.name + " must not be a dynamic class.")

			implicit def any2int(x: Any): Int = {
				x match {
					case s: Symbol => augmentString(s.toString.tail).toInt
					case i: Int => i
					case _ => Int.MaxValue
				}
			}
			def getMetadataPosition(t: AbcTrait): Int = {
				t.metadata match {
					case Some(metadatas) => {
						metadatas.find(_.name == 'Map) match {
							case Some(metadata) => metadata.attributes.getOrElse('pos, Int.MaxValue)
							case _ => Int.MaxValue
						}
					}
					case _ => Int.MaxValue
				}
			}

			def getType(name: AbcName): Symbol = {
				name match {
					case qname: AbcQName => qname match {
						case ANumber => 'double
						case AnInt => qname.name
						case AnUint => qname.name
						case _ => error(qname + " must be Number, int, or uint")
					}
					case _ => error(name + " must of type AbcQName")
				}
			}

			def getMetadataType(t: AbcTrait): Symbol = {
				t match {
					case aSlot: AbcTraitSlot => {
						t.metadata match {
							case Some(metadatas) => {
								metadatas.find(_.name == 'Map) match {
									case Some(metadata) => metadata.attributes.getOrElse('type, getType(aSlot.typeName))
									case _ => getType(aSlot.typeName)
								}
							}
							case _ => getType(aSlot.typeName)
						}
					}
					case _ => error(t + " have to be of type AbcTraitSlot")
				}
			}

			var basePos = 0
			var pos = 0
			var oldPos = Int.MaxValue
			var oldSize = 0
			var fields = List.empty[FieldInfo]

			structureMap.get(nominal.inst.base getOrElse AbcConstantPool.EMPTY_NAME) match {
				case Some(struct) => {
					val field: FieldInfo = struct.fields.head
					basePos = field.position
					oldSize = sizeOf(field.`type`)
					fields = struct.fields
				}
				case _ =>
			}
			for(t <- nominal.inst.traits.sortWith((t1, t2) => {
				val p1 = getMetadataPosition(t1)
				val p2 = getMetadataPosition(t2)
				if(p1 == p2) {
					sizeOf(getMetadataType(t1)) < sizeOf(getMetadataType(t2))
				} else {
					p1 < p2
				}
			})) {
				val fPos = getMetadataPosition(t)
				val `type` = getMetadataType(t)
				if(fPos == Int.MaxValue || fPos != oldPos) {
					pos += oldSize
					fields = FieldInfo(t.name, pos + basePos, `type`) :: fields
					oldSize = sizeOf(`type`)
					oldPos = fPos
				} else {
					fields = FieldInfo(t.name, pos + basePos, `type`) :: fields
					oldSize = sizeOf(`type`)
				}
			}
			structureMap = structureMap.updated(nominal.name, StructureInfo(nominal.name, fields))
		}
	}

	validateAndUpdateInfo()

	@inline private def registerOf(op: AbstractOp): Int = op match {
		case opWithRegister: OpWithRegister => opWithRegister.register
		case _ => error("Unexpected " + op + ".")
	}

	def expand(bytecode: Bytecode, haveBeenModified: Boolean = false): Boolean = {
		if(structures.isEmpty)
			false
		else {
			var callToBeReplaced: AbstractOp = Nop()
			var replaceCallWith: AbstractOp = Nop()

			var dupToBeReplaced: AbstractOp = Nop()
			var getLocalToDuplicate: GetLocal = GetLocal(0)

			var removePop = false
			var replaceDup = false
			var duplicateGetLocal = false

			var removes = List.empty[AbstractOp]
			var replacements = Map.empty[AbstractOp, List[AbstractOp]]

			for(op <- bytecode.ops) {
				op match {
					case Dup() => {
						if(duplicateGetLocal) {
							replaceDup = false
							replacements = replacements.updated(op, List(GetLocal(getLocalToDuplicate.register)))
						} else {
							replaceDup = true
							dupToBeReplaced = op
						}
						removePop = false
						duplicateGetLocal = false
					}
					case CallProperty(property, argCount) => {
						callToBeReplaced = op
						replaceCallWith = CallPropVoid(property, argCount)
						removePop = true
						replaceDup = false
						duplicateGetLocal = false
					}
					case gl@GetLocal(register) => {
						getLocalToDuplicate = gl
						duplicateGetLocal = true
						removePop = false
						replaceDup = false
					}
					case Pop() if (removePop) => {
						removePop = false
						replaceDup = false
						duplicateGetLocal = false
						removes = op :: removes
						replacements = replacements.updated(callToBeReplaced, List(replaceCallWith))
					}
					case SetLocal(register) if (replaceDup) => {
						replaceDup = false
						removePop = false
						duplicateGetLocal = false
						replacements = replacements.updated(dupToBeReplaced, List(SetLocal(register)))
						replacements = replacements.updated(op, List(GetLocal(register)))
					}
					case _ => {
						removePop = false
						replaceDup = false
						duplicateGetLocal = false
					}
				}
			}

			if(removes.nonEmpty || replacements.nonEmpty) {
				removes foreach {
					bytecode remove _
				}
				replacements.iterator foreach {
					x => bytecode.replace(x._1, x._2)
				}
				$expand(bytecode, true)
			} else {
				$expand(bytecode, haveBeenModified)
			}
		}
	}

	@tailrec final def $expand(bytecode: Bytecode, haveBeenModified: Boolean = false): Boolean = {
		var balance = 0
		var removes = List.empty[AbstractOp]
		var removeCoerce = false
		var removeConvertInt = false
		var setStructure = false
		var parameters = List.empty[AbstractOp]
		var replacements = Map.empty[AbstractOp, List[AbstractOp]]
		var localCount = LocalCount(bytecode)
		var registerMap = Map.empty[Int, MemoryAlias]

		val optDebugFile: Option[DebugFile] = bytecode.ops.find(op => op.opCode == Op.debugfile).asInstanceOf[Option[DebugFile]]
		var lineNum = 0

		var currentStructure: Option[StructureInfo] = None

		val markers = bytecode.markers

		var castRegister: Option[CastRegister] = None
		var castIsWaitingForRead = false
		var castIsWaitingForWrite = false

		def getPowerOf2(num: Int) = {
			if((num & (num - 1)) == 0) {
				@tailrec def loop(i: Int, pos: Int = -1): Int = {
					if(i == 0) pos
					else loop(i >>> 1, pos + 1)
				}
				loop(num)
			} else 0
		}

		def getIntConstantOnStack(stackDepthRequired: Int = 2): Option[(Int, AbstractOp)] = {
			if(parameters.size == stackDepthRequired) {
				parameters.head match {
					case pb@PushByte(arg) => Some((arg, pb))
					case ps@PushShort(arg) => Some((arg, ps))
					case pi@PushInt(arg) => Some((arg, pi))
					case _ => None
				}
			} else None
		}

		def getPushOpFromSize(size: Int): AbstractOp = {
			if(size > ((1 << 15) - 1)) {
				PushInt(size)
			} else if(size > ((1 << 7) - 1)) {
				PushShort(size)
			} else if(size >= 0) {
				PushByte(size)
			} else {
				PushInt(size)
			}
		}

		@tailrec def unwindParameterStack(depth: Int, ret: AbstractOp = Nop()): AbstractOp = {
			if((depth < 0) && parameters.nonEmpty) {
				val op = parameters.head
				parameters = parameters.tail
				if(markers.hasMarkerFor(op)) {
					val i = parameters.indexWhere(p => p match {
						case m: OpWithMarker if (m.marker.op.get == op) => true
						case _ => false
					})
					if(i >= 0)
						parameters = parameters.drop(i + 1)
				}
				unwindParameterStack(depth + op.operandDelta, op)
			} else ret
		}

		def throwError(msg: String) {
			bytecode.dump()
			optDebugFile match {
				case Some(debugFile) => error(debugFile.file + ":" + lineNum + " => " + msg)
				case _ => error(msg)
			}
		}

		object Optimisation extends Enumeration {
			type Optimisation = Value
			val None, RemoveCoerce, RemoveConvertInt = Value
		}


		@inline def clearRemove() {
			removeCoerce = false
			removeConvertInt = false
		}

		var optimisation = Optimisation.None

		@inline def clearOptimisation() {
			optimisation = Optimisation.None
		}

		@inline def preCheck() {
			if(setStructure) throwError("The result of the map call must be assigned to a local var")
		}

		@inline def clearCast() {
			if(!castIsWaitingForRead && !castIsWaitingForWrite) castRegister = None
			castIsWaitingForRead = false
		}

		for(op <- bytecode.ops) {
			@inline def pushOp() {
				if(balance > 0) parameters = op :: parameters
			}

			op match {
				case Coerce(aName) => {
					if(optimisation == Optimisation.RemoveCoerce)
						removes = op :: removes
					else {
						preCheck()
						pushOp()
					}
					clearOptimisation()
				}
				case ConvertInt() => {
					preCheck()

					if(optimisation == Optimisation.RemoveConvertInt)
						removes = op :: removes
					else
						pushOp()
					clearOptimisation()
				}
				case DebugLine(line) => {
					lineNum = line
					clearOptimisation()
				}
				case FindPropStrict(aName) => {
					preCheck()

					clearOptimisation()
					clearCast()

					aName match {
						case MapName => {
							balance += 1
							removes = op :: removes
						}
						case SizeOfName => {
							balance += 1
							removes = op :: removes
						}
						case _ => {
							structureMap.get(aName) match {
								case Some(struct) => {
									balance += 1
									removes = op :: removes
								}
								case _ => pushOp
							}
						}
					}
				}
				case CallProperty(aName, argCount) => {
					preCheck()

					clearOptimisation()
					clearCast()

					aName match {
						case MapName if (argCount == 2) => {
							if(balance <= 0) throwError("Invalid CallProperty " + aName)
							removes = op :: removes
							balance -= 1
							optimisation = Optimisation.RemoveCoerce
							setStructure = true
							parameters.head match {
								case gl@GetLex(sName) if (sName.kind == AbcNameKind.QName) => {
									currentStructure = structureMap.get(sName.asInstanceOf[AbcQName])
									if(currentStructure == None) throwError("map is expecting a Class of type Structure as second arguments")
									removes = gl :: removes
									unwindParameterStack(op.operandDelta)
									parameters = PushByte(0) :: parameters
								}
								case _ => throwError("map is expecting a Class of type Structure as second arguments")
							}
						}
						case SizeOfName if (argCount == 1) => {
							if(balance <= 0) throwError("Invalid CallProperty " + aName)
							removes = op :: removes
							balance -= 1
							parameters.head match {
								case gl@GetLex(sName) if (sName.kind == AbcNameKind.QName) => {
									structureMap.get(sName.asInstanceOf[AbcQName]) match {
										case Some(struct) => {
											val field = struct.fields.head
											val size = field.position + sizeOf(field.`type`)
											var args = List.empty[AbstractOp]
											args = getPushOpFromSize(size) :: args
											replacements = replacements.updated(gl, args.reverse)
											unwindParameterStack(op.operandDelta)
											parameters = PushByte(0) :: parameters
										}
										case _ => throwError("sizeOf is expecting a Class of type Structure as argument")
									}
								}
								case _ => throwError("sizeOf is expecting a Class of type Structure as argument")
							}
						}

						case InternalPtrName if (argCount == 0 && (balance > 0)) => {
							preCheck()
							optimisation = Optimisation.RemoveConvertInt
							//              unwindParameterStack(op.operandDelta + 1)
							//              parameters.head match {
							unwindParameterStack(-op.popOperands) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
									//                  parameters = parameters.tail
									parameters = PushByte(0) :: parameters
								}
								case _ => throwError("internalPtr called on unmapped Structure")
							}
						}
						case _ => {
							preCheck()
							structureMap.get(aName) match {
								case Some(struct) => {
									if(balance <= 0) throwError("Invalid cast " + aName)
									if(argCount != 1) throwError("Invalid argument(s) for cast " + aName)
									removes = op :: removes
									parameters.head match {
										case gl@GetLocal(register) if (registerMap.contains(register)) => {
											if (castRegister==None) balance -= 1
											castIsWaitingForRead = true
											castIsWaitingForWrite = true
											unwindParameterStack(-op.popOperands)
											removes = op :: gl :: removes
											castRegister = Some(CastRegister(register, aName))
											//											balance -= 1
											parameters = PushByte(0) :: parameters
										}
										case _ => throwError("cast " + aName + " called on unmapped Structure")
									}
								}
								case _ => {
									unwindParameterStack(-op.popOperands)
									parameters = PushByte(0) :: parameters
								}
							}
						}
					}
				}
				case CallPropVoid(aName, argCount) => {
					preCheck()

					clearOptimisation()
					clearCast()

					aName match {
						case OffsetToName if (argCount == 1 && (balance > 0) && parameters.nonEmpty) => {
							preCheck()
							optimisation = Optimisation.RemoveConvertInt
							unwindParameterStack(op.operandDelta) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									args = SetLocal(memAlias.ptrRegister) :: args
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
								}
								case _ => throwError("offsetTo called on unmapped Structure")
							}
						}
						case SeekToName if (argCount == 1 && (balance > 0) && parameters.nonEmpty) => {
							preCheck()
							optimisation = Optimisation.RemoveConvertInt

							val optConst = getIntConstantOnStack()

							unwindParameterStack(op.operandDelta) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									if(size == 0) {
										args = Nop() :: args
									} else {
										def addMultiply() {
											args = getPushOpFromSize(size) :: args
											args = MultiplyInt() :: args
											args = GetLocal(memAlias.orgPtrRegister) :: args
											args = AddInt() :: args
										}
										def addShift(bitShift: Int) {
											args = PushByte(bitShift) :: args
											args = ShiftLeft() :: args
											args = GetLocal(memAlias.orgPtrRegister) :: args
											args = AddInt() :: args
										}
										optConst match {
											case Some((0, op)) => {
												removes = op :: removes
												args = GetLocal(memAlias.orgPtrRegister) :: args
											}
											case Some((1, op)) => {
												removes = op :: removes
												args = getPushOpFromSize(size) :: args
												args = GetLocal(memAlias.orgPtrRegister) :: args
												args = AddInt() :: args
											}
											case Some((x, op)) => {
												getPowerOf2(size) match {
													case 0 => {
														getPowerOf2(x) match {
															case 0 => addMultiply()
															case bc@_ => {
																removes = op :: removes
																args = getPushOpFromSize(size) :: args
																args = PushByte(bc) :: args
																args = ShiftLeft() :: args
																args = GetLocal(memAlias.orgPtrRegister) :: args
																args = AddInt() :: args
															}
														}
													}
													case bc@_ => addShift(bc)
												}
											}
											case _ => {
												getPowerOf2(size) match {
													case 0 => addMultiply()
													case bc@_ => addShift(bc)
												}
											}
										}
										args = SetLocal(memAlias.ptrRegister) :: args
									}
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
								}
								case _ => throwError("seekTo called on unmapped Structure")
							}
						}
						case SeekByName if (argCount == 1 && (balance > 0) && parameters.nonEmpty) => {
							preCheck()
							optimisation = Optimisation.RemoveConvertInt

							val optConst = getIntConstantOnStack()

							unwindParameterStack(op.operandDelta) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									if(size == 0) {
										args = Nop() :: args
									} else {
										def addMultiply() {
											args = getPushOpFromSize(size) :: args
											args = MultiplyInt() :: args
											args = GetLocal(memAlias.ptrRegister) :: args
											args = AddInt() :: args
										}
										def addShift(bitShift: Int) {
											args = PushByte(bitShift) :: args
											args = ShiftLeft() :: args
											args = GetLocal(memAlias.ptrRegister) :: args
											args = AddInt() :: args
										}
										optConst match {
											case Some((0, op)) => {
												removes = op :: removes
												args = GetLocal(memAlias.ptrRegister) :: args
											}
											case Some((1, op)) => {
												removes = op :: removes
												args = getPushOpFromSize(size) :: args
												args = GetLocal(memAlias.ptrRegister) :: args
												args = AddInt() :: args
											}
											case Some((x, op)) => {
												getPowerOf2(size) match {
													case 0 => {
														getPowerOf2(x) match {
															case 0 => addMultiply()
															case bc@_ => {
																removes = op :: removes
																args = getPushOpFromSize(size) :: args
																args = PushByte(bc) :: args
																args = ShiftLeft() :: args
																args = GetLocal(memAlias.ptrRegister) :: args
																args = AddInt() :: args
															}
														}
													}
													case bc@_ => addShift(bc)
												}
											}
											case _ => {
												getPowerOf2(size) match {
													case 0 => addMultiply()
													case bc@_ => addShift(bc)
												}
											}
										}
										args = SetLocal(memAlias.ptrRegister) :: args
									}
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
								}
								case _ => throwError("seekBy called on unmapped Structure")
							}
						}
						case SwapName if (argCount == 1 && (balance > 0) && parameters.size == 2) => {
							preCheck()
							val p1 = parameters.head
							parameters = parameters.tail
							val p2 = parameters.head
							parameters = parameters.tail
							p1 match {
								case gl1@GetLocal(register1) if (registerMap.contains(register1)) => {
									p2 match {
										case gl2@GetLocal(register2) if (registerMap.contains(register2)) => {
											val memAlias1 = registerMap.get(register1).get
											val memAlias2 = registerMap.get(register2).get
											removes = List(op, gl1, gl2) ::: removes
											balance -= 1
											registerMap = registerMap.updated(register1, memAlias2)
											registerMap = registerMap.updated(register2, memAlias1)
										}
										case _ => throwError("swap expected a Structure as parameter")
									}
								}
								case _ =>
							}
						}
						case OffsetByName if (argCount == 1 && (balance > 0) && parameters.nonEmpty) => {
							preCheck()
							optimisation = Optimisation.RemoveConvertInt
							unwindParameterStack(op.operandDelta) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args
									args = AddInt() :: args
									args = SetLocal(memAlias.ptrRegister) :: args
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
									//                  parameters = parameters.tail
								}
								case _ => throwError("offsetBy called on unmapped Structure")
							}
						}
						case NextName if (argCount == 0 && (balance > 0) && parameters.nonEmpty) => {
							preCheck()
							//              optimisation = Optimisation.RemoveConvertInt
							unwindParameterStack(op.operandDelta) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args
									if(size != 0) {
										args = getPushOpFromSize(size) :: args
										args = AddInt() :: args
										args = SetLocal(memAlias.ptrRegister) :: args
									}
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
								}
								case _ => throwError("next called on unmapped Structure")
							}
						}
						case PrevName if (argCount == 0 && (balance > 0) && parameters.nonEmpty) => {
							preCheck()
							unwindParameterStack(op.operandDelta) match {
								case gl@GetLocal(register) if (registerMap.contains(register)) => {
									removes = gl :: removes
									val memAlias = registerMap.get(register).get
									val structInfo = memAlias.structureInfo
									val field = structInfo.fields.head
									val size = field.position + sizeOf(field.`type`)
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args
									if(size != 0) {
										args = getPushOpFromSize(size) :: args
										args = SubtractInt() :: args
										args = SetLocal(memAlias.ptrRegister) :: args
									}
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
								}
								case _ => throwError("prev called on unmapped Structure")
							}
						}
						case _ => {
							preCheck()
							pushOp()
						}
					}
				}
				case GetLocal(register) if (registerMap.contains(register)) => {
					preCheck()
					clearOptimisation()
					clearCast()
					if(castRegister == None) balance += 1
					pushOp()
				}
				case GetProperty(aName) if (castRegister != None && castIsWaitingForRead) => {
					preCheck()
					castIsWaitingForWrite = false
					balance -= 1
					unwindParameterStack(-op.popOperands)
					val memAlias = registerMap.get(castRegister.get.register).get
					val structInfo = structureMap.get(castRegister.get.castName).get

					structInfo.fields.find(_.name == aName) match {
						case Some(field) => {
							var args = List.empty[AbstractOp]
							args = GetLocal(memAlias.ptrRegister) :: args

							val size = field.position

							if(size > ((1 << 15) - 1)) {
								args = PushInt(size) :: args
								args = AddInt() :: args
							} else if(size > ((1 << 7) - 1)) {
								args = PushShort(size) :: args
								args = AddInt() :: args
							} else if(size > 0) {
								args = PushByte(size) :: args
								args = AddInt() :: args
							}

							args = {
								field.`type` match {
									case 'float => List(GetFloat())
									case 'double => List(GetDouble())
									case 'int => List(GetInt())
									case 'uint => List(GetInt())
									case 'byte => List(GetByte())
									case 'short => List(GetShort())
									case 'sbyte => List(Sign8(), GetByte())
									case 'sshort => List(Sign16(), GetShort())
									case _ => throwError("Unknown type : " + field.`type`); List(Nop())
								}
							} ::: args
							replacements = replacements.updated(op, args.reverse)
							parameters = PushByte(0) :: parameters
						}
						case _ => throwError("Can't find field " + aName + " in " + structInfo.name)
					}
					clearOptimisation()
					clearCast()
				}
				case GetProperty(aName) if (balance > 0 && parameters.nonEmpty) => {
					preCheck()
					clearOptimisation()
					clearCast()
					unwindParameterStack(-op.popOperands) match {
						case gl@GetLocal(register) if (registerMap.contains(register)) => {
							val memAlias = registerMap.get(register).get
							val structInfo = memAlias.structureInfo
							structInfo.fields.find(_.name == aName) match {
								case Some(field) => {
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args

									val size = field.position

									if(size > ((1 << 15) - 1)) {
										args = PushInt(size) :: args
										args = AddInt() :: args
									} else if(size > ((1 << 7) - 1)) {
										args = PushShort(size) :: args
										args = AddInt() :: args
									} else if(size > 0) {
										args = PushByte(size) :: args
										args = AddInt() :: args
									}

									args = {
										field.`type` match {
											case 'float => List(GetFloat())
											case 'double => List(GetDouble())
											case 'int => List(GetInt())
											case 'uint => List(GetInt())
											case 'byte => List(GetByte())
											case 'short => List(GetShort())
											case 'sbyte => List(Sign8(), GetByte())
											case 'sshort => List(Sign16(), GetShort())
											case _ => throwError("Unknown type : " + field.`type`); List(Nop())
										}
									} ::: args
									replacements = replacements.updated(op, args.reverse)
									balance -= 1
									removes = gl :: removes
									parameters = PushByte(0) :: parameters
								}
								case _ => throwError("Can't find field " + aName + " in " + structInfo.name)
							}
						}
						case _ => parameters = PushByte(0) :: parameters //unwindParameterStack(op.operandDelta)
					}
				}
				case Kill(register) if (registerMap.contains(register)) => {
					preCheck()
					clearOptimisation()
					clearCast()
					// TODO remove register alias
				}
				case SetLocal(register) if (setStructure) => {
					clearOptimisation()
					clearCast()

					setStructure = false
					currentStructure match {
						case Some(structureInfo) => {
							registerMap = registerMap.updated(register, MemoryAlias(localCount, structureInfo, localCount + 1))
							replacements = replacements.updated(op, List(Dup(), SetLocal(localCount), SetLocal(localCount + 1)))
							localCount += 2
							currentStructure = None
						}
						case _ => throwError("map is expecting a Class of type Structure as second arguments")
					}
				}
				case SetProperty(aName) if (balance > 0 && parameters.nonEmpty) => {
					clearOptimisation()
					clearCast()

					unwindParameterStack(op.operandDelta) match {
						case gl@GetLocal(register) if (registerMap.contains(register)) => {
							val memAlias = registerMap.get(register).get
							val structInfo = memAlias.structureInfo
							structInfo.fields.find(_.name == aName) match {
								case Some(field) => {
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args
									val size = field.position
									if(size > ((1 << 15) - 1)) {
										args = PushInt(size) :: args
										args = AddInt() :: args
									} else if(size > ((1 << 7) - 1)) {
										args = PushShort(size) :: args
										args = AddInt() :: args
									} else if(size > 0) {
										args = PushByte(size) :: args
										args = AddInt() :: args
									}
									args = {
										field.`type` match {
											case 'float => SetFloat()
											case 'double => SetDouble()
											case 'int => SetInt()
											case 'uint => SetInt()
											case 'byte => SetByte()
											case 'short => SetShort()
											case 'sbyte => SetByte()
											case 'sshort => SetShort()
											case _ => throwError("Unknown type : " + field.`type`); Nop()
										}
									} :: args
									replacements = replacements.updated(op, args.reverse)
									removes = gl :: removes
									balance -= 1
								}
								case _ => throwError("Can't find field " + aName + " in " + structInfo.name)
							}
						}
						case x if (castIsWaitingForWrite && parameters.isEmpty) => {
							castIsWaitingForWrite = false
							val memAlias = registerMap.get(castRegister.get.register).get
							val structInfo = structureMap.get(castRegister.get.castName).get

							structInfo.fields.find(_.name == aName) match {
								case Some(field) => {
									var args = List.empty[AbstractOp]
									args = GetLocal(memAlias.ptrRegister) :: args
									val size = field.position
									if(size > ((1 << 15) - 1)) {
										args = PushInt(size) :: args
										args = AddInt() :: args
									} else if(size > ((1 << 7) - 1)) {
										args = PushShort(size) :: args
										args = AddInt() :: args
									} else if(size > 0) {
										args = PushByte(size) :: args
										args = AddInt() :: args
									}

									args = {
										field.`type` match {
											case 'float => SetFloat()
											case 'double => SetDouble()
											case 'int => SetInt()
											case 'uint => SetInt()
											case 'byte => SetByte()
											case 'short => SetShort()
											case 'sbyte => SetByte()
											case 'sshort => SetShort()
											case _ => throwError("Unknown type : " + field.`type`); Nop()
										}
									} :: args
									replacements = replacements.updated(op, args.reverse)
									//									removes = gl :: removes
									balance -= 1
								}
								case _ => unwindParameterStack(op.operandDelta)
							}
							clearCast()
						}
						case _ => unwindParameterStack(op.operandDelta)
					}
				}
				case _ => {
					preCheck()
					clearOptimisation()
					clearCast()
					pushOp()
				}
			}

			if(balance == 0) {
				parameters = Nil
			}
		}
		if(castIsWaitingForWrite) {
			throwError("Cast " + castRegister.get.castName + "is never read or write")
		}
		if(removes.nonEmpty || replacements.nonEmpty) {
			removes foreach {
				bytecode remove _
			}
			replacements.iterator foreach {
				x => bytecode.replace(x._1, x._2)
			}

			bytecode.body match {
				case Some(body) => {
					val (operandStack, scopeStack) = StackAnalysis(bytecode)
					body.localCount = localCount
					body.maxStack = operandStack
					body.maxScopeDepth = body.initScopeDepth + scopeStack
				}
				case None => log.warning("Bytecode body missing. Cannot adjust stack/locals.")
			}
			$expand(bytecode, true)
		} else {
			haveBeenModified
		}
	}
}
