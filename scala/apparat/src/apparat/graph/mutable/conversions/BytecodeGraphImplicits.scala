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
 * User: Patrick Le Clec'h
 * Date: 11 janv. 2010
 * Time: 22:50:14
 */
package apparat.graph.mutable.conversions

import apparat.bytecode.operations.AbstractOp
import apparat.graph.mutable.MutableAbstractOpBlockVertex

object BytecodeGraphImplicits {
	implicit def aopSeq2MutableBV(list: List[AbstractOp]): MutableAbstractOpBlockVertex = new MutableAbstractOpBlockVertex(list)
}