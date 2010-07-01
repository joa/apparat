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

package com.joa_ebert.apparat.abc.bytecode.asbridge;

import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceKind;
import com.joa_ebert.apparat.abc.multinames.QName;

/**
 * 
 * @author Joa Ebert
 * 
 */
final class Settings
{
	static final Namespace INLINE_NAMESPACE = new Namespace(
			NamespaceKind.PackageNamespace, "com.joa_ebert.apparat.inline" );

	static final Namespace MEMORY_NAMESPACE = new Namespace(
			NamespaceKind.PackageNamespace, "com.joa_ebert.apparat.memory" );

	static final QName BYTECODE_QNAME = new QName( INLINE_NAMESPACE,
			"__bytecode" );

	static final QName MEMORY_QNAME = new QName( MEMORY_NAMESPACE, "Memory" );
}
