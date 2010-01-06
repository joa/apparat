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

import operations.AbstractOp
import apparat.utils.{Dumpable, IndentingPrintWriter}

class BytecodeDump(val ops: Seq[AbstractOp], val markers: MarkerManager, val exceptions: Array[BytecodeExceptionHandler]) extends Dumpable
{
	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Bytecode:"
		writer withIndent {
			writer <= exceptions.length + " exception(s):"
			writer <<< exceptions

			writer <= ops.length + " operation(s):"
			writer withIndent {
				writer.println(ops)(op => {
					val opString = op.toString
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
				})
			}
		}
	}
}