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

package com.joa_ebert.apparat.abc;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.multinames.Multiname;
import com.joa_ebert.apparat.abc.multinames.MultinameL;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.multinames.RTQName;
import com.joa_ebert.apparat.abc.multinames.Typename;
import com.joa_ebert.apparat.abc.utils.StringConverter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class ConstantPool
{
	public static final String EMPTY_STRING = "".intern();
	public static final Namespace ANY_NAMESPACE = new Namespace();
	public static final NamespaceSet EMPTY_NAMESPACESET = new NamespaceSet();
	public static final QName EMPTY_MULTINAME = new QName();

	private static <E> List<E> createList()
	{
		return new LinkedList<E>();
	}

	public List<Integer> intTable = createList();
	public List<Long> uintTable = createList();
	public List<Double> doubleTable = createList();
	public List<String> stringTable = createList();
	public List<Namespace> namespaceTable = createList();
	public List<NamespaceSet> namespaceSetTable = createList();
	public List<AbstractMultiname> multinameTable = createList();

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );
	}

	public boolean add( final AbstractMultiname value )
	{
		if( !multinameTable.contains( value ) )
		{
			switch( value.kind )
			{
				case Multiname:
				case MultinameA:
					add( ( (Multiname)value ).name );
					add( ( (Multiname)value ).namespaceSet );
					break;
				case MultinameL:
				case MultinameLA:
					add( ( (MultinameL)value ).namespaceSet );
					break;
				case QName:
				case QNameA:
					add( ( (QName)value ).name );
					add( ( (QName)value ).namespace );
					break;
				case RTQName:
				case RTQNameA:
					add( ( (RTQName)value ).name );
					break;
				case Typename:
					final Typename typename = (Typename)value;

					add( typename.name );

					for( final AbstractMultiname multiname : typename.parameters )
					{
						add( multiname );
					}
					break;

			}

			return multinameTable.add( value );
		}

		return false;
	}

	public boolean add( final ConstantType type, final Object value )
	{
		switch( type )
		{
			case Int:
				return 0 != getNonZeroIndex( (Integer)value );
			case UInt:
				return 0 != getNonZeroIndex( (Long)value );
			case Double:
				return 0 != getNonZeroIndex( (Double)value );
			case Utf8:
				return 0 != getNonZeroIndex( (String)value );
			case True:
			case False:
			case Null:
			case Undefined:
				return false;
			case Namespace:
			case PackageNamespace:
			case PackageInternalNamespace:
			case ProtectedNamespace:
			case ExplicitNamespace:
			case StaticProtectedNamespace:
			case PrivateNamespace:
				return 0 != getNonZeroIndex( (Namespace)value );
			default:
				return false;
		}
	}

	public boolean add( final Double value )
	{
		if( !doubleTable.contains( value ) )
		{
			return doubleTable.add( value );
		}

		return false;
	}

	public boolean add( final Integer value )
	{
		if( !intTable.contains( value ) )
		{
			return intTable.add( value );
		}

		return false;
	}

	public boolean add( final Long value )
	{
		if( !uintTable.contains( value ) )
		{
			return uintTable.add( value );
		}

		return false;
	}

	public boolean add( final Namespace value )
	{
		add( value.name );

		if( !namespaceTable.contains( value ) )
		{
			return namespaceTable.add( value );
		}

		return false;
	}

	public boolean add( final NamespaceSet value )
	{
		if( !namespaceSetTable.contains( value ) )
		{
			for( final Namespace namespace : value )
			{
				add( namespace );
			}

			return namespaceSetTable.add( value );
		}

		return false;
	}

	public boolean add( final String value )
	{
		if( value == EMPTY_STRING )
		{
			return false;
		}

		if( !stringTable.contains( value ) )
		{
			return stringTable.add( value );
		}

		return false;
	}

	public void clear()
	{
		intTable.clear();
		uintTable.clear();
		doubleTable.clear();
		stringTable.clear();
		namespaceTable.clear();
		namespaceSetTable.clear();
		multinameTable.clear();

		intTable.add( 0 );
		uintTable.add( 0L );
		doubleTable.add( Double.NaN );
		stringTable.add( EMPTY_STRING );
		namespaceTable.add( ANY_NAMESPACE );
		namespaceSetTable.add( EMPTY_NAMESPACESET );
		multinameTable.add( EMPTY_MULTINAME );

		//
		// We have to add this empty string here since the top-level package
		// for instance equals "" but not the EMPTY_STRING which is strange.
		// One could think that this might introduce an unused constant but
		// every ABC format will use it since also the public namespace equals
		// this string, private or protected. Nearly every namespace except
		// explicit ones.
		//

		stringTable.add( "" );
	}

	@Override
	public ConstantPool clone()
	{
		final ConstantPool result = new ConstantPool();

		result.intTable = new ArrayList<Integer>( intTable.size() );
		result.intTable.addAll( intTable );

		result.uintTable = new ArrayList<Long>( uintTable.size() );
		result.uintTable.addAll( uintTable );

		result.doubleTable = new ArrayList<Double>( doubleTable.size() );
		result.doubleTable.addAll( doubleTable );

		result.stringTable = new ArrayList<String>( stringTable.size() );
		result.stringTable.addAll( stringTable );

		result.namespaceTable = new ArrayList<Namespace>( namespaceTable.size() );
		result.namespaceTable.addAll( namespaceTable );

		result.namespaceSetTable = new ArrayList<NamespaceSet>(
				namespaceSetTable.size() );
		result.namespaceSetTable.addAll( namespaceSetTable );

		result.multinameTable = new ArrayList<AbstractMultiname>(
				multinameTable.size() );
		result.multinameTable.addAll( multinameTable );

		return result;
	}

	public void debug( final OutputStream output )
	{
		debug( new PrintWriter( output ) );
	}

	public void debug( final PrintWriter writer )
	{
		writer.write( "Constant Pool:\n" );

		writeList( writer, intTable, "Int" );

		for( final Integer value : intTable )
		{
			writer.write( "\t\t0x" + Integer.toHexString( value ) + "\n" );
		}

		writeList( writer, uintTable, "UInt" );

		for( final Long value : uintTable )
		{
			writer.write( "\t\t" + value.toString() + "\n" );
		}

		writeList( writer, doubleTable, "Double" );

		for( final Double value : doubleTable )
		{
			writer.write( "\t\t" + value.toString() + "\n" );
		}

		writeList( writer, stringTable, "String" );

		for( final String value : stringTable )
		{
			writer.write( "\t\t\"" + value + "\"\n" );
		}

		writeList( writer, namespaceTable, "Namespace" );

		for( final Namespace value : namespaceTable )
		{
			writer.write( "\t\t" + StringConverter.toString( value ) + "\n" );
		}

		writeList( writer, namespaceSetTable, "NamespaceSet" );

		for( final NamespaceSet value : namespaceSetTable )
		{
			writer.write( "\t\t" + StringConverter.toString( value ) + "\n" );
		}

		writeList( writer, multinameTable, "Multiname" );

		for( final AbstractMultiname value : multinameTable )
		{
			writer.write( "\t\t" + StringConverter.toString( value ) + "\n" );
		}

		writer.write( "\n" );

		writer.flush();
	}

	public Object getConstantValue( final ConstantType type, final int index )
			throws AbcException
	{
		switch( type )
		{
			case Int:
				return getInt( index );
			case UInt:
				return getUInt( index );
			case Double:
				return getDouble( index );
			case Utf8:
				return getString( index );
			case True:
				return Boolean.TRUE;
			case False:
				return Boolean.FALSE;
			case Null:
				return null;
			case Undefined:
				return null;
			case Namespace:
			case PackageNamespace:
			case PackageInternalNamespace:
			case ProtectedNamespace:
			case ExplicitNamespace:
			case StaticProtectedNamespace:
			case PrivateNamespace:
				return getNamespace( index );
			default:
				throw new AbcException();
		}
	}

	public Double getDouble( final int index )
	{
		return doubleTable.get( index );
	}

	public int getIndex( final AbstractMultiname value )
	{
		if( EMPTY_MULTINAME == value )
		{
			return 0;
		}

		final int n = multinameTable.size();

		for( int i = 1; i < n; ++i )
		{
			if( multinameTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		multinameTable.add( value );

		return n;
	}

	public int getIndex( final ConstantType type, final Object value )
	{
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

		switch( type )
		{
			case Int:
				return getNonZeroIndex( (Integer)value );
			case UInt:
				return getNonZeroIndex( (Long)value );
			case Double:
				return getNonZeroIndex( (Double)value );
			case Utf8:
				return getNonZeroIndex( (String)value );
			case True:
				return type.getByte();
			case False:
				return type.getByte();
			case Null:
				return type.getByte();
			case Undefined:
				return type.getByte();
			case Namespace:
			case PackageNamespace:
			case PackageInternalNamespace:
			case ProtectedNamespace:
			case ExplicitNamespace:
			case StaticProtectedNamespace:
			case PrivateNamespace:
				return getNonZeroIndex( (Namespace)value );
			default:
				return 0xff;
		}
	}

	public int getIndex( final Double value )
	{
		return getIndex( value, 0 );
	}

	private int getIndex( final Double value, final int from )
	{
		final int n = doubleTable.size();

		for( int i = from; i < n; ++i )
		{
			if( doubleTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		doubleTable.add( value );

		return n;
	}

	public int getIndex( final Integer value )
	{
		return getIndex( value, 0 );
	}

	public int getIndex( final Integer value, final int from )
	{
		final int n = intTable.size();

		for( int i = from; i < n; ++i )
		{
			if( intTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		intTable.add( value );

		return n;
	}

	public int getIndex( final Long value )
	{
		return getIndex( value, 0 );
	}

	public int getIndex( final Long value, final int from )
	{
		final int n = uintTable.size();

		for( int i = from; i < n; ++i )
		{
			if( uintTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		uintTable.add( value );

		return n;
	}

	public int getIndex( final Namespace value )
	{
		if( ANY_NAMESPACE == value )
		{
			return 0;
		}

		final int n = namespaceTable.size();

		for( int i = 1; i < n; ++i )
		{
			if( namespaceTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		namespaceTable.add( value );

		return n;
	}

	public int getIndex( final NamespaceSet value )
	{
		if( EMPTY_NAMESPACESET == value )
		{
			return 0;
		}

		final int n = namespaceSetTable.size();

		for( int i = 1; i < n; ++i )
		{
			if( namespaceSetTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		namespaceSetTable.add( value );

		return n;
	}

	public int getIndex( final String value )
	{
		if( EMPTY_STRING == value )
		{
			return 0;
		}

		final int n = stringTable.size();

		for( int i = 1; i < n; ++i )
		{
			if( stringTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		stringTable.add( value );

		return n;
	}

	public Integer getInt( final int index )
	{
		return intTable.get( index );
	}

	public AbstractMultiname getMultiname( final int index )
	{
		return multinameTable.get( index );
	}

	public Namespace getNamespace( final int index )
	{
		return namespaceTable.get( index );
	}

	public NamespaceSet getNamespaceSet( final int index )
	{
		return namespaceSetTable.get( index );
	}

	public int getNonZeroIndex( final Double value )
	{
		return getIndex( value, 1 );
	}

	public int getNonZeroIndex( final Integer value )
	{
		return getIndex( value, 1 );
	}

	public int getNonZeroIndex( final Long value )
	{
		return getIndex( value, 1 );
	}

	public int getNonZeroIndex( final Namespace value )
	{
		final int n = namespaceTable.size();

		for( int i = 1; i < n; ++i )
		{
			if( namespaceTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		namespaceTable.add( value );

		return n;
	}

	public int getNonZeroIndex( final String value )
	{
		final int n = stringTable.size();

		for( int i = 1; i < n; ++i )
		{
			if( stringTable.get( i ).equals( value ) )
			{
				return i;
			}
		}

		stringTable.add( value );

		return n;
	}

	public String getString( final int index )
	{
		return stringTable.get( index );
	}

	public Long getUInt( final int index )
	{
		return uintTable.get( index );
	}

	public void optimize()
	{
	}

	public void removeAll( final ConstantPool pool )
	{
		subtract( intTable, pool.intTable );
		subtract( uintTable, pool.uintTable );
		subtract( doubleTable, pool.doubleTable );
		subtract( stringTable, pool.stringTable );
		subtract( namespaceTable, pool.namespaceTable );
		subtract( namespaceSetTable, pool.namespaceSetTable );
		subtract( multinameTable, pool.multinameTable );
	}

	private void subtract( final List<?> a, final List<?> b )
	{
		//
		// a -= b
		//

		if( a.isEmpty() || b.isEmpty() )
		{
			return;
		}

		for( final Object inB : b )
		{
			int n = a.size();

			while( --n > -1 )
			{
				if( a.get( n ).equals( inB ) )
				{
					a.remove( n );
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "[ConstantPool]";
	}

	private void writeList( final PrintWriter writer, final List<?> list,
			final String name )
	{
		writer.write( "\t" + name + " table (" + list.size() + " entries):\n" );
	}
}
