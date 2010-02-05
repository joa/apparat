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
package apparat.graph.immutable

import apparat.graph._
import analysis.{StronglyConnectedComponentFinder, Dominance}

object Graph {
	def apply[V](edges: Edge[V]*): Graph[V] = {
		def loop(edges: Seq[Edge[V]], graph: Graph[V]): Graph[V] = {
			if(edges.isEmpty) graph else {
				val edge = edges.head
				val remaining = edges drop 1
				val g = if(graph contains edge.startVertex) { graph } else { graph + edge.startVertex }
				loop(remaining, (if(g contains edge.endVertex) { g } else { g + edge.endVertex }) + edge)
			}
		}

		loop(edges, empty[V])
	}

	def apply[V](edges: Tuple2[V,V]*)(implicit f: (V, V) => Edge[V]): Graph[V] = {
		apply(edges map { edge => f(edge._1, edge._2) }: _*)
	}

	def empty[V]: Graph[V] = new EmptyGraph[V]
}

class Graph[V](val adjacency: Map[V,List[Edge[V]]]) extends GraphLike[V] with DefaultDOTExport[V] with Immutable {
	def this() = this(Map.empty[V, List[Edge[V]]])

	type G = Graph[V]

	lazy val topsort = new TopsortTraversal(this)
	
	lazy val dominance = new Dominance(this)

	lazy val sccs = new StronglyConnectedComponentFinder(this)

	private def newGraph(adjacency: Map[V,List[E]]) = new Graph(adjacency)

	def ++(that: Traversable[V]) = {
		def loop(that: Traversable[V]): Graph[V] = {
			if(that.isEmpty) {
				this
			} else {
				loop(that drop 1) + that.head
			}
		}

		loop(that)
	}

	def +(vertex: V) = {
		assert(!contains(vertex), "Graph must not contain vertex.")
		newGraph(adjacency + (vertex -> Nil))
	}

	def +>(that: Traversable[E]) = {
		def loop(that: Traversable[E]): Graph[V] = {
			if(that.isEmpty) {
				this
			} else {
				loop(that drop 1) + that.head
			}
		}

		loop(that)
	}

	def +(edge: E) = {
		assert(contains(edge.startVertex), "Graph must contain start vertex.")
		assert(contains(edge.endVertex), "Graph must contain end vertex.")
		assert(!contains(edge), "Graph must not contain edge object already.")
		newGraph(adjacency updated (edge.startVertex, edge :: adjacency(edge.startVertex)))
	}

	def +(edge: (V, V))(implicit f: (V, V) => Edge[V]): G = ((contains(edge._1), contains(edge._2)) match {
		case (true, true) => this
		case (false, true) => this + edge._1
		case (true, false) => this + edge._2
		case (false, false) => this + edge._1 + edge._2
	}) + f(edge._1, edge._2)

	def -(vertex: V) = {
		assert(contains(vertex))
		newGraph(adjacency filterNot (_._1 == vertex) map { e => e._1 -> (e._2 filterNot (_.endVertex == vertex))})
	}

	def -(edge: E) = {
		assert(contains(edge.startVertex), "Graph must contain start vertex.")
		assert(contains(edge.endVertex), "Graph must contain end vertex.")
		assert(contains(edge), "Graph must contain edge.")
		newGraph(adjacency updated (edge.startVertex, adjacency(edge.startVertex) filterNot (_ == edge)))
	}

	def -(edge: (V, V)) = if(contains(edge._1) && contains(edge._2)) {
		newGraph(adjacency updated (edge._1, adjacency(edge._1) filterNot (_.endVertex == edge._2)))
	} else { this }

	def replace(v0: V, v1: V) = {
		def rebuild(start: V, end: V, edge: E) = edge match {
			case e: DefaultEdge[_] => DefaultEdge(start, end)
			case e: JumpEdge[_] => JumpEdge(start, end)
			case e: TrueEdge[_] => TrueEdge(start, end)
			case e: FalseEdge[_] => FalseEdge(start, end)
			case e: DefaultCaseEdge[_] => DefaultCaseEdge(start, end)
			case e: CaseEdge[_] => CaseEdge(start, end)
			case e: ThrowEdge[_] => ThrowEdge(start, end)
			case e: ReturnEdge[_] => ReturnEdge(start, end)
			case _ => error(edge + " is not and Edge")
		}
		
		assert(contains(v0), "Graph must contain v0.")
		assert(!contains(v1), "Graph must not contain v1.")

		val oo = outgoingOf(v0)
		val io = incomingOf(v0)
		var result = this - v0 + v1

		for(e <- oo) result = result + rebuild(v1, e.endVertex, e)
		for(e <- io) result = result + rebuild(e.startVertex, v1, e)

		result
	}

	override def contains(vertex: V) = adjacency contains vertex

	override def contains(edge: E) = (adjacency get edge.startVertex) match {
		case Some(list) => list exists (_ == edge)
		case None => false
	}

	override def incomingOf(vertex: V) = {
		assert(contains(vertex), "Graph must contain vertex.")
		adjacency flatMap (_._2) filter (_.endVertex == vertex)
	}

	override def outgoingOf(vertex: V) = {
		assert(contains(vertex), "Graph must contain vertex.")
		adjacency(vertex)
	}

	override def outdegreeOf(vertex: V) = outgoingOf(vertex).length

	override def indegreeOf(vertex: V) = {
		assert(contains(vertex), "Graph must contain vertex.")
		adjacency flatMap (_._2) count (_.endVertex == vertex)
	}

	override def predecessorsOf(vertex: V) = incomingOf(vertex) map (_.startVertex)

	override def successorsOf(vertex: V) = outgoingOf(vertex) map (_.endVertex)

	override def verticesIterator = adjacency.keysIterator

	override def edgesIterator = adjacency.valuesIterator flatMap (_.iterator)

	override def toString = "[Graph]"
}

protected[immutable] final class EmptyGraph[V] extends Graph[V] {
	override def contains(vertex: V) = false
	override def contains(edge: E) = false
	override def incomingOf(vertex: V) = Nil
	override def outgoingOf(vertex: V) = Nil
	override def outdegreeOf(vertex: V) = 0
	override def indegreeOf(vertex: V) = 0
	override def predecessorsOf(vertex: V) = Nil
	override def successorsOf(vertex: V) = Nil
	override def verticesIterator = Iterator.empty
	override def edgesIterator = Iterator.empty
}