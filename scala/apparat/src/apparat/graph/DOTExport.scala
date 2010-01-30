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
package apparat.graph

import apparat.utils.{IndentingPrintWriter}
import apparat.utils.IO._
import apparat.graph.mutable._
import java.io._

class DOTExport[V](val graph: MutableGraphLike[V],
								 val vertexToString: V => String,
								 val edgeToString: Edge[V] => String)
{
	def to(pathname: String): Unit = to(new File(pathname))

	def to(file: File): Unit = using(new FileOutputStream(file)){
		stream => to(new IndentingPrintWriter(new PrintWriter(stream)))
	}

	def to(stream: PrintStream): Unit = to(new PrintWriter(stream))

	def to(writer: PrintWriter): Unit = {
		val indentingPrintWriter = new IndentingPrintWriter(writer)
		to(indentingPrintWriter)
		indentingPrintWriter.flush()
	}

	def to(writer: IndentingPrintWriter): Unit = {
		val fontName = "Bitstream Vera Sans Mono"
		val fontSize = 8
		val vertices = graph.verticesIterator.toList
		
		writer <= "digraph G {"
		writer withIndent {
			writer <= "fontname=\"" + fontName + "\";"
			writer <= "fontsize=" + fontSize + ";"
			writer <= "ratio=auto;"

			writer <= "graph["
			writer withIndent {
				writer <= "rankdir=\"TB\","
				writer <= "splines=true,"
				writer <= "overlap=false"
			}
			writer <= "];"

			writer <= "node["
			writer withIndent {
				writer <= "fontname=\"" + fontName + "\", "
				writer <= "fontsize=" + fontSize + ","
				writer <= "shape=\"box\""
			}
			writer <= "];"

			writer <= "edge["
			writer withIndent {
				writer <= "fontname=\"" + fontName + "\", "
				writer <= "fontsize=" + fontSize + ","
				writer <= "arrowhead=\"empty\""
			}
			writer <= "];"

			writer.println(vertices) {
				vertex => (vertices indexOf vertex) + " [label=\"" + vertexToString(vertex) + "\"];"
			}

			writer.println(graph.edgesIterator) {
				edge => (vertices indexOf edge.startVertex) + " -> " + (vertices indexOf edge.endVertex) + " [label=\"" + edgeToString(edge) + "\"];"
			}
		}
		writer <= "}"
	}
}