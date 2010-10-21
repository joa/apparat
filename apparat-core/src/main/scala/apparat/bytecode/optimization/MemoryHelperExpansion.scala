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
 * public final class Point extends Structure                  {
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
 * pt.seekTo(12); // set the internal memory pointer to 12 (the number will be multiply by the size of the Structure)
 * pt.seekBy(12); // adjust the internal memory by 12 (the number will be multiply by the size of the Structure)
 *
 * x=pt.x; // read x from the new memory address
 *
 */

class MemoryHelperExpansion(abcs: List[Abc]) extends SimpleLog {

  case class FieldInfo(name: AbcName, position: Int, `type` : Symbol)

  case class StructureInfo(name: AbcQName, fields: List[FieldInfo])

  case class MemoryAlias(ptrRegister: Int, structureInfo: StructureInfo)

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

  lazy val structures: Map[AbcName, AbcNominalType] = {
    Map((for (abc <- abcs; nominal <- abc.types if ((nominal.inst.base getOrElse AbcConstantPool.EMPTY_NAME) == ApparatStructure) && !nominal.inst.isInterface) yield (nominal.inst.name -> nominal)): _*)
  }

  lazy val ANumber = AbcQName('Number, AbcNamespace(22, Symbol("")))
  lazy val AnInt = AbcQName('int, AbcNamespace(22, Symbol("")))
  lazy val AnUint = AbcQName('uint, AbcNamespace(22, Symbol("")))

  private var structureMap = Map.empty[AbcQName, StructureInfo]

  def sizeOf(s: Symbol): Int = {
    s match {
      case 'byte => 1
      case 'short => 2
      case 'int => 4
      case 'uint => 4
      case 'float => 4
      case 'double => 8
      case _ => error("Unknow type : " + s)
    }
  }

  def validateAndUpdateInfo() = {
    for (nominal <- structures.valuesIterator) {
      if (nominal.inst.traits.length == 1) error(nominal.name + " have no field member.")
      if (nominal.klass.traits.length != 0) error(nominal.name + " must not have methods.")
      if (!nominal.inst.isSealed) error(nominal.name + " must not be a dynamic class.")

      implicit def a2i(x: Any): Int = {
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

      var pos = 0
      var oldPos = Int.MaxValue
      var oldSize = 0
      var fields = List.empty[FieldInfo]
      for (t <- nominal.inst.traits.sortWith((t1, t2) => {
        val p1 = getMetadataPosition(t1)
        val p2 = getMetadataPosition(t2)
        if (p1 == p2) {
          sizeOf(getMetadataType(t1)) < sizeOf(getMetadataType(t2))
        } else {
          p1 < p2
        }
      })) {
        val fPos = getMetadataPosition(t)
        val `type` = getMetadataType(t)
        if (fPos == Int.MaxValue || fPos != oldPos) {
          pos += oldSize
          fields = FieldInfo(t.name, pos, `type`) :: fields
          oldSize = sizeOf(`type`)
          oldPos = fPos
        } else {
          fields = FieldInfo(t.name, pos, `type`) :: fields
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
    if (structures.isEmpty)
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

      for (op <- bytecode.ops) {
        op match {
          case Dup() => {
            if (duplicateGetLocal) {
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

      if (removes.nonEmpty || replacements.nonEmpty) {
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

    @tailrec def unwindParameterStack(depth: Int, ret: AbstractOp = Nop()): AbstractOp = {
      if ((depth < 0) && parameters.nonEmpty) {
        val op = parameters.head
        parameters = parameters.tail
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
      if (setStructure) throwError("The result of the map call must be assigned to a local var")
    }

    for (op <- bytecode.ops) {
      @inline def pushOp() {
        if (balance > 0) parameters = op :: parameters
      }

      op match {
        case Coerce(aName) => {
          if (optimisation == Optimisation.RemoveCoerce)
            removes = op :: removes
          else {
            preCheck()
            pushOp()
          }
          clearOptimisation()
        }
        case ConvertInt() => {
          preCheck()

          if (optimisation == Optimisation.RemoveConvertInt)
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
          aName match {
            case MapName => {
              //              if (balance > 0) throwError("map or sizeOf call cannot be nested")
              //              clearParameters = true
              balance += 1
              removes = op :: removes
            }
            case SizeOfName => {
              //              if (balance > 0) throwError("map or sizeOf call cannot be nested")
              //              clearParameters = true
              balance += 1
              removes = op :: removes
            }
            case _ => pushOp()
          }
        }
        case CallProperty(aName, argCount) => {
          preCheck()

          clearOptimisation()
          aName match {
            case MapName if (argCount == 2) => {
              if (balance <= 0) throwError("Invalid CallProperty " + aName)
              removes = op :: removes
              balance -= 1
              optimisation = Optimisation.RemoveCoerce
              setStructure = true
              parameters.head match {
                case gl@GetLex(sName) if (sName.kind == AbcNameKind.QName) => {
                  currentStructure = structureMap.get(sName.asInstanceOf[AbcQName])
                  if (currentStructure == None) throwError("map is expecting a Class of type Structure as second arguments")
                  removes = gl :: removes
                  unwindParameterStack(op.operandDelta)
				  parameters = PushByte(0) :: parameters
                }
                case _ => throwError("map is expecting a Class of type Structure as second arguments")
              }
            }
            case SizeOfName if (argCount == 1) => {
              if (balance <= 0) throwError("Invalid CallProperty " + aName)
              removes = op :: removes
              balance -= 1
              parameters.head match {
                case gl@GetLex(sName) if (sName.kind == AbcNameKind.QName) => {
                  structureMap.get(sName.asInstanceOf[AbcQName]) match {
                    case Some(struct) => {
                      val field = struct.fields.head
                      val size = field.position + sizeOf(field.`type`)
                      var args = List.empty[AbstractOp]
                      if (size > ((1 << 15) - 1)) {
                        args = PushInt(size) :: args
                      } else if (size > ((1 << 7) - 1)) {
                        args = PushShort(size) :: args
                      } else if (size >= 0) {
                        args = PushByte(size) :: args
                      }
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
			  unwindParameterStack(op.operandDelta) match {
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
                case _ => throwError("seekBy called on unmapped Structure")
              }
            }
            case _ => {
              preCheck()
//              pushOp()
				unwindParameterStack(-op.popOperands)
				parameters = PushByte(0) :: parameters
            }
          }
        }
        case CallPropVoid(aName, argCount) => {
          preCheck()

          clearOptimisation()
          aName match {
            case SeekToName if (argCount == 1 && (balance > 0) && parameters.nonEmpty) => {
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
                  if (size == 0) {
                    args = Nop() :: args
                  } else {
                    if (size > ((1 << 15) - 1)) {
                      args = PushInt(size) :: args
                    } else if (size > ((1 << 7) - 1)) {
                      args = PushShort(size) :: args
                    } else if (size > 0) {
                      args = PushByte(size) :: args
                    }
                    args = MultiplyInt() :: args
                    args = SetLocal(memAlias.ptrRegister) :: args
                  }
                  replacements = replacements.updated(op, args.reverse)
                  balance -= 1
                }
                case _ => throwError("seekBy called on unmapped Structure")
              }
            }
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
            case SeekByName if (argCount == 1 && (balance > 0) && parameters.nonEmpty) => {
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
                  if (size == 0) {
                    args = Nop() :: args
                  } else {
                    if (size > ((1 << 15) - 1)) {
                      args = PushInt(size) :: args
                    } else if (size > ((1 << 7) - 1)) {
                      args = PushShort(size) :: args
                    } else if (size > 0) {
                      args = PushByte(size) :: args
                    }
                    args = MultiplyInt() :: args
                    args = GetLocal(memAlias.ptrRegister) :: args
                    args = AddInt() :: args
                    args = SetLocal(memAlias.ptrRegister) :: args
                  }
                  replacements = replacements.updated(op, args.reverse)
                  balance -= 1
                  //                  parameters = parameters.tail
                }
                case _ => throwError("seekBy called on unmapped Structure")
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
                  if (size != 0) {
                    if (size > ((1 << 15) - 1)) {
                      args = PushInt(size) :: args
                    } else if (size > ((1 << 7) - 1)) {
                      args = PushShort(size) :: args
                    } else if (size > 0) {
                      args = PushByte(size) :: args
                    }
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
                  if (size != 0) {
                    if (size > ((1 << 15) - 1)) {
                      args = PushInt(size) :: args
                    } else if (size > ((1 << 7) - 1)) {
                      args = PushShort(size) :: args
                    } else if (size > 0) {
                      args = PushByte(size) :: args
                    }
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
          balance += 1
          pushOp()
        }
        case GetProperty(aName) if (balance > 0 && parameters.nonEmpty) => {
          preCheck()
          clearOptimisation()
          unwindParameterStack(-op.popOperands) match {
            case gl@GetLocal(register) if (registerMap.contains(register)) => {
              val memAlias = registerMap.get(register).get
              val structInfo = memAlias.structureInfo
              structInfo.fields.find(_.name == aName) match {
                case Some(field) => {
                  var args = List.empty[AbstractOp]
                  args = GetLocal(memAlias.ptrRegister) :: args

                  val size = field.position

                  if (size > ((1 << 15) - 1)) {
                    args = PushInt(size) :: args
                    args = AddInt() :: args
                  } else if (size > ((1 << 7) - 1)) {
                    args = PushShort(size) :: args
                    args = AddInt() :: args
                  } else if (size > 0) {
                    args = PushByte(size) :: args
                    args = AddInt() :: args
                  }

                  args = {
                    field.`type` match {
                      case 'float => GetFloat()
                      case 'double => GetDouble()
                      case 'int => GetInt()
                      case 'uint => GetInt()
                      case 'byte => GetByte()
                      case 'short => GetShort()
                      case _ => throwError("Unknown type : " + field.`type`); Nop()
                    }
                  } :: args
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
          // TODO remove register alias
        }
        case SetLocal(register) if (setStructure) => {
          clearOptimisation()
          setStructure = false
          currentStructure match {
            case Some(structureInfo) => {
//              registerMap = registerMap.updated(register, MemoryAlias(localCount, structureInfo))
              registerMap = registerMap.updated(register, MemoryAlias(register, structureInfo))
              replacements = replacements.updated(op, List(SetLocal(register)))
//              replacements = replacements.updated(op, List(SetLocal(localCount)))
//              localCount += 1
              currentStructure = None
            }
            case _ => throwError("map is expecting a Class of type Structure as second arguments")
          }
        }
        case SetProperty(aName) if (balance > 0 && parameters.nonEmpty) => {
          clearOptimisation()
          unwindParameterStack(op.operandDelta) match {
            case gl@GetLocal(register) if (registerMap.contains(register)) => {
              val memAlias = registerMap.get(register).get
              val structInfo = memAlias.structureInfo
              structInfo.fields.find(_.name == aName) match {
                case Some(field) => {
                  var args = List.empty[AbstractOp]
                  args = GetLocal(memAlias.ptrRegister) :: args
                  val size = field.position
                  if (size > ((1 << 15) - 1)) {
                    args = PushInt(size) :: args
                    args = AddInt() :: args
                  } else if (size > ((1 << 7) - 1)) {
                    args = PushShort(size) :: args
                    args = AddInt() :: args
                  } else if (size > 0) {
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
            case _ => unwindParameterStack(op.operandDelta)
          }
        }
        case _ => {
          preCheck()
          clearOptimisation()
          pushOp()
        }
      }

      if (balance == 0) {
        parameters = Nil
      }
    }

    if (removes.nonEmpty || replacements.nonEmpty) {
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
//		bytecode.dump()
      $expand(bytecode, true)
    } else {
//		bytecode.dump()
      haveBeenModified
    }
  }
}