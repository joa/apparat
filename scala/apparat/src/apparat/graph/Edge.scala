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
package apparat.graph

object EdgeKind extends Enumeration {
	type EdgeKind = Value
	val Default, Jump, True, False, DefaultCase, Case, NumberedCase, Throw, Return = Value
}

sealed abstract class Edge[V](val startVertex: V, val endVertex: V, val kind: EdgeKind.EdgeKind)

final case class DefaultEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Default)
final case class JumpEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Jump)
final case class TrueEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.True)
final case class FalseEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.False)
final case class DefaultCaseEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.DefaultCase)
final case class CaseEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Case)
final case class NumberedCaseEdge[V](override val startVertex: V, override val endVertex: V, val index: Int) extends Edge[V](startVertex, endVertex, EdgeKind.NumberedCase)
final case class ThrowEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Throw)
final case class ReturnEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Return)

object Edge {
	def copy[V](edge: Edge[V], start: Option[V] = None, end: Option[V] = None) = edge match {
		case e: DefaultEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: JumpEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: TrueEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: FalseEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: DefaultCaseEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: CaseEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: NumberedCaseEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex), e.index)
		case e: ThrowEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case e: ReturnEdge[_] => e.copy(start.getOrElse(e.startVertex), end.getOrElse(e.endVertex))
		case _ => error(edge + " is not and Edge")
	}

	def transpose[X, Y](edge: Edge[X], start: Y, end: Y) = edge match {
		case e: DefaultEdge[_] => DefaultEdge(start, end)
		case e: JumpEdge[_] => JumpEdge(start, end)
		case e: TrueEdge[_] => TrueEdge(start, end)
		case e: FalseEdge[_] => FalseEdge(start, end)
		case e: DefaultCaseEdge[_] => DefaultCaseEdge(start, end)
		case e: CaseEdge[_] => CaseEdge(start, end)
		case e: NumberedCaseEdge[_] => NumberedCaseEdge(start, end, e.index)
		case e: ThrowEdge[_] => ThrowEdge(start, end)
		case e: ReturnEdge[_] => ReturnEdge(start, end)
		case _ => error(edge + " is not and Edge")
	}
}