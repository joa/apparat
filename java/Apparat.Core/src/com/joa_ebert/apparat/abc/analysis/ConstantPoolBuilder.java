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

import java.util.Iterator;
import java.util.Map.Entry;

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
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.AsType;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropLex;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.CallProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.CallSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.CallSuperVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.Coerce;
import com.joa_ebert.apparat.abc.bytecode.operations.ConstructProp;
import com.joa_ebert.apparat.abc.bytecode.operations.Debug;
import com.joa_ebert.apparat.abc.bytecode.operations.DebugFile;
import com.joa_ebert.apparat.abc.bytecode.operations.DefaultXmlNamespace;
import com.joa_ebert.apparat.abc.bytecode.operations.DeleteProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.FindPropStrict;
import com.joa_ebert.apparat.abc.bytecode.operations.FindProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetDescendants;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLex;
import com.joa_ebert.apparat.abc.bytecode.operations.GetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.InitProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.IsType;
import com.joa_ebert.apparat.abc.bytecode.operations.NewCatch;
import com.joa_ebert.apparat.abc.bytecode.operations.PushDouble;
import com.joa_ebert.apparat.abc.bytecode.operations.PushInt;
import com.joa_ebert.apparat.abc.bytecode.operations.PushNamespace;
import com.joa_ebert.apparat.abc.bytecode.operations.PushString;
import com.joa_ebert.apparat.abc.bytecode.operations.PushUInt;
import com.joa_ebert.apparat.abc.bytecode.operations.SetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.SetSuper;
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
public final class ConstantPoolBuilder implements IAbcVisitor
{
	public void visit( final AbcContext context, final Abc abc )
	{
	}

	public void visit( final AbcContext context,
			final AbstractMultiname multiname )
	{
		context.getConstantPool().add( multiname );
	}

	public void visit( final AbcContext context, final AbstractTrait trait )
	{
		context.getConstantPool().add( trait.name );
	}

	public void visit( final AbcContext context, final Class klass )
	{
	}

	public void visit( final AbcContext context, final ConstantPool constantPool )
	{
		// TODO fix me and put this line back in!
		// constantPool.clear();
	}

	public void visit( final AbcContext context,
			final ExceptionHandler exceptionHandler )
	{
		context.getConstantPool().add( exceptionHandler.exceptionType );
		context.getConstantPool().add( exceptionHandler.variableName );
	}

	public void visit( final AbcContext context, final Instance instance )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( instance.name );

		if( null != instance.base )
		{
			pool.add( instance.base );
		}

		if( null != instance.protectedNamespace )
		{
			pool.add( instance.protectedNamespace );
		}

		for( final AbstractMultiname interfaceName : instance.interfaces )
		{
			pool.add( interfaceName );
		}
	}

	public void visit( final AbcContext context, final Metadata metadata )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( metadata.name );

		for( final Entry<String, String> attribute : metadata.attributes
				.entrySet() )
		{
			if( null != attribute.getKey() )
			{
				pool.add( attribute.getKey() );
			}

			if( null != attribute.getValue() )
			{
				pool.add( attribute.getValue() );
			}
		}
	}

	public void visit( final AbcContext context, final Method method )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( method.returnType );

		for( final Parameter parameter : method.parameters )
		{
			pool.add( parameter.type );
		}

		pool.add( method.name );

		if( method.hasOptionalParameters )
		{
			for( final Parameter parameter : method.parameters )
			{
				if( !parameter.isOptional )
				{
					continue;
				}

				pool.add( parameter.optionalType, parameter.optionalValue );
			}
		}

		if( method.hasParameterNames )
		{
			for( final Parameter parameter : method.parameters )
			{
				pool.add( parameter.name );
			}
		}
	}

	public void visit( final AbcContext context, final MethodBody methodBody )
	{
		if( null == methodBody.code )
		{
			return;
		}

		final Iterator<AbstractOperation> iter = methodBody.code.listIterator();
		final ConstantPool pool = context.getConstantPool();

		while( iter.hasNext() )
		{
			final AbstractOperation operation = iter.next();
			final int code = operation.code;

			switch( code )
			{
				case Op.AsType:
					pool.add( ( (AsType)operation ).type );
					break;

				case Op.CallProperty:
					pool.add( ( (CallProperty)operation ).property );
					break;

				case Op.CallPropLex:
					pool.add( ( (CallPropLex)operation ).property );
					break;

				case Op.CallPropVoid:
					pool.add( ( (CallPropVoid)operation ).property );
					break;

				case Op.CallSuper:
					pool.add( ( (CallSuper)operation ).name );
					break;

				case Op.CallSuperVoid:
					pool.add( ( (CallSuperVoid)operation ).name );
					break;

				case Op.Coerce:
					pool.add( ( (Coerce)operation ).type );
					break;

				case Op.ConstructProp:
					pool.add( ( (ConstructProp)operation ).property );
					break;

				case Op.Debug:
					pool.add( ( (Debug)operation ).name );
					break;

				case Op.DebugFile:
					pool.add( ( (DebugFile)operation ).fileName );
					break;

				case Op.DeleteProperty:
					pool.add( ( (DeleteProperty)operation ).property );
					break;

				case Op.DefaultXmlNamespace:
					pool.add( ( (DefaultXmlNamespace)operation ).uri );
					break;

				case Op.FindProperty:
					pool.add( ( (FindProperty)operation ).property );
					break;

				case Op.FindPropStrict:
					pool.add( ( (FindPropStrict)operation ).property );
					break;

				case Op.GetDescendants:
					pool.add( ( (GetDescendants)operation ).name );
					break;

				case Op.GetLex:
					pool.add( ( (GetLex)operation ).property );
					break;

				case Op.GetProperty:
					pool.add( ( (GetProperty)operation ).property );
					break;

				case Op.GetSuper:
					pool.add( ( (GetSuper)operation ).property );
					break;

				case Op.InitProperty:
					pool.add( ( (InitProperty)operation ).property );
					break;

				case Op.IsType:
					pool.add( ( (IsType)operation ).type );
					break;

				case Op.NewCatch:
					pool
							.add( ( (NewCatch)operation ).exceptionHandler.exceptionType );
					pool
							.add( ( (NewCatch)operation ).exceptionHandler.variableName );
					break;

				case Op.PushDouble:
					pool.add( ( (PushDouble)operation ).value );
					break;

				case Op.PushInt:
					pool.add( ( (PushInt)operation ).value );
					break;

				case Op.PushNamespace:
					pool.add( ( (PushNamespace)operation ).value );
					break;

				case Op.PushString:
					pool.add( ( (PushString)operation ).value );
					break;

				case Op.PushUInt:
					pool.add( ( (PushUInt)operation ).value );
					break;

				case Op.SetProperty:
					pool.add( ( (SetProperty)operation ).property );
					break;

				case Op.SetSuper:
					pool.add( ( (SetSuper)operation ).property );
					break;
			}
		}
	}

	public void visit( final AbcContext context, final Multiname multiname )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( multiname );
		pool.add( multiname.name );
		pool.add( multiname.namespaceSet );
	}

	public void visit( final AbcContext context, final MultinameA multinameA )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( multinameA );
		pool.add( multinameA.name );
		pool.add( multinameA.namespaceSet );
	}

	public void visit( final AbcContext context, final MultinameL multinameL )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( multinameL );
		pool.add( multinameL.namespaceSet );
	}

	public void visit( final AbcContext context, final MultinameLA multinameLA )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( multinameLA );
		pool.add( multinameLA.namespaceSet );
	}

	public void visit( final AbcContext context, final Namespace namespace )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( namespace );
		pool.add( namespace.name );
	}

	public void visit( final AbcContext context, final NamespaceSet namespaceSet )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( namespaceSet );
	}

	public void visit( final AbcContext context, final Parameter parameter )
	{
		// Parameters are handled when visiting Method structures.
	}

	public void visit( final AbcContext context, final QName name )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( name );
		pool.add( name.name );
		pool.add( name.namespace );
	}

	public void visit( final AbcContext context, final QNameA nameA )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( nameA );
		pool.add( nameA.name );
		pool.add( nameA.namespace );
	}

	public void visit( final AbcContext context, final RTQName rtqName )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( rtqName );
		pool.add( rtqName.name );
	}

	public void visit( final AbcContext context, final RTQNameA rtqNameA )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( rtqNameA );
		pool.add( rtqNameA.name );
	}

	public void visit( final AbcContext context, final RTQNameL rtqNameL )
	{
		context.getConstantPool().add( rtqNameL );
	}

	public void visit( final AbcContext context, final RTQNameLA rtqNameLA )
	{
		context.getConstantPool().add( rtqNameLA );
	}

	public void visit( final AbcContext context, final Script script )
	{
	}

	public void visit( final AbcContext context, final TraitClass klass )
	{
	}

	public void visit( final AbcContext context, final TraitConst konst )
	{
		context.getConstantPool().add( konst.type );

		if( null != konst.value )
		{
			context.getConstantPool().add( konst.valueType, konst.value );
		}
	}

	public void visit( final AbcContext context, final TraitFunction function )
	{
		context.getConstantPool().add( function.name );
	}

	public void visit( final AbcContext context, final TraitGetter getter )
	{
		context.getConstantPool().add( getter.name );
	}

	public void visit( final AbcContext context, final TraitMethod method )
	{
		context.getConstantPool().add( method.name );
	}

	public void visit( final AbcContext context, final TraitSetter setter )
	{
		context.getConstantPool().add( setter.name );
	}

	public void visit( final AbcContext context, final TraitSlot slot )
	{
		context.getConstantPool().add( slot.type );

		if( null != slot.value )
		{
			context.getConstantPool().add( slot.valueType, slot.value );
		}
	}

	public void visit( final AbcContext context, final Typename typename )
	{
		final ConstantPool pool = context.getConstantPool();

		pool.add( typename.name );

		for( final AbstractMultiname multiname : typename.parameters )
		{
			pool.add( multiname );
		}

		pool.add( typename );
	}

}
