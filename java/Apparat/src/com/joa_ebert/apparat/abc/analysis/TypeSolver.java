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
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbcException;
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
import com.joa_ebert.apparat.abc.utils.StringConverter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TypeSolver implements IAbcVisitor
{
	private boolean typeSolved;
	private AbstractMultiname property;
	private AbstractMultiname type;

	public TypeSolver()
	{

	}

	public AbstractMultiname solve( final AbcEnvironment environment,
			final AbcContext context, final Instance instance,
			final AbstractMultiname property, final boolean strict )
			throws AbcException
	{
		typeSolved = false;
		type = null;

		this.property = property;

		Instance currentInstance = instance;

		do
		{
			currentInstance.accept( context, this );

			if( !typeSolved && null != currentInstance.klass )
			{
				instance.klass.accept( context, this );
			}

			currentInstance = environment.instanceOf( currentInstance.base );
		}
		while( !typeSolved && null != currentInstance );

		if( !typeSolved && strict )
		{
			// TODO check if dynamic!

			throw new AbcException( "Could not solve property "
					+ StringConverter.toString( property ) + " on "
					+ StringConverter.toString( instance ) );
		}

		return type;
	}

	public AbstractMultiname solve( final AbstractTrait trait )
			throws AbcException
	{
		switch( trait.kind )
		{
			case Class:
				throw new AbcException( "Unexpected type." );

			case Const:
				return ( (TraitConst)trait ).type;

			case Function:
				return ( (TraitFunction)trait ).function.returnType;

			case Getter:
				return ( (TraitGetter)trait ).method.returnType;

			case Method:
				return ( (TraitMethod)trait ).method.returnType;

			case Setter:
				return ( (TraitSetter)trait ).method.returnType;

			case Slot:
				return ( (TraitSlot)trait ).type;

			default:
				throw new AbcException( "Unreachable by definition." );
		}
	}

	private void typeFound( final AbstractMultiname type )
	{
		this.type = type;
		this.typeSolved = true;
	}

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
	}

	public void visit( final AbcContext context, final Metadata metadata )
	{
	}

	public void visit( final AbcContext context, final Method method )
	{
	}

	public void visit( final AbcContext context, final MethodBody methodBody )
	{
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
	}

	public void visit( final AbcContext context, final TraitClass klass )
	{
	}

	public void visit( final AbcContext context, final TraitConst konst )
	{
		if( !typeSolved && konst.name.equals( property ) )
		{
			typeFound( konst.type );
		}
	}

	public void visit( final AbcContext context, final TraitFunction function )
	{
		if( !typeSolved && function.name.equals( property ) )
		{
			typeFound( function.function.returnType );
		}
	}

	public void visit( final AbcContext context, final TraitGetter getter )
	{
		if( !typeSolved && getter.name.equals( property ) )
		{
			typeFound( getter.method.returnType );
		}
	}

	public void visit( final AbcContext context, final TraitMethod method )
	{
		if( !typeSolved && method.name.equals( property ) )
		{
			typeFound( method.method.returnType );
		}
	}

	public void visit( final AbcContext context, final TraitSetter setter )
	{
		if( !typeSolved && setter.name.equals( property ) )
		{
			typeFound( setter.method.returnType );
		}
	}

	public void visit( final AbcContext context, final TraitSlot slot )
	{
		if( !typeSolved && slot.name.equals( property ) )
		{
			typeFound( slot.type );
		}
	}

	public void visit( final AbcContext context, final Typename typename )
	{
	}
}
