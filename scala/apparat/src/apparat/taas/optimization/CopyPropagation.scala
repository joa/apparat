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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.taas.optimization

import apparat.taas.ast._
import apparat.taas.graph.TaasGraph

/**
 * @author Joa Ebert
 */
object CopyPropagation {
	def apply(graph: TaasGraph): Boolean = {
		var modified = false
		for(vertex <- graph.verticesIterator) {
			val (m, r) = apply(vertex.block)
			modified |= m
			vertex.block = r
		}

		modified
	}

	def apply(block: List[TExpr]): (Boolean, List[TExpr]) = {
		@inline def usesValue(register: Int)(t: (Int, TValue)) = t._2 match {
			case TReg(index) if register == index => true
			case _ => false
		}

		@inline def usesExpr(register: Int)(t: (Int, T3)) = t._2 match {
			case T3(_, lhs: TReg, _, _) if lhs.index == register => true
			case T3(_, _, rhs: TReg, _) if rhs.index == register => true
			case _ => false
		}

		var modified = false
		var r = List.empty[TExpr]
		var values = Map.empty[Int, TValue]
		var exprs = Map.empty[Int, T3]

		for(op <- block) {
			op match {
				case t2 @ T2(op, rhs: TReg, result) if op == TOp_Nothing => {
					(exprs get rhs.index) match {
						case Some(expr) => {
							val n = T3(expr.op, expr.lhs, expr.rhs, result)
							modified = true
							r = n :: r
							values = (values filterNot { usesValue(result.index) _ }) - result.index
							exprs = (exprs filterNot { usesExpr(result.index) _ }) + (result.index -> n)
						}
						case None => {
							val (m, n) = copy(t2, values)
							modified |= m
							r = n :: r
							values = (values filterNot { usesValue(result.index) _ }) + (result.index -> n.asInstanceOf[T2].rhs)
							exprs = (exprs filterNot { usesExpr(result.index) _ }) - result.index
						}
					}
				}
				case t2 @ T2(op, rhs, result) if op == TOp_Nothing => {
					val (m, n) = copy(t2, values)
					modified |= m
					r = n :: r
					values = (values filterNot { usesValue(result.index) _ }) + (result.index -> n.asInstanceOf[T2].rhs)
					exprs = (exprs filterNot { usesExpr(result.index) _ }) - result.index
				}
				case t3 @ T3(op, lhs, rhs, result) => {
					val (m, n) = copy(t3, values)
					modified |= m
					r = n :: r
					values = (values filterNot { usesValue(result.index) _ }) - result.index
					exprs = (exprs filterNot { usesExpr(result.index) _ }) + (result.index -> n.asInstanceOf[T3])
				}
				case tdef: TDef => {
					val (m, n) = copy(tdef, values)
					modified |= m
					r = n :: r
					values = (values filterNot { usesValue(tdef.register) _ }) - tdef.register
					exprs = (exprs filterNot { usesExpr(tdef.register) _ }) - tdef.register
				}
				case o => {
					val (m, n) = copy(o, values)
					modified |= m
					r = n :: r
				}
			}
		}

		if(modified) {
			(true, r.reverse)
		} else {
			(false, block)
		}
	}

	private def copy(texpr: TExpr, available: Map[Int, TValue]): (Boolean, TExpr) = {
		texpr match {
			// x = op y -> x = op f(y)
			case t2 @ T2(op, rhs: TReg, result) => (available get rhs.index) match {
				case Some(value) => (true, T2(op, value, result))
				case None => (false, t2)
			}

			// x = y op z -> x = f(y) op f(z)
			case t3 @ T3(op, lhs: TReg, rhs: TReg, result) => (available get lhs.index) match {
				case Some(lvalue) => (available get rhs.index) match {
					case Some(rvalue) => (true, T3(op, lvalue, rvalue, result))
					case None => (true, T3(op, lvalue, rhs, result))
				}
				case None => (available get rhs.index) match {
					case Some(rvalue) => (true, T3(op, lhs, rvalue, result))
					case None => (false, t3)
				}
			}
			case t3 @ T3(op, lhs: TReg, rhs, result) => (available get lhs.index) match {
				case Some(value) => (true, T3(op, value, rhs, result))
				case None => (false, t3)
			}
			case t3 @ T3(op, lhs, rhs: TReg, result) => (available get rhs.index) match {
				case Some(value) => (true, T3(op, lhs, value, result))
				case None => (false, t3)
			}

			//if(x op y) -> if(f(x) op f(y))
			case if2 @ TIf2(op, lhs: TReg, rhs: TReg) => (available get lhs.index) match {
				case Some(lvalue) => (available get rhs.index) match {
					case Some(rvalue) => (true, TIf2(op, lvalue, rvalue))
					case None => (true, TIf2(op, lvalue, rhs))
				}
				case None => (available get rhs.index) match {
					case Some(rvalue) => (true, TIf2(op, lhs, rvalue))
					case None => (false, if2)
				}
			}
			case if2 @ TIf2(op, lhs: TReg, rhs) => (available get lhs.index) match {
				case Some(value) => (true, TIf2(op, value, rhs))
				case None => (false, if2)
			}
			case if2 @ TIf2(op, lhs, rhs: TReg) => (available get rhs.index) match {
				case Some(value) => (true, TIf2(op, lhs, value))
				case None => (false, if2)
			}

			// x = y.method(arguments) -> x = f(y).method(arguments)
			// y.method(arguments) -> f(y).method(arguments)
			case call @ TCall(t: TReg, method, arguments, result) => (available get t.index) match {
				case Some(value) => (true, TCall(value, method, arguments, result))
				case None => (false, call)
			}

			case tsuper @ TSuper(base: TReg, arguments) => (available get base.index) match {
				case Some(value) => (true, TSuper(value, arguments))
				case None => (false, tsuper)
			}

			case other => (false, other)
		}
	}
}