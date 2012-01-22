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
package apparat.pbj.optimization

import apparat.pbj.Pbj
import apparat.pbj.pbjdata.POp
import annotation.tailrec
import collection.immutable.SortedSet

/**
 * The PbjLoopDetection is a tiling loop detection which searches for the
 * longest reoccurring sequence of bytecodes in a PBJ file.
 *
 * @param tileSize The maximum tile size to search for.
 * @author Joa Ebert
 */
class PbjLoopDetection(tileSize: Int) {
	def apply(code: List[POp]) = {
		@tailrec def loop(map: Map[List[POp], SortedSet[Int]], i: Int = 0): Map[List[POp], SortedSet[Int]] = i match {
			case x if x < tileSize => loop(expandTiles(map), i + 1)
			case y => map
		}

		calculateRanges(loop(listOccurrences(code) map { x => (x._1 :: Nil) -> x._2}) filterNot { _._1.length < 4 })
	}

	def listOccurrences(code: List[POp]) = {
		@tailrec def loop(code: List[POp], index: Int,
											map: Map[POp, SortedSet[Int]]): Map[POp, SortedSet[Int]] = code match {
			case Nil => map filterNot { _._2.size == 1 } map { x => (x._1, x._2)}
			case x :: xs =>
				loop(xs, index + 1, map find { _._1 ~== x } match {
					case Some(y) => map.updated(y._1, y._2 + index)
					case None => map.updated(x, SortedSet(index))
				})
		}

		loop(code, 0, Map.empty)
	}

	def expandTiles(tiles: Map[List[POp], SortedSet[Int]]): Map[List[POp], SortedSet[Int]] = {
		var available = tiles
		var result = Map.empty[List[POp], SortedSet[Int]]
		var blocked = Set.empty[List[POp]]

		for((key,scala_bug) <- available) {
			if(available(key).nonEmpty) {
				for(index <- available(key)) {
					val oo = try { available find { x => x._2.contains(index + key.length) && (!(x._2 contains (index - x._1.length))) } } catch { case _: NoSuchElementException => None }
					oo match {//favor the smaller approach
						case Some(x) =>
							if(!blocked.contains(x._1) && x._1 != key) {
								val l = key ::: x._1
								val i = (index + key.length)
								blocked = blocked + key
								result = result.updated(l, result.getOrElse(l, SortedSet.empty[Int]) + index)
									.updated(x._1, result.getOrElse(x._1, SortedSet.empty[Int]) filterNot { _ == i })
								available = available.updated(x._1, x._2 filterNot { _ == i })
							} else {
								result = result.updated(key, result.getOrElse(key, SortedSet.empty[Int]) + index)
							}
						case None =>
							if(available(key) contains index) {
								result = result.updated(key, result.getOrElse(key, SortedSet.empty[Int]) + index)
							}
					}
				}
			}
		}

		result filterNot { _._2.size == 1 }
	}

	def calculateRanges(tiles: Map[List[POp], SortedSet[Int]]): Map[List[POp], List[(Int, Int)]] = {
		tiles map {
			x =>
				val tile = x._1
				val set = x._2.toList
				val length = tile.length

				var start = set(0)
				var end = start
				var i = 1
				var n = set.size
				var result = List.empty[(Int, Int)]

				while(i < n) {
					if((end + length) == set(i)) {
						end = end + length
					} else {
						result = (start -> end) :: result
						start = set(i)
						end = start
					}
					i += 1
				}

				result = (start -> end) :: result

				tile -> result.reverse
		}
	}
}
