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

import apparat.taas.frontend.TaasFrontend
import apparat.taas.ast._
import collection.mutable.ListBuffer
import apparat.abc._

/**
 * @author Joa Ebert
 */
class AbcFrontend(main: Abc, libraries: List[Abc]) extends TaasFrontend {
	private val ast: TaasAST = TaasAST(ListBuffer.empty)
	
	override lazy val getAST = {
		main.loadBytecode()
		libraries foreach { _.loadBytecode() }
		
		val target = TaasTarget(ListBuffer.empty)
		val lib = TaasLibrary(ListBuffer.empty)
		ast.children append target
		ast.children append lib

		libraries foreach parse(lib) _
		parse(target)(main)

		ast.init()
	}

	private def parse(unit: TaasUnit)(abc: Abc) = new AbcParser(ast, abc, unit).parseAbc()
}