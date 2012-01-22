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

import apparat.utils.{IndentingPrintWriter}
import apparat.utils.IO._
import java.io.{File => JFile, FileOutputStream => JFileOutputStream,
	PrintWriter => JPrintWriter, PrintStream => JPrintStream}

final class DOTExport[V](val graph: GraphLike[V],
				   val vertexToString: V => String,
				   val edgeToString: Edge[V] => String)
{
	def to(pathname: String): Unit = to(new JFile(pathname))

	def to(file: JFile): Unit = using(new JFileOutputStream(file)){
		stream => to(new IndentingPrintWriter(new JPrintWriter(stream)))
	}

	def to(stream: JPrintStream): Unit = to(new JPrintWriter(stream))

	def to(writer: JPrintWriter): Unit = {
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
				writer <= "shape=\"record\""
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
				vertex => (vertices indexOf vertex) + " " + vertexToString(vertex) + ";"
			}

			writer.println(graph.edgesIterator) {
				edge => (vertices indexOf edge.startVertex) + " -> " + (vertices indexOf edge.endVertex) + " " + edgeToString(edge) + ";"
			}
		}
		writer <= "}"
	}

	private def escape(value: String) = value.replaceAll("\\\\", "\\\\\\\\").
			replaceAll("\n", "\\\\\\\\n").replaceAll("\"", "\\\\\"")
}
