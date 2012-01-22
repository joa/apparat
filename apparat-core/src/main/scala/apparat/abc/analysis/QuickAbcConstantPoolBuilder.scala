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
package apparat.abc.analysis

import apparat.abc._
import collection.immutable.HashSet

/**
 * @author Joa Ebert
 */
object QuickAbcConstantPoolBuilder {
	def using(abc: Abc) = {
		val builder = new QuickAbcConstantPoolBuilder()
		builder add abc
		builder.createPool
	}
}

class QuickAbcConstantPoolBuilder extends AbcVisitor with AbstractAbcConstantPoolBuilder {
	var ints = HashSet.empty[Int]
	var uints = HashSet.empty[Long]
	var doubles = HashSet.empty[Double]
	var strings = HashSet.empty[Symbol]
	var namespaces = HashSet.empty[AbcNamespace]
	var nssets = HashSet.empty[AbcNSSet]
	var names = HashSet.empty[AbcName]

	override def reset = {
		super.reset()

		ints = HashSet.empty
		uints = HashSet.empty
		doubles = HashSet.empty
		strings = HashSet.empty
		namespaces = HashSet.empty
		nssets = HashSet.empty
		names = HashSet.empty
	}

	def createPool = {
		val intArray = (0 :: ints.toList).toArray
		val uintArray = (0L :: uints.toList).toArray
		val doubleArray = if(addNaN) {
			(Double.NaN :: Double.NaN :: doubles.toList).toArray
		} else {
			(Double.NaN :: doubles.toList).toArray
		}
		val stringArray = (AbcConstantPool.EMPTY_STRING :: strings.toList).toArray
		val namespaceArray = (AbcConstantPool.EMPTY_NAMESPACE :: namespaces.toList).toArray
		val nssetArray = (AbcConstantPool.EMPTY_NSSET :: nssets.toList).toArray
		val nameArray = (AbcConstantPool.EMPTY_NAME :: names.toList).toArray
		new AbcConstantPool(intArray, uintArray, doubleArray, stringArray,
			namespaceArray, nssetArray, nameArray)
	}

	override protected def addValueToPool(value: Int): Unit  = ints += value
	override protected def addValueToPool(value: Long): Unit  = uints += value
	override protected def addValueToPool(value: Double): Unit  = doubles += value
	override protected def addValueToPool(value: Symbol): Unit  = strings += value
	override protected def addValueToPool(value: AbcNamespace): Unit  = namespaces += value
	override protected def addValueToPool(value: AbcNSSet): Unit  = nssets += value
	override protected def addValueToPool(value: AbcName): Unit = names += value
}
