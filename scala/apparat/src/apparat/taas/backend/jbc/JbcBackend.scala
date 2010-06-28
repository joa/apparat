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
import apparat.taas.ast._
import org.objectweb.asm.util.{CheckClassAdapter => JCheckClassAdapter, TraceClassVisitor => JTraceClassVisitor}
import org.objectweb.asm.{Opcodes => JOpcodes, Label => JLabel, ClassWriter => JClassWriter, ClassVisitor => JClassVisitor, ClassReader => JClassReader}

/**
 * @author Joa Ebert
 */
object JbcBackend {
	val DEBUG = true
}

/**
 * @author Joa Ebert
 */
class JbcBackend extends TaasBackend {
	var classMap = Map.empty[String, Array[Byte]]

	override def emit(ast: TaasAST) = {
		for(nominal <- TaasDependencyGraphBuilder(ast).topsort) {
			val cw = new JClassWriter(JClassWriter.COMPUTE_FRAMES)
			val cv = if(JbcBackend.DEBUG) new JTraceClassVisitor(cw, new JPrintWriter(System.out)) else cw

			cv.visit(
				JOpcodes.V1_6,
				visibilityOf(nominal) + (nominal match {
					case i: TaasInterface => JOpcodes.ACC_INTERFACE
					case _ => JOpcodes.ACC_SUPER
				}),
				toJavaName(nominal.qualifiedName),
				null,
				nominal.base match {
					case Some(base) => base match {
						case t: TaasNominalType => toJavaName(t.nominal.qualifiedName)
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

			val bytes = cw.toByteArray()
			cv.visitEnd()
			classMap += nominal.qualifiedName -> bytes

			if(JbcBackend.DEBUG) {
				JCheckClassAdapter.verify(new JClassReader(bytes), true, new JPrintWriter(System.out))
			}
		}
	}

	@inline private def methodDesc(returnType: String, parameters: ListBuffer[TaasParameter]): String = "("+(parameters map { _.`type` } map { toJavaType } mkString ",")+")"+returnType
	@inline private def methodDesc(returnType: TaasType, parameters: ListBuffer[TaasParameter]): String = methodDesc(toJavaType(returnType), parameters)
	@inline private def methodDesc(method: TaasMethod): String = methodDesc(toJavaType(method.`type`), method.parameters)

	private def emitMethod(method: TaasMethod, cv: JClassVisitor): Unit = emitMethod(method, cv, method.name.name, toJavaType(method.`type`))

	private def emitMethod(method: TaasMethod, cv: JClassVisitor, name: String, returnType: String): Unit = {
		val mv = cv.visitMethod(visibilityOf(method), name, methodDesc(returnType, method.parameters), null, null)
		var maxL = 0
		@inline def load(value: TValue) = {
			value match {
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
					if(reg.index+1 > maxL) {
						maxL = reg.index+1
					}

					reg.`type` match {
						case TaasAnyType | TaasObjectType | TaasStringType | TaasFunctionType | _: TaasNominalType => mv.visitVarInsn(JOpcodes.ALOAD, reg.index)
						case TaasBooleanType | TaasIntType => mv.visitVarInsn(JOpcodes.ILOAD, reg.index)
						case TaasDoubleType => mv.visitVarInsn(JOpcodes.DLOAD, reg.index)
						case TaasLongType => mv.visitVarInsn(JOpcodes.LLOAD, reg.index)
						case TaasVoidType => error("Cannot store void in register.")
					}
				}
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

		@inline def storeByValue(value: TValue, reg: TReg): Unit = storeByType(value.`type`, reg)
		@inline def storeByType(value: TaasType, reg: TReg): Unit = {
			if(reg.index+1 > maxL) {
				maxL = reg.index+1
			}

			//TODO check if this is valid
			reg typeAs value
			
			value match {
				case TaasAnyType | TaasObjectType | TaasStringType | TaasFunctionType | _: TaasNominalType => mv.visitVarInsn(JOpcodes.ASTORE, reg.index)
				case TaasBooleanType | TaasIntType => mv.visitVarInsn(JOpcodes.ISTORE, reg.index)
				case TaasDoubleType => mv.visitVarInsn(JOpcodes.DSTORE, reg.index)
				case TaasLongType => mv.visitVarInsn(JOpcodes.LSTORE, reg.index)
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
					}
				}
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
					case TOp_<< => mv.visitInsn(JOpcodes.ISHL)
					case TOp_/ => mv.visitInsn(JOpcodes.IDIV)
				}
			}
		}


		@inline def implicitCast(from: TaasType, to: TaasType) = if(from != to) {
			from match {
				case TaasIntType => to match {
					case TaasDoubleType => mv.visitInsn(JOpcodes.I2D)
					case TaasObjectType => mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;")
				}
				case TaasDoubleType => to match {
					case TaasIntType => mv.visitInsn(JOpcodes.D2I)
					case TaasObjectType => mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;")
				}
				case other => error("TODO implicit cast from "+from+" to "+to)
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
					println("Emitting TAAS code:")
					lin.dump()
				}

				for(op <- ops) {
					(labels get op) match {
						case Some(label) => mv.visitLabel(label)
						case None =>
					}
					
					op match {
						case _: TNop =>
						case T2(TOp_Nothing, TLexical(t: TaasClass), _) =>
						case T2(TOp_Nothing, TLexical(t: TaasFunction), _) =>
						case T2(operator, rhs, result) => {
							load(rhs)
							unop(operator, rhs)
							storeByValue(rhs, result)
						}
						case T3(TOp_-, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index => mv.visitIincInsn(result.index, -n)
						case T3(TOp_-, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index => mv.visitIincInsn(result.index, -n)
						case T3(TOp_+, TInt(n), rhs: TReg, result) if n >= -0x80 && n <= 0x7f && rhs.index == result.index => mv.visitIincInsn(result.index, n)
						case T3(TOp_+, lhs: TReg, TInt(n), result) if n >= -0x80 && n <= 0x7f && lhs.index == result.index => mv.visitIincInsn(result.index, n)
						case T3(operator, lhs, rhs, result) => {
							load(lhs)
							load(rhs)
							val t = TaasType.widen(lhs, rhs)
							binopWithType(operator, lhs, rhs, t)
							storeByType(t, result)
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
										case _: TaasClass | _: TaasInterface => load(t)
										case _: TaasFunction =>
									}
							}

							if(n < m) { error("optional parameters")}
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
											println("[WARNING]: "+method+" should have been resolved to its setter.")
											load(arguments(0))
											implicitCast(arguments(0).`type`, method.`type`)
										}
										case _: TaasFunction => {
											println("[WARNING]: Method "+method+" has variable arguments.")
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
									case k: TaasClass => mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, toJavaName(ownerOf(method)), method.name.name, methodDesc(method))
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
								case Some(result) => {
									storeByType(method.`type`, result)
								}
								case None =>
							}
						}
						case TLoad(obj, field, result) => {
							mv.visitFieldInsn(JOpcodes.GETSTATIC, toJavaName(ownerOf(field)), field.name.name, toJavaType(field.`type`))
							storeByType(field.`type`, result)

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

		mv.visitMaxs(1, 1)//4, maxL)
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
		case TaasFunctionType => "Las3/Function"
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
		case TaasFunctionType => "as3/Function"
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
}