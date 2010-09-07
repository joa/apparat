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
package apparat.pbj

import optimization.PbjOptimizer
import pbjdata._

import java.io.{
	File => JFile,
	BufferedInputStream => JBufferedInputStream,
	ByteArrayInputStream => JByteArrayInputStream,
	ByteArrayOutputStream => JByteArrayOutputStream,
	FileInputStream => JFileInputStream,
	FileOutputStream => JFileOutputStream,
	InputStream => JInputStream,
	OutputStream => JOutputStream}

import apparat.utils.IO._
import collection.mutable.ListBuffer
import apparat.utils.{IndentingPrintWriter, Dumpable}

object Pbj {
	def main(args: Array[String]): Unit = {
		val pbj = Pbj fromFile args(0)
		//PbjOptimizer(pbj)
		pbj.dump()
		println(pbj.toVertexShader)
		println(pbj.toFragmentShader)
	}

	def fromByteArray(byteArray: Array[Byte]) = {
		val pbj = new Pbj
		pbj read byteArray
		pbj
	}

	def fromFile(file: JFile): Pbj = {
		val pbj = new Pbj
		pbj read file
		pbj
	}

	def fromFile(pathname: String): Pbj = fromFile(new JFile(pathname))

	def fromInputStream(input: JInputStream) = {
		val pbj = new Pbj
		pbj read input
		pbj
	}
}

/**
 * @author Joa Ebert
 */
class Pbj extends Dumpable {
	var version = 1
	var name = ""
	var metadata = List.empty[PMeta]
	var parameters: List[(PParam, List[PMeta])] = (OutCoord, List.empty[PMeta]) :: Nil
	var textures = List.empty[PTexture]
	var code = List.empty[POp]

	def read(file: JFile): Unit = using(new JBufferedInputStream(new JFileInputStream(file), 0x1000)) { read(_) }

	def read(pathname: String): Unit = read(new JFile(pathname))

	def read(input: JInputStream): Unit = using(new PbjInputStream(input)) { read(_) }

	def read(data: Array[Byte]): Unit = using(new JByteArrayInputStream(data)) { read(_) }

	def read(input: PbjInputStream): Unit = {
		var metadataBuffer = List.empty[PMeta]
		var parameterBuffer = List.empty[(PParam, ListBuffer[PMeta])]
		var parameterMetadataBuffer = ListBuffer.empty[PMeta]
		var textureBuffer = List.empty[PTexture]
		var codeBuffer = List.empty[POp]

		for(op <- input) op match {
			case PKernelMetaData(value) => metadataBuffer = value :: metadataBuffer
			case PParameterData(value) =>
				parameterMetadataBuffer = ListBuffer.empty[PMeta]
				parameterBuffer = (value -> parameterMetadataBuffer) :: parameterBuffer 
			case PParameterMetaData(value) => parameterMetadataBuffer += value
			case PTextureData(value) => textureBuffer = value :: textureBuffer
			case PKernelName(value) => name = value
			case PVersionData(value) => version = value
			case _ => codeBuffer = op :: codeBuffer
		}

		metadata = metadataBuffer.reverse
		parameters = parameterBuffer.reverse map { x => x._1 -> x._2.toList }
		textures = textureBuffer.reverse
		code = codeBuffer.reverse
	}

	def write(file: JFile): Unit = using(new JFileOutputStream(file)) { write(_) }

	def write(pathname: String): Unit = write(new JFile(pathname))

	def write(output: JOutputStream): Unit = using(new PbjOutputStream(output)) { write(_) }

	def write(output: PbjOutputStream): Unit = {
		@inline def writeOp(value: POp) = output writeOp value
		@inline def mapAndWrite[A, B <: POp](l: List[A], m: A => B) = l map m foreach writeOp
		writeOp(PVersionData(version))
		writeOp(PKernelName(name))
		mapAndWrite(metadata, PKernelMetaData(_: PMeta))
		for((p, m) <- parameters) {
			writeOp(PParameterData(p))
			mapAndWrite(m, PParameterMetaData(_: PMeta))
		}
		mapAndWrite(textures, PTextureData(_: PTexture))
		code foreach writeOp
		output.flush()
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Pbj:"
		writer withIndent {
			writer <= "Version: "+version
			writer <= "Name: "+name
			writer <= metadata.length+" metadata:"
			writer <<< metadata
			writer <= parameters.length+" parameter(s):"
			writer <<< parameters
			writer <= textures.length+" texture(s):"
			writer <<< textures
			writer <= "Code:"
			writer <<< code
		}
	}

	def toByteArray = {
		val byteArrayOutputStream = new JByteArrayOutputStream()
		using(byteArrayOutputStream) { write(_) }
		byteArrayOutputStream.toByteArray
	}

	def toVertexShader = "void main(){"+
			((textures map { _.index } map { "gl_TexCoord[%1$d] = gl_MultiTexCoord%1$d;" format _ }).foldLeft("") { _ + _ } )+
			"gl_Position = ftransform();}"

	def toFragmentShader = {
		val builder = new StringBuilder()
		val outReg = (parameters map { _._1 } find { case POutParameter(_, _, _) => true; case _ => false } map { _.register }) getOrElse error("Could not find output parameter.")

		var ints = Set.empty[Int]
		var floats = Set.empty[Int]
		var mat2 = Set.empty[Int]
		var mat3 = Set.empty[Int]
		var mat4 = Set.empty[Int]

		@inline def add(r: PReg) = r match {
			case PIntReg(index, _) => ints += index
			case PFloatReg(index, swizzle) => swizzle match {
				case PChannelM2x2 :: Nil => mat2 += index
				case PChannelM3x3 :: Nil => mat3 += index
				case PChannelM4x4 :: Nil => mat4 += index
				case _ => floats += index
			}
		}

		code foreach {
			case PCopy(dst, src) => {
				if(src.swizzle != Nil && src.swizzle.length == 1) {
					src.swizzle.head match {
						case PChannelM2x2 => mat2 += dst.index
						case PChannelM3x3 => mat3 += dst.index
						case PChannelM4x4 => mat4 += dst.index
						case _ =>
							add(dst)
							add(src)
					}
				} else {
					add(dst)
					add(src)
				}
			}
			case op: PLogical =>
				add(op.dst)
				add(op.src)
				ints += 0
			case op: PDstAndSrc =>
				add(op.dst)
				add(op.src)
			case op: PSrc => add(op.src)
			case op: PDst => add(op.dst)
			case _ =>
		}

		@inline def write(value: String) = builder.append(value)
		@inline def swizzleToString(swizzle: List[PChannel]) = {
			if(swizzle.length == 0) "" else {
				val result = (swizzle map { _ match {
					case PChannelR => "x"
					case PChannelG => "y"
					case PChannelB => "z"
					case PChannelA => "w"
					case _ => ""
				}}).foldLeft("") { _ + _ }
				if(result.length != 0) "."+result else ""
			}
		}

		@inline def regToString(reg: PReg) = reg match {
			case PFloatReg(index, swizzle) => index match {
				case x if x == outReg.index => "gl_FragColor"+swizzleToString(swizzle)
				case y => "f"+y+swizzleToString(swizzle)
			}
			case PIntReg(index, swizzle) => "i"+index+swizzleToString(swizzle)
		}

		@inline def binop(dst: PReg, src: PReg, operator: String = "?"): Unit = write(regToString(dst)+"="+regToString(dst)+operator+regToString(src)+";")
		@inline def unop(dst: PReg, src: PReg, operator: String = "?"): Unit = write(regToString(dst)+"="+operator+regToString(src)+";")
		@inline def logical(dst: PReg, src: PReg, operator: String = "?"): Unit = write("i0.x=int("+regToString(dst)+operator+regToString(src)+");")
		@inline def call2(dst: PReg, src: PReg, name: String = "?"): Unit = write(regToString(dst)+"="+name+"("+regToString(dst)+","+regToString(src)+");")
		@inline def call1(dst: PReg, src: PReg, name: String = "?"): Unit = write(regToString(dst)+"="+name+"("+regToString(src)+");")
		@inline def visit(dst: PReg, src: PReg, operator: String = "?"): Unit = write(regToString(dst)+"="+regToString(dst)+operator+regToString(src)+";")
		@inline def glslType(`type`: PNumeric) = `type` match {
			case PFloatType => "float"
			case PFloat2Type => "vec2"
			case PFloat3Type => "vec3"
			case PFloat4Type => "vec4"
			case PFloat2x2Type => "mat2"
			case PFloat3x3Type => "mat3"
			case PFloat4x4Type => "mat4"
			case PIntType => "int"
			case PInt2Type => "ivec2"
			case PInt3Type => "ivec3"
			case PInt4Type => "ivec4"
			case PBoolType => "bool"
			case PBool2Type => "bvec2"
			case PBool3Type => "bvec3"
			case PBool4Type => "bvec4"
		}
		val inputs = parameters map { _._1 } collect { case in: PInParameter if in.name != "_OutCoord" => in }
		textures map { _.index } map { "uniform sampler2D tex%d;" format _ } foreach write
		inputs map { p => "uniform "+glslType(p.`type`)+" "+p.name+";" } foreach write
		write("void main(){")
		ints map { "ivec4 i"+_+";" } foreach write
		floats map { "vec4 f"+_+";" } foreach write
		mat2 map { "mat2 f"+_+";" } foreach write
		mat3 map { "mat3 f"+_+";" } foreach write
		mat4 map { "mat4 f"+_+";" } foreach write
		write("f0.xy=gl_FragCoord.xy;")
		inputs map { p => regToString(p.register)+"="+p.name+";" } foreach write
		code foreach {
			case PNop() =>
			case PAdd(dst, src) => binop(dst, src, "+")
			case PSubtract(dst, src) => binop(dst, src, "-")
			case PMultiply(dst, src) => binop(dst, src, "*")
			case PReciprocal(dst, src) => write(regToString(dst)+"=1.0/"+regToString(src)+";")
			case PDivide(dst, src) => binop(dst, src, "/")
			case PAtan2(dst, src) => call2(dst, src, "atan2")
			case PPow(dst, src) => call2(dst, src, "pow")
			case PMod(dst, src) => call2(dst, src, "mod")
			case PMin(dst, src) => call2(dst, src, "min")
			case PMax(dst, src) => call2(dst, src, "max")
			case PStep(dst, src) => call2(dst, src, "step")
			case PSin(dst, src) => call1(dst, src, "sin")
			case PCos(dst, src) => call1(dst, src, "cos")
			case PTan(dst, src) => call1(dst, src, "tan")
			case PASin(dst, src) => call1(dst, src, "asin")
			case PACos(dst, src) => call1(dst, src, "acos")
			case PATan(dst, src) => call1(dst, src, "atan")
			case PExp(dst, src) => call1(dst, src, "exp")
			case PExp2(dst, src) => call1(dst, src, "exp2")
			case PLog(dst, src) => call1(dst, src, "log")
			case PLog2(dst, src) => call1(dst, src, "log2")
			case PSqrt(dst, src) => call1(dst, src, "sqrt")
			case PRSqrt(dst, src) => write(regToString(dst)+"=1.0/sqrt("+regToString(src)+");")
			case PAbs(dst, src) => call1(dst, src, "abs")
			case PSign(dst, src) => call1(dst, src, "sign")
			case PFloor(dst, src) => call1(dst, src, "floor")
			case PCeil(dst, src) => call1(dst, src, "ceil")
			case PFract(dst, src) => call1(dst, src, "fract")
			case PCopy(dst, src) => src.swizzle match {
				case PChannelM2x2 :: Nil => write(regToString(dst)+"=mat2("+regToString(src)+");")
				case _ => write(regToString(dst)+"="+regToString(src)+";")
			}
			case PFloatToInt(dst, src) => call1(dst, src, "int")
			case PIntToFloat(dst, src) => call1(dst, src, "float")
			case PMatrixMatrixMultiply(dst, src) => binop(dst, src, "*")
			case PVectorMatrixMultiply(dst, src) => binop(dst, src, "*")
			case PMatrixVectorMultiply(dst, src) => binop(dst, src, "*")
			case PNormalize(dst, src) => call1(dst, src, "normalize")
			case PLength(dst, src) => call1(dst, src, "length")
			case PDistance(dst, src) => call1(dst, src, "distance")
			case PDotProduct(dst, src) => call2(dst, src, "dot")
			case PCrossProduct(dst, src) => call2(dst, src, "cross")
			case PEqual(dst, src) => logical(dst, src, "==")
			case PNotEqual(dst, src) => logical(dst, src, "!=")
			case PLessThan(dst, src) => logical(dst, src, "<")
			case PLessThanEqual(dst, src) => logical(dst, src, "<=")
			case PLogicalNot(dst, src) => unop(dst, src, "~")
			case PLogicalAnd(dst, src) => binop(dst, src, "&")
			case PLogicalOr(dst, src) => binop(dst, src, "|")
			case PLogicalXor(dst, src) => binop(dst, src, "^")
			case PSampleNearest(dst, src, texture: Int) => write(regToString(dst)+"=texture2D(tex"+texture+","+regToString(src)+");")
			case PSampleBilinear(dst, src, texture: Int) => write(regToString(dst)+"=texture2D(tex"+texture+","+regToString(src)+");")
			case PLoadInt(dst: PReg, value: Int) => write(regToString(dst)+"="+value.toString+";")
			case PLoadFloat(dst: PReg, value: Float) => write(regToString(dst)+"="+value.toString+";")
			case PSelect(dst, src, src0, src1) => write(regToString(dst)+"=bool("+regToString(src)+")?"+regToString(src0)+":"+regToString(src1)+";")
			case PIf(condition) => write("if(bool("+regToString(condition)+")){")
			case PElse() => write("}else{")
			case PEndif() => write("}")
			case PFloatToBool(dst, src) => call1(dst, src, "bool")
			case PBoolToFloat(dst, src) => call1(dst, src, "float")
			case PIntToBool(dst, src) => call1(dst, src, "bool")
			case PBoolToInt(dst, src) => call1(dst, src, "int")
			case PVectorEqual(dst, src) =>  logical(dst, src, "==")
			case PVectorNotEqual(dst, src) =>  logical(dst, src, "!=")
			case PAny(dst, src) => visit(dst, src)
			case PAll(dst, src) => visit(dst, src)
			case PKernelMetaData(_) =>
			case PParameterData(_) =>
			case PParameterMetaData(_) =>
			case PTextureData(_) =>
			case PKernelName(_) =>
			case PVersionData(_) =>
		}
		write("}")
		builder.toString
	}
}