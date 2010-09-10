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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.pbj.optimization

import apparat.pbj.Pbj
import annotation.tailrec
import apparat.pbj.pbjdata._

/**
 * The PbjOptimizer class is a 1-pass optimizer for Pbj code.
 *
 * It performs copy-propagation and dead-code-elimination in a single step.
 *
 * @author Joa Ebert
 */
object PbjOptimizer extends (Pbj => Unit) {
	//
	// PbjOptimizer is far more complex and annoying than it.
	// might look since one has to take all the swizzle stuff
	// into account when checking for def-use chains.
	//
	// For instance, the current implementation would create
	// a false positive for dead code in this example:
	//
	// i0.x = 1.0;
	// i0.yz = i1.xy * i1.xy;
	//
	// The first assignment marks "i0.x = 1.0;" as possible dead statement
	// and "i0" as being defined. The second assignment will define "i0"
	// again so the preceding statement is marked as dead. This is incorrect
	// however, since we can imagine the register is actually "index * 4 + channel"
	// and in that case "i0.x" defines "0" and "i0.yz" defines "1" and "2".
	//
	// This means we will have to iterate through all the channels for every
	// def-use case and calculate the index correct based on the swizzle.
	// A task that is not hard to do but painful to implement :/
	//

	private val MAX_ITERATIONS = 32

	override def apply(pbj: Pbj): Unit = {
		error("TODO")
		@tailrec def loop(i: Int = 0): Unit = i match {
			case x if x >= MAX_ITERATIONS =>
			case y => if(optimize(pbj)) loop(y+10000)
		}

		loop()
	}

	/**
	 * Optimizes a given Pbj file.
	 *
	 * @return <code>true</code> if any modification happened; <code>false</code> otherwise.
	 */
	def optimize(pbj: Pbj): Boolean = {
		error("TODO")
		@tailrec def splitIntoBlocks(result: List[List[POp]],
																 in: List[POp]): List[List[POp]] = {
			//
			// Split the code into basic blocks.
			//
			// b0
			// If
			// b1
			// Else
			// b2
			// Endif
			// b3
			//
			// b0 will include the If, b1 will include the Else and b2 will include
			// the Endif statement. b3 will be the last block.
			//

			val (block, rest) = in splitAt {
				(in indexWhere {
					op => op.opCode == POp.If || op.opCode == POp.Else || op.opCode == POp.Endif
				}) + 1
			}

			if(block != Nil) {
				splitIntoBlocks(block :: result, rest)
			} else {
				(rest :: result).reverse
			}
		}

		val code = pbj.code
		val (newCode, transformed) = if((code exists { _.opCode == POp.If }) &&
			code.last.opCode != POp.If) {
			val optimizedBlocks = splitIntoBlocks(List.empty, code) map optimizeBlock(pbj, false)
			(optimizedBlocks flatMap { _._1 }) -> (optimizedBlocks exists { _._2 })
		} else {
			optimizeBlock(pbj, true)(code)
		}

		/*
		val dead = ((pbj.parameters map { _._1.register.code }) ::: (pbj.code collect {
			case l: PLogical => 0x8000
			case d: PDst => d.dst.code
		})).distinct diff ((pbj.parameters map { _._1.register.code }) ::: (pbj.code flatMap {
			case PCopy(_, src) => src.code :: Nil
			case d: PDstAndSrc => d.dst.code :: d.src.code :: Nil
			case s: PSrc => s.src.code :: Nil
			case _ => Nil
		})).distinct

		if(log.debugEnabled) {
			log.debug("PBJ globally dead:")
			for(d <- dead) log.debug("  %s", d)
		}


		pbj.code = pbj.code filterNot {
			case l: PLogical => dead contains 0x8000
			case d: PDst => dead exists { _ == d.dst.code }
			case _ => false
		}
		 */
		pbj.code = newCode
		transformed
	}

	def optimizeBlock(pbj: Pbj, killCode: Boolean)(block: List[POp]): (List[POp], Boolean )= {
		var deadCandidates = List.empty[POp]
		var copyCandidates = List.empty[PDst]
		var dead = List.empty[POp]
		var modified = false

		def updateDead(op: POp): POp = op match {
			case op: PBinop =>
				if(killCode) {
					deadCandidates = deadCandidates filterNot {
						x => (x defines op.src) || (x defines op.dst) }
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PUnop =>
				if(killCode) {
					deadCandidates = deadCandidates filterNot { _ defines op.src }
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PArity1 =>
				if(killCode) {
					deadCandidates = deadCandidates filterNot { _ defines op.src }
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PArity2 =>
				if(killCode) {
					deadCandidates = deadCandidates filterNot {
						x => (x defines op.src) || (x defines op.dst) }
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PLogical =>
				if(killCode) {
					deadCandidates = deadCandidates filterNot {
						x => (x defines op.src) || (x defines op.dst) }
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PDstAndSrc =>
				if(killCode) {
					deadCandidates = deadCandidates filterNot {
						x => (x defines op.src) || (x defines op.dst) }
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PDst =>
				if(killCode) {
					dead = (deadCandidates filter { _ defines op.dst }) ::: dead
					deadCandidates = op :: deadCandidates
				}
				op
			case op: PSrc =>
				if(killCode) {
					deadCandidates = op :: deadCandidates filterNot { _ defines op.src }
				}
				op
			case op => op
		}

		def updateCopy(op: POp, tail: List[POp]): POp = op match {
			case op: PLogical =>
				copyCandidates = if(1 == (tail count { _ uses 0x8000 })) {//<- optimize
					op :: (copyCandidates filterNot { _ uses 0x8000 })
				} else {
					(copyCandidates filterNot { _ uses 0x8000 })
				}
				op
			case op: PDst =>
				val dst = op.dst
				copyCandidates = if(1 == (tail count { _ uses dst })) {//<- optimize
					op :: (copyCandidates filterNot { _ uses dst })
				} else {
					(copyCandidates filterNot { _ uses dst })
				}
				op
			case op => op
		}

		def transform(op: POp, tail: List[POp]): POp = op match {
			case PCopy(dst, src) =>
				updateCopy(updateDead(copyCandidates find { _ defines src } match {
					case Some(x) if x.dst.swizzle == dst.swizzle =>
						modified = true
						x mapDef dst.code
					case _ => op
				}), tail)
			//case PIf(src) => ...
			case op => updateCopy(updateDead(op), tail)
		}

		@tailrec def loop(value: List[POp],
											result: List[POp] = Nil): List[POp] = value match {
			case Nil => result.reverse
			case x :: xs => {
				val a = transform(x, xs)
				loop(xs, a :: result)
			}
		}

		val list = loop(block)

		if(killCode && deadCandidates.length > 0) {
			dead = deadCandidates ::: dead
		}

		if(modified) {
			if(killCode) {
				(list filterNot { dead contains _ }, true)
			} else {
				(list, true)
			}
		} else {
			if(killCode && dead.length > 0) {
				(list filterNot { dead contains _ }, true)
			} else {
				(list, false)
			}
		}
	}
}