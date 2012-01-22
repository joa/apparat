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
package apparat.taas.optimization

import apparat.taas.ast._
import apparat.taas.graph.{TaasBlock, LivenessAnalysis, TaasGraph}

/**
 * @author Joa Ebert
 */
object DeadCodeElimination extends TaasOptimization {
	def name = "DeadCodeElimination"

	def optimize(context: TaasOptimizationContext) = apply(context.
			code.method map { _.parameters.length } getOrElse -1,
			context.code.graph) match {
		case true => context.copy(modified = true)
		case false => context
	}

	def apply(graph: TaasGraph): Boolean = apply(-1, graph)

	def apply(numParameters: Int, graph: TaasGraph): Boolean = {
		var modified: Boolean = false

		for(vertex <- graph.verticesIterator) {
			val (m, r) = removeDeadDefs(vertex.block)
			vertex.block = r
			modified |= m
		}

		if(numParameters >= 0) {
			val liveness = new LivenessAnalysis(numParameters, graph)

			for(vertex <- graph.verticesIterator) {
				val (m, r) = removeDeadDefsInFlow(vertex, liveness)
				vertex.block = r
				modified |= m
			}
		}

		//val (m, r) = removeDeadVerts(graph)
		//(m || modified, r)

		modified
	}

	/*error: type mismatch;
	found   : apparat.graph.ControlFlowGraph[apparat.taas.ast.TExpr,apparat.taas.graph.TaasBlock]
	required: apparat.taas.graph.TaasGraph
	result -= vertex*/
	/*private def removeDeadVerts(graph: TaasGraph) = {
		var visited = graph vertexMap { vertex => false }
		var S = List(graph.entryVertex)
		var modified = false

		while (S.nonEmpty) {
			val v = S.head
			S = S.tail
			if (!visited(v)) {
				visited = visited updated (v, true)
				S = (graph successorsOf v filterNot { visited(_) }).toList ::: S
			}
		}

		var result = graph

		for((vertex, alive) <- visited.elements if !alive && !graph.isEntry(vertex)) {
			modified = true
			result -= vertex
		}

		(modified, result)
	}*/

	private def removeDeadDefs(block: List[TExpr]) = {
		var r = List.empty[TDef]
		var h = List.empty[TDef]

		for(op <- block) {
			op match {
				case t2 @ T2(TOp_Nothing, a: TReg, b) if a.index == b.index => r = t2 :: r
				case t2 @ T2(TCoerce(t), a, b) if TaasType.isEqual(a.`type`, t) => r = t2 :: r
				case t2 @ T2(TConvert(t), a, b) if TaasType.isEqual(a.`type`, t) => r = t2 :: r
				case d: TDef => {
					val p = h partition { h => h.register == d.register && !(d uses h.register) }
					r = p._1 ::: r
					if(!d.hasSideEffect) {
						h = d :: (p._2 filterNot { d uses _.register })
					} else {
						h = p._2 filterNot { d uses _.register }
					}
				}
				case o => h = h filterNot { o uses _.register }
			}
		}

		if(r.nonEmpty) {
			(true, block filterNot { r contains _ })
		} else {
			(false, block)
		}
	}

	private def removeDeadDefsInFlow(vertex: TaasBlock, liveness: LivenessAnalysis) = {
		val block = vertex.block
		var r = List.empty[TDef]

		for(op <- block if !op.hasSideEffect) op match {
			case d: TDef => {
				if(!(block exists { _ uses d.register }) && !(liveness liveOut vertex contains d.register)) {
					r = d :: r
				}
			}
			case o =>
		}

		if(r.nonEmpty) {
			(true, block filterNot { r contains _ })
		} else {
			(false, block)
		}
	}
}
