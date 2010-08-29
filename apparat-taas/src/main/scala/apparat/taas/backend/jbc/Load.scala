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

import apparat.taas.ast._

import org.objectweb.asm.{MethodVisitor => JMethodVisitor}
import org.objectweb.asm.{Opcodes => JOpcodes}
import apparat.log.SimpleLog

/**
 * @author Joa Ebert
 */
protected[jbc] object Load extends SimpleLog {
	final case class Error(message: String)

	def apply(value: TValue, asType: TaasType, mapIndex: Int => Int)(implicit mv: JMethodVisitor, currentNominal: TaasNominal): Option[Load.Error] = {
		def loadWithCast() = {
			val result = apply(value, mapIndex)

			Cast(value.`type`, asType) match {
				case Right(_) =>
				case Left(x) => {
					log.error("Implicit cast failed: %s", x.message)
					error(x.message)
				}
			}

			result
		}

		if(value == TNull) {
			apply(TNull, mapIndex)
		} else if(value.`type` != asType) {
			value match {
				case TInt(i) => asType match {
					case TaasDoubleType => apply(TDouble(i.toDouble), mapIndex)
					case TaasLongType => apply(TLong(i.toLong), mapIndex)
					case TaasStringType => apply(TString(Symbol(i.toString)), mapIndex)
					case _ => loadWithCast()
				}
				case TDouble(d) => asType match {
					case TaasIntType => apply(TInt(d.toInt), mapIndex)
					case TaasLongType => apply(TLong(d.toLong), mapIndex)
					case TaasStringType => apply(TString(Symbol(d.toString)), mapIndex)
					case _ => loadWithCast()
				}
				case TLong(l) => asType match {
					case TaasIntType => apply(TInt(l.toInt), mapIndex)
					case TaasDoubleType => apply(TDouble(l.toDouble), mapIndex)
					case TaasStringType => apply(TString(Symbol(l.toString)), mapIndex)
					case _ => loadWithCast()
				}
				case _ => loadWithCast()
			}
		} else {
			apply(value, mapIndex)
		}
	}

	def apply(value: TValue, mapIndex: Int => Int)(implicit mv: JMethodVisitor, currentNominal: TaasNominal): Option[Load.Error] = value match {
		case TClosure(value) => {
			val closureName = Java.nameOf(currentNominal.qualifiedName)+"$"+value.name.name
			val nominalType = Java typeOf TaasNominalTypeInstance(currentNominal)
			mv.visitTypeInsn(JOpcodes.NEW, closureName)
			mv.visitInsn(JOpcodes.DUP)
			mv.visitVarInsn(JOpcodes.ALOAD, 0)
			mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, closureName, "<init>", "("+nominalType+")V")
			None
		}
		case TInt(value) => value match {
			case -1 =>
				mv.visitInsn(JOpcodes.ICONST_M1)
				None
			case 0 =>
				mv.visitInsn(JOpcodes.ICONST_0)
				None
			case 1 =>
				mv.visitInsn(JOpcodes.ICONST_1)
				None
			case 2 =>
				mv.visitInsn(JOpcodes.ICONST_2)
				None
			case 3 =>
				mv.visitInsn(JOpcodes.ICONST_3)
				None
			case 4 =>
				mv.visitInsn(JOpcodes.ICONST_4)
				None
			case 5 =>
				mv.visitInsn(JOpcodes.ICONST_5)
				None
			case b if b < 0x80 && b >= -0x80 =>
				mv.visitIntInsn(JOpcodes.BIPUSH, b)
				None
			case si if si < 0x8000 && si >= -0x8000 =>
				mv.visitIntInsn(JOpcodes.SIPUSH, si)
				None
			case i =>
				mv.visitLdcInsn(new java.lang.Integer(i))
				None
		}
		case TLong(value) => value match {
			case 0L =>
				mv.visitInsn(JOpcodes.LCONST_0)
				None
			case 1L =>
				mv.visitInsn(JOpcodes.LCONST_1)
				None
			case l =>
				mv.visitLdcInsn(new java.lang.Long(l))
				None
		}
		case TBool(value) => value match {
			case true =>
				mv.visitInsn(JOpcodes.ICONST_1)
				None
			case false =>
				mv.visitInsn(JOpcodes.ICONST_0)
				None
		}
		case TString(value) =>
			mv.visitLdcInsn(value.name)
			None
		case TDouble(value) => value match {
			case 0.0 =>
				mv.visitInsn(JOpcodes.DCONST_0)
				None
			case 1.0 =>
				mv.visitInsn(JOpcodes.DCONST_1)
				None
			case d =>
				mv.visitLdcInsn(new java.lang.Double(d))
				None
		}
		case TLexical(definition) => definition match {
			case method: TaasMethod => {
				if(method.isStatic) {
					Some(Load.Error("static method"))
				} else {
					mv.visitVarInsn(JOpcodes.ALOAD, 0)
					mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, Java nameOfOwnerOf method, method.name.name, Java methodDesc method)
					None
				}
			}
			case klass: TaasClass => None
			case fun: TaasFunction => None
			case a: TaasAnnotation => Some(Load.Error("Unexpected TaasAnnotation "+a+"."))
			case f: TaasField => Some(Load.Error("Unexpected TaasField "+f+"."))
			case i: TaasInterface => Some(Load.Error("Unexpected TaasInterface "+i+"."))
		}
		case reg: TReg => {
			val index = mapIndex(reg.index)
			reg.`type` match {
				case TaasAnyType | TaasObjectType | TaasStringType | TaasFunctionType | _: TaasNominalType =>
					mv.visitVarInsn(JOpcodes.ALOAD, index)
					None
				case TaasBooleanType | TaasIntType =>
					mv.visitVarInsn(JOpcodes.ILOAD, index)
					None
				case TaasDoubleType =>
					mv.visitVarInsn(JOpcodes.DLOAD, index)
					None
				case TaasLongType =>
					mv.visitVarInsn(JOpcodes.LLOAD, index)
					None
				case TaasVoidType => Some(Load.Error("Cannot store void in register."))
			}
		}
		case TNull =>
			mv.visitInsn(JOpcodes.ACONST_NULL)
			None
		case TClass(value) => Some(Load.Error("Unexpected TClass "+value+"."))
		case TVoid => Some(Load.Error("Cannot load TVoid."))
		case TInstance(value) => Some(Load.Error("Unexpected TInstance "+value+"."))
	}
}