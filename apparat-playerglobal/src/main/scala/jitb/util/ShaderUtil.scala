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

import apparat.pbj.Pbj
import flash.utils.ByteArray
import apparat.pbj.pbjdata._
import apparat.pbj.optimization.PbjOptimizer
import flash.display.{ShaderInput, ShaderParameter}
import org.lwjgl.opengl.GLContext
import util.parsing.combinator.syntactical.StandardTokenParsers

/**
 * @author Joa Ebert
 */
object ShaderUtil {
	object GLSLParameterParser extends StandardTokenParsers {
		lexical.delimiters ++= List("+","-","*","/","(",")", ";", ".", "=", "{", "}")

		def apply(value: String): Array[ShaderParameter] = {
			import lexical.Identifier

			var tokens = new lexical.Scanner(value)
			var i = 0
			var t = ""
			var params = List.empty[ShaderParameter]

			while(!tokens.atEnd) {
				tokens.first match {
					case Identifier("uniform") if i == 0 => i = 1
					case Identifier("float") if i == 1 => t = "float"; i = 2
					case Identifier("vec2") if i == 1 => t = "float2"; i = 2
					case Identifier("vec3") if i == 1 => t = "float3"; i = 2
					case Identifier("vec4") if i == 1 => t = "float4"; i = 2
					case Identifier("mat2") if i == 1 => t = "float2x2"; i = 2
					case Identifier("mat3") if i == 1 => t = "float3x3"; i = 2
					case Identifier("mat4") if i == 1 => t = "float4x4"; i = 2
					case Identifier("int") if i == 1 => t = "int"; i = 2
					case Identifier("ivec2") if i == 1 => t = "int2"; i = 2
					case Identifier("ivec3") if i == 1 => t = "int3"; i = 2
					case Identifier("ivec4") if i == 1 => t = "int4"; i = 2
					case Identifier("bool") if i == 1 => t = "bool"; i = 2
					case Identifier("bvec2") if i == 1 => t = "bool2"; i = 2
					case Identifier("bvec3") if i == 1 => t = "bool3"; i = 2
					case Identifier("bvec4") if i == 1 => t = "bool4"; i = 2
					case Identifier(name) if i == 2 =>
						i = 0
						params = ShaderParameter.JITB$create(name, null, null, null, null, t, -1) :: params
					case _ => i = 0
				}

				tokens = tokens.rest
			}

			params.toArray
		}
	}

	object GLSLTextureParser extends StandardTokenParsers {
		lexical.delimiters ++= List("+","-","*","/","(",")", ";", ".", "=", "{", "}")

		def apply(value: String): Array[ShaderInput] = {
			import lexical.Identifier

			var tokens = new lexical.Scanner(value)
			var i = 0
			var n = 0
			var inputs = List.empty[ShaderInput]

			while(!tokens.atEnd) {
				tokens.first match {
					case Identifier("uniform") if i == 0 => i = 1
					case Identifier("sampler2D") if i == 1 => i = 2
					case Identifier("sampler2DRect") if i == 1 => i = 2
					case Identifier(name) if i == 2 =>
						i = 0
						inputs = ShaderInput.JITB$create(name, 4, n) :: inputs
						n = n + 1
					case _ => i = 0
				}

				tokens = tokens.rest
			}

			inputs.toArray
		}
	}
	
	lazy val shaderSupport = GLContext.getCapabilities.GL_ARB_shader_objects

	def getPbj(byteCode: ByteArray): apparat.pbj.Pbj = {
		val pbj = Pbj fromByteArray byteCode.JITB$toByteArray()
		//PbjOptimizer(pbj)
		pbj
	}

	def getGLSLParameters(shader: String): Array[ShaderParameter] = GLSLParameterParser(shader)
	def getGLSLTextures(shader: String): Array[ShaderInput] = GLSLTextureParser(shader)
	
	def getShaderParameters(pbj: apparat.pbj.Pbj): Array[ShaderParameter] = {
		val result = new Array[ShaderParameter](pbj.parameters.length)
		var i = 0

		for((parameter, metadata) <- pbj.parameters) {
			val name = parameter.name
			val description = metadata find { _.key == "description" } map { _.value } map asJava getOrElse null.asInstanceOf[String]
			val minValue = metadata find { _.key == "minValue" } map { _.value } map asFlash getOrElse null.asInstanceOf[jitb.lang.Array]
			val maxValue = metadata find { _.key == "minValue" } map { _.value } map asFlash getOrElse null.asInstanceOf[jitb.lang.Array]
			val defaultValue = metadata find { _.key == "minValue" } map { _.value } map asFlash getOrElse null.asInstanceOf[jitb.lang.Array]
			val `type` = parameter.`type` match {
				case PFloatType => "float"
				case PFloat2Type => "float2"
				case PFloat3Type => "float3"
				case PFloat4Type => "float4"
				case PFloat2x2Type => "float2x2"
				case PFloat3x3Type => "float3x3"
				case PFloat4x4Type => "float4x4"
				case PIntType => "int"
				case PInt2Type => "int2"
				case PInt3Type => "int3"
				case PInt4Type => "int4"
				case PBoolType => "bool"
				case PBool2Type => "bool2"
				case PBool3Type => "bool3"
				case PBool4Type => "bool4"
				case PStringType => "string"
			}
			val index = parameter.register.index

			result(i) = ShaderParameter.JITB$create(name, description, minValue, maxValue, defaultValue, `type`, index)
			i += 1
		}

		result
	}

	def getShaderTextures(pbj: apparat.pbj.Pbj): Array[ShaderInput] = {
		val result = new Array[ShaderInput](pbj.textures.length)
		var i = 0

		for(texture <- pbj.textures) {
			result(i) = ShaderInput.JITB$create(texture.name, texture.channels, texture.index)
			i += 1
		}

		result
	}

	private def asJava(value: PConst) = value match {
		case PString(value) => value
		case _ => "(Unexpected "+value.toString+")"
	}

	private def asFlash(value: PConst) = {
		val result = new jitb.lang.Array()

		value match {
			case PFloat(x) =>
				result push x.toDouble.asInstanceOf[AnyRef]
			case PFloat2(x, y) =>
				result push x.toDouble.asInstanceOf[AnyRef]
				result push y.toDouble.asInstanceOf[AnyRef]
			case PFloat3(x, y, z) =>
				result push x.toDouble.asInstanceOf[AnyRef]
				result push y.toDouble.asInstanceOf[AnyRef]
				result push z.toDouble.asInstanceOf[AnyRef]
			case PFloat4(x, y, z, w) =>
				result push x.toDouble.asInstanceOf[AnyRef]
				result push y.toDouble.asInstanceOf[AnyRef]
				result push z.toDouble.asInstanceOf[AnyRef]
				result push w.toDouble.asInstanceOf[AnyRef]
			case value: PFloat2x2 => for(i <- 0 until  4) result push value(i).toDouble.asInstanceOf[AnyRef]
			case value: PFloat3x3 => for(i <- 0 until  9) result push value(i).toDouble.asInstanceOf[AnyRef]
			case value: PFloat4x4 => for(i <- 0 until 16) result push value(i).toDouble.asInstanceOf[AnyRef]
			case PInt(x) =>
				result push x.asInstanceOf[AnyRef]
			case PInt2(x, y) =>
				result push x.asInstanceOf[AnyRef]
				result push y.asInstanceOf[AnyRef]
			case PInt3(x, y, z) =>
				result push x.asInstanceOf[AnyRef]
				result push y.asInstanceOf[AnyRef]
				result push z.asInstanceOf[AnyRef]
			case PInt4(x, y, z, w) =>
				result push x.asInstanceOf[AnyRef]
				result push y.asInstanceOf[AnyRef]
				result push z.asInstanceOf[AnyRef]
				result push w.asInstanceOf[AnyRef]
			case PBool(x) =>
				result push x.asInstanceOf[AnyRef]
			case PBool2(x, y) =>
				result push x.asInstanceOf[AnyRef]
				result push y.asInstanceOf[AnyRef]
			case PBool3(x, y, z) =>
				result push x.asInstanceOf[AnyRef]
				result push y.asInstanceOf[AnyRef]
				result push z.asInstanceOf[AnyRef]
			case PBool4(x, y, z, w) =>
				result push x.asInstanceOf[AnyRef]
				result push y.asInstanceOf[AnyRef]
				result push z.asInstanceOf[AnyRef]
				result push w.asInstanceOf[AnyRef]
			case PString(x) =>
				result push x.asInstanceOf[AnyRef]
		}

		result
	}
}