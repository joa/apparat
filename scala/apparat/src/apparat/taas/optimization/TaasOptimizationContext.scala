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
package apparat.taas.optimization

import apparat.taas.ast.TaasCode
import apparat.utils.{IndentingPrintWriter, Dumpable}
import apparat.taas.graph.TaasGraphLinearizer

/**
 * @author Joa Ebert
 */
case class TaasOptimizationContext(code: TaasCode, modified: Boolean, level: Int, flags: Int) extends Dumpable {
	override def dump(writer: IndentingPrintWriter): Unit = {
		writer <= "TaasOptimizationContext:"
		writer withIndent {
			writer <= "Modified: " + modified
			new TaasGraphLinearizer(code.graph) dump writer
		}
	}
}