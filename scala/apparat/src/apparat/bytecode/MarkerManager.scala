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
package apparat.bytecode

import collection.mutable.HashMap
import operations.AbstractOp
import collection.immutable.{SortedMap, TreeMap}

class MarkerManager {
	private var numMarkers = 0
	private var markers = HashMap[AbstractOp, Marker]()
	private var unresolved = TreeMap[Int, Marker]()

	def apply(op: AbstractOp) = getMarkerFor(op)

	def hasMarkerFor(op: AbstractOp) = markers get op match {
		case Some(_) => true
		case None => false
	}

	def getMarkerFor(op: AbstractOp) = markers get op

	def mark(op: AbstractOp) = markers get op getOrElse {
		val marker = new Marker(numMarkers)
		marker.op = Some(op)
		markers += op -> marker
		numMarkers += 1
		marker
	}
	
	protected[bytecode] def hasMarkerAt(position: Int) = unresolved get position match {
		case Some(_) => true
		case None => false
	}

	protected[bytecode] def getMarkerAt(position: Int) = unresolved get position

	protected[bytecode] def putMarkerAt(position: Int) = {
		if(hasMarkerAt(position)) {
			getMarkerAt(position).getOrElse(error("Internal error."))
		} else {
			val marker = new Marker(numMarkers)
			marker.position = position
			unresolved = unresolved + (position -> marker)
			numMarkers += 1
			marker
		}
	}

	protected[bytecode] def solve(map: SortedMap[Int, AbstractOp]) = {
		var previous: Option[Int] = None
		for((position, op) <- map) {
			unresolved range (0, position + 1) lastOption match {
				case Some((markerPosition, marker)) => if(!previous.isDefined || markerPosition > previous.get) {
					if(marker.op.isDefined) error("Illegal marker at " + markerPosition + ".")
					marker.op = Some(op)
					markers += op -> marker
				}
				case None => {} 
			}

			previous = Some(position)
		}

		unresolved = TreeMap.empty
		this
	}
}