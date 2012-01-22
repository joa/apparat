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
package apparat.taas.backend.jbc

import apparat.taas.ast._

import org.objectweb.asm.{MethodVisitor => JMethodVisitor}
import org.objectweb.asm.{Opcodes => JOpcodes}

/**
 * @author Joa Ebert
 */
protected[jbc] object Binop {
	final case class Error(message: String)

	def apply(
			op: TaasBinop, lhs: TValue, rhs: TValue,
			`type`: TaasType, mapIndex: Int => Int,
			loadAs: (TValue, TaasType) => Unit)(implicit mv: JMethodVisitor, currentNominal: TaasNominal): Option[Binop.Error] = {
		if(TaasStringType != `type`) {
			loadAs(lhs, `type`)
			loadAs(rhs, `type`)
		}

		`type` match {
			case TaasIntType => op match {
				case TOp_+   => mv.visitInsn(JOpcodes.IADD); None
				case TOp_-   => mv.visitInsn(JOpcodes.ISUB); None
				case TOp_*   => mv.visitInsn(JOpcodes.IMUL); None
				case TOp_/   => mv.visitInsn(JOpcodes.IDIV); None
				case TOp_%   => mv.visitInsn(JOpcodes.IREM); None
				case TOp_&   => mv.visitInsn(JOpcodes.IAND); None
				case TOp_^   => mv.visitInsn(JOpcodes.IXOR); None
				case TOp_|   => mv.visitInsn(JOpcodes.IOR ); None
				case TOp_<<  => mv.visitInsn(JOpcodes.ISHL); None
				case TOp_>>  => mv.visitInsn(JOpcodes.ISHR); None
				case TOp_>>> => mv.visitInsn(JOpcodes.IUSHR); None
				case TOp_==  => Some(Binop.Error("TODO =="))
				case TOp_>=  => Some(Binop.Error("TODO >="))
				case TOp_>   => Some(Binop.Error("TODO >"))
				case TOp_<=  => Some(Binop.Error("TODO <="))
				case TOp_<   => Some(Binop.Error("TODO <"))
				case TOp_!=  => Some(Binop.Error("TODO !="))
				case TOp_!>= => Some(Binop.Error("TODO !>="))
				case TOp_!>  => Some(Binop.Error("TODO !>"))
				case TOp_!<= => Some(Binop.Error("TODO !<="))
				case TOp_!<  => Some(Binop.Error("TODO !<"))
				case TOp_=== => Some(Binop.Error("TODO ==="))
				case TOp_!== => Some(Binop.Error("TODO !=="))
			}
			case TaasDoubleType => op match {
				case TOp_+   => mv.visitInsn(JOpcodes.DADD); None
				case TOp_-   => mv.visitInsn(JOpcodes.DSUB); None
				case TOp_*   => mv.visitInsn(JOpcodes.DMUL); None
				case TOp_/   => mv.visitInsn(JOpcodes.DDIV); None
				case TOp_%   => mv.visitInsn(JOpcodes.DREM); None
				case TOp_&   => Some(Binop.Error("Bitwise AND not supported for double type."))
				case TOp_^   => Some(Binop.Error("Bitwise XOR not supported for double type."))
				case TOp_|   => Some(Binop.Error("Bitwise OR not supported for double type."))
				case TOp_<<  => Some(Binop.Error("Bitwise SHL not supported for double type."))
				case TOp_>>  => Some(Binop.Error("Bitwise SHR not supported for double type."))
				case TOp_>>> => Some(Binop.Error("Bitwise USHR not supported for double type."))
				case TOp_==  => Some(Binop.Error("TODO =="))
				case TOp_>=  => Some(Binop.Error("TODO >="))
				case TOp_>   => Some(Binop.Error("TODO >"))
				case TOp_<=  => Some(Binop.Error("TODO <="))
				case TOp_<   => Some(Binop.Error("TODO <"))
				case TOp_!=  => Some(Binop.Error("TODO !="))
				case TOp_!>= => Some(Binop.Error("TODO !>="))
				case TOp_!>  => Some(Binop.Error("TODO !>"))
				case TOp_!<= => Some(Binop.Error("TODO !<="))
				case TOp_!<  => Some(Binop.Error("TODO !<"))
				case TOp_=== => Some(Binop.Error("TODO ==="))
				case TOp_!== => Some(Binop.Error("TODO !=="))
			}
			case TaasAnyType => Some(Binop.Error("TODO AnyType"))
			case TaasBooleanType => Some(Binop.Error("TODO BooleanType"))
			case TaasFunctionType => Some(Binop.Error("TODO FunctionType"))
			case TaasLongType => op match {
				case TOp_+   => mv.visitInsn(JOpcodes.LADD); None
				case TOp_-   => mv.visitInsn(JOpcodes.LSUB); None
				case TOp_*   => mv.visitInsn(JOpcodes.LMUL); None
				case TOp_/   => mv.visitInsn(JOpcodes.LDIV); None
				case TOp_%   => mv.visitInsn(JOpcodes.LREM); None
				case TOp_&   => mv.visitInsn(JOpcodes.LAND); None
				case TOp_^   => mv.visitInsn(JOpcodes.LXOR); None
				case TOp_|   => mv.visitInsn(JOpcodes.LOR ); None
				case TOp_<<  => mv.visitInsn(JOpcodes.LSHL); None
				case TOp_>>  => mv.visitInsn(JOpcodes.LSHR); None
				case TOp_>>> => mv.visitInsn(JOpcodes.LUSHR); None
				case TOp_==  => Some(Binop.Error("TODO =="))
				case TOp_>=  => Some(Binop.Error("TODO >="))
				case TOp_>   => Some(Binop.Error("TODO >"))
				case TOp_<=  => Some(Binop.Error("TODO <="))
				case TOp_<   => Some(Binop.Error("TODO <"))
				case TOp_!=  => Some(Binop.Error("TODO !="))
				case TOp_!>= => Some(Binop.Error("TODO !>="))
				case TOp_!>  => Some(Binop.Error("TODO !>"))
				case TOp_!<= => Some(Binop.Error("TODO !<="))
				case TOp_!<  => Some(Binop.Error("TODO !<"))
				case TOp_=== => Some(Binop.Error("TODO ==="))
				case TOp_!== => Some(Binop.Error("TODO !=="))
			}
			case x: TaasNominalType => Some(Binop.Error("TODO NominalType"))
			case TaasObjectType => Some(Binop.Error("TODO ObjectType"))
			case TaasStringType => op match {
				case TOp_+ => {
					mv.visitTypeInsn(JOpcodes.NEW, "java/lang/StringBuilder");
					mv.visitInsn(JOpcodes.DUP)
					mv.visitMethodInsn(JOpcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V")
					loadAs(lhs, `type`)
					mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
					loadAs(rhs, `type`)
					mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
					mv.visitMethodInsn(JOpcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;")
					None
				}
				case _ => Some(Binop.Error("Operator "+op+" is not overloaded for TaasStringType."))
			}
			case TaasVoidType => Some(Binop.Error("TODO VoidType"))
		}
	}
}
