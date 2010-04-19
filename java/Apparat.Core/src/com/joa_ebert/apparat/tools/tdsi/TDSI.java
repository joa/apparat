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

package com.joa_ebert.apparat.tools.tdsi;

import java.io.ByteArrayInputStream;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.bytecode.analysis.DeadCodeElimination;
import com.joa_ebert.apparat.abc.bytecode.analysis.IInterpreter;
import com.joa_ebert.apparat.abc.bytecode.analysis.InheritanceAnalyzer;
import com.joa_ebert.apparat.abc.bytecode.asbridge.BytecodeInlineJob;
import com.joa_ebert.apparat.abc.bytecode.asbridge.MemoryInlineJob;
import com.joa_ebert.apparat.abc.bytecode.permutations.DecrementIntPattern;
import com.joa_ebert.apparat.abc.bytecode.permutations.GetLexPattern;
import com.joa_ebert.apparat.abc.bytecode.permutations.IBytecodePermutation;
import com.joa_ebert.apparat.abc.bytecode.permutations.IncrementIntPattern;
import com.joa_ebert.apparat.abc.bytecode.permutations.IntegerCalculus;
import com.joa_ebert.apparat.abc.bytecode.permutations.NamespaceSetCleanup;
import com.joa_ebert.apparat.abc.bytecode.permutations.PermutationChain;
import com.joa_ebert.apparat.abc.io.AbcInputStream;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.tools.ITool;
import com.joa_ebert.apparat.tools.IToolConfiguration;
import com.joa_ebert.apparat.tools.ToolLog;
import com.joa_ebert.apparat.tools.ToolRunner;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TDSI implements ITool, ITagVisitor
{
	public static void main( final String[] arguments )
	{
		final ToolRunner toolRunner = new ToolRunner( new TDSI(), arguments );

		toolRunner.run();
	}

	private boolean inlineMemory = true;
	private boolean inlineBytecode = true;
	private boolean bytecodePermutations = true;
	private boolean integerCalculus = false;
	private boolean finalize = false;
	private boolean deadCodeElimination = false;

	private IToolConfiguration config;

	public String getHelp()
	{
		return "-input [file]\t\t\t\tThe input file to read.\n"
				+ "-output [file]\t\t\t\tThe resulting output file.\n\n"
				+ "-inline-bytecode [true|false]\t\tWhether or not __bytecode will be inlined.\n"
				+ "-integer-calculus [true|false]\t\tUse integer calculus when possible.\n"
				+ "-bytecode-permutations [true|false]\tBytecode permutations except integer calculus.\n"
				+ "-inline-memory [true|false]\t\tWhether or not Memory will be inlined.\n"
				+ "-finalize [true|false]\t\tMark classes final if possible.\n"
				+ "-dead-code-elimination [true|false]\tPeforms dead code elimination for every method.";
	}

	public String getName()
	{
		return "TurboDieselSportInjection";
	}

	public boolean needsOutput()
	{
		return true;
	}

	public void run() throws Exception
	{
		if( config.hasOption( "inline-bytecode" ) )
		{
			inlineBytecode = Boolean.parseBoolean( config
					.getOption( "inline-bytecode" ) );
		}

		if( config.hasOption( "integer-calculus" ) )
		{
			integerCalculus = Boolean.parseBoolean( config
					.getOption( "integer-calculus" ) );
		}

		if( config.hasOption( "bytecode-permutations" ) )
		{
			bytecodePermutations = Boolean.parseBoolean( config
					.getOption( "bytecode-permutations" ) );
		}

		if( config.hasOption( "inline-memory" ) )
		{
			inlineMemory = Boolean.parseBoolean( config
					.getOption( "inline-memory" ) );
		}

		if( config.hasOption( "finalize" ) )
		{
			finalize = Boolean.parseBoolean( config.getOption( "finalize" ) );
		}

		if( config.hasOption( "dead-code-elimination" ) )
		{
			deadCodeElimination = Boolean.parseBoolean( config
					.getOption( "dead-code-elimination" ) );
		}

		ToolLog.info( "TDSI configuration:" );
		ToolLog.info( "\tinlineBytecode\t\t= " + inlineBytecode );
		ToolLog.info( "\tintegerCalculus\t\t= " + integerCalculus );
		ToolLog.info( "\tbytecodePermutations\t= " + bytecodePermutations );
		ToolLog.info( "\tinlineMemory\t\t= " + inlineMemory );
		ToolLog.info( "\tfinalize\t\t= " + finalize );
		ToolLog.info( "\tdeadCodeElimination\t= " + deadCodeElimination );

		final TagIO tagIO = new TagIO( config.getInput() );

		tagIO.read();

		for( final ITag tag : tagIO.getTags() )
		{
			if( tag instanceof DoABCTag )
			{
				run( (DoABCTag)tag );
			}
		}

		tagIO.write( config.getOutput() );
		tagIO.close();
	}

	private void run( final DoABCTag doABC )
	{
		final Abc abc = new Abc();

		try
		{
			abc.read( new AbcInputStream( new ByteArrayInputStream(
					doABC.abcData ) ) );
		}
		catch( final Exception e )
		{
			throw new RuntimeException( e );
		}

		final AbcEnvironment abcEnvironment = new AbcEnvironment(
				new AbcContext( abc ) );

		final IInterpreter memInterp = new MemoryInlineJob();
		final IInterpreter bytecodeInterp = new BytecodeInlineJob();
		final InheritanceAnalyzer inheritanceAnalyzer = new InheritanceAnalyzer();

		for( final Method method : abc.methods )
		{
			if( null == method.body )
			{
				continue;
			}

			//
			// Inline bytecode
			//

			if( inlineBytecode )
			{
				bytecodeInterp.interpret( abcEnvironment, method.body.code );
			}

			//
			// Destructive integer calculus
			//

			if( integerCalculus )
			{
				abc.accept( new PermutationChain( new IBytecodePermutation[] {
					new IntegerCalculus()
				} ) );
			}

			//
			// Apply permutations
			//

			if( bytecodePermutations )
			{
				abc.accept( new PermutationChain( new IBytecodePermutation[] {
						new GetLexPattern(),
						new DecrementIntPattern(),
						new IncrementIntPattern(),
						new NamespaceSetCleanup(),
				} ) );
			}

			//
			// Inline Memory API
			//

			if( inlineMemory )
			{
				memInterp.interpret( abcEnvironment, method.body.code );
			}

			//
			// Remove dead code
			//

			if( deadCodeElimination )
			{
				abc.accept( new DeadCodeElimination( true ) );
			}
		}

		//
		// Mark classes final if possible
		//

		if( finalize )
		{
			inheritanceAnalyzer.finalize( abc.instances );
		}

		try
		{
			abc.write( doABC );
		}
		catch( final Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	public void setConfiguration( final IToolConfiguration configuration )
	{
		config = configuration;
	}

	public void visit( final ITag tag )
	{
		if( tag instanceof DoABCTag )
		{
			run( (DoABCTag)tag );
		}
	}
}
