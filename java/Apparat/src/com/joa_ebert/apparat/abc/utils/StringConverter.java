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

package com.joa_ebert.apparat.abc.utils;

import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MultinameKind;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceSet;
import com.joa_ebert.apparat.abc.Script;
import com.joa_ebert.apparat.abc.bytecode.Marker;
import com.joa_ebert.apparat.abc.multinames.Multiname;
import com.joa_ebert.apparat.abc.multinames.MultinameL;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.multinames.RTQName;
import com.joa_ebert.apparat.abc.multinames.Typename;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class StringConverter
{
	public static String toString( final AbstractMultiname name )
	{
		if( null == name )
		{
			return "(NULL_NAME)";
		}

		switch( name.kind )
		{
			case QName:
			case QNameA:
				final QName qName = (QName)name;
				return "QName"
						+ ( name.kind == MultinameKind.QNameA ? "A" : "" )
						+ "(" + toString( qName.namespace ) + ", \""
						+ qName.name + "\")";
			case Multiname:
			case MultinameA:
				final Multiname multiname = (Multiname)name;
				return "Multiname"
						+ ( name.kind == MultinameKind.MultinameA ? "A" : "" )
						+ "(" + toString( multiname.namespaceSet ) + ", \""
						+ multiname.name + "\")";
			case MultinameL:
			case MultinameLA:
				final MultinameL multinameL = (MultinameL)name;
				return "MultinameL"
						+ ( name.kind == MultinameKind.MultinameLA ? "A" : "" )
						+ "(" + toString( multinameL.namespaceSet ) + ")";
			case RTQName:
			case RTQNameA:
				final RTQName rtqName = (RTQName)name;
				return "RTQName"
						+ ( name.kind == MultinameKind.RTQNameA ? "A" : "" )
						+ "(" + rtqName.name + ")";
			case RTQNameL:
			case RTQNameLA:
				return "RTQNameL"
						+ ( name.kind == MultinameKind.RTQNameLA ? "A" : "" )
						+ "()";
			case Typename:
				final Typename typename = (Typename)name;
				final StringBuilder builder = new StringBuilder( "Typename(" );
				builder.append( toString( typename.name ) );
				for( final AbstractMultiname param : typename.parameters )
				{
					builder.append( ", " );
					builder.append( toString( param ) );
				}
				builder.append( ")" );
				return builder.toString();
		}

		return "UnknownMULTINAME()";
	}

	public static String toString( final Class klass )
	{
		if( null == klass )
		{
			return "(NULL_CLASS)";
		}

		return "Class()";
	}

	public static String toString( final ExceptionHandler exceptionHandler )
	{
		if( null == exceptionHandler )
		{
			return "(NULL_EXCEPTIONHANDLER)";
		}

		return "ExceptionHandler(" + toString( exceptionHandler.exceptionType )
				+ ", " + toString( exceptionHandler.variableName ) + ", "
				+ toString( exceptionHandler.from ) + ", "
				+ toString( exceptionHandler.to ) + ", "
				+ toString( exceptionHandler.target ) + ")";
	}

	public static String toString( final Instance instance )
	{
		if( null == instance )
		{
			return "(NULL_INSTANCE)";
		}

		return "Instance(" + toString( instance.name ) + ", "
				+ toString( instance.klass ) + ")";
	}

	public static String toString( final Marker marker )
	{
		if( null == marker )
		{
			return "(NULL_MARKER)";
		}

		return "L" + Integer.toString( marker.key );
	}

	public static String toString( final Method method )
	{
		if( null == method )
		{
			return "(NULL_METHOD)";
		}

		return "Method(\"" + method.name + "\")";
	}

	public static String toString( final Namespace namespace )
	{
		if( null == namespace )
		{
			return "(NULL_NAMESPACE)";
		}

		switch( namespace.kind )
		{
			case ExplicitNamespace:
				return "ExplicitNamespace(\"" + namespace.name + "\")";
			case Namespace:
				return "Namespace(" + namespace.name + ")";
			case PackageInternalNamespace:
				return "PackageInternalNamespace(\"" + namespace.name + "\")";
			case PackageNamespace:
				return "PackageNamespace(\"" + namespace.name + "\")";
			case PrivateNamespace:
				return "PrivateNamespace(\"" + namespace.name + "\")";
			case ProtectedNamespace:
				return "ProtectedNamespace(\"" + namespace.name + "\")";
			case StaticProtectedNamespace:
				return "StaticProtectedNamespace(\"" + namespace.name + "\")";
		}

		return "UnknownNAMESPACE()";
	}

	public static String toString( final NamespaceSet namespaceSet )
	{
		if( null == namespaceSet )
		{
			return "(NULL_NSSET)";
		}

		final StringBuilder builder = new StringBuilder( "NamespaceSet(" );

		final int n = namespaceSet.size();
		final int m = n - 1;

		for( int i = 0; i < n; ++i )
		{
			builder.append( toString( namespaceSet.get( i ) ) );

			if( i != m )
			{
				builder.append( ", " );
			}
		}

		builder.append( ")" );

		return builder.toString();
	}

	public static String toString( final Script script )
	{
		if( null == script )
		{
			return "(NULL_SCRIPT)";
		}

		return "Script()";
	}

	private StringConverter()
	{
	}
}
