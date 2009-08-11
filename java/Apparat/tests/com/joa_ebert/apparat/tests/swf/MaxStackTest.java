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

package com.joa_ebert.apparat.tests.swf;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.IMethodVisitor;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.bytecode.analysis.DeadCodeElimination;
import com.joa_ebert.apparat.abc.io.AbcInputStream;
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
public class MaxStackTest
{

	private static byte[] abcData;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		final Swf swf = new Swf();

		swf.read( "assets/onefunction.swf" );

		final ITagVisitor visitor = new ITagVisitor()
		{
			public void visit( final ITag tag )
			{
				if( tag.getType() == Tags.DoABC )
				{
					abcData = ( (DoABCTag)tag ).abcData;
				}
			}
		};

		swf.accept( visitor );
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		abcData = null;
	}

	private AbcInputStream input;

	@Before
	public void setUp() throws Exception
	{
		input = new AbcInputStream( new ByteArrayInputStream( abcData ) );
	}

	@After
	public void tearDown() throws Exception
	{
		input.close();

		input = null;
	}

	@Test
	public void testRead() throws Exception
	{
		final Abc abc = new Abc();

		final FileOutputStream fos = new FileOutputStream( "assets/log.txt" );

		abc.read( input );
		abc.accept( new DeadCodeElimination() );
		abc.accept( new IMethodVisitor()
		{
			public void visit( final AbcContext context, final Method method )
			{
				if( null == method.body )
				{
					return;
				}

				// System.out.println( StringConverter.toString( method ) );

				// final StackAnalyzer calc = new StackAnalyzer();

				// final IInterpreter pat = new DecrementIntPattern();
				// pat.interpret( new AbcEnvironment( context ),
				// method.body.code );
				//
				// final IInterpreter pat2 = new IntegerCalculus();
				// pat2
				// .interpret( new AbcEnvironment( context ),
				// method.body.code );
				//
				// final IInterpreter pat3 = new NamespaceSetCleanup();
				// pat3
				// .interpret( new AbcEnvironment( context ),
				// method.body.code );

				final BytecodePrinter printer = new BytecodePrinter( fos );

				final PrintWriter pw = new PrintWriter( fos );
				pw
						.write( "\n--------------------------------------------------------\n" );
				pw.flush();
				printer.interpret( new AbcEnvironment( context ),
						method.body.code );
				// calc
				// .interpret( new AbcEnvironment( context ),
				// method.body.code );
				//				

				// final ControlFlowGraphBuilder builder = new
				// ControlFlowGraphBuilder();
				//
				// builder.interpret( new AbcEnvironment( context ),
				// method.body.code );
				//
				// new DOTExporter<BytecodeVertex, Edge<BytecodeVertex>>(
				// new BytecodeVertex.LabelProvider() ).export(
				// System.out, builder.getGraph() );
				//
				// System.out.println( "\n" );
			}
		} );

		// abc.write( "assets/output.swf" );
		//
		// final Abc abc2 = new Abc();
		// abc2.read( "assets/output.swf" );
		// abc2.accept( new IMethodVisitor()
		// {
		// public void visit( final AbcContext context, final Method method )
		// {
		// if( null == method.body )
		// {
		// return;
		// }
		//
		// final int maxStackBefore = method.body.maxStack;
		// final StackAnalyzer calc = new StackAnalyzer();
		// final BytecodePrinter printer = new BytecodePrinter( System.out );
		//
		// printer.interpret( new AbcEnvironment(), method.body.code );
		// calc.interpret( new AbcEnvironment(), method.body.code );
		//
		// System.out.println( maxStackBefore );
		// }
		// } );

	}
}
