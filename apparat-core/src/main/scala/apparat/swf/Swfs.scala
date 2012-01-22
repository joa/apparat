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
package apparat.swf

import apparat.abc.Abc

/**
 * @author Joa Ebert
 */
object Swfs {
	def doABC(documentClass: String)(f: => Abc) = {
		val abc = new DoABC()
		val symbolClass = new SymbolClass()

		abc.flags = 1L
		abc.name = "frame1"
		f.write(abc)

		symbolClass.symbols = Array(0 -> documentClass)
		symbolClass

		abc :: symbolClass :: Nil
	}

	def swf(width: Int = 400, height: Int = 400, backgroundColor: Int = 0x333333, frameRate: Float = 255.0f)(f: => List[SwfTag]) = {
		val result = new Swf()
		val fileAttributes = new FileAttributes
		val scriptLimits = new ScriptLimits()
		val setBackgroundColor = new SetBackgroundColor()

		fileAttributes.useDirectBlit = false
		fileAttributes.useGPU = false
		fileAttributes.hasMetadata = true
		fileAttributes.actionScript3 = true
		fileAttributes.useNetwork = true

		scriptLimits.maxRecursionDepth = 1000
		scriptLimits.scriptTimeoutSeconds = 60

		setBackgroundColor.color = new RGB(
			(backgroundColor & 0xff0000) >> 16,
			(backgroundColor & 0xff00) >> 8,
			 backgroundColor & 0xff)

		result.compressed = true
		result.version = 10
		result.frameSize = new Rect(0, width * 20, 0, height * 20)
		result.frameRate = frameRate
		result.frameCount = 1
		result.tags = (fileAttributes :: scriptLimits ::
				setBackgroundColor :: Nil) ::: f ::: (new ShowFrame() :: new End() :: Nil)

		result
	}
}
