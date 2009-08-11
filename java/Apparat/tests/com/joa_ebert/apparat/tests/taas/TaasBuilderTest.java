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

package com.joa_ebert.apparat.tests.taas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.controlflow.BasicBlock;
import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.taas.TaasBuilder;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.toolkit.livenessAnalysis.LivenessAnalysis;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasBuilderTest
{
	@Test
	@Ignore
	public void testBuilder() throws Exception
	{
		final Abc builtin = new Abc();
		builtin.read( "assets/builtin.abc" );

		final Abc toplevel = new Abc();
		toplevel.read( "assets/toplevel.abc" );

		final Swc playerGlobalSwc = new Swc();

		playerGlobalSwc.read( "assets/playerglobal.swc" );

		final Swf playerGlobalSwf = new Swf();
		playerGlobalSwf.read(
				new ByteArrayInputStream( playerGlobalSwc.library ),
				playerGlobalSwc.library.length );

		final List<Abc> playerglobal = new LinkedList<Abc>();

		for( final ITag tag : playerGlobalSwf.tags )
		{
			if( tag.getType() == Tags.DoABC )
			{
				final Abc temp = new Abc();

				temp
						.read( new ByteArrayInputStream(
								( (DoABCTag)tag ).abcData ) );

				playerglobal.add( temp );
			}
		}

		Assert.assertFalse( playerglobal.isEmpty() );

		final Swf test = new Swf();
		test.read( "assets/lorenz.swf" );

		final Abc custom = new Abc();

		for( final ITag tag : test.tags )
		{
			if( tag.getType() == Tags.DoABC )
			{
				final DoABCTag doAbc = (DoABCTag)tag;

				custom.read( new ByteArrayInputStream( doAbc.abcData ) );

				break;
			}
		}

		final AbcEnvironment env = new AbcEnvironment( new Abc[] {
				builtin, toplevel, custom
		} );

		env.addAll( playerglobal );

		final TaasBuilder builder = new TaasBuilder();

		int i = 0;

		for( final Method m : custom.methods )
		{
			final Bytecode bytecode = m.body.code;

			try
			{
				final TaasMethod method = builder.build( env, bytecode );
				LivenessAnalysis la = null;

				try
				{
					la = new LivenessAnalysis( method );
					la.solve();
				}
				catch( final TaasException e )
				{
					e.printStackTrace();
					System.out.println( method.code.dump() );
					Assert.fail( e.getMessage() );
				}

				final File f = new File( "assets/taas" + Integer.toString( i++ )
						+ ".txt" );
				final FileOutputStream fos = new FileOutputStream( f );
				final Writer printWriter = new PrintWriter( fos );
				printWriter.write( builder.getCode().dump() );
				printWriter.flush();
				printWriter.write( "\n\n" );
				printWriter.flush();
				new BytecodePrinter( fos ).interpret( env, bytecode );
				printWriter.flush();
				printWriter.write( "\n\n" );
				for( final BasicBlock<TaasVertex> block : la.getGraph()
						.vertexList() )
				{
					printWriter.write( block.toString() + "\n" );
					printWriter.write( "IN:  " + la.liveIn( block ).toString()
							+ "\n" );
					printWriter.write( "OUT: " + la.liveOut( block ).toString()
							+ "\n\n" );
				}
				printWriter.write( "\n\n" );
				printWriter.flush();
				printWriter.close();
			}
			catch( final TaasException ex )
			{
				( new BytecodePrinter( System.out ) ).interpret( env, bytecode );

				ex.printStackTrace();
				Assert.fail( ex.getMessage() );
			}
			catch( final Throwable t )
			{
				t.printStackTrace();
				Assert.fail( t.getMessage() );
			}
		}

		// final Method method = new Method();
		// final MethodBody body = new MethodBody();
		// final Bytecode code = new Bytecode();
		//
		// method.hasOptionalParameters = false;
		// method.hasParameterNames = false;
		// method.name = "";
		// method.needsActivation = false;
		// method.needsArguments = false;
		// method.needsRest = false;
		// method.parameters = new LinkedList<Parameter>();
		// method.returnType = new QName( new Namespace(
		// NamespaceKind.PackageNamespace, "" ), "int" );
		// method.setsDXNS = false;
		// method.body = body;
		//
		// body.exceptions = new LinkedList<ExceptionHandler>();
		// body.initScopeDepth = 0;
		// body.maxScopeDepth = 1;
		// body.localCount = 0;
		// body.maxStack = 3;
		// body.method = method;
		// body.traits = new LinkedList<AbstractTrait>();
		// body.code = code;
		//
		// code.method = method;
		// code.methodBody = body;
		//
		// final MarkerManager markers = code.markers;
		//
		// final IfTrue it = new IfTrue();
		// final Jump jmp = new Jump();
		// final Add add = new Add();
		// final PushDouble pd = new PushDouble( 3.0 );
		//
		// it.marker = markers.mark( pd );
		// jmp.marker = markers.mark( add );

		// code.add( new PushDouble( 1.0 ) );
		// code.add( new PushDouble( 1.0 ) );
		// code.add( new Add() );
		// code.add( new ReturnValue() );

		// code.add( new PushDouble( 1.0 ) );
		// code.add( new PushTrue() );
		// code.add( it );
		// code.add( new PushDouble( 2.0 ) );
		// code.add( jmp );
		// code.add( pd );
		// code.add( add );
		// code.add( new ReturnValue() );

		// final TaasBuilder builder = new TaasBuilder();

		// builder.build( new AbcEnvironment(), code );
		//
		// new BytecodePrinter( System.out )
		// .interpret( new AbcEnvironment(), code );
		//
		// final File f = new File( "assets/taas.txt" );
		// final FileOutputStream fos = new FileOutputStream( f );
		// final Writer printWriter = new PrintWriter( fos );
		// printWriter.write( builder.getCode().dump() );
		// printWriter.flush();
		// printWriter.close();
	}
}
