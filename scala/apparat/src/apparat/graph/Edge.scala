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
	val Default, Jump, True, False, DefaultCase, Case, Throw, Return = Value
}

sealed abstract class Edge[V](val startVertex: V, val endVertex: V, val kind: EdgeKind.EdgeKind)

case class DefaultEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Default)
case class JumpEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Jump)
case class TrueEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.True)
case class FalseEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.False)
case class DefaultCaseEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.DefaultCase)
case class CaseEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Case)
case class ThrowEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Throw)
case class ReturnEdge[V](override val startVertex: V, override val endVertex: V) extends Edge[V](startVertex, endVertex, EdgeKind.Return)