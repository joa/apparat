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

import java.io.PrintWriter;
import java.text.Collator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.AbstractTrait;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.ConstantType;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.Parameter;
import com.joa_ebert.apparat.abc.TraitKind;
import com.joa_ebert.apparat.abc.multinames.Multiname;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.multinames.Typename;
import com.joa_ebert.apparat.abc.traits.TraitConst;
import com.joa_ebert.apparat.abc.traits.TraitGetter;
import com.joa_ebert.apparat.abc.traits.TraitMethod;
import com.joa_ebert.apparat.abc.traits.TraitSetter;
import com.joa_ebert.apparat.abc.traits.TraitSlot;
import com.joa_ebert.apparat.utils.IndentingPrintWriter;

/**
 * @author Joa Ebert
 * 
 */
public class UMLGraphPrinter
{
	private final IndentingPrintWriter output;

	public UMLGraphPrinter( final PrintWriter output )
	{
		this.output = new IndentingPrintWriter( output );
	}

	private void buildIndices( final HashMap<QName, Integer> instanceMap,
			final Abc abc, final int startIndex )
	{
		int currentIndex = startIndex;

		for( final Instance instance : abc.instances )
		{
			if( null == instance.name )
			{
				continue;
			}

			instanceMap.put( instance.name, currentIndex++ );
		}
	}

	private void buildPackages( final HashMap<String, List<Instance>> packages,
			final Abc abc )
	{
		for( final Instance instance : abc.instances )
		{
			if( null == instance.name )
			{
				continue;
			}

			if( !packages.containsKey( instance.name.namespace.name ) )
			{
				packages.put( instance.name.namespace.name,
						new LinkedList<Instance>() );
			}

			final List<Instance> instances = packages
					.get( instance.name.namespace.name );

			instances.add( instance );
		}
	}

	private void epilog()
	{
		output
				.println( "label=\"Powered by Apparat - http://apparat.googlecode.com/\";" );
		output.popIndent();
		output.println( "}" );
		output.flush();
	}

	private String getParameters( final Method method )
	{
		final StringBuilder buffer = new StringBuilder();

		final int n = method.parameters.size();
		final int m = n - 1;

		for( int i = 0; i < n; ++i )
		{
			final Parameter param = method.parameters.get( i );

			if( null != param.name && param.name != "" )
			{
				buffer.append( param.name );
			}
			else
			{
				buffer.append( "p" + i );
			}

			buffer.append( ": " );
			buffer.append( getType( param.type ) );

			if( param.isOptional )
			{
				buffer.append( " = "
						+ getValue( param.optionalType, param.optionalValue ) );
			}

			if( i != m )
			{
				buffer.append( ", " );
			}
		}

		return buffer.toString();
	}

	private QName getQName( final AbstractMultiname abstractMultiname )
	{
		if( abstractMultiname instanceof QName )
		{
			return (QName)abstractMultiname;
		}

		if( abstractMultiname instanceof Multiname )
		{
			final Multiname multiname = (Multiname)abstractMultiname;

			if( multiname.namespaceSet.size() == 1 )
			{
				final Namespace namespace = multiname.namespaceSet.get( 0 );

				return new QName( namespace, multiname.name );
			}

			return null;
		}
		else
		{
			return null;
		}
	}

	private String getType( final AbstractMultiname type )
	{
		switch( type.kind )
		{
			case QName:
				return ( (QName)type ).name;

			case Typename:
				final Typename typename = (Typename)type;
				return typename.name.name + ".\\<"
						+ getType( typename.parameters.get( 0 ) ) + "\\>";

			default:
				return "*";
		}
	}

	private String getValue( final ConstantType type, final Object value )
	{
		switch( type )
		{
			case Double:
				return ( (Double)value ).toString();

			case False:
				return "false";

			case Int:
				return ( (Integer)value ).toString();

			case Null:
				return "null";

			case True:
				return "true";

			case UInt:
				return ( (Long)value ).toString();

			case Undefined:
				return "undefined";

			case Utf8:
				return ( (String)value ).toString();
				// TODO add namespaces

			default:
				return "?";
		}
	}

	private String getVisibility( final QName name )
	{
		switch( name.namespace.kind )
		{
			case PackageInternalNamespace:
				return "~ ";

			case PackageNamespace:
				return "+ ";

			case PrivateNamespace:
				return "- ";

			case ProtectedNamespace:
				return "# ";

			default:
				return "  ";
		}
	}

	public void print( final Abc abc )
	{
		final HashMap<QName, Integer> indexMap = new LinkedHashMap<QName, Integer>(
				abc.instances.size() );
		final HashMap<String, List<Instance>> packages = new LinkedHashMap<String, List<Instance>>();

		buildIndices( indexMap, abc, 0 );
		buildPackages( packages, abc );

		prolog();
		printPackages( packages, indexMap, abc );
		printInheritance( indexMap, abc );
		epilog();
	}

	private void printInheritance( final HashMap<QName, Integer> indexMap,
			final Abc abc )
	{
		output.println( "edge[style=\"solid\"];" );

		for( final Instance instance : abc.instances )
		{
			if( instance.name == null || instance.base == null )
			{
				continue;
			}

			final Integer base = indexMap.get( instance.base );

			if( null != base )
			{
				output.println( indexMap.get( instance.name ) + " -> " + base
						+ ";" );
			}
		}

		output.println( "edge[style=\"dashed\"];" );

		for( final Instance instance : abc.instances )
		{
			if( instance.name == null || instance.interfaces == null
					|| instance.interfaces.size() == 0 )
			{
				continue;
			}

			final Integer inst = indexMap.get( instance.name );

			for( final AbstractMultiname interfaceName : instance.interfaces )
			{
				final QName qname = getQName( interfaceName );

				if( null != qname )
				{
					final Integer iface = indexMap.get( qname );

					if( null != iface )
					{
						output.println( inst + " -> " + iface + ";" );
					}
				}
			}
		}
	}

	private void printInstances( final List<Instance> instances,
			final HashMap<QName, Integer> indexMap, final Abc abc )
	{
		for( final Instance instance : instances )
		{
			if( null == instance.name )
			{
				continue;
			}

			final StringBuilder buffer = new StringBuilder();

			final List<String> properties = new LinkedList<String>();
			final List<String> methods = new LinkedList<String>();

			final Map<String, Boolean> visitedMap = new LinkedHashMap<String, Boolean>();

			final Class klass = instance.klass;

			for( final AbstractTrait trait : klass.traits )
			{
				switch( trait.kind )
				{
					case Const:
						final TraitConst traitConst = (TraitConst)trait;

						properties.add( "$ "
								+ getVisibility( traitConst.name )
								+ traitConst.name.name
								+ ": "
								+ getType( traitConst.type )
								+ ( traitConst.value == null ? "" : " = "
										+ getValue( traitConst.valueType,
												traitConst.value ) )
								+ " \\{read-only\\}" );
						break;

					case Slot:
						final TraitSlot traitSlot = (TraitSlot)trait;
						properties.add( "$ "
								+ getVisibility( traitSlot.name )
								+ traitSlot.name.name
								+ ": "
								+ getType( traitSlot.type )
								+ ( traitSlot.value == null ? "" : " = "
										+ getValue( traitSlot.valueType,
												traitSlot.value ) ) );
						break;

					case Method:
						final TraitMethod traitMethod = (TraitMethod)trait;

						methods
								.add( "$ "
										+ getVisibility( traitMethod.name )
										+ traitMethod.name.name
										+ "("
										+ getParameters( traitMethod.method )
										+ "): "
										+ getType( traitMethod.method.returnType )
										+ ( traitMethod.isOverride ? " \\{redefines "
												+ traitMethod.name.name + "\\}"
												: "" )
										+ ( traitMethod.isFinal ? " \\{final\\}"
												: "" ) );
						break;

					case Getter:
					{
						final TraitGetter traitGetter = (TraitGetter)trait;

						final Boolean visited = visitedMap
								.get( traitGetter.name.name );

						if( null != visited && visited )
						{
							break;
						}

						boolean hasSetter = false;

						for( final AbstractTrait search : instance.traits )
						{
							if( search.kind == TraitKind.Setter )
							{
								if( ( (TraitSetter)search ).name
										.equals( traitGetter.name ) )
								{
									hasSetter = true;
									break;
								}
							}
						}

						properties.add( "$ "
								+ getVisibility( traitGetter.name )
								+ traitGetter.name.name
								+ ": "
								+ getType( traitGetter.method.returnType )
								+ ( traitGetter.isOverride ? " \\{redefines "
										+ traitGetter.name.name + "\\}" : "" )
								+ ( traitGetter.isFinal ? " \\{final\\}" : "" )
								+ ( !hasSetter ? " \\{read-only\\}" : "" ) );

						visitedMap.put( traitGetter.name.name, Boolean.TRUE );

						break;
					}

					case Setter:
					{
						final TraitSetter traitSetter = (TraitSetter)trait;

						final Boolean visited = visitedMap
								.get( traitSetter.name.name );

						if( null != visited && visited )
						{
							break;
						}

						boolean hasGetter = false;

						for( final AbstractTrait search : instance.traits )
						{
							if( search.kind == TraitKind.Getter )
							{
								if( ( (TraitGetter)search ).name
										.equals( traitSetter.name ) )
								{
									hasGetter = true;
									break;
								}
							}
						}

						properties.add( "$ "
								+ getVisibility( traitSetter.name )
								+ traitSetter.name.name
								+ ": "
								+ getType( traitSetter.method.parameters
										.get( 0 ).type )
								+ ( traitSetter.isOverride ? " \\{redefines "
										+ traitSetter.name.name + "\\}" : "" )
								+ ( traitSetter.isFinal ? " \\{final\\}" : "" )
								+ ( !hasGetter ? " \\{write-only\\}" : "" ) );

						visitedMap.put( traitSetter.name.name, Boolean.TRUE );

						break;
					}
				}
			}

			visitedMap.clear();

			for( final AbstractTrait trait : instance.traits )
			{
				switch( trait.kind )
				{
					case Const:
						final TraitConst traitConst = (TraitConst)trait;

						properties.add( getVisibility( traitConst.name )
								+ traitConst.name.name
								+ ": "
								+ getType( traitConst.type )
								+ ( traitConst.value == null ? "" : " = "
										+ getValue( traitConst.valueType,
												traitConst.value ) )
								+ " \\{read-only\\}" );
						break;

					case Slot:
						final TraitSlot traitSlot = (TraitSlot)trait;
						properties.add( getVisibility( traitSlot.name )
								+ traitSlot.name.name
								+ ": "
								+ getType( traitSlot.type )
								+ ( traitSlot.value == null ? "" : " = "
										+ getValue( traitSlot.valueType,
												traitSlot.value ) ) );
						break;

					case Method:
						final TraitMethod traitMethod = (TraitMethod)trait;

						methods
								.add( getVisibility( traitMethod.name )
										+ traitMethod.name.name
										+ "("
										+ getParameters( traitMethod.method )
										+ "): "
										+ getType( traitMethod.method.returnType )
										+ ( traitMethod.isOverride ? " \\{redefines "
												+ traitMethod.name.name + "\\}"
												: "" )
										+ ( traitMethod.isFinal ? " \\{final\\}"
												: "" ) );
						break;

					case Getter:
					{
						final TraitGetter traitGetter = (TraitGetter)trait;

						final Boolean visited = visitedMap
								.get( traitGetter.name.name );

						if( null != visited && visited )
						{
							break;
						}

						boolean hasSetter = false;

						for( final AbstractTrait search : instance.traits )
						{
							if( search.kind == TraitKind.Setter )
							{
								if( ( (TraitSetter)search ).name
										.equals( traitGetter.name ) )
								{
									hasSetter = true;
									break;
								}
							}
						}

						properties.add( getVisibility( traitGetter.name )
								+ traitGetter.name.name
								+ ": "
								+ getType( traitGetter.method.returnType )
								+ ( traitGetter.isOverride ? " \\{redefines "
										+ traitGetter.name.name + "\\}" : "" )
								+ ( traitGetter.isFinal ? " \\{final\\}" : "" )
								+ ( !hasSetter ? " \\{read-only\\}" : "" ) );

						visitedMap.put( traitGetter.name.name, Boolean.TRUE );

						break;
					}

					case Setter:
					{
						final TraitSetter traitSetter = (TraitSetter)trait;

						final Boolean visited = visitedMap
								.get( traitSetter.name.name );

						if( null != visited && visited )
						{
							break;
						}

						boolean hasGetter = false;

						for( final AbstractTrait search : instance.traits )
						{
							if( search.kind == TraitKind.Getter )
							{
								if( ( (TraitGetter)search ).name
										.equals( traitSetter.name ) )
								{
									hasGetter = true;
									break;
								}
							}
						}

						properties.add( getVisibility( traitSetter.name )
								+ traitSetter.name.name
								+ ": "
								+ getType( traitSetter.method.parameters
										.get( 0 ).type )
								+ ( traitSetter.isOverride ? " \\{redefines "
										+ traitSetter.name.name + "\\}" : "" )
								+ ( traitSetter.isFinal ? " \\{final\\}" : "" )
								+ ( !hasGetter ? " \\{write-only\\}" : "" ) );

						visitedMap.put( traitSetter.name.name, Boolean.TRUE );

						break;
					}
				}
			}

			Collections.sort( properties, Collator.getInstance() );
			Collections.sort( methods, Collator.getInstance() );

			//
			// Head
			//

			if( instance.isInterface )
			{
				buffer.append( "\\<\\<interface\\>\\>\\n" );
			}

			buffer.append( instance.name.name );

			if( instance.isFinal )
			{
				buffer.append( "\\n\\{final\\}" );
			}

			//
			//
			//

			if( !instance.isInterface )
			{
				buffer.append( "|" );

				//
				// Properties
				//

				for( final String property : properties )
				{
					buffer.append( property );
					buffer.append( "\\l" );
				}
			}

			//
			//
			//

			buffer.append( "|" );

			//
			// Methods
			//

			for( final String method : methods )
			{
				buffer.append( method );
				buffer.append( "\\l" );
			}

			//
			// Output:
			//

			output.println( indexMap.get( instance.name ) + " [label=\"{"
					+ buffer.toString() + "}\"];" );
		}
	}

	private void printPackages( final HashMap<String, List<Instance>> packages,
			final HashMap<QName, Integer> indexMap, final Abc abc )
	{
		final Set<Entry<String, List<Instance>>> entrySet = packages.entrySet();

		int packageIndex = 0;

		for( final Entry<String, List<Instance>> entry : entrySet )
		{
			final String namespace = entry.getKey();
			final List<Instance> instances = entry.getValue();

			final String packageName = namespace.length() == 0 ? "TopLevel"
					: namespace;

			output.println( "subgraph clusterP" + packageIndex++ + "{" );
			output.pushIndent();

			printInstances( instances, indexMap, abc );

			output.println( "label=\"" + packageName + "\";" );

			output.popIndent();
			output.println( "}" );
		}
	}

	private void prolog()
	{
		final String fontName = "Bitstream Vera Sans Mono";
		final int fontSize = 8;

		output.println( "digraph G {" );
		output.pushIndent();

		output.println( "fontname=\"" + fontName + "\";" );
		output.println( "fontsize=" + fontSize + ";" );
		output.println( "ranksep=2;" );
		output.println( "ratio=auto;" );

		output.println( "graph[" );
		output.pushIndent();

		output.println( "rankdir= \"TB\"," );
		output.println( "splines= true," );
		output.println( "overlap= false" );

		output.popIndent();
		output.println( "];" );

		output.println( "node[" );
		output.pushIndent();

		output.println( "fontname=\"" + fontName + "\"," );
		output.println( "fontsize=" + fontSize + "," );
		output.println( "shape=\"record\"" );

		output.popIndent();
		output.println( "];" );

		output.println( "edge[" );
		output.pushIndent();

		output.println( "fontname=\"" + fontName + "\"," );
		output.println( "fontsize=" + fontSize + "," );
		output.println( "arrowhead=\"empty\"" );

		output.popIndent();
		output.println( "];" );
	}
}
