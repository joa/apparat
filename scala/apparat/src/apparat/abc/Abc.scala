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

import scala.annotation.tailrec
import apparat.bytecode._
import apparat.utils.IO
import apparat.utils.IO._
import apparat.utils.Dumpable
import apparat.utils.IndentingPrintWriter
import apparat.swf.{DoABC, Swf, SwfTag, SwfTags}
import apparat.abc.analysis._
import scala.collection.immutable._
import apparat.actors.Futures._
import java.io.{
	BufferedInputStream => JBufferedInputStream,
	ByteArrayInputStream => JByteArrayInputStream,
	ByteArrayOutputStream => JByteArrayOutputStream,
	InputStream => JInputStream,
	File => JFile,
	FileInputStream => JFileInputStream,
	FileOutputStream => JFileOutputStream,
	OutputStream => JOutputStream
}
import scala.math.max

object Abc {
	val MINOR = 16
	//val MINOR = 17//with decimals!
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

	def fromSwf(swf: Swf) = swf.tags find (_.kind == SwfTags.DoABC) match {
		case Some(doABC) => fromTag(doABC)
		case None => None
	}

	def fromFile(file: JFile): Abc = {
		val abc = new Abc()
		abc read file
		abc
	}

	def fromFile(pathname: String): Abc = fromFile(new JFile(pathname))
}

class Abc extends Dumpable {
	var cpool = new AbcConstantPool(new Array[Int](0),
		new Array[Long](0), new Array[Double](0), new Array[Symbol](0),
		new Array[AbcNamespace](0), new Array[AbcNSSet](0), new Array[AbcName](0))
	var methods = new Array[AbcMethod](0)
	var metadata = new Array[AbcMetadata](0)
	var types = new Array[AbcNominalType](0)
	var scripts = new Array[AbcScript](0)
	var bytecodeAvailable = false

	def accept(visitor: AbcVisitor) = {
		visitor visit this
		cpool accept visitor
		methods foreach (_ accept visitor)
		metadata foreach (_ accept visitor)
		types foreach (_ accept visitor)
		scripts foreach (_ accept visitor)
	}

	def rebuildPool() = cpool = AbcConstantPoolBuilder using this
	
	def loadBytecode() = if(!bytecodeAvailable) {
		implicit val abc = this

		methods filter (_.body.isDefined) map {
			method => future {
				val body = method.body.get
				body.bytecode = Some(Bytecode fromBody body)
			}
		} map { _() }

		/*methods foreach {
			_.body match {
				case Some(body) => body.bytecode = Some(Bytecode fromBody body)
				case None => {}
			}
		}*/

		bytecodeAvailable = true
	}

	def saveBytecode() = if(bytecodeAvailable) {
		implicit val abc = this

		val tasks = for(method <- methods if method.body.isDefined) yield future {
			val body = method.body.get
			body.bytecode match {
				case Some(bytecode) => bytecode storeIn body
				case None =>
			}
		}

		tasks foreach { _() }

		/*methods foreach {
			_.body match {
				case Some(body) => body.bytecode match {
					case Some(bytecode) => bytecode storeIn body
					case None => {}
				}
				case None => {}
			}
		}*/
	}

	def read(file: JFile): Unit = using(new JBufferedInputStream(new JFileInputStream(file), 0x1000))(read _)

	def read(pathname: String): Unit = read(new JFile(pathname))

	def read(input: JInputStream): Unit = using(new AbcInputStream(input))(read _)

	def read(data: Array[Byte]): Unit = using(new JByteArrayInputStream(data))(read _)

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

	def write(file: JFile): Unit = using(new JFileOutputStream(file))(write _)

	def write(pathname: String): Unit = write(new JFile(pathname))

	def write(output: JOutputStream): Unit = using(new AbcOutputStream(output))(write _)

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
		val byteArrayOutputStream = new JByteArrayOutputStream()
		using(byteArrayOutputStream)(write _)
		byteArrayOutputStream.toByteArray
	}

	private def readPool(implicit input: AbcInputStream) = {
		def readTable[T](t: Array[T], empty: T)(reader: => T) = {
			t(0) = empty
			for (i <- 1 until t.length) t(i) = reader
			t
		}

		val ints = readTable(new Array[Int](max(1, input.readU30())), 0) { input.readS32() }
		val uints = readTable(new Array[Long](max(1, input.readU30())), 0L) { input.readU32() }
		val doubles = readTable(new Array[Double](max(1, input.readU30())), Double.NaN) { input.readD64() }
		//val decimals = ...
		val strings = readTable(new Array[Symbol](max(1, input.readU30())), AbcConstantPool.EMPTY_STRING) { Symbol(input.readString()) }
		val namespaces = readTable(new Array[AbcNamespace](max(1, input.readU30())), AbcConstantPool.EMPTY_NAMESPACE) { AbcNamespace(input.readU08(), strings(input.readU30())) }
		val nssets = readTable(new Array[AbcNSSet](max(1, input.readU30())), AbcConstantPool.EMPTY_NSSET) { AbcNSSet(Array.fill(input.readU08()) { namespaces(input.readU30()) }) }
		val tmp = new Array[AbcName](max(1, input.readU30()))
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
					AbcTypename((tmp(input.readU30())).asInstanceOf[AbcQName], Array.fill(input.readU30()) { tmp(input.readU30()) })
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

		writeTable(cpool.namespaces) {
			namespace => {
				output writeU08 namespace.kind
				writePooledString(namespace.name)
			}
		}

		writeTable(cpool.nssets) {
			nsset => {
				output writeU08 nsset.set.length
				nsset.set foreach writePooledNamespace
			}
		}

		writeTable(cpool.names) {
			name => {
				output writeU08 name.kind
				name match {
					case AbcQName(name, namespace) => {
						writePooledNamespace(namespace)
						writePooledString(name)
					}
					case AbcQNameA(name, namespace) => {
						writePooledNamespace(namespace)
						writePooledString(name)
					}
					case AbcRTQName(name) => writePooledString(name)
					case AbcRTQNameA(name) => writePooledString(name)
					case AbcRTQNameL | AbcRTQNameLA => {}
					case AbcMultiname(name, nsset) => {
						writePooledString(name)
						writePooledNSSet(nsset)
					}
					case AbcMultinameA(name, nsset) => {
						writePooledString(name)
						writePooledNSSet(nsset)
					}
					case AbcMultinameL(nsset) => writePooledNSSet(nsset)
					case AbcMultinameLA(nsset) => writePooledNSSet(nsset)
					case AbcTypename(name, parameters) => {
						writePooledName(name)
						output writeU30 parameters.length
						parameters foreach writePooledName
					}
				}
			}
		}
	}

	private def readMethods(implicit input: AbcInputStream) = Array.fill(input.readU30()) {
		val numParameters = input.readU30()
		val returnType = readPooledName()
		val parameters = Array.fill(numParameters) { new AbcMethodParameter(readPooledName()) }
		val name = readPooledString()
		val flags = input.readU08()

		if (0 != (flags & 0x08)) {
			val numOptional = input.readU30()
			assert(numOptional <= numParameters)

			parameters takeRight numOptional foreach {
				parameter => {
					val index = input.readU30()
					parameter.optional = true;
					parameter.optionalType = Some(input.readU08())
					parameter.optionalVal = Some(cpool.constant(parameter.optionalType.get, index))
				}
			}
		}

		if (0 != (flags & 0x80))
			parameters foreach (_.name = Some(readPooledString()))

		new AbcMethod(parameters, returnType, name,
			0 != (flags & 0x01), 0 != (flags & 0x02), 0 != (flags & 0x04),
			0 != (flags & 0x08), 0 != (flags & 0x10), 0 != (flags & 0x20),
			0 != (flags & 0x40), 0 != (flags & 0x80))
	}

	private def writeMethods(implicit output: AbcOutputStream) = writeAll(methods) {
		method => {
			output writeU30 method.parameters.length
			writePooledName(method.returnType)

			method.parameters foreach (((_: AbcMethodParameter).typeName) andThen writePooledName)
			
			writePooledString(method.name)

			output writeU08 (
				  (if(method.needsArguments) 0x01 else 0x00)
				| (if(method.needsActivation) 0x02 else 0x00)
				| (if(method.needsRest) 0x04 else 0x00)
				| (if(method.hasOptionalParameters) 0x08 else 0x00)
			    | (if(method.ignoreRest) 0x10 else 0x00)
				| (if(method.isNative) 0x20 else 0x00)
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
				method.parameters foreach (((_: AbcMethodParameter).name.get) andThen writePooledString)
		}
	}

	private def readMetadata(implicit input: AbcInputStream) = Array.fill(input.readU30()) {
		val name = readPooledString()
		val n = input.readU30()
		val keys = Array.fill(n)(readPooledString)

		@tailrec def traverse(index: Int, map: Map[Symbol, Symbol]): Map[Symbol, Symbol] = index match {
			case x if x == n => map
			case y => traverse(y + 1, map + (keys(y) -> readPooledString()))
		}

		new AbcMetadata(name, traverse(0, new HashMap[Symbol, Symbol]))
	}

	private def writeMetadata(implicit output: AbcOutputStream) = writeAll(metadata) {
		meta => {
			output writeU30 (cpool indexOf meta.name)
			output writeU30 meta.attributes.size
			meta.attributes.keysIterator foreach writePooledString
			meta.attributes.valuesIterator foreach writePooledString
		}
	}

	private def readTypes(implicit input: AbcInputStream) = {
		val result = Array.fill(input.readU30()) {
			val name = readPooledNonZeroName().asInstanceOf[AbcQName]
			val base = input.readU30() match {
				case 0 => None
				case x => Some(cpool.names(x))
			}
			val flags = input.readU08()
			val protectedNs = if (0 != (flags & 0x08)) Some(readPooledNamespace()) else None
			val interfaces = Array.fill(input.readU30())(readPooledName)

			new AbcNominalType(new AbcInstance(name, base, 0 != (flags & 0x01),
				0 != (flags & 0x02), 0 != (flags & 0x04), 0 != (flags & 0x10), protectedNs,
				interfaces, methods(input.readU30()), readTraits()))
		}

		result foreach (_.klass = new AbcClass(methods(input.readU30()), readTraits()))

		result
	}

	private def writeTypes(implicit output: AbcOutputStream) = {
		writeAll(types) {
			nominalType => {
				val inst = nominalType.inst

				writePooledName(inst.name)

				output writeU30 (inst.base match {
					case Some(x) => cpool indexOf x
					case None => 0
				})

				output writeU08 (
					  (if(inst.isSealed) 0x01 else 0x00)
					| (if(inst.isFinal) 0x02 else 0x00)
					| (if(inst.isInterface) 0x04 else 0x00)
					| (if(inst.protectedNs.isDefined) 0x08 else 0x00)
					| (if(inst.nonNullable) 0x10 else 0x00))

				inst.protectedNs match {
					case Some(x) => writePooledNamespace(x)
					case None => {}
				}

				output writeU30 inst.interfaces.length
				inst.interfaces foreach writePooledName

				output writeU30 (methods indexOf inst.init)

				writeTraits(inst.traits)
			}
		}

		for(nominalType <- types) {
			val klass = nominalType.klass

			output writeU30 (methods indexOf klass.init)
			writeTraits(klass.traits)
		}
	}
	
	private def readScripts(implicit input: AbcInputStream) = Array.fill(input.readU30()) { new AbcScript(methods(input.readU30()), readTraits()) }

	private def writeScripts(implicit output: AbcOutputStream) = writeAll(scripts) {
		script => {
			output writeU30 (methods indexOf script.init)
			writeTraits(script.traits)
		}
	}

	private def readTraits()(implicit input: AbcInputStream): Array[AbcTrait] = Array.fill(input.readU30()) { //why is the type annotation needed?
		val name = readPooledNonZeroName().asInstanceOf[AbcQName]

		val pack = input.readU08()
		val kind = pack & 0x0f
		val attr = (pack & 0xf0) >> 4

		def meta(a: Int) = if (0 != (a & 0x04)) {
			Some(Array.fill(input.readU30()) { metadata(input.readU30()) })
		} else {
			None
		}

		kind match {
			case AbcTraitKind.Slot => {
				val index = input.readU30()
				val typeName = readPooledName()
				val value = input.readU30()
				if (0 != value) {
					val kind = input.readU08()
					new AbcTraitSlot(name, index, typeName, Some(kind), Some(cpool.constant(kind, value)), meta(attr))
				} else { new AbcTraitSlot(name, index, typeName, None, None, meta(attr)) }
			}
			case AbcTraitKind.Const => {
				val index = input.readU30()
				val typeName = readPooledName()
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

	private def writeTraits(traits: Array[AbcTrait])(implicit output: AbcOutputStream) = writeAll(traits) {
		t => {
			writePooledName(t.name)

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
					writePooledName(typeName)
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
					writePooledName(typeName)
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
					meta foreach (x => output writeU30 (metadata indexOf x))//TODO chain with andThen?
				}
				case None => {}
			}
		}
	}

	private def readBodies(implicit input: AbcInputStream) = for(i <- 0 until input.readU30()) {
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
	private def readExceptions()(implicit input: AbcInputStream) = Array.fill(input.readU30()) { new AbcExceptionHandler(input.readU30(), input.readU30(), input.readU30(), readPooledName(), readPooledName()) }

	private def writeExceptions(exceptions: Array[AbcExceptionHandler])(implicit output: AbcOutputStream) = writeAll(exceptions) {
		handler => {
			output writeU30 handler.from
			output writeU30 handler.to
			output writeU30 handler.target
			writePooledName(handler.typeName)
			writePooledName(handler.varName)
		}
	}

	/*
	private def readPooledInt()(implicit input: AbcInputStream) = cpool.ints(input.readU30())

	private def writePooledInt(value: Int)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)

	private def readPooledUInt()(implicit input: AbcInputStream) = cpool.uints(input.readU30())

	private def writePooledUInt(value: Long)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)

	private def readPooledDouble()(implicit input: AbcInputStream) = cpool.doubles(input.readU30())

	private def writePooledDouble(value: Double)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)
	*/

	private def readPooledString()(implicit input: AbcInputStream): Symbol = cpool.strings(input.readU30())

	private def writePooledString(value: Symbol)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)

	private def readPooledNamespace()(implicit input: AbcInputStream) = cpool.namespaces(input.readU30())

	private def writePooledNamespace(value: AbcNamespace)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)

	private def readPooledNSSet()(implicit input: AbcInputStream) = cpool.nssets(input.readU30())

	private def writePooledNSSet(value: AbcNSSet)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)

	private def readPooledName()(implicit input: AbcInputStream) = cpool.names(input.readU30())

	private def writePooledName(value: AbcName)(implicit output: AbcOutputStream) = output writeU30 (cpool indexOf value)

	private def readPooledNonZeroName()(implicit input: AbcInputStream) = {
		val index = input.readU30()
		if (0 == index) error("Constant pool index may not be zero.")
		cpool.names(index)
	}

	private def writePooledNonZeroName(value: AbcName)(implicit output: AbcOutputStream) = {
		val index = cpool indexOf value
		if(0 == index) error("Constant pool index may not be zero.")
		output writeU30 index
	}

	private def writeAll[T](array: Array[T])(write: T => Unit)(implicit output: AbcOutputStream) = {
		output writeU30 array.length
		array foreach write
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Abc:"
		writer withIndent {
			cpool dump writer
			writer <= "Functions:"
			writer withIndent {
				methods filter (_.anonymous) foreach (_ dump writer)
			}
			writer <= "Metadata:"
			writer <<< metadata
			writer <= "Scripts:"
			writer withIndent {
				scripts foreach (_ dump writer)
			}
		}
	}
}
