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
import java.util.HashMap;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.multinames.Multiname;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.utils.IndentingPrintWriter;

/**
 * @author Joa Ebert
 * 
 */
public class InheritanceGraphPrinter
{
	private final IndentingPrintWriter output;

	public InheritanceGraphPrinter( final PrintWriter output )
	{
		this.output = new IndentingPrintWriter( output );
	}

	private void edge( final int from, final int to,
			final boolean isImplementation )
	{
		if( isImplementation )
		{
			output.println( Integer.toString( from ) + " -> "
					+ Integer.toString( to )
					+ " [arrowhead=\"dot\",color=\"grey\"];" );
		}
		else
		{
			output.println( Integer.toString( from ) + " -> "
					+ Integer.toString( to ) + ";" );
		}
	}

	public void print( final Abc abc )
	{
		final HashMap<QName, Integer> instanceMap = new HashMap<QName, Integer>(
				abc.instances.size() );

		int index = 0;

		output.println( "digraph G" );
		output.println( "{" );

		output.pushIndent();

		output.println( "size=\"128,128\";" );
		output.println( "ranksep=3;" );
		output.println( "ratio=auto;" );

		output.println( "graph[" );
		output.pushIndent();
		output.println( "rankdir=\"TB\"," );
		output.println( "splines=true," );
		output.println( "overlap=false" );
		output.popIndent();
		output.println( "];" );

		output.println( "node[" );
		output.pushIndent();
		output.println( "fontsize=\"9\"," );
		output.println( "fontname=\"Verdana\"," );
		output.println( "shape=\"box\"," );
		output.println( "style=\"filled\"," );
		output.println( "color=\"lightgrey\"" );
		output.popIndent();
		output.println( "];" );

		output.println( "edge[" );
		output.pushIndent();
		output.println( "fontsize=\"6\"," );
		output.println( "fontname=\"Verdana\"," );
		output.println( "arrowsize=\"0.5\"" );
		output.popIndent();
		output.println( "];" );

		for( final Instance instance : abc.instances )
		{
			if( null != instance.name
					&& !instanceMap.containsKey( instance.name ) )
			{
				vertex( instance.name, index );
				instanceMap.put( instance.name, index++ );
			}

			if( null != instance.base && instance.base instanceof QName
					&& !instanceMap.containsKey( instance.base ) )
			{
				vertex( (QName)instance.base, index );
				instanceMap.put( (QName)instance.base, index++ );
			}

			if( null != instance.interfaces )
			{
				for( final AbstractMultiname interfac : instance.interfaces )
				{
					final QName qname = toQName( abc, interfac );

					if( null != qname && !instanceMap.containsKey( qname ) )
					{
						vertex( qname, index );
						instanceMap.put( qname, index++ );
					}
				}
			}
		}

		for( final Instance instance : abc.instances )
		{
			if( null != instance.base && instance.base instanceof QName
					&& instanceMap.containsKey( instance.base ) )
			{
				edge( instanceMap.get( instance.name ), instanceMap
						.get( instance.base ), false );
			}

			if( null != instance.interfaces )
			{
				for( final AbstractMultiname interfac : instance.interfaces )
				{
					final QName qname = toQName( abc, interfac );

					if( null != qname && instanceMap.containsKey( qname ) )
					{
						edge( instanceMap.get( instance.name ), instanceMap
								.get( qname ), true );
					}
				}
			}
		}

		output.popIndent();
		output.println( "}" );
		output.flush();
	}

	private QName toQName( final Abc abc,
			final AbstractMultiname abstractMultiname )
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

				for( final Instance instance : abc.instances )
				{
					if( instance.name != null
							&& multiname.name.equals( instance.name.name )
							&& namespace.equals( instance.name.namespace ) )
					{
						return instance.name;
					}
				}
			}

			return null;
		}
		else
		{
			return null;
		}
	}

	private String toString( final QName name )
	{
		if( null == name )
		{
			return "(null)";
		}
		else
		{
			if( null == name.namespace )
			{
				return name.name;
			}
			else
			{
				return name.namespace.name + "::" + name.name;
			}
		}
	}

	private void vertex( final QName name, final int index )
	{
		output.println( Integer.toString( index ) + " [label=\""
				+ toString( name ) + "\"];" );
	}
}
