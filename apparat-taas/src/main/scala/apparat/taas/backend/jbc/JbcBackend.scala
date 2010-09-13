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
import org.objectweb.asm.util.{ASMifierClassVisitor, TraceClassVisitor => JTraceClassVisitor}
import apparat.taas.ast._

/**
 * @author Joa Ebert
 */
object JbcBackend {
	val DEBUG = "true" == System.getProperty("apparat.debug", "false")
	private val WRITER_PARAMETERS = if(DEBUG) JClassWriter.COMPUTE_MAXS else JClassWriter.COMPUTE_FRAMES
	private val JAVA_VERSION = if(DEBUG) JOpcodes.V1_5 else JOpcodes.V1_6
}

/**
 * @author Joa Ebert
 */
class JbcBackend extends TaasBackend with SimpleLog {
	var classMap = Map.empty[String, Array[Byte]]
	var closures = List.empty[(TaasMethod, TaasMethod)]
	implicit var currentNominal: TaasNominal = _

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

			val coreType = nominal match {
				case i: TaasInterface => "java/lang/Object"
				case _ => "jitb/lang/Object"
			}

			cv.visit(
				JbcBackend.JAVA_VERSION,
				Java.visibilityOf(nominal) + (nominal match {
					case i: TaasInterface => JOpcodes.ACC_INTERFACE
					case _ => JOpcodes.ACC_SUPER
				}),
				Java nameOf nominal.qualifiedName,
				null,
				nominal.base match {
					case Some(base) => base match {
						case t: TaasNominalType => Java nameOf t.nominal.qualifiedName
						case TaasObjectType => coreType
						case _ => error("Expected TaasNominalType, got "+base)
					}
					case None => coreType
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
				case TaasClass(_, _, _, _, _, _, _, _, fields, _) => for(field <- fields if !field.isStatic) emitField(field, cv)
				case TaasFunction(_, _, _) =>
				case TaasInterface(_, _, _, _, _) =>
			}

			closures = Nil
			
			val bytes = cw.toByteArray()
			cv.visitEnd()
			classMap += Java.liftToplevel(nominal.qualifiedName) -> bytes
		}
	}

	private def emitField(field: TaasField, cv: JClassVisitor): Unit = {
		val fv = cv.visitField(Java visibilityOf field, field.name.name, Java typeOf field.`type`, null, null)
		fv.visitEnd()
	}

	private def emitMethod(method: TaasMethod, cv: JClassVisitor): Unit = emitMethod(method, cv, method.name.name, Java typeOf method.`type`)

	private def emitMethod(method: TaasMethod, cv: JClassVisitor, name: String, returnType: String): Unit = {
		implicit val implicitMethod = method
		implicit val mv = cv.visitMethod(Java visibilityOf method, name, Java.methodDesc(returnType, method.parameters), null, null)
		var maxL = 0
		@inline def load(value: TValue) = Load(value, x => mapIndex(x)) match {
			case Some(e) => error(e.message)
			case None => value match {
				case TClosure(value) => closures = (method, value) :: closures
				case _ =>
			}
		}

		@inline def loadAs(value: TValue, `type`: TaasType): Unit = Load(value, `type`, x => mapIndex(x)) match {
			case Some(e) => error(e.message)
			case None => value match {
				case TClosure(value) => closures = (method, value) :: closures
				case _ =>
			}
		}

		@inline def loadArray(list: List[TValue], `type`: TaasType, javaArrayType: String) = {
			load(TInt(list.length))
			mv.visitTypeInsn(JOpcodes.ANEWARRAY, javaArrayType)
			var i = 0
			for(value <- list) {
				mv.visitInsn(JOpcodes.DUP)
				load(TInt(i))
				loadAs(value, `type`)
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
				case TConvert(target) => Cast(value.`type`, target)
				case TCoerce(_) =>
				case _ => error("TODO "+op)
			}
		}

		@inline def binopWithType(op: TaasBinop, lhs: TValue, rhs: TValue, `type`: TaasType, mapIndex: Int => Int): Unit = {
			Binop(op, lhs, rhs, `type`, mapIndex, (x, y) => loadAs(x, y)) match {
				case Some(e) => {
					log.error(e.message)
					error(e.message)
				}
				case None =>
			}
		}

		@inline def implicitCast(from: TaasType, to: TaasType) = Cast(from, to) match {
			case Right(_) =>
			case Left(x) => {
				log.error("Implicit cast failed: %s", x.message)
				error(x.message)
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
					log.debug("Emit %s", op)
					(labels get op) match {
						case Some(label) => mv.visitLabel(label)
						case None =>
					}
					
					op match {
						case _: TValue =>
						case _: TNop =>
						case T2(TOp_Nothing, lex @ TLexical(t: TaasClass), reg) => reg typeAs lex.`type`
						case T2(TOp_Nothing, lex @ TLexical(t: TaasFunction), reg) => reg typeAs lex.`type`
						case t2 @ T2(operator, rhs, result) => {
							load(rhs)
							unop(operator, rhs)
							storeByType(t2.`type`, result)
						}
						case T3(TOp_-, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index && TaasType.isEqual(result.`type`, TaasIntType) => {
							mv.visitIincInsn(mapIndex(result.index), -n)
							result typeAs TaasIntType
						}
						case T3(TOp_-, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index && TaasType.isEqual(result.`type`, TaasIntType) => {
							mv.visitIincInsn(mapIndex(result.index), -n)
							result typeAs TaasIntType
						}
						case T3(TOp_+, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index && TaasType.isEqual(result.`type`, TaasIntType) => {
							mv.visitIincInsn(mapIndex(result.index), n)
							result typeAs TaasIntType
						}
						case T3(TOp_+, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index && TaasType.isEqual(result.`type`, TaasIntType) => {
							mv.visitIincInsn(mapIndex(result.index), n)
							result typeAs TaasIntType
						}
						case t3 @ T3(operator, lhs, rhs, result) => {
							val t = t3.`type`
							binopWithType(operator, lhs, rhs, t, x => mapIndex(x))
							storeByType(t, result)
						}
						case if1 @ TIf1(op, rhs) => {
							load(rhs)
							op match {
								case TOp_true =>
									rhs.`type` match {
										case TaasIntType | TaasBooleanType =>
											mv.visitInsn(JOpcodes.ICONST_0)
											mv.visitJumpInsn(JOpcodes.IFNE, labels(jumps(if1)(0)))
										case _ => mv.visitJumpInsn(JOpcodes.IFNONNULL, labels(jumps(if1)(0)))
									}
								case TOp_false | TOp_! =>
									rhs.`type` match {
										case TaasIntType | TaasBooleanType =>
											mv.visitInsn(JOpcodes.ICONST_0)
											mv.visitJumpInsn(JOpcodes.IFEQ, labels(jumps(if1)(0)))
										case _ => mv.visitJumpInsn(JOpcodes.IFNULL, labels(jumps(if1)(0)))
									}
								case TOp_~ => error("Invalid operator ~ in if statement.")
								case TOp_Nothing => error("Invalid operator Nothing in if statement.")
								case TCoerce(_) => error("Invalid operator Coerce in if statement.")
								case TConvert(_) => error("Invalid operator Convert in if statement.")
							}
						}
						case if2 @ TIf2(op, lhs, rhs) => {
							val t = TaasType.widen(lhs, rhs)

							loadAs(lhs, t)
							loadAs(rhs, t)

							op match {
								case TOp_!= => {
									t match {
										case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPNE, labels(jumps(if2)(0)))
										case TaasDoubleType => {
											mv.visitInsn(JOpcodes.DCMPG)
											load(TInt(0))
											mv.visitJumpInsn(JOpcodes.IFNE, labels(jumps(if2)(0)))
										}
									}
								}
								case TOp_== => {
									t match {
										case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPEQ, labels(jumps(if2)(0)))
										case TaasDoubleType => {
											mv.visitInsn(JOpcodes.DCMPG)
											load(TInt(0))
											mv.visitJumpInsn(JOpcodes.IFEQ, labels(jumps(if2)(0)))
										}
									}
								}
								case TOp_< => {
									t match {
										case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPLT, labels(jumps(if2)(0)))
										case TaasDoubleType => {
											mv.visitInsn(JOpcodes.DCMPG)
											load(TInt(-1))
											mv.visitJumpInsn(JOpcodes.IFEQ, labels(jumps(if2)(0)))
										}
									}
								}
								case TOp_!< => {
									t match {
										case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPGE, labels(jumps(if2)(0)))
										case TaasDoubleType => {
											mv.visitInsn(JOpcodes.DCMPG)
											load(TInt(-1))
											mv.visitJumpInsn(JOpcodes.IF_ICMPGT, labels(jumps(if2)(0)))
										}
									}
								}
								case TOp_!> => t match {
									case TaasDoubleType => {
										mv.visitInsn(JOpcodes.DCMPG)
										load(TInt(1))
										mv.visitJumpInsn(JOpcodes.IF_ICMPLT, labels(jumps(if2)(0)))
									}
									case TaasIntType => mv.visitJumpInsn(JOpcodes.IF_ICMPLE, labels(jumps(if2)(0)))
								}
							}
						}

						case jump: TJump => {
							mv.visitJumpInsn(JOpcodes.GOTO, labels(jumps(jump)(0)))
						}

						case TCall(t, TSetProperty, property :: value :: Nil, result) => {
							load(t)
							Cast.checkCast(TaasObjectType)
							loadAs(property, TaasStringType)
							loadAs(value, TaasObjectType)
							mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "jitb/lang/Object", "JITB$setProperty",  "(Ljava/lang/String;Ljava/lang/Object;)V")
						}

						case TCall(t, TGetProperty, property :: Nil, result) => {
							load(t)
							Cast.checkCast(TaasObjectType)
							loadAs(property, TaasStringType)
							mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "jitb/lang/Object", "JITB$getProperty", "(Ljava/lang/String;)Ljava/lang/Object;")
							result match {
								case Some(result) => storeByType(TGetIndex.`type`, result)
								case None => if(TGetIndex.`type` != TaasVoidType) {
									mv.visitInsn(JOpcodes.POP)
								}
							}
						}

						case TCall(t, TSetIndex, index :: value :: Nil, result) => {
							load(t)
							Cast.checkCast(TaasObjectType)
							loadAs(index, TaasIntType)
							loadAs(value, TaasObjectType)
							mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "jitb/lang/Object", "JITB$setIndex",  "(ILjava/lang/Object;)V")
						}

						case TCall(t, TGetIndex, index :: Nil, result) => {
							load(t)
							Cast.checkCast(TaasObjectType)
							loadAs(index, TaasIntType)
							mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "jitb/lang/Object", "JITB$getIndex", "(I)Ljava/lang/Object;")
							result match {
								case Some(result) => storeByType(TGetIndex.`type`, result)
								case None => if(TGetIndex.`type` != TaasVoidType) {
									mv.visitInsn(JOpcodes.POP)
								}
							}
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

							if(n < m) {
								while(i < n) {
									loadAs(arguments(i), method.parameters(i).`type`)
									i += 1
								}

								while(i < m) {
									val defaultValue = method.parameters(i).defaultValue
									loadAs(defaultValue getOrElse error("Missing parameter."), method.parameters(i).`type`)
									i += 1
								}
							}
							else if(n == m) {
								while(i < n) {
									loadAs(arguments(i), method.parameters(i).`type`)
									i += 1
								}
							} else if(m == 0 && n == 1) {
								method.parent match {
									case Some(parent) => parent match {
										case _: TaasClass | _: TaasInterface => {
											//assume setter method
											log.warning(method+" should have been resolved to its setter.")
											loadAs(arguments(0), method.`type`)
										}
										case _: TaasFunction => {
											log.warning(method+" has variable arguments.")
											varargs = true
											loadArray(arguments, TaasObjectType, "java/lang/Object")
										}
									}
									case None => error("Method without parent.")
								}
							} else {
								log.warning(method+" has variable arguments.")
								varargs = true
								loadArray(arguments, TaasObjectType, "java/lang/Object")
							}

							method.parent match {
								case Some(parent) => parent match {
									case k: TaasClass => {
										if(method.isStatic) {
											mv.visitMethodInsn(
												JOpcodes.INVOKESTATIC,
												Java nameOfOwnerOf method,
												method.name.name, Java methodDesc method)
										} else {
											mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, Java nameOfOwnerOf method, method.name.name, Java methodDesc method)
										}
									}
									case i: TaasInterface => mv.visitMethodInsn(JOpcodes.INVOKEINTERFACE, Java nameOfOwnerOf method, method.name.name, Java methodDesc method)
									case f: TaasFunction => mv.visitMethodInsn(
										JOpcodes.INVOKESTATIC,
										Java nameOfOwnerOf method,
										"callStatic",
										if(varargs) "([Ljava/lang/Object;)Ljava/lang/Object;" else Java.methodDesc(method))
								}
								case None => error("Method without parent.")
							}

							result match {
								case Some(result) => storeByType(method.`type`, result)
								case None => if(method.`type` != TaasVoidType) {
									mv.visitInsn(JOpcodes.POP)
								}
							}
						}
						
						case TConstruct(obj, arguments, result) => {
							val ctor = obj.`type` match {
								case TaasNominalTypeInstance(nominal) => nominal match {
									case TaasClass(_, _, _, _, _, ctor, _, _, _, _) => ctor
									case other => error("Unexpected definition: "+other)
								}
								case nominalType: TaasNominalType => nominalType.nominal match {
									case TaasClass(_, _, _, _, _, ctor, _, _, _, _) => ctor
									case other => error("Unexpected definition: "+other)
								}
								case other => error("Unexpected objet: "+other)
							}

							mv.visitTypeInsn(JOpcodes.NEW, Java nameOf obj.`type`)
							mv.visitInsn(JOpcodes.DUP)

							var i = 0
							var n = arguments.length
							var m = ctor.parameters.length

							while(i < n) {
								loadAs(arguments(i), ctor.parameters(i).`type`)
								i += 1
							}

							while(i < m) {
								val defaultValue = ctor.parameters(i).defaultValue
								loadAs(defaultValue getOrElse error("Missing parameter."), ctor.parameters(i).`type`)
								i += 1
							}

							mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, Java nameOfOwnerOf ctor, "<init>", Java.methodDesc("V", ctor.parameters))
							
							storeByType(obj.`type`, result)
						}
						case TLoad(obj, field, result) => {
							if(field.isStatic) {
								mv.visitFieldInsn(JOpcodes.GETSTATIC, Java nameOfOwnerOf field, field.name.name, Java typeOf field.`type`)
							} else {
								load(obj)
								mv.visitFieldInsn(JOpcodes.GETFIELD, Java nameOfOwnerOf field, field.name.name, Java typeOf field.`type`)
							}
							storeByType(field.`type`, result)

						}
						case TStore(obj, field, value) => {
							load(obj)
							loadAs(value, field.`type`)
							mv.visitFieldInsn(JOpcodes.PUTFIELD, Java nameOfOwnerOf field, field.name.name, Java typeOf field.`type`)
						}
						case TSuper(base, arguments) => {
							load(base)
							arguments foreach load
							mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, Java.nameOf(base.`type` match {
								case n: TaasNominalType => n.nominal.base match {
									case Some(base) => base
									case None => TaasObjectType
								}
							}), "<init>", base.`type` match {
								case nominalType: TaasNominalType => nominalType.nominal match {
									case TaasClass(_, _, _, _, _, ctor, _, _, _, _) => Java.methodDesc("V", ctor.parameters)
									case _ => "()V"
								}
								case _ => "()V"
							})
						}
						case TReturn(TVoid) => mv.visitInsn(JOpcodes.RETURN)
						case TReturn(value) =>
							loadAs(value, method.`type`)
							method.`type` match {
								case TaasBooleanType =>mv.visitInsn(JOpcodes.IRETURN)
								case TaasDoubleType => mv.visitInsn(JOpcodes.DRETURN)
								case TaasIntType => mv.visitInsn(JOpcodes.IRETURN)
								case TaasLongType => mv.visitInsn(JOpcodes.LRETURN)
								case _ => mv.visitInsn(JOpcodes.ARETURN)
							}
					}
				}
			}
			case None =>
		}

		mv.visitMaxs(0, 0)
		mv.visitEnd()
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

	@inline private def mapIndex(value: Int)(implicit method: TaasMethod) = {
		if(value <= method.parameters.length) value else value << 1
	}
	
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
	private def emitClosure(ov: JClassVisitor, nominal: TaasNominal, method: TaasMethod, closure: TaasMethod) = if(!classMap.contains(Java.liftToplevel(nominal.qualifiedName)+"$"+closure.name.name)) {
		assume(closure.parameters.length == 1)//for now
		assume(closure.`type` == TaasVoidType)

		val outerClassName = Java nameOf nominal.qualifiedName
		val innerClassName = outerClassName+"$"+closure.name.name
		val outerClassType = Java typeOf TaasNominalTypeInstance(nominal)

		ov.visitInnerClass(innerClassName, null, null, 0);
		
		val cw = new JClassWriter(JbcBackend.WRITER_PARAMETERS)
		val cv = decorateWriter(cw)

		cv.visit(
			JbcBackend.JAVA_VERSION,
			JOpcodes.ACC_SUPER,
			innerClassName,
			"Ljitb/lang/closure/Function1<"+Java.typeOf(closure.parameters(0).`type`)+"Ljava/lang/Object;>;",
			"jitb/lang/closure/Function1",
			null
		)

		cv.visitOuterClass(outerClassName, method.name.name, Java methodDesc method)
		cv.visitInnerClass(innerClassName, null, null, 0)

		val fv = cv.visitField(JOpcodes.ACC_FINAL + JOpcodes.ACC_SYNTHETIC, "this$0", outerClassType, null, null)
		fv.visitEnd()

		{
			val mv = cv.visitMethod(0, "<init>", "("+outerClassType+")V", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
			mv.visitFieldInsn(JOpcodes.PUTFIELD, innerClassName, "this$0", outerClassType)
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, "jitb/lang/closure/Function1", "<init>", "()V")
			mv.visitInsn(JOpcodes.RETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC, "apply1", "(Ljitb/lang/Object;"+Java.typeOf(closure.parameters(0).`type`)+")Ljava/lang/Object;", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			//add check for thisArg and whether it is null or not.
			mv.visitFieldInsn(JOpcodes.GETFIELD, innerClassName, "this$0", outerClassType)
			mv.visitVarInsn(JOpcodes.ALOAD, 2)
			closure.parent match {
				case Some(parent) => parent match {
					case k: TaasClass => mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, Java nameOfOwnerOf closure, closure.name.name, Java methodDesc closure)
					case i: TaasInterface => mv.visitMethodInsn(JOpcodes.INVOKEINTERFACE, Java nameOfOwnerOf closure, closure.name.name, Java methodDesc closure)
					case f: TaasFunction => mv.visitMethodInsn(
						JOpcodes.INVOKESTATIC,
						Java nameOfOwnerOf closure,
						"callStatic",
						Java methodDesc closure)
					case _ => error("Unexpected parent "+parent+".")
				}
				case None => error("Method without parent.")
			}
			mv.visitInsn(JOpcodes.ACONST_NULL)
			mv.visitInsn(JOpcodes.ARETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC, "applyVoid1", "(Ljitb/lang/Object;"+Java.typeOf(closure.parameters(0).`type`)+")V", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			//add check for thisArg and whether it is null or not.
			mv.visitFieldInsn(JOpcodes.GETFIELD, innerClassName, "this$0", outerClassType)
			mv.visitVarInsn(JOpcodes.ALOAD, 2)
			closure.parent match {
				case Some(parent) => parent match {
					case k: TaasClass => mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, Java nameOfOwnerOf closure, closure.name.name, Java methodDesc closure)
					case i: TaasInterface => mv.visitMethodInsn(JOpcodes.INVOKEINTERFACE, Java nameOfOwnerOf closure, closure.name.name, Java methodDesc closure)
					case f: TaasFunction => mv.visitMethodInsn(
						JOpcodes.INVOKESTATIC,
						Java nameOfOwnerOf closure,
						"callStatic",
						Java methodDesc closure)
					case _ => error("Unexpected parent "+parent+".")
				}
				case None => error("Method without parent.")
			}
			mv.visitInsn(JOpcodes.RETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		// Bridge methods

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC + JOpcodes.ACC_BRIDGE + JOpcodes.ACC_SYNTHETIC, "apply1", "(Ljitb/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
			mv.visitTypeInsn(JOpcodes.CHECKCAST, "jitb/lang/Object")
			mv.visitVarInsn(JOpcodes.ALOAD, 2)
			mv.visitTypeInsn(JOpcodes.CHECKCAST, Java nameOf closure.parameters(0).`type`)
			mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, innerClassName, "apply1", "(Ljitb/lang/Object;"+Java.typeOf(closure.parameters(0).`type`)+")Ljava/lang/Object;")
			mv.visitInsn(JOpcodes.ARETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC + JOpcodes.ACC_BRIDGE + JOpcodes.ACC_SYNTHETIC, "applyVoid1", "(Ljitb/lang/Object;Ljava/lang/Object;)V", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
			mv.visitTypeInsn(JOpcodes.CHECKCAST, "jitb/lang/Object")
			mv.visitVarInsn(JOpcodes.ALOAD, 2)
			mv.visitTypeInsn(JOpcodes.CHECKCAST, Java nameOf closure.parameters(0).`type`)
			mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, innerClassName, "applyVoid1", "(Ljitb/lang/Object;"+Java.typeOf(closure.parameters(0).`type`)+")V")
			mv.visitInsn(JOpcodes.RETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		val bytes = cw.toByteArray()
		cv.visitEnd()
		classMap += (Java.liftToplevel(nominal.qualifiedName)+"$"+closure.name.name) -> bytes
	}
}