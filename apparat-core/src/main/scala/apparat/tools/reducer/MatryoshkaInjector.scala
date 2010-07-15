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
import apparat.log.SimpleLog
import apparat.swf._

class MatryoshkaInjector(source: Swf, matryoshkaType: Int) extends SimpleLog {
	lazy val swf = {
		var resource = getClass.getResource("/apparat-matryoshka-"+
				(if(MatryoshkaType.QUIET == matryoshkaType) "quiet" else "preloader")+".swf")
		var length = 0L

		if(null == resource) {
			log.error("Could not read SWF resource.")
			new Swf
		} else {
			//
			// Read the length of the SWF resource
			//

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
	}

	private lazy val bytes = source.toLZMAByteArray

	def toByteArray = {
		swf.tags find { _.kind == SwfTags.DefineBinaryData } match {
			case Some(x: DefineBinaryData) => x.data = bytes
			case _ => error("Could not find DefineBinaryData tag.")
		}

		swf.tags find { _.kind == SwfTags.SetBackgroundColor } match {
			case Some(x: SetBackgroundColor) => source.tags find { _.kind == SwfTags.SetBackgroundColor } match {
				case Some(y: SetBackgroundColor) => x.color = y.color
				case _ =>
			}
			case _ => error("Could not find SetBackgroundColor tag.")
		}

		swf.tags find { _.kind == SwfTags.ScriptLimits } match {
			case Some(x: ScriptLimits) => source.tags find { _.kind == SwfTags.ScriptLimits } match {
				case Some(y: ScriptLimits) =>
					x.maxRecursionDepth = y.maxRecursionDepth
					x.scriptTimeoutSeconds = y.scriptTimeoutSeconds
				case _ =>
			}
			case _ => error("Could not find ScriptLimits tag.")
		}

		swf.tags find { _.kind == SwfTags.FileAttributes } match {
			case Some(x: FileAttributes) => source.tags find { _.kind == SwfTags.FileAttributes } match {
				case Some(y: FileAttributes) =>
					x.useDirectBlit = y.useDirectBlit
					x.useNetwork = y.useNetwork
				case _ =>
			}
			case _ => error("Could not find FileAttributes tag.")
		}

		swf.frameRate = source.frameRate
		swf.frameSize = source.frameSize
		swf.toByteArray
	}
}