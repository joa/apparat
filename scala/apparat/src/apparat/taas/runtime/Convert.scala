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
package apparat.taas.runtime

import apparat.taas.ast._

/**
 * @author Joa Ebert
 */
object Convert {
	def apply(value: TConst, toType: TaasType): Option[TConst] = {
		if(value.`type` == toType) {
			Some(value)
		} else {
			value match {
				case value: TInt => apply(value, toType)
				case value: TLong => apply(value, toType)
				case value: TBool => None
				case value: TString => None
				case value: TDouble => apply(value, toType)
				case value: TClass => None
				case value: TInstance => None
			}
		}
	}

	def apply(value: TInt, toType: TaasType): Option[TConst] = toType match {
		case TaasBooleanType => value match {
			case TInt(0) => Some(TBool(false))
			case _ => Some(TBool(true))
		}
		case TaasDoubleType => Some(TDouble(value.value.toDouble))
		case TaasIntType => Some(value)
		case TaasStringType => Some(TString(Symbol(value.value.toString)))
		case TaasLongType => Some(TLong(value.value.toLong))
		case _ => None
	}

	def apply(value: TLong, toType: TaasType): Option[TConst] = toType match {
		case TaasBooleanType => value match {
			case TLong(0L) => Some(TBool(false))
			case _ => Some(TBool(true))
		}
		case TaasDoubleType => Some(TDouble(value.value.toDouble))
		case TaasIntType => Some(TInt(value.value.toInt))
		case TaasStringType => Some(TString(Symbol(value.value.toString)))
		case TaasLongType => Some(value)
		case _ => None
	}

	def apply(value: TDouble, toType: TaasType): Option[TConst] = toType match {
		case TaasBooleanType => value match {
			case TDouble(0.0) => Some(TBool(false))
			case _ => Some(TBool(true))
		}
		case TaasDoubleType => Some(value)
		case TaasIntType => Some(TInt(value.value.toInt))
		case TaasStringType => Some(TString(Symbol(value.value.toString)))
		case TaasLongType => Some(TLong(value.value.toLong))
		case _ => None
	}
}