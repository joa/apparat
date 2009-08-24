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
import java.util.List;

import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.ConstantPool;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceSet;
import com.joa_ebert.apparat.utils.IndentingPrintWriter;

/**
 * @author Joa Ebert
 * 
 */
public final class ConstantPoolPrinter
{
	private final IndentingPrintWriter output;

	public ConstantPoolPrinter( final PrintWriter output )
	{
		this.output = new IndentingPrintWriter( output );
	}

	public void print( final ConstantPool pool )
	{
		output.println( "Constant Pool:" );
		output.pushIndent();
		printHeader( pool.intTable, "Int" );
		output.pushIndent();

		for( final Integer value : pool.intTable )
		{
			output.println( value.toString() );
		}

		output.popIndent();
		printHeader( pool.uintTable, "UInt" );
		output.pushIndent();

		for( final Long value : pool.uintTable )
		{
			output.println( value.toString() );
		}

		output.popIndent();
		printHeader( pool.doubleTable, "Double" );
		output.pushIndent();

		for( final Double value : pool.doubleTable )
		{
			output.println( value.toString() );
		}

		output.popIndent();
		printHeader( pool.stringTable, "String" );
		output.pushIndent();

		for( final String value : pool.stringTable )
		{
			output.println( "\"" + value + "\"" );
		}

		output.popIndent();
		printHeader( pool.namespaceTable, "Namespace" );
		output.pushIndent();

		for( final Namespace value : pool.namespaceTable )
		{
			output.println( StringConverter.toString( value ) );
		}

		output.popIndent();
		printHeader( pool.namespaceSetTable, "NamespaceSet" );
		output.pushIndent();

		for( final NamespaceSet value : pool.namespaceSetTable )
		{
			output.println( StringConverter.toString( value ) );
		}

		output.popIndent();
		printHeader( pool.multinameTable, "Multiname" );
		output.pushIndent();

		for( final AbstractMultiname value : pool.multinameTable )
		{
			output.println( StringConverter.toString( value ) );
		}

		output.popIndent();
		output.popIndent();
		output.flush();
	}

	private void printHeader( final List<?> list, final String name )
	{
		output.println( name + " table (" + list.size() + " total):" );
	}
}
