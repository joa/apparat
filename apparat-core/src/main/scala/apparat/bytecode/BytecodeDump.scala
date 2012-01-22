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
package apparat.bytecode

import apparat.abc.AbcMethodBody
import apparat.utils.{Dumpable, IndentingPrintWriter}
import operations.AbstractOp

object BytecodeDump {
	var `type`: BytecodeDumpType = BytecodeDumpTypeDefault
}

class BytecodeDump(val ops: Seq[AbstractOp], val markers: MarkerManager, val exceptions: Array[BytecodeExceptionHandler], val body: Option[AbcMethodBody]) extends Dumpable {
	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Bytecode:"
		writer withIndent {
			body match {
				case Some(body) => {
					writer <= "operandStack: "+body.maxStack
					writer <= "scopeStack:   "+(body.maxScopeDepth - body.initScopeDepth)
					writer <= "localCount:   "+body.localCount
				}
				case None => writer <= "(unknown body)"
			}
			writer <= exceptions.length + " exception(s):"
			writer <<< exceptions

			writer <= ops.length + " operation(s):"
			writer withIndent {
				writer.println(ops) {
					op => {
						val opString = "+"+op.pushOperands+"|-"+op.popOperands+"  "+op.toString
						val builder = new StringBuilder(opString.length + 6)

						markers(op) match {
							case Some(marker) => {
								if(exceptions exists (_.from == marker)) {
									writer <= "try {"
									writer ++ 1
								} else if(exceptions exists (_.to == marker)) {
									val exceptionEnds = exceptions filter (_.to == marker)
									writer -- 1
									writer <= "} catch {"
									writer withIndent {
										for(exception <- exceptionEnds) {
											writer <= "(" + exception.varName + ", " + exception.typeName + ") => " + exception.target
										}
									}
									writer <= "}"
								}

								builder append marker.toString
								builder append ':'
							}
							case None => {}
						}

						(8 - builder.length) match {
							case x if x > 0 => builder append new String(Array.fill(x)(' '))
							case _ => {}
						}

						builder append opString
						builder.toString
					}
				}
			}
		}
	}
}
