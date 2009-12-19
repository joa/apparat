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

object AbcConstantPool {
	val EMPTY_STRING = ""
	val EMPTY_NAMESPACE = AbcNamespace(0, EMPTY_STRING)
	val EMPTY_NSSET = AbcNSSet(Set(EMPTY_NAMESPACE))
	val EMPTY_NAME = AbcQName(EMPTY_STRING, EMPTY_NAMESPACE)
}

class AbcConstantPool(
		val ints: Array[Int],
		val uints: Array[Long],
		val doubles: Array[Double],
		val strings: Array[String],
		val namespaces: Array[AbcNamespace],
		val nssets: Array[AbcNSSet],
		val names: Array[AbcName]) {
	def constant(kind: Some[Int], index: Int): Any = constant(kind.get, index)

	def constant(kind: Int, index: Int): Any = kind match {
		case AbcConstantType.Int => ints(index)
		case AbcConstantType.UInt => uints(index)
		case AbcConstantType.Double => doubles(index)
		case AbcConstantType.Utf8 => strings(index);
		case AbcConstantType.True => true
		case AbcConstantType.False => false;
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
}