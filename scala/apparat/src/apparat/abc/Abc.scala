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

import java.io.{File, FileInputStream}
import java.io.{InputStream}
import java.io.{ByteArrayInputStream}

import scala.collection.immutable._
import scala.annotation.tailrec

class Abc {
	var cpool = new AbcConstantPool(new Array[Int](0),
		new Array[Long](0), new Array[Double](0), new Array[String](0),
		new Array[AbcNamespace](0), new Array[AbcNSSet](0), new Array[AbcName](0))
	var methods = new Array[AbcMethod](0)
	var metadata = new Array[AbcMetadata](0)
	var types = new Array[AbcNominalType](0)
	var scripts = new Array[AbcScript](0)

	def read(file: File): Unit = using(new FileInputStream(file))(read _)

	def read(pathname: String): Unit = read(new File(pathname))

	def read(input: InputStream): Unit = using(new AbcInputStream(input))(read _)

	def read(data: Array[Byte]): Unit = using(new ByteArrayInputStream(data))(read _)

	def read(input: AbcInputStream): Unit = {
		if (input.readU16 != 16) error("Only minor version 16 is supported.")
		if (input.readU16 != 46) error("Only major version 46 is supported.")
		cpool = readPool(input)
		methods = readMethods(input)
		metadata = readMetadata(input)
		types = readTypes(input)
		scripts = readScripts(input)
		readBodies(input)
	}

	private def readPool(implicit input: AbcInputStream) = {
		def table[T](t: Array[T], empty: T)(reader: => T) = {
			t(0) = empty
			for (i <- 1 until t.length) t(i) = reader
			t
		}

		val ints = table(new Array[Int](Math.max(1, input.readU30())), 0) {
			input.readS32()
		}

		val uints = table(new Array[Long](Math.max(1, input.readU30())), 0L) {
			input.readU32()
		}

		val doubles = table(new Array[Double](Math.max(1, input.readU30())), Double.NaN) {
			input.readD64()
		}

		val strings = table(new Array[String](Math.max(1, input.readU30())), AbcConstantPool.EMPTY_STRING) {
			input.readString()
		}

		val namespaces = table(new Array[AbcNamespace](Math.max(1, input.readU30())), AbcConstantPool.EMPTY_NAMESPACE) {
			AbcNamespace(input.readU08(), strings(input.readU30()))
		}

		val nssets = table(new Array[AbcNSSet](Math.max(1, input.readU30())), AbcConstantPool.EMPTY_NSSET) {
			AbcNSSet(Set((for (i <- 0 until input.readU08()) yield namespaces(input.readU30())): _*))
		}

		val tmp = new Array[AbcName](Math.max(1, input.readU30()))
		val names = table(tmp, AbcConstantPool.EMPTY_NAME) {
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
					AbcTypename((tmp(input.readU30())).asInstanceOf[AbcQName], for (i <- 0 until input.readU30()) yield tmp(input.readU30()))
				}
				case _ => error("Unknown multiname kind.")
			}
		}

		new AbcConstantPool(ints, uints, doubles, strings, namespaces, nssets, names)
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

	private def readMetadata(implicit input: AbcInputStream) = {
		val result = new Array[AbcMetadata](input.readU30())
		for (i <- 0 until result.length) {
			val name = cpoolString()
			val n = input.readU30()
			val keys = new Array[String](n)
			for (i <- 0 until n) keys(i) = cpoolString()

			@tailrec def traverse(index: Int, map: Map[String, String]): Map[String, String] = index match {
				case x if x == n => map
				case y => {traverse(y + 1, map + (keys(y) -> cpoolString()))}
			}

			result(i) = new AbcMetadata(name, traverse(0, new TreeMap[String, String]))
		}

		result
	}

	private def readTypes(implicit input: AbcInputStream) = {
		val result = new Array[AbcNominalType](input.readU30())

		for (i <- 0 until result.length) {
			val name = cpoolNameNZ()

			assert(name.kind == AbcNameKind.QName)

			val baseIndex = input.readU30()
			val base = baseIndex match {
				case 0 => None
				case x => Some(cpool.names(x))
			}

			val flags = input.readU08()

			val protectedNs = if (0 != (flags & 0x08)) Some(cpoolNs()) else None

			val interfaces = new Array[AbcName](input.readU30())
			for (j <- 0 until interfaces.length) interfaces(j) = cpoolName()

			val init = methods(input.readU30())

			result(i) = new AbcNominalType(new AbcInstance(name, base, 0 != (flags & 0x01),
				0 != (flags & 0x02), 0 != (flags & 0x04), protectedNs,
				interfaces, init, readTraits()))
		}

		for (i <- 0 until result.length) {
			result(i).klass = new AbcClass(methods(input.readU30()), readTraits())
		}

		result
	}

	private def readScripts(implicit input: AbcInputStream) = {
		val result = new Array[AbcScript](input.readU30())

		for (i <- 0 until result.length) {
			result(i) = new AbcScript(methods(input.readU30()), readTraits())
		}

		result
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
					} else {new AbcTraitSlot(name, index, typeName, None, None, meta(attr))}
				}
				case AbcTraitKind.Const => {
					val index = input.readU30()
					val typeName = cpoolName()
					val value = input.readU30()
					if (0 != value) {
						val kind = input.readU08()
						new AbcTraitConst(name, index, typeName, Some(kind), Some(cpool.constant(kind, value)), meta(attr))
					} else {new AbcTraitConst(name, index, typeName, None, None, meta(attr))}
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

	private def readExceptions()(implicit input: AbcInputStream) = {
		val result = new Array[AbcExceptionHandler](input.readU30())

		for (i <- 0 until result.length) {
			result(i) = new AbcExceptionHandler(input.readU30(),
				input.readU30(), input.readU30(), cpoolName(), cpoolName())
		}

		result
	}

	private def cpoolInt()(implicit input: AbcInputStream) = cpool.ints(input.readU30())

	private def cpoolUInt()(implicit input: AbcInputStream) = cpool.uints(input.readU30())

	private def cpoolDouble()(implicit input: AbcInputStream) = cpool.doubles(input.readU30())

	private def cpoolString()(implicit input: AbcInputStream) = cpool.strings(input.readU30())

	private def cpoolName()(implicit input: AbcInputStream) = cpool.names(input.readU30())

	private def cpoolNameNZ()(implicit input: AbcInputStream) = {
		val index = input.readU30()
		if (0 == index) error("cpool index may not be zero.")
		cpool.names(index)
	}

	private def cpoolNs()(implicit input: AbcInputStream) = cpool.namespaces(input.readU30())
}
