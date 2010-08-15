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
package apparat.taas.backend.jbc

import apparat.taas.backend.TaasBackend
import apparat.taas.analysis.TaasDependencyGraphBuilder
import apparat.taas.graph._
import java.io.{PrintWriter => JPrintWriter}
import collection.mutable.ListBuffer
import org.objectweb.asm.{Opcodes => JOpcodes, Label => JLabel, ClassWriter => JClassWriter, ClassVisitor => JClassVisitor, ClassReader => JClassReader}
import apparat.log.{Debug, SimpleLog}
import org.objectweb.asm.util.{ASMifierClassVisitor, CheckClassAdapter => JCheckClassAdapter, TraceClassVisitor => JTraceClassVisitor}
import apparat.taas.ast._

/**
 * @author Joa Ebert
 */
object JbcBackend {
	val DEBUG = true
	private val WRITER_PARAMETERS = if(DEBUG) JClassWriter.COMPUTE_MAXS else JClassWriter.COMPUTE_FRAMES
	private val JAVA_VERSION = if(DEBUG) JOpcodes.V1_5 else JOpcodes.V1_6
}

/**
 * @author Joa Ebert
 */
class JbcBackend extends TaasBackend with SimpleLog {
	var classMap = Map.empty[String, Array[Byte]]
	var closures = List.empty[(TaasMethod, TaasMethod)]
	var currentNominal: TaasNominal = _

	private def decorateWriter(writer: JClassWriter) = {
		if(JbcBackend.DEBUG) {
			new JTraceClassVisitor(writer, new JPrintWriter(log asWriterFor Debug))
		} else {
			writer
		}
	}
	override def emit(ast: TaasAST) = {
		for(nominal <- TaasDependencyGraphBuilder(ast).topsort) {
			currentNominal = nominal

			val cw = new JClassWriter(JbcBackend.WRITER_PARAMETERS)
			val cv = decorateWriter(cw)

			cv.visit(
				JbcBackend.JAVA_VERSION,
				visibilityOf(nominal) + (nominal match {
					case i: TaasInterface => JOpcodes.ACC_INTERFACE
					case _ => JOpcodes.ACC_SUPER
				}),
				toJavaName(nominal.qualifiedName),
				null,
				nominal.base match {
					case Some(base) => base match {
						case t: TaasNominalType => toJavaName(t.nominal.qualifiedName)
						case TaasObjectType => "java/lang/Object"
						case _ => error("Expected TaasNominalType, got "+base)
					}
					case None => "java/lang/Object"
				},
				null//Array.empty[String]//TODO map to interface names...
			)

			nominal match {
				case TaasClass(_, _, _, _, init, ctor, _, _, _, _) => {
					emitMethod(ctor, cv, "<init>", "V")
				}
				case _ =>
			}

			for(method <- nominal.methods if !method.isStatic) {
				emitMethod(method, cv)
			}

			for(closure <- closures) {
				emitClosure(cv, nominal, closure._1, closure._2)
			}

			nominal match {
				case TaasClass(_, _, _, _, _, _, _, _, fields, _) => {
					for(field <- fields if !field.isStatic) {
						emitField(field, cv)
					}
				}
			}
			closures = Nil
			
			val bytes = cw.toByteArray()
			cv.visitEnd()
			classMap += nominal.qualifiedName -> bytes

			if(JbcBackend.DEBUG) {
				JCheckClassAdapter.verify(new JClassReader(bytes), true, new JPrintWriter(Console.out))
			}
		}
	}

	@inline private def methodDesc(returnType: String, parameters: ListBuffer[TaasParameter]): String = "("+(parameters map { _.`type` } map { toJavaType } mkString "")+")"+returnType
	@inline private def methodDesc(returnType: TaasType, parameters: ListBuffer[TaasParameter]): String = methodDesc(toJavaType(returnType), parameters)
	@inline private def methodDesc(method: TaasMethod): String = methodDesc(toJavaType(method.`type`), method.parameters)

	private def emitField(field: TaasField, cv: JClassVisitor): Unit = {
		val fv = cv.visitField(visibilityOf(field), field.name.name, toJavaType(field.`type`), null, null)
		fv.visitEnd()
	}

	private def emitMethod(method: TaasMethod, cv: JClassVisitor): Unit = emitMethod(method, cv, method.name.name, toJavaType(method.`type`))

	private def emitMethod(method: TaasMethod, cv: JClassVisitor, name: String, returnType: String): Unit = {
		val mv = cv.visitMethod(visibilityOf(method), name, methodDesc(returnType, method.parameters), null, null)
		var maxL = 0
		@inline def load(value: TValue) = {
			value match {
				case TClosure(value) => {
					val closureName = toJavaName(currentNominal.qualifiedName)+"$"+value.name.name
					val nominalType = toJavaType(TaasNominalTypeInstance(currentNominal))
					mv.visitTypeInsn(JOpcodes.NEW, closureName)
					mv.visitInsn(JOpcodes.DUP)
					mv.visitVarInsn(JOpcodes.ALOAD, 0)
					mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, closureName, "<init>", "("+nominalType+")V")

					closures = (method, value) :: closures
				}
				case TInt(value) => value match {
					case -1 => mv.visitInsn(JOpcodes.ICONST_M1)
					case 0 => mv.visitInsn(JOpcodes.ICONST_0)
					case 1 => mv.visitInsn(JOpcodes.ICONST_1)
					case 2 => mv.visitInsn(JOpcodes.ICONST_2)
					case 3 => mv.visitInsn(JOpcodes.ICONST_3)
					case 4 => mv.visitInsn(JOpcodes.ICONST_4)
					case 5 => mv.visitInsn(JOpcodes.ICONST_5)
					case b if b < 0x80 && b >= -0x80 => mv.visitIntInsn(JOpcodes.BIPUSH, b)
					case si if si < 0x8000 && si >= -0x8000 => mv.visitIntInsn(JOpcodes.SIPUSH, si)
					case i => mv.visitLdcInsn(new java.lang.Integer(i))
				}
				case TLong(value) => value match {
					case 0L => mv.visitInsn(JOpcodes.LCONST_0)
					case 1L => mv.visitInsn(JOpcodes.LCONST_1)
					case l => mv.visitLdcInsn(new java.lang.Long(l))
				}
				case TBool(value) => value match {
					case true => mv.visitInsn(JOpcodes.ICONST_1)
					case false => mv.visitInsn(JOpcodes.ICONST_0)
				}
				case TString(value) => mv.visitLdcInsn(value.name)
				case TDouble(value) => value match {
					case 0.0 => mv.visitInsn(JOpcodes.DCONST_0)
					case 1.0 => mv.visitInsn(JOpcodes.DCONST_1)
					case d => mv.visitLdcInsn(new java.lang.Double(d))
				}
				case TLexical(definition) => definition match {
					case method: TaasMethod => {
						if(method.isStatic) {
							error("static method")
						} else {
							mv.visitVarInsn(JOpcodes.ALOAD, 0)
							mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, toJavaName(ownerOf(method)), method.name.name, methodDesc(method))
						}
					}
					case klass: TaasClass => //nothing to do here!
					case fun: TaasFunction => 
				}
				case reg: TReg => {
					val index = mapIndex(reg.index)
					reg.`type` match {
						case TaasAnyType | TaasObjectType | TaasStringType | TaasFunctionType | _: TaasNominalType => mv.visitVarInsn(JOpcodes.ALOAD, index)
						case TaasBooleanType | TaasIntType => mv.visitVarInsn(JOpcodes.ILOAD, index)
						case TaasDoubleType => mv.visitVarInsn(JOpcodes.DLOAD, index)
						case TaasLongType => mv.visitVarInsn(JOpcodes.LLOAD, index)
						case TaasVoidType => error("Cannot store void in register.")
					}
				}
				case TNull => mv.visitInsn(JOpcodes.ACONST_NULL)
			}
		}

		@inline def loadArray(list: List[TValue], `type`: TaasType) = {
			load(TInt(list.length))
			mv.visitTypeInsn(JOpcodes.ANEWARRAY, toJavaName(`type`))
			var i = 0
			for(value <- list) {
				mv.visitInsn(JOpcodes.DUP)
				load(TInt(i))
				load(value)
				implicitCast(value.`type`, `type`)
				mv.visitInsn(JOpcodes.AASTORE)
				i += 1
			}
		}

		@inline def storeByValue(value: TValue, reg: TReg): Unit = if(value == TNull) storeByType(TaasAnyType, reg) else storeByType(value.`type`, reg)
		@inline def storeByType(value: TaasType, reg: TReg): Unit = {
			reg typeAs value

			val index = mapIndex(reg.index)
			value match {
				case TaasAnyType | TaasObjectType | TaasStringType | TaasFunctionType | _: TaasNominalType => mv.visitVarInsn(JOpcodes.ASTORE, index)
				case TaasBooleanType | TaasIntType => mv.visitVarInsn(JOpcodes.ISTORE, index)
				case TaasDoubleType => mv.visitVarInsn(JOpcodes.DSTORE, index)
				case TaasLongType => mv.visitVarInsn(JOpcodes.LSTORE, index)
				case TaasVoidType => error("Cannot store void in register.")
			}
		}

		@inline def unop(op: TaasUnop, value: TValue) = {
			op match {
				case TOp_Nothing =>
				case TConvert(toType) => if(value.`type` != toType) {
					value.`type` match {
						case TaasIntType => toType match {
							case TaasDoubleType => mv.visitInsn(JOpcodes.I2D)
							case TaasBooleanType => mv.visitInsn(JOpcodes.I2B)
							case TaasLongType => mv.visitInsn(JOpcodes.I2L)
							case TaasStringType => mv.visitInsn(JOpcodes.I2S)
							case other => error("Cannot convert from TaasIntType to "+toType+".")
						}
						case TaasLongType => toType match {
							case TaasIntType => mv.visitInsn(JOpcodes.L2I)
							case other => error("Cannot convert from TaasLongType to "+toType+".")
						}
						case TaasDoubleType => toType match {
							case TaasIntType => mv.visitInsn(JOpcodes.D2I)
							case other => error("Cannot convert from TaasDoubleType to "+toType+".")
						}
					}
				}
				case TCoerce(_) =>
				case _ => error("TODO "+op)
			}
		}

		@inline def binop(op: TaasBinop, lhs: TValue, rhs: TValue): Unit = binopWithType(op, lhs, rhs,
			TaasType.widen(lhs, rhs))

		@inline def binopWithType(op: TaasBinop, lhs: TValue, rhs: TValue, `type`: TaasType): Unit = {
			def doublesToInt() = {
				mv.visitInsn(JOpcodes.D2I)
				mv.visitInsn(JOpcodes.SWAP)
				mv.visitInsn(JOpcodes.D2I)
				mv.visitInsn(JOpcodes.SWAP)
			}
			`type` match {
				case TaasIntType => op match {
					case TOp_+ => mv.visitInsn(JOpcodes.IADD)
					case TOp_- => mv.visitInsn(JOpcodes.ISUB)
					case TOp_<< => mv.visitInsn(JOpcodes.ISHL)
					case TOp_>> => mv.visitInsn(JOpcodes.ISHR)
					case TOp_| => mv.visitInsn(JOpcodes.IOR)
					case TOp_/ => mv.visitInsn(JOpcodes.IDIV)
					case TOp_& => mv.visitInsn(JOpcodes.IAND)
				}
				case TaasDoubleType => op match {
					case TOp_+ => mv.visitInsn(JOpcodes.DADD)
					case TOp_- => mv.visitInsn(JOpcodes.DSUB)
					case TOp_* => mv.visitInsn(JOpcodes.DMUL)
					case TOp_/ => mv.visitInsn(JOpcodes.DDIV)
					case TOp_& => {
						doublesToInt()
						mv.visitInsn(JOpcodes.IAND)
					}
					case TOp_>> => {
						doublesToInt()
						mv.visitInsn(JOpcodes.ISHR)
					}
				}
			}
		}


		@inline def implicitCast(from: TaasType, to: TaasType) = if(from != to) {
			from match {
				case TaasIntType => to match {
					case TaasDoubleType => mv.visitInsn(JOpcodes.I2D)
					case TaasLongType => mv.visitInsn(JOpcodes.I2L)
					case TaasObjectType => mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;")
				}
				case TaasDoubleType => to match {
					case TaasIntType => mv.visitInsn(JOpcodes.D2I)
					case TaasObjectType => mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;")
				}
				case TaasLongType => to match {
					case TaasDoubleType => mv.visitInsn(JOpcodes.L2D)
				}
				case TaasStringType => to match {
					case TaasObjectType => 
				}
				case a: TaasNominalType => to match {
					case b: TaasNominalType => {
						if(a.nominal != b.nominal) {
							def loop(current: Option[TaasType]): Boolean = {
								current match {
									case Some(nominal: TaasNominalType) => if(nominal.nominal == b.nominal) { return true } else { loop(nominal.nominal.base) }
									case Some(other) => false
									case None => false
								}
							}
							if(!loop(Some(a))) {
								error("TODO implicit nominal cast from "+from+" to "+to)
							}
						}
					}
					case other => {
						error("TODO implicit cast from "+from+" to "+to)
					}
				}
				case other => {
					error("TODO implicit cast from "+from+" to "+to)
				}
			}
		}

		mv.visitCode()

		method.code match {
			case Some(code) => {
				val lin = new TaasGraphLinearizer(code.graph)
				val ops = lin.list
				val labels = generateLabels(lin.map)
				val jumps = lin.map

				if(JbcBackend.DEBUG) {
					log.debug("Emitting TAAS:")
					lin.dump(log, Debug)
				}

				for(op <- ops) {
					(labels get op) match {
						case Some(label) => mv.visitLabel(label)
						case None =>
					}
					
					op match {
						case _: TNop =>
						case T2(TOp_Nothing, lex @ TLexical(t: TaasClass), reg) => reg typeAs lex.`type`
						case T2(TOp_Nothing, lex @ TLexical(t: TaasFunction), reg) => reg typeAs lex.`type`
						case t2 @ T2(operator, rhs, result) => {
							load(rhs)
							unop(operator, rhs)
							storeByType(t2.`type`, result)
						}
						case T3(TOp_-, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index => {
							mv.visitIincInsn(mapIndex(result.index), -n)
							result typeAs TaasIntType
						}
						case T3(TOp_-, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index => {
							mv.visitIincInsn(mapIndex(result.index), -n)
							result typeAs TaasIntType
						}
						case T3(TOp_+, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index => {
							mv.visitIincInsn(mapIndex(result.index), n)
							result typeAs TaasIntType
						}
						case T3(TOp_+, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index => {
							mv.visitIincInsn(mapIndex(result.index), n)
							result typeAs TaasIntType
						}
						case T3(operator, lhs, rhs, result) => {
							val t = TaasType.widen(lhs, rhs) 
							load(lhs)
							implicitCast(lhs.`type`, t)
							load(rhs)
							implicitCast(rhs.`type`, t)
							binopWithType(operator, lhs, rhs, t)
							storeByType(t, result)
						}
						case if1 @ TIf1(op, rhs) => {
							load(rhs)
							op match {
								case TOp_true => {
									mv.visitJumpInsn(JOpcodes.IFNONNULL, labels(jumps(if1)(0)))
								}
							}
						}
						case if2 @ TIf2(op, lhs, rhs) => {
							val t = TaasType.widen(lhs, rhs)

							load(lhs)
							implicitCast(lhs.`type`, t)

							load(rhs)
							implicitCast(rhs.`type`, t)

							op match {
								case TOp_< => {
									t match {
										case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPLT, labels(jumps(if2)(0)))
										case TaasDoubleType => {
											mv.visitInsn(JOpcodes.DCMPG)
											mv.visitJumpInsn(JOpcodes.IFLT, labels(jumps(if2)(0)))
										}
									}
								}
								case TOp_!> => t match {
									case TaasDoubleType => {
										mv.visitInsn(JOpcodes.DCMPG)
										mv.visitJumpInsn(JOpcodes.IF_ICMPLE, labels(jumps(if2)(0)))
									}
									case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPLE, labels(jumps(if2)(0)))
								}
							}
						}

						case jump: TJump => {
							mv.visitJumpInsn(JOpcodes.GOTO, labels(jumps(jump)(0)))
						}

						case TCall(t, method, arguments, result) => {
							var i = 0
							var n = arguments.length
							var m = method.parameters.length
							var varargs = false

							method.parent match {
									case Some(parent) => parent match {
										case _: TaasClass | _: TaasInterface => t match {
											case TLexical(_: TaasClass) if !method.isStatic => mv.visitVarInsn(JOpcodes.ALOAD, 0)
											case TLexical(_: TaasInterface) => mv.visitVarInsn(JOpcodes.ALOAD, 0)
											case _ => load(t)
										}
										case _: TaasFunction =>
									}
							}

							if(n < m) { error("optional parameters in "+method)}
							else if(n == m) {
								while(i < n) {
									load(arguments(i))
									implicitCast(arguments(i).`type`, method.parameters(i).`type`)
									i += 1
								}
							} else if(m == 0 && n == 1) {
								method.parent match {
									case Some(parent) => parent match {
										case _: TaasClass | _: TaasInterface => {
											//assume setter method
											log.warning(method+" should have been resolved to its setter.")
											load(arguments(0))
											implicitCast(arguments(0).`type`, method.`type`)
										}
										case _: TaasFunction => {
											log.warning(method+" has variable arguments.")
											varargs = true
											loadArray(arguments, TaasObjectType)
										}
									}
									case None => error("Method without parent.")
								}
							} else {
								error("Invalid op "+op+".")
							}

							method.parent match {
								case Some(parent) => parent match {
									case k: TaasClass => {
										if(method.isStatic) {
											mv.visitMethodInsn(
												JOpcodes.INVOKESTATIC,
												toJavaName(ownerOf(method)),
												method.name.name, methodDesc(method))
										} else {
											mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, toJavaName(ownerOf(method)), method.name.name, methodDesc(method))
										}
									}
									case i: TaasInterface => mv.visitMethodInsn(JOpcodes.INVOKEINTERFACE, toJavaName(ownerOf(method)), method.name.name, methodDesc(method))
									case f: TaasFunction => mv.visitMethodInsn(
										JOpcodes.INVOKESTATIC,
										toJavaName(ownerOf(method)),
										"callStatic",
										if(varargs) "([Ljava/lang/Object;)V" else methodDesc(method))
								}
								case None => error("Method without parent.")
							}

							result match {
								case Some(result) => storeByType(method.`type`, result)
								case None =>
							}
						}
						
						case TConstruct(obj, arguments, result) => {
							val ctor = obj.`type` match {
								case TaasNominalTypeInstance(nominal) => nominal match {
									case TaasClass(_, _, _, _, _, ctor, _, _, _, _) => ctor
									case other => error("Unexpected definition: "+other)
								}
								case other => error("Unexpected objet: "+other)
							}

							mv.visitTypeInsn(JOpcodes.NEW, toJavaName(obj.`type`));
							mv.visitInsn(JOpcodes.DUP);

							var i = 0
							var n = arguments.length
							var m = ctor.parameters.length

							while(i < n) {
								load(arguments(i))
								implicitCast(arguments(i).`type`, ctor.parameters(i).`type`)
								i += 1
							}

							while(i < m) {
								val defaultValue = ctor.parameters(i).defaultValue
								load(defaultValue getOrElse error("Missing parameter."))
								implicitCast(defaultValue.get.`type`, ctor.parameters(i).`type`)
								i += 1
							}
							
							mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, toJavaName(obj.`type`), "<init>", methodDesc("V", ctor.parameters))
							
							storeByType(obj.`type`, result)
						}
						case TLoad(obj, field, result) => {
							if(field.isStatic) {
								mv.visitFieldInsn(JOpcodes.GETSTATIC, toJavaName(ownerOf(field)), field.name.name, toJavaType(field.`type`))
							} else {
								load(obj)
								mv.visitFieldInsn(JOpcodes.GETFIELD, toJavaName(ownerOf(field)), field.name.name, toJavaType(field.`type`))
							}
							storeByType(field.`type`, result)

						}
						case TStore(obj, field, value) => {
							load(obj)
							load(value)
							mv.visitFieldInsn(JOpcodes.PUTFIELD, toJavaName(ownerOf(field)), field.name.name, toJavaType(field.`type`))
						}
						case TSuper(base, arguments) => {
							load(base)
							arguments foreach load
							mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, toJavaName(base.`type` match {
								case n: TaasNominalType => n.nominal.base match {
									case Some(base) => base
									case None => TaasObjectType
								}
							}), "<init>", "()V")//TODO!
						}
						case TReturn(TVoid) => mv.visitInsn(JOpcodes.RETURN)
					}
				}
			}
			case None =>
		}

		mv.visitMaxs(0, 0)
		mv.visitEnd()
	}

	private def toJavaType(`type`: TaasType) = `type` match {
		case TaasAnyType | TaasObjectType => "Ljava/lang/Object;"
		case TaasVoidType => "V"
		case TaasBooleanType => "Z"
		case TaasDoubleType => "D"
		case TaasIntType => "I"
		case TaasLongType => "J"
		case TaasStringType => "Ljava/lang/String;"
		case TaasFunctionType => "Ljitb/Function;"
		case t: TaasNominalType => "L"+toJavaName(t.nominal.qualifiedName)+";"
	}

	private def toJavaName(`type`: TaasType): String = `type` match {
		case TaasAnyType | TaasObjectType => "java/lang/Object"
		case TaasVoidType => error("Void has no name.")
		case TaasBooleanType => "java/lang/Boolean"
		case TaasDoubleType => "java/lang/Double"
		case TaasIntType => "java/lang/Integer"
		case TaasLongType => "java/lang/Long"
		case TaasStringType => "java/lang/String"
		case TaasFunctionType => "jitb/Function"
		case t: TaasNominalType => toJavaName(t.nominal.qualifiedName)
	}

	private def toJavaName(qname: String): String = qname.replaceAll("\\.", "\\/")
	
	private def visibilityOf(somethingWithNamespace: {def namespace: TaasNamespace}) = {
		somethingWithNamespace.namespace match {
			case TaasPublic => JOpcodes.ACC_PUBLIC
			case TaasInternal => JOpcodes.ACC_PUBLIC
			case TaasProtected => JOpcodes.ACC_PROTECTED
			case TaasPrivate => JOpcodes.ACC_PRIVATE
			case TaasExplicit(_) => JOpcodes.ACC_PUBLIC
		}
	}

	private def ownerOf(element: TaasDefinition) = {
		element.parent match {
			case Some(parent) => parent match {
				case p: TaasPackage => element.qualifiedName
				case k: TaasClass => k.qualifiedName
				case i: TaasInterface => i.qualifiedName
				case f: TaasFunction => f.qualifiedName
				case _ => error("Unexpected parent "+parent+".")
			}
			case None => error("No parent for "+element+".")
		}
	}

	private def generateLabels(map: Map[TExpr, ListBuffer[TExpr]]) = {
		import scala.collection.mutable.HashMap

		var result = HashMap.empty[TExpr, JLabel]

		for {
			values <- map.valuesIterator
			expr <- values if !(result contains expr)
		} {
			result += expr -> new JLabel()
		}

		result
	}

	@inline private def mapIndex(value: Int) = value << 1
	
	/**
	 * nominal is the type in which we are using a closure
	 * method is the method that calls the closure
	 * closure is the actual closure
	 *
	 * imagine
	 *
	 * class A extends EventDispatcher {
	 *   public function B() {
	 *     addEventListener(..., C)
	 *   }
	 *
	 *   public function C(event: Event): void {}
	 * }
	 *
	 * in this case A is the nominal, B is method since it contains the closure call and C is the closure.
	 */
	private def emitClosure(ov: JClassVisitor, nominal: TaasNominal, method: TaasMethod, closure: TaasMethod) = if(!classMap.contains(toJavaName(nominal.qualifiedName)+"$"+closure.name.name)) {
		assume(closure.parameters.length == 1)//for now
		assume(closure.`type` == TaasVoidType)

		ov.visitInnerClass(toJavaName(nominal.qualifiedName)+"$"+closure.name.name, null, null, 0);
		
		val cw = new JClassWriter(JbcBackend.WRITER_PARAMETERS)
		val cv = decorateWriter(cw)

		cv.visit(
			JbcBackend.JAVA_VERSION,
			JOpcodes.ACC_SUPER,
			toJavaName(nominal.qualifiedName)+"$"+closure.name.name,
			"Ljitb/Function1<"+toJavaType(closure.parameters(0).`type`)+"Ljava/lang/Object;>;",
			"jitb/Function1",
			null
		)

		cv.visitOuterClass(toJavaName(nominal.qualifiedName), method.name.name, methodDesc(method))
		cv.visitInnerClass(toJavaName(nominal.qualifiedName)+"$"+closure.name.name, null, null, 0)

		val fv = cv.visitField(JOpcodes.ACC_FINAL + JOpcodes.ACC_SYNTHETIC, "this$0", toJavaType(TaasNominalTypeInstance(nominal)), null, null)
		fv.visitEnd()

		{
			val mv = cv.visitMethod(0, "<init>", "("+toJavaType(TaasNominalTypeInstance(nominal))+")V", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
			mv.visitFieldInsn(JOpcodes.PUTFIELD, toJavaName(nominal.qualifiedName)+"$"+closure.name.name, "this$0", toJavaType(TaasNominalTypeInstance(nominal)))
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, "jitb/Function1", "<init>", "()V")
			mv.visitInsn(JOpcodes.RETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC, "apply1", "("+toJavaType(closure.parameters(0).`type`)+")Ljava/lang/Object;", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitFieldInsn(JOpcodes.GETFIELD, toJavaName(nominal.qualifiedName)+"$"+closure.name.name, "this$0", toJavaType(TaasNominalTypeInstance(nominal)))
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
			closure.parent match {
				case Some(parent) => parent match {
					case k: TaasClass => mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, toJavaName(ownerOf(closure)), closure.name.name, methodDesc(closure))
					case i: TaasInterface => mv.visitMethodInsn(JOpcodes.INVOKEINTERFACE, toJavaName(ownerOf(closure)), closure.name.name, methodDesc(closure))
					case f: TaasFunction => mv.visitMethodInsn(
						JOpcodes.INVOKESTATIC,
						toJavaName(ownerOf(closure)),
						"callStatic",
						methodDesc(closure))
				}
				case None => error("Method without parent.")
			}
			mv.visitInsn(JOpcodes.ACONST_NULL)
			mv.visitInsn(JOpcodes.ARETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC + JOpcodes.ACC_BRIDGE + JOpcodes.ACC_SYNTHETIC, "apply1", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitCode();
			mv.visitVarInsn(JOpcodes.ALOAD, 0);
			mv.visitVarInsn(JOpcodes.ALOAD, 1);
			mv.visitTypeInsn(JOpcodes.CHECKCAST, toJavaName(closure.parameters(0).`type`));
			mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, toJavaName(nominal.qualifiedName)+"$"+closure.name.name, "apply1", "("+toJavaType(closure.parameters(0).`type`)+")Ljava/lang/Object;");
			mv.visitInsn(JOpcodes.ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}

		val bytes = cw.toByteArray()
		cv.visitEnd()
		classMap += (toJavaName(nominal.qualifiedName)+"$"+closure.name.name) -> bytes
	}
}