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
package apparat.taas.runtime

import apparat.taas.ast._

/**
 * @author Joa Ebert
 */
object Eval {
	def apply(op: TaasBinop, lhs: TConst, rhs: TConst): Option[TConst] = {
		TaasType.widenOption(lhs, rhs) match {
			case Some(newType) =>
				Convert(lhs, newType) match {
					case Some(newLHS) => Convert(rhs, newType) match {
						case Some(newRHS) => {
							newType match {
								case TaasStringType => op match {
									case TOp_+ => Some(TString(Symbol(
										newLHS.asInstanceOf[TString].value.name +
												newRHS.asInstanceOf[TString].value.name)))
									case _ => None
								}
								case TaasDoubleType => {
									val lt = newLHS.asInstanceOf[TDouble]
									val rt = newRHS.asInstanceOf[TDouble]
									op match {
										case TOp_+   => Some(TDouble(lt.value + rt.value))
										case TOp_-   => Some(TDouble(lt.value - rt.value))
										case TOp_*   => Some(TDouble(lt.value * rt.value))
										case TOp_/   => Some(TDouble(lt.value / rt.value))
										case TOp_%   => Some(TDouble(lt.value % rt.value))
										case TOp_&   => Some(TInt(lt.value.toInt & rt.value.toInt))
										case TOp_^   => Some(TInt(lt.value.toInt ^ rt.value.toInt))
										case TOp_|   => Some(TInt(lt.value.toInt | rt.value.toInt))
										case TOp_<<  => Some(TInt(lt.value.toInt << rt.value.toInt))
										case TOp_>>  => Some(TInt(lt.value.toInt >> rt.value.toInt))
										case TOp_>>> => Some(TInt(lt.value.toInt >>> rt.value.toInt))
										case TOp_==  => Some(TBool(lt.value == rt.value))
										case TOp_>=  => Some(TBool(lt.value >= rt.value))
										case TOp_>   => Some(TBool(lt.value > rt.value))
										case TOp_<=  => Some(TBool(lt.value <= rt.value))
										case TOp_<   => Some(TBool(lt.value < rt.value))
										case TOp_!=  => Some(TBool(lt.value != rt.value))
										case TOp_!>= => Some(TBool(!(lt.value >= rt.value)))
										case TOp_!>  => Some(TBool(!(lt.value > rt.value)))
										case TOp_!<= => Some(TBool(!(lt.value <= rt.value)))
										case TOp_!<  => Some(TBool(!(lt.value < rt.value)))
										case TOp_=== => Some(TBool(lt.value == rt.value))
										case TOp_!== => Some(TBool(lt.value != rt.value))
									}
								}
								case TaasLongType => {
									val lt = newLHS.asInstanceOf[TLong]
									val rt = newRHS.asInstanceOf[TLong]
									op match {
										case TOp_+   => Some(TLong(lt.value + rt.value))
										case TOp_-   => Some(TLong(lt.value - rt.value))
										case TOp_*   => Some(TLong(lt.value * rt.value))
										case TOp_/   => Some(TLong(lt.value / rt.value))
										case TOp_%   => Some(TLong(lt.value % rt.value))
										case TOp_&   => Some(TLong(lt.value & rt.value))
										case TOp_^   => Some(TLong(lt.value ^ rt.value))
										case TOp_|   => Some(TLong(lt.value | rt.value))
										case TOp_<<  => Some(TLong(lt.value << rt.value))
										case TOp_>>  => Some(TLong(lt.value >> rt.value))
										case TOp_>>> => Some(TLong(lt.value >>> rt.value))
										case TOp_==  => Some(TBool(lt.value == rt.value))
										case TOp_>=  => Some(TBool(lt.value >= rt.value))
										case TOp_>   => Some(TBool(lt.value > rt.value))
										case TOp_<=  => Some(TBool(lt.value <= rt.value))
										case TOp_<   => Some(TBool(lt.value < rt.value))
										case TOp_!=  => Some(TBool(lt.value != rt.value))
										case TOp_!>= => Some(TBool(!(lt.value >= rt.value)))
										case TOp_!>  => Some(TBool(!(lt.value > rt.value)))
										case TOp_!<= => Some(TBool(!(lt.value <= rt.value)))
										case TOp_!<  => Some(TBool(!(lt.value < rt.value)))
										case TOp_=== => Some(TBool(lt.value == rt.value))
										case TOp_!== => Some(TBool(lt.value != rt.value))
									}
								}
								case TaasIntType => {
									val lt = newLHS.asInstanceOf[TInt]
									val rt = newRHS.asInstanceOf[TInt]
									op match {
										case TOp_+   => Some(TInt(lt.value + rt.value))
										case TOp_-   => Some(TInt(lt.value - rt.value))
										case TOp_*   => Some(TInt(lt.value * rt.value))
										case TOp_/   => Some(TInt(lt.value / rt.value))
										case TOp_%   => Some(TInt(lt.value % rt.value))
										case TOp_&   => Some(TInt(lt.value & rt.value))
										case TOp_^   => Some(TInt(lt.value ^ rt.value))
										case TOp_|   => Some(TInt(lt.value | rt.value))
										case TOp_<<  => Some(TInt(lt.value << rt.value))
										case TOp_>>  => Some(TInt(lt.value >> rt.value))
										case TOp_>>> => Some(TInt(lt.value >>> rt.value))
										case TOp_==  => Some(TBool(lt.value == rt.value))
										case TOp_>=  => Some(TBool(lt.value >= rt.value))
										case TOp_>   => Some(TBool(lt.value > rt.value))
										case TOp_<=  => Some(TBool(lt.value <= rt.value))
										case TOp_<   => Some(TBool(lt.value < rt.value))
										case TOp_!=  => Some(TBool(lt.value != rt.value))
										case TOp_!>= => Some(TBool(!(lt.value >= rt.value)))
										case TOp_!>  => Some(TBool(!(lt.value > rt.value)))
										case TOp_!<= => Some(TBool(!(lt.value <= rt.value)))
										case TOp_!<  => Some(TBool(!(lt.value < rt.value)))
										case TOp_=== => Some(TBool(lt.value == rt.value))
										case TOp_!== => Some(TBool(lt.value != rt.value))
									}
								}
								case _ => None
							}
						}
						case None => None
					}
					case None => None
				}
			case None => None
		}
	}
}
