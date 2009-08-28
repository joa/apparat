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

package com.joa_ebert.apparat.taas;

import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.IMethodVisitor;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.bytecode.permutations.PermutationChain;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;

/**
 * @author Joa Ebert
 * 
 */
public class TaasCompiler implements IMethodVisitor
{
	private static final boolean DEBUG = true;

	private final AbcEnvironment environment;

	private final TaasBuilder builder = new TaasBuilder();
	private final TaasEmitter emitter = new TaasEmitter();

	private final List<ITaasTool> optimizers = new LinkedList<ITaasTool>();

	private PermutationChain preprocessor;
	private PermutationChain postprocessor;

	public TaasCompiler( final AbcEnvironment environment )
	{
		this.environment = environment;
	}

	public void addOptimizer( final ITaasTool optimizer )
	{
		optimizers.add( optimizer );
	}

	public boolean compile( final Method method )
	{
		if( null == method || null == method.body || null == method.body.code )
		{
			return false;
		}

		preprocess( method );

		final TaasMethod taasMethod = convertToTaas( method.body.code );

		if( null == taasMethod || !optimize( taasMethod ) )
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

	private boolean optimize( final TaasMethod method )
	{
		try
		{
			boolean changed;

			do
			{
				changed = false;

				for( final ITaasTool tool : optimizers )
				{
					changed = tool.manipulate( environment, method ) || changed;
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

	private void postprocess( final Method method )
	{
		if( DEBUG )
		{
			System.out.println( "Post:" );
			final BytecodePrinter printer = new BytecodePrinter( System.out );
			printer.interpret( environment, method.body.code );
			System.out
					.println( "\n-----------------------------------------\n" );
		}

		if( null == postprocessor )
		{
			return;
		}

		method.accept( new AbcContext( method.abc ), postprocessor );
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
		compile( method );
	}
}
