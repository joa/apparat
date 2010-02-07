package apparat.graph

import annotation.tailrec

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
 * Time: 21:58:59
 */
abstract class AbstractBlockSlicer[T](val elms: List[T]) {
	// function that return true if we are starting a block
	def isBeginningOfBlock(elm: T): Boolean

	// function that return true if we are ending a block
	def isEndingOfBlock(elm: T): Boolean

	private var blockBeginning = false

	private def _isBeginningOfBlock(elm: T): Boolean = {
		blockBeginning = isBeginningOfBlock(elm)
		blockBeginning
	}

	private var blockEnding = false

	private def _isEndingOfBlock(elm: T): Boolean = {
		blockEnding = isEndingOfBlock(elm)
		blockEnding
	}

	private def isSameBlock(elm: T): Boolean = {
		!_isBeginningOfBlock(elm) && !_isEndingOfBlock(elm)
	}

	def foreach(body: List[T] => Unit) = {
		implicit def bool2int(b: Boolean): Int = if (b) 1 else 0

		var startIdx = 0

		def nextBlock() = {
			val elm = elms(startIdx)
			val newStartIdx = startIdx + isBeginningOfBlock(elm)
			val endIdx = {
				if (_isEndingOfBlock(elm))
					newStartIdx
				else
					newStartIdx + elms.segmentLength(isSameBlock(_), newStartIdx) + 1 - blockBeginning
			}
			val block = elms.slice(startIdx, endIdx)
			startIdx = endIdx
			block
		}

		@tailrec def loop() {
			if (startIdx < elms.length) {
				body(nextBlock)
				loop()
			}
		}
		loop()
	}
}