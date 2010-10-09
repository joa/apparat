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
sealed trait TaasType {
	def isEqual(that: TaasType) = TaasType.isEqual(this, that)
}

object TaasType {
	def widenOption(a: TaasTyped, b: TaasTyped): Option[TaasType] = widenOption(a.`type`, b.`type`)

	def widenOption(a: TaasType, b: TaasType): Option[TaasType] = {
		if(a == b) { Some(a)
		} else if(a == TaasAnyType) { Some(b)
		} else if(b == TaasAnyType) { Some(a)
		} else if(a == TaasStringType || b == TaasStringType) { Some(TaasStringType)
		} else if(a == TaasDoubleType || b == TaasDoubleType) { Some(TaasDoubleType)
		} else if((a == TaasLongType && b != TaasLongType) || (a != TaasLongType && b == TaasLongType)) { Some(TaasDoubleType)
		} else if(a == TaasIntType || b == TaasIntType) { Some(TaasIntType)
		} else { None
		}
	}

	def widen(a: TaasTyped, b: TaasTyped): TaasType = widen(a.`type`, b.`type`)

	def widen(a: TaasType, b: TaasType): TaasType = widenOption(a, b) getOrElse error("Cannot widen types "+a+" and "+b+".")

	def isEqual(a: TaasType, b: TaasType): Boolean = a match {
		case TaasAnyType => b == TaasAnyType
		case TaasVoidType => b == TaasVoidType
		case TaasBooleanType => b == TaasBooleanType
		case TaasDoubleType => b == TaasDoubleType
		case TaasIntType => b == TaasIntType
		case TaasObjectType => b == TaasObjectType
		case TaasStringType => b == TaasStringType
		case TaasLongType => b == TaasLongType
		case TaasFunctionType => b == TaasFunctionType
		case x: TaasParameterizedType => b match {
			case y: TaasParameterizedType => if(x.nominal == y.nominal) {
				x.parameters zip y.parameters forall { x => x._1 == x._2 }
			} else { false }
			case _ => false
		}
		case x: TaasNominalType => b match {
			case y: TaasNominalType => x.nominal == y.nominal
			case _ => false
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
		"TaasType(\"" + nominal.qualifiedName + "\")"
	}
}

trait TaasParameterizedType extends TaasNominalType {
	def parameters: List[TaasType]
	override def toString = {
		"TaasType(" + nominal.qualifiedName + "<" + parameters.mkString(",")+ ">)"
	}
}

case class TaasNominalTypeInstance(nominal: TaasNominal) extends TaasNominalType