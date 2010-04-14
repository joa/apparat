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
package apparat.taas.frontend.abc

import scala.actors.Futures._
import apparat.abc.{Abc, AbcMethod}
import apparat.graph.immutable.{Graph, BytecodeControlFlowGraphBuilder}
import apparat.taas.ast._
/**
 * @author Joa Ebert
 */
protected[abc] class AbcCode(ast: TaasTree, abc: Abc, method: AbcMethod) extends TaasCode {
	lazy val graph = {
		val f = future { computeGraph() }
		f()
	}
	
	private def computeGraph() = {
		require(method.body.isDefined, "MethodBody has to be defined.")
		require(method.body.get.bytecode.isDefined, "Bytecode has to be defined.")

		val body = method.body.get
		val bytecode = body.bytecode.get
		val g = BytecodeControlFlowGraphBuilder(bytecode)
		
		g.dump()
		
		new Graph[TaasElement]()
	}

	override def toString = "AbcCode"
}