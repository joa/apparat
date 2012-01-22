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

object AbcConstantPoolBuilder {
	def using(abc: Abc) = {
		val builder = new AbcConstantPoolBuilder()
		builder add abc
		builder.createPool
	}
}
class AbcConstantPoolBuilder extends AbcVisitor with AbstractAbcConstantPoolBuilder {
	var ints: List[Int] = Nil
	var uints: List[Long] = Nil
	var doubles: List[Double] = Nil
	var strings: List[Symbol] = Nil
	var namespaces: List[AbcNamespace] = Nil
	var nssets: List[AbcNSSet] = Nil
	var names: List[AbcName] = Nil

	override def reset() = {
		super.reset()

		ints = Nil
		uints = Nil
		doubles = Nil
		strings = Nil
		namespaces = Nil
		nssets = Nil
		names = Nil
	}

	def optimize[B](list: List[B]) = {
		val noDuplicates = list.distinct
		val count = Map(noDuplicates zip (noDuplicates map { x => list count (_ == x) }): _*)
		noDuplicates sortWith { (a, b) => count(a) > count(b) }
	}

	def createPool = {
		import apparat.actors.Futures._
		val intFuture = future { (0 :: optimize(ints)).toArray }
		val uintFuture = future { (0L :: optimize(uints)).toArray }
		val doubleFuture = future {
			if(addNaN) {
				(Double.NaN :: Double.NaN :: optimize(doubles)).toArray
			} else {
				(Double.NaN :: optimize(doubles)).toArray
			}
		}
		val stringFuture = future { (AbcConstantPool.EMPTY_STRING :: optimize(strings)).toArray }
		val namespaceFuture = future { (AbcConstantPool.EMPTY_NAMESPACE :: optimize(namespaces)).toArray }
		val nssetFuture = future { (AbcConstantPool.EMPTY_NSSET :: optimize(nssets)).toArray }
		val nameFuture = future {
			val noDuplicates = names.distinct
			val count = Map(noDuplicates zip (noDuplicates map { x => names count (_ == x) }): _*)
			(AbcConstantPool.EMPTY_NAME :: noDuplicates.sortWith((a, b) => a match {
				case AbcTypename(_, _) => false
				case other => b match {
					case AbcTypename(_, _) => true
					case _ => count(a) > count(b)
				}
			}).distinct).toArray
		}
		new AbcConstantPool(intFuture(), uintFuture(), doubleFuture(), stringFuture(),
			namespaceFuture(), nssetFuture(), nameFuture())
	}

	override protected def addValueToPool(value: Int): Unit  = ints = value :: ints
	override protected def addValueToPool(value: Long): Unit  = uints = value :: uints
	override protected def addValueToPool(value: Double): Unit  = doubles = value :: doubles
	override protected def addValueToPool(value: Symbol): Unit  = strings = value :: strings
	override protected def addValueToPool(value: AbcNamespace): Unit  = namespaces = value :: namespaces
	override protected def addValueToPool(value: AbcNSSet): Unit  = nssets = value :: nssets
	override protected def addValueToPool(value: AbcName): Unit = names = value :: names
}
