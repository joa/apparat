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

package com.joa_ebert.apparat.tests.asbridge;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.bytecode.analysis.IInterpreter;
import com.joa_ebert.apparat.abc.bytecode.asbridge.BytecodeInlineJob;
import com.joa_ebert.apparat.abc.bytecode.asbridge.MemoryInlineJob;
import com.joa_ebert.apparat.abc.io.AbcInputStream;
import com.joa_ebert.apparat.abc.utils.StringConverter;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class BytecodeTest
{
	private static DoABCTag abcTag;
	private static Swf swf;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		swf = new Swf();
		swf.read( "assets/Underflow.swf" );

		final ITagVisitor visitor = new ITagVisitor()
		{
			public void visit( final ITag tag )
			{
				if( tag.getType() == Tags.DoABC )
				{
					abcTag = (DoABCTag)tag;
				}
			}
		};

		swf.accept( visitor );
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		swf.write( "assets/Underflow_.swf" );
	}

	@Test
	public void testInjection() throws Exception
	{
		final Abc abc = new Abc();

		abc
				.read( new AbcInputStream( new ByteArrayInputStream(
						abcTag.abcData ) ) );

		final AbcEnvironment abcEnvironment = new AbcEnvironment(
				new AbcContext( abc ) );

		final PrintStream printStream = new PrintStream( new FileOutputStream(
				"debug/inline.txt" ) );

		final IInterpreter memInterp = new MemoryInlineJob();
		final IInterpreter bytecodeInterp = new BytecodeInlineJob();
		final IInterpreter printer = new BytecodePrinter( printStream );

		for( final Method method : abc.methods )
		{
			if( null == method.body )
			{
				continue;
			}

			printStream.println( StringConverter.toString( method ) );
			printStream.println( "\tBefore:" );
			printer.interpret( abcEnvironment, method.body.code );
			bytecodeInterp.interpret( abcEnvironment, method.body.code );
			memInterp.interpret( abcEnvironment, method.body.code );
			printStream.println( "\tAfter:" );
			printer.interpret( abcEnvironment, method.body.code );
		}

		// abc.constantPool.debug( System.out );

		try
		{
			abc.write( abcTag );
		}
		catch( final Exception e )
		{
			e.printStackTrace();
			throw e;
		}
	}
}
