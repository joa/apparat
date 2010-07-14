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
package apparat.tools.reducer

import apparat.utils.IO
import apparat.swf.{SwfTags, DefineBinaryData, Swf}
import apparat.log.SimpleLog

class MatryoshkaInjector(bytes: Array[Byte]) extends SimpleLog {
	lazy val swf = {
		var resource = getClass.getResource("/apparat-matryoshka-quiet.swf")
		var length = 0L

		IO.using(resource.openStream) {
			stream => {
				var buffer = new Array[Byte](0x1000)
				var bytesRead = 0

				bytesRead = stream.read(buffer, 0, 0x1000)

				while(-1 != bytesRead) {
					length += bytesRead
					bytesRead = stream.read(buffer, 0, 0x1000)
				}
			}
		}

		log.debug("Matryoshka size is %dbytes.", length)

		//
		// Now read the SWF since we know the length.
		//

		IO.using(resource.openStream) {
			stream => Swf.fromInputStream(stream, length)
		}
	}

	def toByteArray = {
		swf.tags find { _.kind == SwfTags.DefineBinaryData } match {
			case Some(x: DefineBinaryData) => x.data = bytes
			case _ => error("Could not find DefineBinaryData tag.")
		}

		swf.toByteArray
	}
}