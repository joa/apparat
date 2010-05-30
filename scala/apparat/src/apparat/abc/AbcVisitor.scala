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

trait AbcVisitor {
	def visit(value: Abc): Unit = {}
	def visit(value: AbcClass): Unit = {}
	def visit(value: AbcConstantPool): Unit = {}
	def visit(value: AbcExceptionHandler): Unit = {}
	def visit(value: AbcInstance): Unit = {}
	def visit(value: AbcMetadata): Unit = {}
	def visit(value: AbcMethod): Unit = {}
	def visit(value: AbcMethodBody): Unit = {}
	def visit(value: AbcMethodParameter): Unit = {}
	def visit(value: AbcNominalType): Unit = {}
	def visit(value: AbcScript): Unit = {}
	def visit(value: AbcTrait): Unit = {}
}