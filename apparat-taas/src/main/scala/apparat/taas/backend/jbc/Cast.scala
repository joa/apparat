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
import org.objectweb.asm.{Type, MethodVisitor => JMethodVisitor, Opcodes => JOpcodes}

/**
 * @author Joa Ebert
 */
protected[jbc] object Cast {
	//
	// If REQUIRE_TYPEERROR is set to false a TypeError will not be passed to
	// ActionScript catch blocks when a type coercion error occurs.
	// This is usually bad practice and an error that indicates a bug. Since it
	// is an expensive check we can omit it in 99% of all cases anyways for more
	// performance.
	//
	val REQUIRE_TYPEERROR = true

	final case class Error(message: String)

	//TODO replace either with option
	def apply(source: TaasType, target: TaasType)(implicit mv: JMethodVisitor): Either[Cast.Error, Unit] = {
		if(source == target) {
			Right(())
		} else {
			source match {
				case TaasIntType => target match {
					case TaasDoubleType => Right(mv.visitInsn(JOpcodes.I2D))
					case TaasLongType => Right(mv.visitInsn(JOpcodes.I2L))
					case TaasObjectType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"))
					case TaasStringType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;"))
					case other => Left(Cast.Error("Cannot convert from Int to "+other))
				}
				case TaasDoubleType => target match {
					case TaasIntType => Right(mv.visitInsn(JOpcodes.D2I))
					case TaasLongType => Right(mv.visitInsn(JOpcodes.D2L))
					case TaasObjectType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;"))
					case TaasStringType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(D)Ljava/lang/String;"))
					case other => Left(Cast.Error("Cannot convert from Double to "+other))
				}
				case TaasLongType => target match {
					case TaasDoubleType => Right(mv.visitInsn(JOpcodes.L2D))
					case TaasIntType =>  Right(mv.visitInsn(JOpcodes.L2I))
					case TaasObjectType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"))
					case TaasStringType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(J)Ljava/lang/String;"))
					case other => Left(Cast.Error("Cannot convert from Long to "+other))
				}
				case TaasStringType => target match {
					case TaasObjectType => Right(())
					case other => Left(Cast.Error("Cannot convert String to "+other))
				}
				case a: TaasNominalType => target match {
					case b: TaasNominalType => if(a.nominal == b.nominal) {
						Right(())
					} else {
						def loop(current: Option[TaasType]): Boolean = {
							current match {
								case Some(nominal: TaasNominalType) => if(nominal.nominal == b.nominal) { return true } else { loop(nominal.nominal.base) }
								case Some(other) => false
								case None => false
							}
						}

						if(loop(Some(a))) {
							Right(())
						} else {
							Right(checkCast(target))
						}
					}
					case TaasObjectType | TaasAnyType => Right(())
					case TaasStringType => Right(mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;"))
					case other => Right(checkCast(other))
				}
				case TaasObjectType | TaasAnyType => Right(checkCast(target))
				case _ => Left(Cast.Error("Cannot convert from "+source+" to "+target))
			}
		}
	}

	def checkCast(target: TaasType)(implicit mv: JMethodVisitor) = {
		if(REQUIRE_TYPEERROR) {
			mv.visitInsn(JOpcodes.DUP)
			mv.visitLdcInsn(Type.getType(Java typeOf target))
			mv.visitMethodInsn(JOpcodes.INVOKESTATIC, "jitb/lang/AVM", "coerce", "(Ljava/lang/Object;Ljava/lang/Class;)V")
		}

		mv.visitTypeInsn(JOpcodes.CHECKCAST, Java nameOf target)
	}
}