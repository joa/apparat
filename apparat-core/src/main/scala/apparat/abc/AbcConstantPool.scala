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

import apparat.utils.{Dumpable, IndentingPrintWriter}
import compat.Platform

object AbcConstantPool {
	val EMPTY_STRING = Symbol(null)
	val EMPTY_NAMESPACE = AbcNamespace(0, EMPTY_STRING)
	val EMPTY_NSSET = AbcNSSet(Array(EMPTY_NAMESPACE))
	val EMPTY_NAME = AbcQName(EMPTY_STRING, EMPTY_NAMESPACE)
}

class AbcConstantPool(
		val ints: Array[Int],
		val uints: Array[Long],
		val doubles: Array[Double],
		val strings: Array[Symbol],
		val namespaces: Array[AbcNamespace],
		val nssets: Array[AbcNSSet],
		val names: Array[AbcName]) extends Dumpable {

	def +(that: AbcConstantPool) = new AbcConstantPool(
		(ints.toList ::: (that.ints.toList drop 1)).distinct.toArray,
		(uints.toList ::: (that.uints.toList drop 1)).distinct.toArray,
		(doubles.toList ::: (that.doubles.toList drop 1)).distinct.toArray,
		(strings.toList ::: (that.strings.toList drop 1)).distinct.toArray,
		(namespaces.toList ::: (that.namespaces.toList drop 1)).distinct.toArray,
		(nssets.toList ::: (that.nssets.toList drop 1)).distinct.toArray,
		(names.toList ::: (that.names.toList drop 1)).distinct.toArray)

	def accept(visitor: AbcVisitor) = visitor visit this
	
	def constant(kind: Some[Int], index: Int): Any = constant(kind.get, index)

	def constant(kind: Int, index: Int): Any = kind match {
		case AbcConstantType.Int => ints(index)
		case AbcConstantType.UInt => uints(index)
		case AbcConstantType.Double => doubles(index)
		case AbcConstantType.Utf8 => strings(index)
		case AbcConstantType.True => true
		case AbcConstantType.False => false
		case AbcConstantType.Null => null
		case AbcConstantType.Undefined => null
		case AbcConstantType.Namespace |
				AbcConstantType.PackageNamespace |
				AbcConstantType.InternalNamespace |
				AbcConstantType.ProtectedNamespace |
				AbcConstantType.ExplicitNamespace |
				AbcConstantType.StaticProtectedNamespace |
				AbcConstantType.PrivateNamespace => namespaces(index)
	}

	def indexOf(value: Int): Int = ints indexOf value

	def indexOf(value: Long): Int = uints indexOf value

	def indexOf(value: Double): Int = doubles indexOf value
	
	def indexOf(value: Symbol): Int = strings indexOf value

	def indexOf(value: AbcNamespace) = namespaces indexOf value

	def indexOf(value: AbcNSSet) = nssets indexOf value

	def indexOf(value: AbcName) = names indexOf value

	def indexOf(kind: Option[Int], value: Option[Any]): Int = {
		value match {
			case Some(x) => indexOf(kind getOrElse error("Constant value without type."), x)
			case None => 0
		}
	}

	def indexOf(kind: Int, value: Any): Int = {
		//
		// Undocumented: If a value type has no value associated with it. I.e.
		// Null we have to return the type of the constant.
		//

		//
		// NOTE: Although index zero is correct for certain values (e.g. type
		// is Int and value is "0" the index is usually 0) we may not return
		// it since a trait has no value associated with it if the index
		// is zero.
		//
		kind match {
			case AbcConstantType.Int => ints.indexOf(value.asInstanceOf[Int], 1)
			case AbcConstantType.UInt => uints.indexOf(value.asInstanceOf[Long], 1)
			case AbcConstantType.Double => {
				//TODO fix when fixed
				//http://lampsvn.epfl.ch/trac/scala/ticket/3291
				val double = value.asInstanceOf[Double]
				if(double.isNaN) {
					for(i <- 1 until doubles.length) {
						if(doubles(i).isNaN) {
							return i
						}
					}
					
					-1
				} else {
					doubles.indexOf(value.asInstanceOf[Double], 1)
				}
			}
			case AbcConstantType.Utf8 => strings.indexOf(value.asInstanceOf[Symbol], 1)
			case AbcConstantType.True |
					AbcConstantType.False |
					AbcConstantType.Null |
					AbcConstantType.Undefined => kind
			case AbcConstantType.Namespace |
					AbcConstantType.PackageNamespace |
					AbcConstantType.InternalNamespace |
					AbcConstantType.ProtectedNamespace |
					AbcConstantType.ExplicitNamespace |
					AbcConstantType.StaticProtectedNamespace |
					AbcConstantType.PrivateNamespace => namespaces.indexOf(value.asInstanceOf[AbcNamespace], 1)
			case _ => 0xff
		}
	}

	override def toString = "[AbcConstantPool]"

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "ConstantPool:"
		writer withIndent {
			writer <= ints.length + " integer(s):"
			writer <<< ints
			writer <= uints.length + " uint(s):"
			writer <<< uints
			writer <= doubles.length + " double(s):"
			writer <<< doubles
			writer <= strings.length + " string(s):"
			writer withIndent writer.println(strings)("\"" + _.name + "\"")
			writer <= namespaces.length + " namespace(s):"
			writer <<< namespaces
			writer <= nssets.length + " namespaceset(s):"
			writer <<< nssets
			writer <= names.length + " multiname(s):"
			writer <<< names
		}
	}

	//

	def add(value: Int): AbcConstantPool  = new AbcConstantPool(addToPool(value, ints) { _ == value }, uints, doubles, strings, namespaces, nssets, names)

	def add(value: Long): AbcConstantPool  = new AbcConstantPool(ints, addToPool(value, uints) { _ == value }, doubles, strings, namespaces, nssets, names)

	def add(value: Double): AbcConstantPool  = {
		if(value.isNaN) {
			new AbcConstantPool(ints, uints, addToPool(Double.NaN, doubles) { _.isNaN }, strings, namespaces, nssets, names)
		} else {
			new AbcConstantPool(ints, uints, addToPool(value, doubles) { _ == value }, strings, namespaces, nssets, names)
		}
	}

	def add(value: Symbol): AbcConstantPool = {
		if(value != AbcConstantPool.EMPTY_STRING) {
			new AbcConstantPool(ints, uints, doubles, addToPool(value, strings) { _ == value }, namespaces, nssets, names)
		} else {
			this
		}
	}

	def add(value: AbcNamespace): AbcConstantPool = {
		val result = add(value.name)

		if(value != AbcConstantPool.EMPTY_NAMESPACE) {
			new AbcConstantPool(result.ints, result.uints, result.doubles, result.strings, addToPool(value, result.namespaces) { _ == value }, result.nssets, result.names)
		} else {
			result
		}
	}

	def add(value: AbcNSSet): AbcConstantPool = {
		var result = this

		for(ns <- value.set) {
			result = result add ns
		}

		if(value != AbcConstantPool.EMPTY_NSSET) {
			new AbcConstantPool(result.ints, result.uints, result.doubles, result.strings, result.namespaces, addToPool(value, result.nssets) { _ == value }, result.names)
		} else {
			result
		}
	}

	def add(value: AbcName): AbcConstantPool = {
		var result = if(value != AbcConstantPool.EMPTY_NAME) {
			new AbcConstantPool(ints, uints, doubles, strings, namespaces, nssets, addToPool(value, names) { _ == value })
		} else {
			this
		}

		value match {
			case AbcQName(name, namespace) => {
				result = result add name
				result = result add namespace
			}
			case AbcQNameA(name, namespace) => {
				result = result add name
				result = result add namespace
			}
			case AbcRTQName(name) => result = result add name
			case AbcRTQNameA(name) => result = result add name
			case AbcRTQNameL | AbcRTQNameLA =>
			case AbcMultiname(name, nsset) => {
				result = result add name
				result = result add nsset
			}
			case AbcMultinameA(name, nsset) => {
				result = result add name
				result = result add nsset
			}
			case AbcMultinameL(nsset) => result = result add nsset
			case AbcMultinameLA(nsset) => result = result add nsset
			case AbcTypename(name, parameters) => {
				result = result add name
				for(parameter <- parameters) {
					result = result add parameter
				}
			}
		}

		result
	}

	@inline private def addToPool[T: ClassManifest](value: T, array: Array[T])(condition: T => Boolean): Array[T] = {
		var i = 1//NOTE we ignore index 0. this can lead to duplicates for the 0 entry but is safe for optionals
		val n = array.length
		while(i < n) {
			if(condition(array(i))) {
				return array
			}
			i += 1
		}

		val r = new Array[T](n + 1)
		Platform.arraycopy(array, 0, r, 0, n)
		r(n) = value

		r
	}
}