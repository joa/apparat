package apparat.graph

import apparat.bytecode.operations.{Label, AbstractOp}
import apparat.bytecode.{MarkerManager, Bytecode}
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
 * Date: 10 janv. 2010
 * Time: 22:40:18
 */

class AbstractOpBasicBlockSlicer(elms: Seq[AbstractOp], val markers: Option[MarkerManager] = None) extends AbstractBlockSlicer[AbstractOp](elms) {
	def this(bytecode: Bytecode) = this (bytecode.ops, Some(bytecode.markers))

	def isBeginningOfBlock(elm: AbstractOp) = {
		elm.isInstanceOf[Label] || {markers map {_ hasMarkerFor elm} getOrElse false}
	}

	def isEndingOfBlock(elm: AbstractOp) = elm.controlsFlow
}

object AbstractOpBasicBlockSlicer {
	def apply(seq: Seq[AbstractOp]) = {
		new AbstractOpBasicBlockSlicer(seq)
	}

	def apply(seq: Seq[AbstractOp], markers: MarkerManager) = {
		new AbstractOpBasicBlockSlicer(seq, Some(markers))
	}

	def apply(bytecode: Bytecode) = {
		new AbstractOpBasicBlockSlicer(bytecode)
	}
}