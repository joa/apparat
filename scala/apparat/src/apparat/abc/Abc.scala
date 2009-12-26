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
package apparat.abc

import apparat.utils.IO
import apparat.utils.IO._

import scala.collection.immutable._
import scala.annotation.tailrec
import java.io._
import apparat.swf.{DoABC, SwfTag}

object Abc {
	val MINOR = 16
	val MAJOR = 46

	def fromDoABC(doABC: DoABC) = fromByteArray(doABC.abcData)

	def fromByteArray(byteArray: Array[Byte]) = {
		val abc = new Abc
		abc read byteArray
		abc
	}

	def fromTag(tag: SwfTag) = tag match {
		case doABC : DoABC => Some(fromDoABC(doABC))
		case _ => None
	}
}

class Abc {
	var cpool = new AbcConstantPool(new Array[Int](0),
		new Array[Long](0), new Array[Double](0), new Array[Symbol](0),
		new Array[AbcNamespace](0), new Array[AbcNSSet](0), new Array[AbcName](0))
	var methods = new Array[AbcMethod](0)
	var metadata = new Array[AbcMetadata](0)
	var types = new Array[AbcNominalType](0)
	var scripts = new Array[AbcScript](0)

	def read(file: File): Unit = using(new FileInputStream(file))(read _)

	def read(pathname: String): Unit = read(new File(pathname))

	def read(input: InputStream): Unit = using(new AbcInputStream(input))(read _)

	def read(data: Array[Byte]): Unit = using(new ByteArrayInputStream(data))(read _)

	def read(doABC: DoABC): Unit = read(doABC.abcData)

	def read(input: AbcInputStream): Unit = {
		if (input.readU16() != Abc.MINOR) error("Minor version not supported.")
		if (input.readU16() != Abc.MAJOR) error("Major version not supported.")
		cpool = readPool(input)
		methods = readMethods(input)
		metadata = readMetadata(input)
		types = readTypes(input)
		scripts = readScripts(input)
		readBodies(input)
	}

	def write(file: File): Unit = using(new FileOutputStream(file))(write _)

	def write(pathname: String): Unit = write(new File(pathname))

	def write(output: OutputStream): Unit = using(new AbcOutputStream(output))(write _)

	def write(doABC: DoABC): Unit = doABC.abcData = toByteArray

	def write(output: AbcOutputStream): Unit = {
		output writeU16 Abc.MINOR
		output writeU16 Abc.MAJOR
		writePool(output)
		writeMethods(output)
		writeMetadata(output)
		writeTypes(output)
		writeScripts(output)
		writeBodies(output)
	}

	def toByteArray = {
		val baos = new ByteArrayOutputStream()
		using(baos)(write _)
		baos.toByteArray()
	}

	private def readPool(implicit input: AbcInputStream) = {
		def readTable[T](t: Array[T], empty: T)(reader: => T) = {
			t(0) = empty
			for (i <- 1 until t.length) t(i) = reader
			t
		}

		val ints = readTable(new Array[Int](Math.max(1, input.readU30())), 0) { input.readS32() }
		val uints = readTable(new Array[Long](Math.max(1, input.readU30())), 0L) { input.readU32() }
		val doubles = readTable(new Array[Double](Math.max(1, input.readU30())), Double.NaN) { input.readD64() }
		val strings = readTable(new Array[Symbol](Math.max(1, input.readU30())), AbcConstantPool.EMPTY_STRING) { Symbol(input.readString()) }
		val namespaces = readTable(new Array[AbcNamespace](Math.max(1, input.readU30())), AbcConstantPool.EMPTY_NAMESPACE) { AbcNamespace(input.readU08(), strings(input.readU30())) }
		val nssets = readTable(new Array[AbcNSSet](Math.max(1, input.readU30())), AbcConstantPool.EMPTY_NSSET) { AbcNSSet((for (i <- 0 until input.readU08()) yield namespaces(input.readU30())).toArray) }
		val tmp = new Array[AbcName](Math.max(1, input.readU30()))
		val names = readTable(tmp, AbcConstantPool.EMPTY_NAME) {
			input.readU08() match {
				case AbcNameKind.QName => {
					val namespace = input.readU30()
					val name = input.readU30()
					AbcQName(strings(name), namespaces(namespace))
				}
				case AbcNameKind.QNameA => {
					val namespace = input.readU30()
					val name = input.readU30()
					AbcQNameA(strings(name), namespaces(namespace))
				}
				case AbcNameKind.RTQName => AbcRTQName(strings(input.readU30()))
				case AbcNameKind.RTQNameA => AbcRTQNameA(strings(input.readU30()))
				case AbcNameKind.RTQNameL => AbcRTQNameL
				case AbcNameKind.RTQNameLA => AbcRTQNameLA
				case AbcNameKind.Multiname => AbcMultiname(strings(input.readU30()), nssets(input.readU30()))
				case AbcNameKind.MultinameA => AbcMultinameA(strings(input.readU30()), nssets(input.readU30()))
				case AbcNameKind.MultinameL => AbcMultinameL(nssets(input.readU30()))
				case AbcNameKind.MultinameLA => AbcMultinameLA(nssets(input.readU30()))
				case AbcNameKind.Typename => {
					AbcTypename((tmp(input.readU30())).asInstanceOf[AbcQName], (for (i <- 0 until input.readU30()) yield tmp(input.readU30())).toArray)
				}
				case _ => error("Unknown multiname kind.")
			}
		}

		new AbcConstantPool(ints, uints, doubles, strings, namespaces, nssets, names)
	}

	private def writePool(implicit output: AbcOutputStream) = {
		def writeTable[T](t: Array[T])(writer: T => Unit) = {
			t.length match {
				case 1 => output writeU30 0
				case n => {
					output writeU30 n
					for(i <- 1 until n)
						writer(t(i))
				}
			}
		}

		writeTable(cpool.ints)(output writeS32 _)
		writeTable(cpool.uints)(output writeU32 _)
		writeTable(cpool.doubles)(output writeD64 _)
		writeTable(cpool.strings)(output writeString _.name)
		writeTable(cpool.namespaces)(x => {
			output writeU08 x.kind
			output writeU30 (cpool indexOf x.name)
		})
		writeTable(cpool.nssets)(x => {
			output writeU08 x.set.length
			x.set foreach (y => output writeU30 (cpool indexOf y))
		})
		writeTable(cpool.names)(x => {
			output writeU08 x.kind
			x match {
				case AbcQName(name, namespace) => {
					output writeU30 (cpool indexOf namespace)
					output writeU30 (cpool indexOf name)
				}
				case AbcQNameA(name, namespace) => {
					output writeU30 (cpool indexOf namespace)
					output writeU30 (cpool indexOf name)
				}
				case AbcRTQName(name) => output writeU30 (cpool indexOf name)
				case AbcRTQNameA(name) => output writeU30 (cpool indexOf name)
				case AbcRTQNameL | AbcRTQNameLA => {}
				case AbcMultiname(name, nsset) => {
					output writeU30 (cpool indexOf  name)
					output writeU30 (cpool indexOf nsset)
				}
				case AbcMultinameA(name, nsset) => {
					output writeU30 (cpool indexOf  name)
					output writeU30 (cpool indexOf nsset)
				}
				case AbcMultinameL(nsset) => output writeU30 (cpool indexOf nsset)
				case AbcMultinameLA(nsset) => output writeU30 (cpool indexOf nsset)
				case AbcTypename(name, parameters) => {
					output writeU30 (cpool indexOf name)
					output writeU30 parameters.length
					parameters foreach (p => output writeU30 (cpool indexOf p))
				}
			}
		})
	}

	private def readMethods(implicit input: AbcInputStream) = {
		val result = new Array[AbcMethod](input.readU30())
		
		for (i <- 0 until result.length) {
			val numParameters = input.readU30()
			val returnType = cpoolName()
			val parameters = new Array[AbcMethodParameter](numParameters)

			for (j <- 0 until numParameters)
				parameters(j) = new AbcMethodParameter(cpoolName())

			val name = cpoolString()
			val flags = input.readU08()

			if (0 != (flags & 0x08)) {
				val numOptional = input.readU30()
				assert(numOptional <= numParameters)

				for (j <- (numParameters - numOptional) until numParameters) {
					val parameter = parameters(j)
					val index = input.readU30()
					parameter.optional = true;
					parameter.optionalType = Some(input.readU08())
					parameter.optionalVal = Some(cpool.constant(parameter.optionalType.get, index))
				}
			}

			if (0 != (flags & 0x80))
				parameters foreach (_.name = Some(cpoolString()))

			result(i) = new AbcMethod(parameters, returnType, name,
				0 != (flags & 0x01), 0 != (flags & 0x02), 0 != (flags & 0x04),
				0 != (flags & 0x08), 0 != (flags & 0x40), 0 != (flags & 0x80))
		}

		result
	}

	private def writeMethods(implicit output: AbcOutputStream) = {
		output writeU30 methods.length

		for(method <- methods) {
			output writeU30 method.parameters.length
			output writeU30 (cpool indexOf method.returnType)

			for(parameter <- method.parameters)
				output writeU30 (cpool indexOf parameter.typeName)

			output writeU30 (cpool indexOf method.name)

			output writeU08 (
				  (if(method.needsArguments) 0x01 else 0x00)
				| (if(method.needsActivation) 0x02 else 0x00)
				| (if(method.needsRest) 0x04 else 0x00)
				| (if(method.hasOptionalParameters) 0x08 else 0x00)
				| (if(method.setsDXNS) 0x40 else 0x00)
				| (if(method.hasParameterNames) 0x80 else 0x00))

			if(method.hasOptionalParameters) {
				output writeU30 (method.parameters count (_.optional == true))

				for(parameter <- method.parameters if parameter.optional) {
					output writeU30 (cpool indexOf (parameter.optionalType.get, parameter.optionalVal.get))
					output writeU08 parameter.optionalType.get
				}
			}

			if(method.hasParameterNames)
				method.parameters foreach (x => output writeU30 (cpool indexOf x.name.get))
		}
	}

	private def readMetadata(implicit input: AbcInputStream) = {
		val result = new Array[AbcMetadata](input.readU30())

		for (i <- 0 until result.length) {
			val name = cpoolString()
			val n = input.readU30()
			val keys = new Array[Symbol](n)

			for (i <- 0 until n)
				keys(i) = cpoolString()

			@tailrec def traverse(index: Int, map: Map[Symbol, Symbol]): Map[Symbol, Symbol] = index match {
				case x if x == n => map
				case y => {traverse(y + 1, map + (keys(y) -> cpoolString()))}
			}

			result(i) = new AbcMetadata(name, traverse(0, new HashMap[Symbol, Symbol]))
		}

		result
	}

	private def writeMetadata(implicit output: AbcOutputStream) = {
		output writeU30 metadata.length

		for(meta <- metadata) {
			output writeU30 (cpool indexOf meta.name)
			output writeU30 meta.attributes.size
			meta.attributes.keysIterator foreach (x => output writeU30 (cpool indexOf x))
			meta.attributes.valuesIterator foreach (x => output writeU30 (cpool indexOf x))
		}
	}

	private def readTypes(implicit input: AbcInputStream) = {
		val result = new Array[AbcNominalType](input.readU30())

		for (i <- 0 until result.length) {
			val name = cpoolNameNZ().asInstanceOf[AbcQName]
			val base = input.readU30() match {
				case 0 => None
				case x => Some(cpool.names(x))
			}
			val flags = input.readU08()
			val protectedNs = if (0 != (flags & 0x08)) Some(cpoolNs()) else None
			val interfaces = new Array[AbcName](input.readU30())

			for (j <- 0 until interfaces.length)
				interfaces(j) = cpoolName()

			val init = methods(input.readU30())

			result(i) = new AbcNominalType(new AbcInstance(name, base, 0 != (flags & 0x01),
				0 != (flags & 0x02), 0 != (flags & 0x04), protectedNs,
				interfaces, init, readTraits()))
		}

		for (i <- 0 until result.length)
			result(i).klass = new AbcClass(methods(input.readU30()), readTraits())

		result
	}

	private def writeTypes(implicit output: AbcOutputStream) = {
		output writeU30 types.length

		for(nominalType <- types) {
			val inst = nominalType.inst

			output writeU30 (cpool indexOf inst.name)
			output writeU30 (inst.base match {
				case Some(x) => cpool indexOf x
				case None => 0
			})

			output writeU08 (
				  (if(inst.isSealed) 0x01 else 0x00)
				| (if(inst.isFinal) 0x02 else 0x00)
				| (if(inst.isInterface) 0x04 else 0x00)
				| (if(inst.protectedNs.isDefined) 0x08 else 0x00))

			inst.protectedNs match {
				case Some(x) => output writeU30 (cpool indexOf x)
				case None => {}
			}

			output writeU30 inst.interfaces.length
			inst.interfaces foreach (x => output writeU30 (cpool indexOf x))

			output writeU30 (methods indexOf inst.init)

			writeTraits(inst.traits)
		}

		for(nominalType <- types) {
			val klass = nominalType.klass

			output writeU30 (methods indexOf klass.init)
			writeTraits(klass.traits)
		}
	}
	
	private def readScripts(implicit input: AbcInputStream) = {
		val result = new Array[AbcScript](input.readU30())

		for (i <- 0 until result.length)
			result(i) = new AbcScript(methods(input.readU30()), readTraits())

		result
	}

	private def writeScripts(implicit output: AbcOutputStream) = {
		output writeU30 scripts.length
		for(script <- scripts) {
			output writeU30 (methods indexOf script.init)
			writeTraits(script.traits)
		}
	}

	private def readTraits()(implicit input: AbcInputStream) = {
		val result = new Array[AbcTrait](input.readU30())

		for (i <- 0 until result.length) {
			val name = cpoolNameNZ().asInstanceOf[AbcQName]

			val pack = input.readU08()
			val kind = pack & 0x0f
			val attr = (pack & 0xf0) >> 4

			def meta(a: Int) = if (0 != (a & 0x04)) {
				val mr = new Array[AbcMetadata](input.readU30())
				for (j <- 0 until mr.length) mr(j) = metadata(input.readU30())
				Some(mr)
			} else {
				None
			}

			result(i) = kind match {
				case AbcTraitKind.Slot => {
					val index = input.readU30()
					val typeName = cpoolName()
					val value = input.readU30()
					if (0 != value) {
						val kind = input.readU08()
						new AbcTraitSlot(name, index, typeName, Some(kind), Some(cpool.constant(kind, value)), meta(attr))
					} else { new AbcTraitSlot(name, index, typeName, None, None, meta(attr)) }
				}
				case AbcTraitKind.Const => {
					val index = input.readU30()
					val typeName = cpoolName()
					val value = input.readU30()
					if (0 != value) {
						val kind = input.readU08()
						new AbcTraitConst(name, index, typeName, Some(kind), Some(cpool.constant(kind, value)), meta(attr))
					} else { new AbcTraitConst(name, index, typeName, None, None, meta(attr)) }
				}
				case AbcTraitKind.Method => new AbcTraitMethod(name, input.readU30(), methods(input.readU30()), 0 != (attr & 0x01), 0 != (attr & 0x02), meta(attr))
				case AbcTraitKind.Getter => new AbcTraitGetter(name, input.readU30(), methods(input.readU30()), 0 != (attr & 0x01), 0 != (attr & 0x02), meta(attr))
				case AbcTraitKind.Setter => new AbcTraitSetter(name, input.readU30(), methods(input.readU30()), 0 != (attr & 0x01), 0 != (attr & 0x02), meta(attr))
				case AbcTraitKind.Class => new AbcTraitClass(name, input.readU30(), types(input.readU30()), meta(attr))
				case AbcTraitKind.Function => new AbcTraitFunction(name, input.readU30(), methods(input.readU30()), meta(attr))
			}
		}

		result
	}

	private def writeTraits(traits: Array[AbcTrait])(implicit output: AbcOutputStream) = {
		output writeU30 traits.length

		for(t <- traits) {
			output writeU30 (cpool indexOf t.name)
			output writeU08 (t.kind | (((t match {
				case x: AbcTraitAnyMethod => ((if(x.isFinal) 0x01 else 0x00) | (if(x.isOverride) 0x02 else 0x00))
				case _ => 0x00
			}) | (t.metadata match {
				case Some(x) => 0x04
				case None => 0x00
			})) << 0x04))

			t match {
				case AbcTraitSlot(name, index, typeName, valueType, value, metadata) => {
					output writeU30 index
					output writeU30 (cpool indexOf typeName)
					value match {
						case Some(x) => {
							output writeU30 (cpool indexOf (valueType.get, value.get))
							output writeU08 valueType.get
						}
						case None => output writeU30 0
					}
				}
				case AbcTraitConst(name, index, typeName, valueType, value, metadata) => {
					output writeU30 index
					output writeU30 (cpool indexOf typeName)
					value match {
						case Some(x) => {
							output writeU30 (cpool indexOf (valueType.get, value.get))
							output writeU08 valueType.get
						}
						case None => output writeU30 0
					}
				}
				case AbcTraitMethod(name, dispId, method, isFinal, isOverride, metadata) => {
					output writeU30 dispId
					output writeU30 (methods indexOf method)
				}
				case AbcTraitGetter(name, dispId, method, isFinal, isOverride, metadata) => {
					output writeU30 dispId
					output writeU30 (methods indexOf method)
				}
				case AbcTraitSetter(name, dispId, method, isFinal, isOverride, metadata) => {
					output writeU30 dispId
					output writeU30 (methods indexOf method)
				}
				case AbcTraitClass(name, index, nominalType, metadata) => {
					output writeU30 index
					output writeU30 (types indexOf nominalType)
				}
				case AbcTraitFunction(name, index, function, metadata) => {
					output writeU30 index
					output writeU30 (methods indexOf function)
				}
			}

			t.metadata match {
				case Some(meta) => {
					output writeU30 meta.length
					meta foreach (x => output writeU30 (metadata indexOf x))
				}
				case None => {}
			}
		}
	}

	private def readBodies(implicit input: AbcInputStream) = {
		for (i <- 0 until input.readU30()) {
			val methodId = input.readU30()
			val maxStack = input.readU30()
			val localCount = input.readU30()
			val initScopeDepth = input.readU30()
			val maxScopeDepth = input.readU30()
			val code = IO read input.readU30()

			methods(methodId).body = Some(new AbcMethodBody(maxStack, localCount,
				initScopeDepth, maxScopeDepth, code, readExceptions(),
				readTraits()))
		}
	}

	private def writeBodies(implicit output: AbcOutputStream) = {
		output writeU30 (methods count (method => method.body.isDefined))
		for(i <- 0 until methods.length) {
			val method = methods(i)
			method.body match {
				case Some(body) => {
					output writeU30 i
					output writeU30 body.maxStack
					output writeU30 body.localCount
					output writeU30 body.initScopeDepth
					output writeU30 body.maxScopeDepth
					output writeU30 body.code.length
					output write body.code

					writeExceptions(body.exceptions)
					writeTraits(body.traits)
				}
				case None => {}
			}
		}
	}
	private def readExceptions()(implicit input: AbcInputStream) = {
		val result = new Array[AbcExceptionHandler](input.readU30())

		for (i <- 0 until result.length)
			result(i) = new AbcExceptionHandler(input.readU30(),
				input.readU30(), input.readU30(), cpoolName(), cpoolName())

		result
	}

	private def writeExceptions(exceptions: Array[AbcExceptionHandler])(implicit output: AbcOutputStream) = {
		output writeU30 exceptions.length
		for(handler <- exceptions) {
			output writeU30 handler.from
			output writeU30 handler.to
			output writeU30 handler.target
			output writeU30 (cpool indexOf handler.typeName)
			output writeU30 (cpool indexOf handler.varName)
		}
	}

	private def cpoolInt()(implicit input: AbcInputStream) = cpool.ints(input.readU30())

	private def cpoolUInt()(implicit input: AbcInputStream) = cpool.uints(input.readU30())

	private def cpoolDouble()(implicit input: AbcInputStream) = cpool.doubles(input.readU30())

	private def cpoolString()(implicit input: AbcInputStream): Symbol = cpool.strings(input.readU30())

	private def cpoolName()(implicit input: AbcInputStream) = cpool.names(input.readU30())

	private def cpoolNameNZ()(implicit input: AbcInputStream) = {
		val index = input.readU30()
		if (0 == index) error("Constant pool index may not be zero.")
		cpool.names(index)
	}

	private def cpoolNs()(implicit input: AbcInputStream) = cpool.namespaces(input.readU30())
}
