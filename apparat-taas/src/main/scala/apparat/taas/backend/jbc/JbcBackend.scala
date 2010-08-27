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

	private def emitField(field: TaasField, cv: JClassVisitor): Unit = {
		val fv = cv.visitField(Java visibilityOf field, field.name.name, Java typeOf field.`type`, null, null)
		fv.visitEnd()
	}

	private def emitMethod(method: TaasMethod, cv: JClassVisitor): Unit = emitMethod(method, cv, method.name.name, Java typeOf method.`type`)

	private def emitMethod(method: TaasMethod, cv: JClassVisitor, name: String, returnType: String): Unit = {
		implicit val implicitMethod = method
		implicit val mv = cv.visitMethod(Java visibilityOf method, name, Java.methodDesc(returnType, method.parameters), null, null)
		var maxL = 0
		@inline def load(value: TValue) = {
			Load(value, x => mapIndex(x)) match {
				case Some(e) => error(e.message)
				case None => value match {
					case TClosure(value) => closures = (method, value) :: closures
					case _ =>
				}
			}
		}

		@inline def loadArray(list: List[TValue], `type`: TaasType) = {
			load(TInt(list.length))
			mv.visitTypeInsn(JOpcodes.ANEWARRAY, Java nameOf `type`)
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
				case TConvert(target) => Cast(value.`type`, target)
				case TCoerce(_) =>
				case _ => error("TODO "+op)
			}
		}

		@inline def binop(op: TaasBinop, lhs: TValue, rhs: TValue): Unit = binopWithType(op, lhs, rhs,
			TaasType.widen(lhs, rhs))

		@inline def binopWithType(op: TaasBinop, lhs: TValue, rhs: TValue, `type`: TaasType): Unit = {
			`type` match {
				case TaasIntType => op match {
					case TOp_+ => mv.visitInsn(JOpcodes.IADD)
					case TOp_- => mv.visitInsn(JOpcodes.ISUB)
					case TOp_* => mv.visitInsn(JOpcodes.IMUL)
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
				}
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
						case T3(TOp_-, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index && result.`type` == TaasIntType => {
							mv.visitIincInsn(mapIndex(result.index), -n)
							result typeAs TaasIntType
						}
						case T3(TOp_-, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index && result.`type` == TaasIntType => {
							mv.visitIincInsn(mapIndex(result.index), -n)
							result typeAs TaasIntType
						}
						case T3(TOp_+, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index && result.`type` == TaasIntType => {
							mv.visitIincInsn(mapIndex(result.index), n)
							result typeAs TaasIntType
						}
						case T3(TOp_+, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index && result.`type` == TaasIntType => {
							mv.visitIincInsn(mapIndex(result.index), n)
							result typeAs TaasIntType
						}
						case t3 @ T3(operator, lhs, rhs, result) => {
							val t = t3.`type`
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
								case TOp_true => mv.visitJumpInsn(JOpcodes.IFNONNULL, labels(jumps(if1)(0)))
								case TOp_false => mv.visitJumpInsn(JOpcodes.IFNULL, labels(jumps(if1)(0)))
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
								case other => error("Unexpected objet: "+other)
							}

							mv.visitTypeInsn(JOpcodes.NEW, Java nameOf obj.`type`)
							mv.visitInsn(JOpcodes.DUP)

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
							
							mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, Java nameOf obj.`type`, "<init>", Java.methodDesc("V", ctor.parameters))
							
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
							load(value)
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
	private def emitClosure(ov: JClassVisitor, nominal: TaasNominal, method: TaasMethod, closure: TaasMethod) = if(!classMap.contains(Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name)) {
		assume(closure.parameters.length == 1)//for now
		assume(closure.`type` == TaasVoidType)

		ov.visitInnerClass(Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name, null, null, 0);
		
		val cw = new JClassWriter(JbcBackend.WRITER_PARAMETERS)
		val cv = decorateWriter(cw)

		cv.visit(
			JbcBackend.JAVA_VERSION,
			JOpcodes.ACC_SUPER,
			Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name,
			"Ljitb/Function1<"+Java.typeOf(closure.parameters(0).`type`)+"Ljava/lang/Object;>;",
			"jitb/Function1",
			null
		)

		cv.visitOuterClass(Java nameOf nominal.qualifiedName, method.name.name, Java methodDesc method)
		cv.visitInnerClass(Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name, null, null, 0)

		val fv = cv.visitField(JOpcodes.ACC_FINAL + JOpcodes.ACC_SYNTHETIC, "this$0", Java typeOf TaasNominalTypeInstance(nominal), null, null)
		fv.visitEnd()

		{
			val mv = cv.visitMethod(0, "<init>", "("+Java.typeOf(TaasNominalTypeInstance(nominal))+")V", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
			mv.visitFieldInsn(JOpcodes.PUTFIELD, Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name, "this$0", Java typeOf TaasNominalTypeInstance(nominal))
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, "jitb/Function1", "<init>", "()V")
			mv.visitInsn(JOpcodes.RETURN)
			mv.visitMaxs(2, 2)
			mv.visitEnd()
		}

		{
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC, "apply1", "("+Java.typeOf(closure.parameters(0).`type`)+")Ljava/lang/Object;", null, null)
			mv.visitCode()
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitFieldInsn(JOpcodes.GETFIELD, Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name, "this$0", Java typeOf TaasNominalTypeInstance(nominal))
			mv.visitVarInsn(JOpcodes.ALOAD, 1)
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
			val mv = cv.visitMethod(JOpcodes.ACC_PUBLIC + JOpcodes.ACC_BRIDGE + JOpcodes.ACC_SYNTHETIC, "apply1", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitCode();
			mv.visitVarInsn(JOpcodes.ALOAD, 0);
			mv.visitVarInsn(JOpcodes.ALOAD, 1);
			mv.visitTypeInsn(JOpcodes.CHECKCAST, Java nameOf closure.parameters(0).`type`)
			mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name, "apply1", "("+Java.typeOf(closure.parameters(0).`type`)+")Ljava/lang/Object;");
			mv.visitInsn(JOpcodes.ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}

		val bytes = cw.toByteArray()
		cv.visitEnd()
		classMap += (Java.nameOf(nominal.qualifiedName)+"$"+closure.name.name) -> bytes
	}
}