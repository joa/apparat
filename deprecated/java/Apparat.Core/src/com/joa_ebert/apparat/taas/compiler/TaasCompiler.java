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

package com.joa_ebert.apparat.taas.compiler;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbcException;
import com.joa_ebert.apparat.abc.IMethodVisitor;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.bytecode.permutations.PermutationChain;
import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.SwfFormatException;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.taas.TaasBuilder;
import com.joa_ebert.apparat.taas.TaasEmitter;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.generic.ConstantFolding;
import com.joa_ebert.apparat.taas.toolkit.generic.CopyPropagation;
import com.joa_ebert.apparat.taas.toolkit.generic.DeadCodeElimination;
import com.joa_ebert.apparat.taas.toolkit.generic.FlowOptimizer;
import com.joa_ebert.apparat.taas.toolkit.generic.InlineExpansion;
import com.joa_ebert.apparat.taas.toolkit.generic.LoopInvariantCodeMotion;
import com.joa_ebert.apparat.taas.toolkit.generic.StrengthReduction;
import com.joa_ebert.apparat.taas.toolkit.generic.TailRecursionElimination;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * @author Joa Ebert
 * 
 */
public class TaasCompiler implements IMethodVisitor
{
	public static final boolean SHOW_ALL_TRANSFORMATIONS = false;
	private static final boolean DEBUG = false;

	private final AbcEnvironment environment;

	private final TaasBuilder builder = new TaasBuilder();
	private final TaasEmitter emitter = new TaasEmitter();

	private PermutationChain preprocessor;
	private PermutationChain postprocessor;
	private boolean optimizationsEnabled = true;

	private int methodIndex = 0;
	private final int targetMethod = -1;

	public TaasCompiler() throws IOException
	{
		this( DefaultEnvironmentFactory.create() );
	}

	public TaasCompiler( final AbcEnvironment environment )
	{
		this.environment = environment;

		setPreprocessor( TaasPreprocessor.INSTANCE );
		setPostprocessor( TaasPostprocessor.INSTANCE );
	}

	public void addLibrary( final Abc library )
	{
		environment.add( library );
	}

	public void addLibrary( final List<ITag> library ) throws IOException,
			AbcException
	{
		environment.add( library );
	}

	public void addLibrary( final Swc library ) throws SwfFormatException,
			IOException, DataFormatException, AbcException
	{
		environment.add( library );
	}

	public void addLibrary( final Swf library ) throws IOException,
			AbcException
	{
		environment.add( library );
	}

	public void addLibrary( final TagIO library ) throws IOException,
			AbcException
	{
		environment.add( library );
	}

	public TaasMethod compile( final Method method )
	{
		if( null == method || null == method.body || null == method.body.code
				|| !method.body.exceptions.isEmpty() )
		{
			return null;
		}

		preprocess( method );

		final TaasMethod taasMethod = convertToTaas( method.body.code );

		if( null == taasMethod )
		{
			return null;
		}

		if( optimizationsEnabled && !optimize( taasMethod ) )
		{
			return null;
		}

		return taasMethod;
	}

	private TaasMethod convertToTaas( final Bytecode bytecode )
	{
		try
		{
			return builder.build( environment, bytecode );
		}
		catch( final TaasException taasException )
		{
			taasException.printStackTrace();

			return null;
		}
	}

	public MethodBody emitBytecode( final TaasMethod method )
	{
		try
		{
			return emitter.emit( environment, method );
		}
		catch( final TaasException taasException )
		{
			taasException.printStackTrace();

			return null;
		}
	}

	private boolean execOptimizer( final ITaasTool tool, final TaasMethod method )
	{
		try
		{
			boolean changed = false;

			do
			{
				changed = tool.manipulate( environment, method );
			}
			while( changed );

			return true;
		}
		catch( final TaasException taasException )
		{
			taasException.printStackTrace();

			return false;
		}

	}

	public AbcEnvironment getAbcEnvironment()
	{
		return environment;
	}

	public boolean getOptimizationsEnabled()
	{
		return optimizationsEnabled;
	}

	public boolean optimize( final TaasMethod method )
	{
		try
		{
			if( !execOptimizer( new FlowOptimizer(), method ) )
			{
				return false;
			}

			if( !execOptimizer( new TailRecursionElimination(), method ) )
			{
				return false;
			}

			boolean changed = false;

			final InlineExpansion inlineExpansion = new InlineExpansion();
			final StrengthReduction strengthReduction = new StrengthReduction();
			final LoopInvariantCodeMotion loopInvariantCodeMotion = new LoopInvariantCodeMotion();

			do
			{
				changed = inlineExpansion.manipulate( environment, method );

				changed = strengthReduction.manipulate( environment, method )
						|| changed;

				changed = loopInvariantCodeMotion.manipulate( environment,
						method )
						|| changed;

				if( changed && !performCPCFDCE( method ) )
				{
					return false;
				}
			}
			while( changed );

			return true;
		}
		catch( final TaasException taasException )
		{
			taasException.printStackTrace();

			return false;
		}
	}

	private boolean performCPCFDCE( final TaasMethod method )
	{
		try
		{
			final CopyPropagation copyPropagation = new CopyPropagation();
			final ConstantFolding constantFolding = new ConstantFolding();
			final DeadCodeElimination deadCodeElimination = new DeadCodeElimination();

			boolean changed;

			do
			{
				changed = false;

				changed = copyPropagation.manipulate( environment, method )
						|| changed;

				changed = constantFolding.manipulate( environment, method )
						|| changed;

				changed = deadCodeElimination.manipulate( environment, method )
						|| changed;
			}
			while( changed );

			return true;
		}
		catch( final TaasException taasException )
		{
			taasException.printStackTrace();

			return false;
		}
	}

	private void postprocess( final Method method )
	{
		if( null == postprocessor )
		{
			return;
		}

		method.accept( new AbcContext( method.abc ), postprocessor );

		if( DEBUG )
		{
			System.out.println( "Post " + methodIndex + ":" );
			final BytecodePrinter printer = new BytecodePrinter( System.out );
			printer.setShowPositions( false );
			printer.interpret( environment, method.body.code );
			System.out
					.println( "\n-----------------------------------------\n" );
		}
	}

	private void preprocess( final Method method )
	{
		if( DEBUG )
		{
			System.out.println( "Pre " + methodIndex + ":" );
			final BytecodePrinter printer = new BytecodePrinter( System.out );
			printer.setShowPositions( false );
			printer.interpret( environment, method.body.code );
		}

		if( null == preprocessor )
		{
			return;
		}

		method.accept( new AbcContext( method.abc ), preprocessor );
	}

	public boolean replace( final Method method )
	{
		final TaasMethod taasMethod = compile( method );

		if( null == taasMethod )
		{
			return false;
		}

		final MethodBody methodBody = emitBytecode( taasMethod );

		if( null == methodBody )
		{
			return false;
		}

		final int minLocalCount = method.parameters.size() + 1;

		if( methodBody.localCount < minLocalCount )
		{
			methodBody.localCount = minLocalCount;
		}

		method.body = methodBody;

		postprocess( method );

		return true;
	}

	public void setOptimizationsEnabled( final boolean value )
	{
		optimizationsEnabled = value;
	}

	public void setPostprocessor( final PermutationChain value )
	{
		postprocessor = value;
	}

	public void setPreprocessor( final PermutationChain value )
	{
		preprocessor = value;
	}

	public void visit( final AbcContext context, final Method method )
	{
		if( DEBUG )
		{
			if( targetMethod != -1 )
			{
				if( methodIndex == targetMethod )
				{
					replace( method );
				}
			}
			else
			{
				replace( method );
			}
		}
		else
		{
			replace( method );
		}

		if( DEBUG )
		{
			methodIndex++;
		}
	}
}
