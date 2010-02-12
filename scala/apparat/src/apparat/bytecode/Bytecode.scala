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

object Bytecode {
	def fromMethod(method: AbcMethod)(implicit abc: Abc) = method.body match {
		case Some(body) => fromBody(body)
		case None => None
	}

	def fromBody(body: AbcMethodBody)(implicit abc: Abc) = BytecodeDecoder(body.code, body.exceptions)

	def bytecode(body: => List[AbstractOp]) = new Bytecode(body, new MarkerManager(), new Array(0))
}

class Bytecode(var ops: List[AbstractOp], val markers: MarkerManager, var exceptions: Array[BytecodeExceptionHandler]) extends Dumpable {
	override def dump(writer: IndentingPrintWriter) = new BytecodeDump(ops, markers, exceptions) dump writer

	def storeIn(body: AbcMethodBody)(implicit abc: Abc) = {
		val (code, exceptions) = BytecodeEncoder(this)

		body.code = code
		body.exceptions = exceptions
	}

	def remove(op: AbstractOp) = ops = ops filterNot (_ ~== op)

	def removeAll(f: PartialFunction[AbstractOp, Boolean]) = {
		while(ops exists (op => f(op))) {
			ops = ops filterNot { op => f(op) }
		}
	}

	def removeStrict(op: AbstractOp) = ops = ops filterNot (_ == op)
	
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

	def replace[A](chain: BytecodeChain[A])(body: A => List[AbstractOp]) = {
		var modified = false
		var processed: List[AbstractOp] = Nil
		var unprocessed: List[AbstractOp] = ops

		while(unprocessed.nonEmpty) {
			chain(unprocessed) match {
				case Success(value, remaining) => {
					processed :::= body(value).reverse
					unprocessed = remaining
					modified = true
				}
				case Failure(_) => {
					processed ::= unprocessed.head
					unprocessed = unprocessed.tail
				}
			}
		}

		ops = processed.reverse
		modified
	}

	def replaceAll[A](chain: BytecodeChain[A])(rule: A => List[AbstractOp]): Boolean = {
		replace(chain)(rule) match {
			case true => replaceAll(chain)(rule) || true
			case false => false
		}
	}

	def toByteArray(implicit abc: Abc) = BytecodeEncoder(this)._1
}