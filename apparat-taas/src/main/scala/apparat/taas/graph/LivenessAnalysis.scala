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
package apparat.taas.graph

import scala.collection.SortedSet
import apparat.taas.ast.{TaasCode, TaasMethod, TDef}

/**
 * @author Joa Ebert
 */
class LivenessAnalysis(numParameters: Int, graph: TaasGraph) {
	require(numParameters >= 0)

	def liveIn(vertex: TaasBlock) = inSet(vertex)
	def liveOut(vertex: TaasBlock) = outSet(vertex)

	private lazy val maxRegister = {
		var result = 1

		for(vertex <- graph.verticesIterator; op <- vertex.block) op match {
			case tdef: TDef if (tdef.register + 1) > result => result = tdef.register + 1
			case _ =>
		}

		result
	}

	private var inSet = Map.empty[TaasBlock, SortedSet[Int]]
	private var outSet = Map.empty[TaasBlock, SortedSet[Int]]
	private def solve() = {
		inSet += graph.exitVertex -> SortedSet.empty
		outSet += graph.exitVertex -> SortedSet.empty

		val blocks = ((graph dft graph.entryVertex toList) filterNot { _ == graph.exitVertex }).reverse

		blocks foreach { inSet += _ -> SortedSet.empty }

		var changed = false

		do {
			changed = false

			for(block <- blocks) {
				var theOutSet = (outSet get block) match {
					case Some(set) => set
					case None => {
						changed = true
						null
					}
				}

				val newOutSet = SortedSet((graph successorsOf block flatMap inSet).toSeq:_*)

				if(null == theOutSet || newOutSet != theOutSet) {
					changed = true
					outSet += block -> newOutSet
					theOutSet = newOutSet
				}

				val defSet = `def`(block)
				val useSet = use(block) ++ (theOutSet filterNot defSet.contains)
				val theInSet = inSet(block)

				if(useSet.size != theInSet.size) {
					changed = true
				} else {
					changed |= useSet != theInSet
				}

				inSet += block -> useSet
			}
		} while(changed)
	}

	private def use(vertex: TaasBlock) = {
		var result = SortedSet.empty[Int]
		var i = 0
		val n = maxRegister

		def loop(): Unit = for(op <- vertex.block) {
			if(op uses i) {
				result += i
				return
			} else if(op defines i) {
				return
			}
		}

		while(i < n) {
			loop()
			i += 1
		}

		result
	}

	private def `def`(vertex: TaasBlock) = {
		if(graph isEntry vertex) {
			SortedSet((0 to numParameters):_*)
		} else {
			var result = SortedSet.empty[Int]
			var i = 0
			val n = maxRegister

			def loop(): Unit = for(op <- vertex.block) {
				if(op uses i) {
					return
				} else if(op defines i) {
					result += i
					return
				}
			}

			while(i < n) {
				loop()
				i += 1
			}

			result
		}
	}

	solve()
}
