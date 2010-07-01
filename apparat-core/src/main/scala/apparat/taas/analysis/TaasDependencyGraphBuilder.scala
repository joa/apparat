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
package apparat.taas.analysis

import apparat.graph.immutable.Graph
import apparat.taas.ast._
import collection.mutable.ListBuffer
import apparat.graph.{DefaultEdge, GraphLike}

/**
 * @author Joa Ebert
 */
object TaasDependencyGraphBuilder extends (TaasAST => GraphLike[TaasNominal]) {
	private class TaasNominalVisitor extends TaasVisitor {
		var list = ListBuffer.empty[TaasNominal]

		override def visit(value: TaasClass) = if(!list.contains(value)) list += value
		override def visit(value: TaasInterface) = if(!list.contains(value)) list += value
	}

	def apply(ast: TaasAST): GraphLike[TaasNominal] = {
		val visitor = new TaasNominalVisitor()
		for(unit <- ast.units) {
			unit match {
				case TaasTarget(packages) => packages foreach { _ accept visitor }
				case _ =>
			}
		}

		var graph: Graph[TaasNominal] = new Graph() ++ visitor.list

		for(nominal <- visitor.list) {
			nominal.base match {
				case Some(base) => base match {
					case t: TaasNominalType => {
						if(graph contains t.nominal) {
							graph += new DefaultEdge(t.nominal, nominal)
						}
					}
					case TaasObjectType =>
					case _ => error("TaasNominalType expected, got "+base+".")
				}
				case None =>
			}
		}
		
		graph
	}
}