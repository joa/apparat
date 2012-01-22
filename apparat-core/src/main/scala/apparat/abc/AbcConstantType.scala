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
package apparat.abc

object AbcConstantType {
	val Int = 0x03
	val UInt = 0x04
	val Double = 0x06
	val Utf8 = 0x01
	val True = 0x0b
	val False = 0x0a
	val Null = 0x0c
	val Undefined = 0x00
	val Namespace = 0x08
	val PackageNamespace = 0x16
	val InternalNamespace = 0x17
	val ProtectedNamespace = 0x18
	val ExplicitNamespace = 0x19
	val StaticProtectedNamespace = 0x1a
	val PrivateNamespace = 0x05
}
