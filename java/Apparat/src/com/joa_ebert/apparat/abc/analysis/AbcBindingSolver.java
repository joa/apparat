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

package com.joa_ebert.apparat.abc.analysis;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.AbstractTrait;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.ConstantPool;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.IAbcVisitor;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Metadata;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceSet;
import com.joa_ebert.apparat.abc.Parameter;
import com.joa_ebert.apparat.abc.Script;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.CallMethod;
import com.joa_ebert.apparat.abc.bytecode.operations.CallStatic;
import com.joa_ebert.apparat.abc.bytecode.operations.NewCatch;
import com.joa_ebert.apparat.abc.bytecode.operations.NewClass;
import com.joa_ebert.apparat.abc.bytecode.operations.NewFunction;
import com.joa_ebert.apparat.abc.multinames.Multiname;
import com.joa_ebert.apparat.abc.multinames.MultinameA;
import com.joa_ebert.apparat.abc.multinames.MultinameL;
import com.joa_ebert.apparat.abc.multinames.MultinameLA;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.multinames.QNameA;
import com.joa_ebert.apparat.abc.multinames.RTQName;
import com.joa_ebert.apparat.abc.multinames.RTQNameA;
import com.joa_ebert.apparat.abc.multinames.RTQNameL;
import com.joa_ebert.apparat.abc.multinames.RTQNameLA;
import com.joa_ebert.apparat.abc.multinames.Typename;
import com.joa_ebert.apparat.abc.traits.TraitClass;
import com.joa_ebert.apparat.abc.traits.TraitConst;
import com.joa_ebert.apparat.abc.traits.TraitFunction;
import com.joa_ebert.apparat.abc.traits.TraitGetter;
import com.joa_ebert.apparat.abc.traits.TraitMethod;
import com.joa_ebert.apparat.abc.traits.TraitSetter;
import com.joa_ebert.apparat.abc.traits.TraitSlot;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class AbcBindingSolver implements IAbcVisitor
{
	public void visit( final AbcContext context, final Abc abc )
	{
	}

	public void visit( final AbcContext context,
			final AbstractMultiname multiname )
	{
	}

	public void visit( final AbcContext context, final AbstractTrait trait )
	{
	}

	public void visit( final AbcContext context, final Class klass )
	{
		klass.classInitializer = context.getAbc().getMethod(
				klass.classInitializerIndex );
	}

	public void visit( final AbcContext context, final ConstantPool constantPool )
	{
	}

	public void visit( final AbcContext context,
			final ExceptionHandler exceptionHandler )
	{
	}

	public void visit( final AbcContext context, final Instance instance )
	{
		instance.instanceInitializer = context.getAbc().getMethod(
				instance.instanceInitializerIndex );
	}

	public void visit( final AbcContext context, final Metadata metadata )
	{
	}

	public void visit( final AbcContext context, final Method method )
	{
	}

	public void visit( final AbcContext context, final MethodBody methodBody )
	{
		if( null == methodBody || null == methodBody.code )
		{
			return;
		}

		final Bytecode bytecode = methodBody.code;
		final Abc abc = context.getAbc();

		for( final AbstractOperation operation : bytecode )
		{
			final int code = operation.code;

			switch( code )
			{
				case Op.CallMethod:
					final CallMethod callMethod = (CallMethod)operation;
					callMethod.method = abc.getMethod( callMethod.methodIndex );
					break;

				case Op.CallStatic:
					final CallStatic callStatic = (CallStatic)operation;
					callStatic.method = abc.getMethod( callStatic.methodIndex );
					break;

				case Op.NewCatch:
					final NewCatch newCatch = (NewCatch)operation;
					newCatch.exceptionHandler = methodBody
							.getExceptionHandler( newCatch.exceptionHandlerIndex );
					break;

				case Op.NewClass:
					final NewClass newClass = (NewClass)operation;
					newClass.klass = abc.getClass( newClass.classIndex );
					break;

				case Op.NewFunction:
					final NewFunction newFunction = (NewFunction)operation;
					newFunction.function = abc
							.getMethod( newFunction.functionIndex );
					break;

			}
		}
	}

	public void visit( final AbcContext context, final Multiname multiname )
	{
	}

	public void visit( final AbcContext context, final MultinameA multinameA )
	{
	}

	public void visit( final AbcContext context, final MultinameL multinameL )
	{
	}

	public void visit( final AbcContext context, final MultinameLA multinameLA )
	{
	}

	public void visit( final AbcContext context, final Namespace namespace )
	{
	}

	public void visit( final AbcContext context, final NamespaceSet namespaceSet )
	{
	}

	public void visit( final AbcContext context, final Parameter parameter )
	{
	}

	public void visit( final AbcContext context, final QName name )
	{
	}

	public void visit( final AbcContext context, final QNameA nameA )
	{
	}

	public void visit( final AbcContext context, final RTQName rtqName )
	{
	}

	public void visit( final AbcContext context, final RTQNameA rtqNameA )
	{
	}

	public void visit( final AbcContext context, final RTQNameL rtqNameL )
	{
	}

	public void visit( final AbcContext context, final RTQNameLA rtqNameLA )
	{
	}

	public void visit( final AbcContext context, final Script script )
	{
		script.initializer = context.getAbc().getMethod(
				script.initializerIndex );
	}

	public void visit( final AbcContext context, final TraitClass klass )
	{
		klass.klass = context.getAbc().getClass( klass.classIndex );
	}

	public void visit( final AbcContext context, final TraitConst konst )
	{
	}

	public void visit( final AbcContext context, final TraitFunction function )
	{
		function.function = context.getAbc().getMethod( function.functionIndex );
	}

	public void visit( final AbcContext context, final TraitGetter getter )
	{
		getter.method = context.getAbc().getMethod( getter.methodIndex );
	}

	public void visit( final AbcContext context, final TraitMethod method )
	{
		method.method = context.getAbc().getMethod( method.methodIndex );
	}

	public void visit( final AbcContext context, final TraitSetter setter )
	{
		setter.method = context.getAbc().getMethod( setter.methodIndex );
	}

	public void visit( final AbcContext context, final TraitSlot slot )
	{
	}

	public void visit( final AbcContext context, final Typename typename )
	{
	}
}
