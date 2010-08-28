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

/**
 * @author Joa Ebert
 */
protected[jbc] object Binop {
	final case class Error(message: String)

	def apply(op: TaasBinop, lhs: TValue, rhs: TValue)(implicit mv: JMethodVisitor): Option[Binop.Error] =
		apply(op, lhs, rhs, TaasType.widen(lhs, rhs))

	def apply(op: TaasBinop, lhs: TValue, rhs: TValue, `type`: TaasType)(implicit mv: JMethodVisitor): Option[Binop.Error] = `type` match {
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
		case TaasStringType => Some(Binop.Error("TODO StringType"))
		case TaasVoidType => Some(Binop.Error("TODO VoidType"))
	}
}