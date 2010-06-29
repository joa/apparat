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
import annotation.tailrec

/**
 * @author Joa Ebert
 */
object CopyPropagation extends TaasOptimization {
	def name = "Copy Propagation"
	
	def optimize(context: TaasOptimizationContext) = apply(context.code.graph) match {
		case true => context.copy(modified = true)
		case false => context
	}

	def apply(graph: TaasGraph): Boolean = {
		var modified = false
		for(vertex <- graph.verticesIterator) {
			val (m, r) = propagateValuesAndExpressions(vertex.block)
			modified |= m
			vertex.block = r
		}

		modified
	}

	def propagateValuesAndExpressions(block: List[TExpr]): (Boolean, List[TExpr]) = {
		var modified = false
		var r = List.empty[TExpr]
		var values = Map.empty[Int, TValue]
		var exprs = Map.empty[Int, T3]

		@inline def usesValue(register: Int)(t: (Int, TValue)) = t._2 match {
			case TReg(index) if register == index => true
			case _ => false
		}

		@inline def usesExpr(register: Int)(t: (Int, T3)) = t._2 match {
			case T3(_, lhs: TReg, _, _) if lhs.index == register => true
			case T3(_, _, rhs: TReg, _) if rhs.index == register => true
			case _ => false
		}

		@inline def update(index: Int, valuef: Map[Int, TValue] => Map[Int, TValue], exprf: Map[Int, T3] => Map[Int, T3]): Unit = {
			values = valuef(values filterNot { usesValue(index) _ })
			exprs = exprf(exprs filterNot { usesExpr(index) _ })
		}

		@inline def map(original: TExpr): TExpr = {
			val (m, n) = copy(original, values)
			modified |= m
			r = n :: r
			n
		}

		for(op <- block) {
			op match {
				case t2 @ T2(op, rhs: TReg, result) if op == TOp_Nothing => {
					(exprs get rhs.index) match {
						case Some(expr) => {
							val n = T3(expr.op, expr.lhs, expr.rhs, result)

							modified = true
							r = n :: r

							update(result.index, _ - result.index, _ + (result.index -> n))
						}
						case None => {
							val n = map(t2)
							update(result.index, _ + (result.index -> n.asInstanceOf[T2].rhs), _ - result.index)
						}
					}
				}
				case t2 @ T2(op, rhs, result) if op == TOp_Nothing => {
					val n = map(t2)
					update(result.index, _ + (result.index -> n.asInstanceOf[T2].rhs), _ - result.index)
				}
				case t3 @ T3(op, lhs, rhs, result) => {
					val n = map(t3)
					update(result.index, _ - result.index, _ + (result.index -> n.asInstanceOf[T3]))
				}
				case tdef: TDef => {
					map(tdef)
					update(tdef.register, _ - tdef.register, _ - tdef.register)
				}
				case o => map(o)
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
				case Some(value) => {
					(true, TCall(value, method, copy(arguments, available)._2, result))
				}
				case None => copy(arguments, available) match {
					case (true, na) => (true, TCall(t, method, na, result))
					case _ => (false, call)
				}
			}

			case tsuper @ TSuper(base: TReg, arguments) => (available get base.index) match {
				case Some(value) => (true, TSuper(value, copy(arguments, available)._2))
				case None => copy(arguments, available) match {
					case (true, na) => (true, TSuper(base, na))
					case _ => (false, tsuper)
				}
			}

			case other => (false, other)
		}
	}

	def copy(list: List[TValue], available: Map[Int, TValue]): (Boolean, List[TValue]) = {
		var m = false
		val r = list map {
			_ match {
				case reg: TReg => (available get reg.index) match {
					case Some(value) => { m = true; value }
					case None => reg
				}
				case o => o
			}
		}

		(m, r)
	}
}