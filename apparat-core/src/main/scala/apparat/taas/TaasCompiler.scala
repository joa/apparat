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
package apparat.taas

import ast._
import backend.TaasBackend
import frontend.TaasFrontend
import optimization._

/**
 * @author Joa Ebert
 */
class TaasCompiler(val frontend: TaasFrontend, val backend: TaasBackend) {
	def compile() = backend emit transform(frontend.getAST)

	def transform(ast: TaasAST) = {
		def loop(tree: TaasTree): Unit = tree match {
			case method: TaasMethod => optimizer optimize method
			case parent: TaasParent => parent.children foreach loop
			case _ =>
		}
		ast.units collect { case target: TaasTarget => target } foreach loop
		ast
	}

	val optimizer = new TaasOptimizer(
		CopyPropagation :: ConstantFolding :: DeadCodeElimination :: StrengthReduction :: Nil,
		0
	)
}