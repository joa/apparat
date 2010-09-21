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
package jitb.util

import org.lwjgl.opengl.{GL11, EXTTextureRectangle, GLContext}

/**
 * @author Joa Ebert
 */
object TextureUtil {
	lazy val useTextureRectangle = {
		val capabilities = GLContext.getCapabilities
		capabilities.GL_EXT_texture_rectangle || capabilities.GL_ARB_texture_rectangle
	}

	lazy val mode = if(useTextureRectangle) EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT else GL11.GL_TEXTURE_2D
}