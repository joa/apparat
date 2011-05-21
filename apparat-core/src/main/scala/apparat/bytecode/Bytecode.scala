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

import apparat.abc.{Abc, AbcMethod, AbcMethodBody}
import apparat.utils.{Dumpable, IndentingPrintWriter}
import combinator.{Failure, Success, BytecodeChain}
import operations.AbstractOp
import annotation.tailrec
import operations.Nop
import apparat.graph.immutable.BytecodeControlFlowGraphBuilder

object Bytecode {
	def fromMethod(method: AbcMethod)(implicit abc: Abc) = method.body match {
		case Some(body) => fromBody(body)
		case None => None
	}

	def fromBody(body: AbcMethodBody)(implicit abc: Abc) = BytecodeDecoder(body.code, body.exceptions, body)

	def bytecode(body: => List[AbstractOp]) = new Bytecode(body, new MarkerManager(), new Array(0), None)
}

class Bytecode(var ops: List[AbstractOp], val markers: MarkerManager, var exceptions: Array[BytecodeExceptionHandler], var body: Option[AbcMethodBody]) extends Dumpable {
	override def dump(writer: IndentingPrintWriter) = BytecodeDump.`type` match {
		case BytecodeDumpTypeDefault => new BytecodeDump(ops, markers, exceptions, body) dump writer
		case BytecodeDumpTypeCFG => BytecodeControlFlowGraphBuilder(this).dotExport to writer
	}

	def storeIn(body: AbcMethodBody)(implicit abc: Abc) = {
		val (code, exceptions) = BytecodeEncoder(this)

		body.code = code
		body.exceptions = exceptions
	}

	def filterNot(f: AbstractOp => Boolean) = {
		/*@tailrec*/ def loop(list: List[AbstractOp]): List[AbstractOp] = list match {//FIXME make tailrec
			case x :: Nil => f(x) match {
				case true => {
					val nop = Nop()
					markers.forwardMarker(x, nop)
					nop :: Nil
				}
				case false => x :: Nil
			}
			case x :: xs => f(x) match {
				case true => {
					markers.forwardMarker(x, xs.head)
					loop(xs)
				}
				case false => x :: loop(xs)
			}
			case Nil => Nil
		}

		ops = loop(ops)
	}

	def removeAny(op: AbstractOp) = filterNot(_ ~== op)

	def removeAll(f: PartialFunction[AbstractOp, Boolean]) = filterNot(f)

	def remove(op: AbstractOp) = filterNot(_ == op)
	
	def contains[A](chain: BytecodeChain[A]) = indexOf(chain) != -1

	def indexOf[A](chain: BytecodeChain[A]) = {
		@tailrec def loop(list: List[AbstractOp], index: Int): Int = {
			if(list.isEmpty) -1
			else {
				chain(list) match {
					case Success(_, remaining) => index
					case Failure(_) => loop(list drop 1, index + 1)
				}
			}
		}

		loop(ops, 0)
	}

	def rewrite[A <: AbstractOp](rule: BytecodeChain[List[A]]) = replace(rule) { a => a }

	def replace[A](chain: BytecodeChain[A])(body: A => List[AbstractOp]) = replaceFrom(0, chain)(body)

	def replaceFrom[A](fromIndex: Int, chain: BytecodeChain[A])(body: A => List[AbstractOp]) = {
		if(ops.length <= fromIndex) {
			false
		} else {
			var modified = false
			var processed: List[AbstractOp] = Nil
			var unprocessed: List[AbstractOp] = if(0 == fromIndex) { ops } else { ops drop fromIndex }

			while(unprocessed.nonEmpty) {
				chain(unprocessed) match {
					case Success(value, remaining) => {
						val replacement = body(value)

						processed :::= replacement.reverse
						unprocessed = remaining

						if(replacement.isEmpty) {
							if(unprocessed.isEmpty) {
								unprocessed = List(Nop())
							}

							markers.patchMissing(processed ::: unprocessed, exceptions, unprocessed.head)
						} else {
							markers.patchMissing(processed ::: unprocessed, exceptions, replacement.head)
						}

						modified = true
					}
					case Failure(_) => {
						processed ::= unprocessed.head
						unprocessed = unprocessed.tail
					}
				}
			}

			ops = if(0 == fromIndex) { processed.reverse } else { (ops take fromIndex) ::: processed.reverse}
			modified
		}
	}

	def replaceAll[A](chain: BytecodeChain[A])(rule: A => List[AbstractOp]): Boolean = {
		replace(chain)(rule) match {
			case true => replaceAll(chain)(rule) || true
			case false => false
		}
	}

	def replace(tuple: (AbstractOp, AbstractOp)): Unit = replace(tuple._1, tuple._2)

	def replace(existing: AbstractOp, replacement: AbstractOp): Unit = replace(existing, replacement :: Nil)

	def replace(existing: AbstractOp, replacement: List[AbstractOp]): Unit = {
		if(replacement.nonEmpty) {
			val index = ops indexOf existing
			if(-1 != index) {
				markers.forwardMarker(existing, replacement.head)
				ops = ((ops take index) ::: replacement) ::: (ops drop (index + 1))
//				ops = ops map {
//					case e if e == existing => replacement
//					case other => other
//				}
			}
		}
	}

	def toByteArray(implicit abc: Abc) = BytecodeEncoder(this)._1
}