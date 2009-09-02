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

import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.IMethodVisitor;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.bytecode.permutations.PermutationChain;
import com.joa_ebert.apparat.taas.TaasBuilder;
import com.joa_ebert.apparat.taas.TaasEmitter;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.constantFolding.ConstantFolding;
import com.joa_ebert.apparat.taas.toolkit.copyPropagation.CopyPropagation;
import com.joa_ebert.apparat.taas.toolkit.deadCodeElimination.DeadCodeElimination;
import com.joa_ebert.apparat.taas.toolkit.flowOptimizer.FlowOptimizer;
import com.joa_ebert.apparat.taas.toolkit.inlineExpansion.InlineExpansion;
import com.joa_ebert.apparat.taas.toolkit.strengthReduction.StrengthReduction;

/**
 * @author Joa Ebert
 * 
 */
public class TaasCompiler implements IMethodVisitor
{
	public static final boolean SHOW_ALL_TRANSFORMATIONS = false;

	private static final boolean DEBUG = true;

	private final AbcEnvironment environment;

	private final TaasBuilder builder = new TaasBuilder();
	private final TaasEmitter emitter = new TaasEmitter();

	private PermutationChain preprocessor;
	private PermutationChain postprocessor;

	public TaasCompiler( final AbcEnvironment environment )
	{
		this.environment = environment;
		setPreprocessor( TaasPreprocessor.INSTANCE );
		setPostprocessor( TaasPostprocessor.INSTANCE );
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

		if( null == taasMethod || !optimize( taasMethod ) )
		{
			return null;
		}

		return taasMethod;
	}

	private MethodBody convertToBytecode( final TaasMethod method )
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

	private boolean CPCFDCE( final TaasMethod method )
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

	private boolean optimize( final TaasMethod method )
	{
		try
		{
			if( !execOptimizer( new FlowOptimizer(), method ) )
			{
				return false;
			}

			boolean changed = false;

			final InlineExpansion inlineExpansion = new InlineExpansion();
			final StrengthReduction strengthReduction = new StrengthReduction();

			do
			{
				changed = inlineExpansion.manipulate( environment, method );

				CPCFDCE( method );

				changed = strengthReduction.manipulate( environment, method )
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
			System.out.println( "Post:" );
			final BytecodePrinter printer = new BytecodePrinter( System.out );
			printer.interpret( environment, method.body.code );
			System.out
					.println( "\n-----------------------------------------\n" );
		}
	}

	private void preprocess( final Method method )
	{
		if( DEBUG )
		{
			System.out.println( "Pre:" );
			final BytecodePrinter printer = new BytecodePrinter( System.out );
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

		final MethodBody methodBody = convertToBytecode( taasMethod );

		if( null == methodBody )
		{
			return false;
		}

		method.body = methodBody;

		postprocess( method );

		return true;
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
		replace( method );
	}
}
