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

import apparat.utils.Performance._
import java.io.{File, FileInputStream, FileOutputStream}
import java.io.{InputStream, OutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import apparat.utils.IO._

class Abc {
	var cpool = new AbcConstantPool(new Array[Int](0),
			new Array[Long](0), new Array[Double](0), new Array[String](0),
			new Array[AbcNamespace](0), new Array[AbcNSSet](0), new Array[AbcName](0))
	
	var methods = new Array[AbcMethod](0)
	
	def read(file: File): Unit = using(new FileInputStream(file)) (read _) 
	def read(pathname: String): Unit = read(new File(pathname))
	def read(input: InputStream): Unit = using(new AbcInputStream(input)) (read _)
	def read(data: Array[Byte]): Unit = using(new ByteArrayInputStream(data)) (read _)
	def read(input: AbcInputStream): Unit = {
		if(input.readU16 != 16) error("Only minor version 16 is supported.")
		if(input.readU16 != 46) error("Only major version 46 is supported.")
		
		cpool = readPool(input)
		methods = readMethods(input)
	}
  
	private def readPool(implicit input: AbcInputStream) = {
		def table[T](table: Array[T], empty: T)(reader: => T) = {
			table(0) = empty
			for(i <- 1 until table.length) {
				table(i) = reader
			}
			table
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
			AbcNSSet(Set((for(i <- 0 until input.readU08()) yield namespaces(input.readU30())):_*))
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
					AbcTypename((tmp(input.readU30())).asInstanceOf[AbcQName], for(i <- 0 until input.readU30()) yield tmp(input.readU30()))
				}
				case _ => error("Unknown multiname kind.")
			}
		}
		
		new AbcConstantPool(ints, uints, doubles, strings, namespaces, nssets, names)
	}
	
	private def readMethods(implicit input: AbcInputStream) = (for(i <- 0 until input.readU30()) yield {
		val numParameters = input.readU30()
		val returnType = cpoolName()
		val parameters = new Array[AbcMethodParameter](numParameters)

		for(j <- 0 until numParameters)
			parameters(j) = new AbcMethodParameter(cpoolName())
		
		val name = cpoolString()
		val flags = input.readU08()
		
		if(0 != (flags & 0x08)) {
			val numOptional = input.readU30()
			
			assert(numOptional <= numParameters)
			
			for(j <- (numParameters - numOptional) until numParameters) {
				val parameter = parameters(j)
				val index = input.readU30()
				//TODO read constant from cpool
				parameter.optional = true;
				parameter.optionalType = Some(input.readU08())
				parameter.optionalVal = None
			}
		}
		
		if(0 != (flags & 0x80))
			parameters foreach (_.name = Some(cpoolString()))
		
		new AbcMethod(parameters, returnType, name,
				0 != (flags & 0x01), 0 != (flags & 0x02), 0 != (flags & 0x04),
				0 != (flags & 0x08), 0 != (flags & 0x40), 0 != (flags & 0x80))
	}) toArray
	
	private def cpoolInt()(implicit input: AbcInputStream) = cpool.ints(input.readU30())
	private def cpoolUInt()(implicit input: AbcInputStream) = cpool.uints(input.readU30())
	private def cpoolDouble()(implicit input: AbcInputStream) = cpool.doubles(input.readU30())
	private def cpoolString()(implicit input: AbcInputStream) = cpool.strings(input.readU30())
	private def cpoolName()(implicit input: AbcInputStream) = cpool.names(input.readU30())
}
