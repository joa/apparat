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
package apparat.taas.ast

/**
 * @author Joa Ebert
 */
sealed trait TaasType

object TaasType {
	def widen(a: TaasType, b: TaasType): TaasType = {
		if(a == b) { a
		} else if(a == TaasStringType || b == TaasStringType) { TaasStringType
		} else if(a == TaasDoubleType || b == TaasDoubleType) { TaasDoubleType
		} else if((a == TaasLongType && b != TaasLongType) || (a != TaasLongType && b == TaasLongType)) { TaasDoubleType
		} else if(a == TaasIntType || b == TaasIntType) { TaasIntType
		} else { error("Cannot widen types "+a+" and "+b+".")
		}
	}
}

object TaasAnyType extends TaasType {
	override def toString = "TaasType(*)"
}

object TaasVoidType extends TaasType {
	override def toString = "TaasType(void)"
}

object TaasBooleanType extends TaasType {
	override def toString = "TaasType(boolean)"
}

object TaasDoubleType extends TaasType {
	override def toString = "TaasType(double)"
}

object TaasIntType extends TaasType {
	override def toString = "TaasType(int)"
}

object TaasObjectType extends TaasType {
	override def toString = "TaasType(object)"
}

object TaasStringType extends TaasType {
	override def toString = "TaasType(string)"
}

object TaasLongType extends TaasType {
	override def toString = "TaasType(long)"
}

object TaasFunctionType extends TaasType {
	override def toString = "TaasType(function)"
}

trait TaasNominalType extends TaasType  {
	def nominal: TaasNominal
	override def toString = {
		"TaasType(" + nominal.qualifiedName + ")"
	}
}

trait TaasParameterizedType extends TaasNominalType {
	def parameters: List[TaasType]
	override def toString = {
		"TaasType(" + nominal.qualifiedName + "<" + parameters.mkString(",")+ ">)"
	}
}

case class TaasNominalTypeInstance(nominal: TaasNominal) extends TaasNominalType