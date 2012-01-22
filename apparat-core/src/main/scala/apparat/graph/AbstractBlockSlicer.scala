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
package apparat.graph

import annotation.tailrec

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
			val endIdx= {
				if (_isEndingOfBlock(elm))
					startIdx + 1
				else {
					val newStartIdx = startIdx + isBeginningOfBlock(elm)
					newStartIdx + elms.segmentLength(isSameBlock(_), newStartIdx) + 1 - blockBeginning
				}
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
