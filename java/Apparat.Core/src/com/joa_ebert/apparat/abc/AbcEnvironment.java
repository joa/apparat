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

package com.joa_ebert.apparat.abc;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.analysis.TypeSolver;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.traits.TraitFunction;
import com.joa_ebert.apparat.abc.traits.TraitGetter;
import com.joa_ebert.apparat.abc.traits.TraitMethod;
import com.joa_ebert.apparat.abc.traits.TraitSetter;
import com.joa_ebert.apparat.abc.utils.StringConverter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class AbcEnvironment
{
	/**
	 * The MethodInfo class is a descriptor for a method.
	 * 
	 * @author Joa Ebert
	 */
	public static final class MethodInfo
	{
		/**
		 * The method.
		 */
		public Method method;

		/**
		 * Whether or not the method is considered final.
		 */
		public boolean isFinal;

		/**
		 * The instance to which this method belongs to.
		 * 
		 * This field is also set if a method belongs to a class instead of an
		 * instance. It is not set if the method belongs to a script.
		 */
		public Instance instance;

		/**
		 * The script to which this method belongs to.
		 * 
		 * This field is not set if the method belongs to an instance or a
		 * class.
		 */
		public Script script;

		/**
		 * The trait of the method.
		 */
		public AbstractTrait trait;

		/**
		 * Creates and returns a new MethodInfo instance.
		 */
		public MethodInfo()
		{

		}
	}

	private static final TypeSolver TYPER_INSTANCE = new TypeSolver();

	private final List<AbcContext> contexts = new LinkedList<AbcContext>();

	public AbcEnvironment()
	{
	}

	public AbcEnvironment( final Abc abc )
	{
		addAbc( abc );
	}

	public AbcEnvironment( final Abc[] abcs )
	{
		for( final Abc abc : abcs )
		{
			addAbc( abc );
		}
	}

	public AbcEnvironment( final AbcContext context )
	{
		contexts.add( context );
	}

	public void addAbc( final Abc abc )
	{
		contexts.add( new AbcContext( abc ) );
	}

	public void addAll( final Collection<? extends Abc> collection )
	{
		for( final Abc abc : collection )
		{
			addAbc( abc );
		}
	}

	public void addContext( final AbcContext context )
	{
		contexts.add( context );
	}

	public QName baseType( final QName name ) throws AbcException
	{
		for( final AbcContext context : contexts )
		{
			final Abc abc = context.getAbc();

			for( final Instance instance : abc.instances )
			{
				if( instance.name.equals( name ) )
				{
					return (QName)instance.base;
				}
			}
		}

		throw new AbcException( "Can not find base type of "
				+ StringConverter.toString( name ) + "." );
	}

	public Class classOf( final Method method )
	{
		final AbcContext context = contextOf( method );

		if( null == context )
		{
			return null;
		}

		final Abc abc = context.getAbc();

		for( final Class klass : abc.classes )
		{
			if( klass.classInitializer == method
					|| traitsContain( klass.traits, method ) )
			{
				return klass;
			}
		}

		return null;
	}

	public Iterator<AbcContext> contextIterator()
	{
		return contexts.listIterator();
	}

	public AbcContext contextOf( final Method method )
	{
		for( final AbcContext context : contexts )
		{
			if( context.getAbc().methods.contains( method ) )
			{
				return context;
			}
		}

		return null;
	}

	private Method findMethod( final AbstractTrait trait )
	{
		if( trait.kind == TraitKind.Method )
		{
			return ( (TraitMethod)trait ).method;
		}
		else if( trait.kind == TraitKind.Getter )
		{
			return ( (TraitGetter)trait ).method;
		}
		else if( trait.kind == TraitKind.Setter )
		{
			return ( (TraitSetter)trait ).method;
		}
		else if( trait.kind == TraitKind.Function )
		{
			return ( (TraitFunction)trait ).function;
		}
		else
		{
			return null;
		}
	}

	public MethodInfo findProperty( final AbstractMultiname object,
			final AbstractMultiname property ) throws AbcException
	{
		Instance instance = null;
		Method result = null;

		search: for( final AbcContext ctx : contexts )
		{
			for( final Instance inst : ctx.getAbc().instances )
			{
				if( inst.name.equals( object ) )
				{
					instance = inst;
					break search;
				}
			}

			for( final Script script : ctx.getAbc().scripts )
			{
				for( final AbstractTrait trait : script.traits )
				{
					if( !trait.name.equals( object )
							|| !trait.name.equals( property ) )
					{
						continue;
					}

					result = findMethod( trait );

					if( null != result )
					{
						//
						// Package functions can not be overriden, so they are
						// always marked as final.
						//

						final MethodInfo methodInfo = new MethodInfo();

						methodInfo.instance = null;
						methodInfo.isFinal = true;
						methodInfo.method = result;
						methodInfo.script = script;
						methodInfo.trait = trait;

						return methodInfo;
					}
				}
			}
		}

		while( null != instance )
		{
			List<AbstractTrait> traits = instance.traits;

			for( final AbstractTrait trait : traits )
			{
				if( !trait.name.equals( property ) )
				{
					continue;
				}

				result = findMethod( trait );

				if( null != result )
				{
					//
					// An instance trait is final, if the instance itself or
					// the trait is final.
					//
					//
					// An instance trait is also final, if it is part of a
					// custom namespace or private.
					//

					final MethodInfo methodInfo = new MethodInfo();

					methodInfo.instance = instance;
					methodInfo.isFinal = instance.isFinal
							|| isTraitFinal( trait );
					methodInfo.method = result;
					methodInfo.script = null;
					methodInfo.trait = trait;

					return methodInfo;
				}
			}

			traits = instance.klass.traits;

			for( final AbstractTrait trait : traits )
			{
				if( !trait.name.equals( property ) )
				{
					continue;
				}

				result = findMethod( trait );

				if( null != result )
				{
					//
					// A class trait is always considered final.
					//

					final MethodInfo methodInfo = new MethodInfo();

					methodInfo.instance = instance;
					methodInfo.isFinal = true;
					methodInfo.method = result;
					methodInfo.script = null;
					methodInfo.trait = trait;

					return methodInfo;
				}
			}

			instance = instanceOf( instance.base );
		}

		return null;
	}

	public List<AbcContext> getContexts()
	{
		return contexts;
	}

	public Instance instanceOf( final AbstractMultiname multiname )
	{
		for( final AbcContext context : contexts )
		{
			for( final Instance instance : context.getAbc().instances )
			{
				if( instance.name.equals( multiname ) )
				{
					return instance;
				}
			}
		}

		return null;
	}

	public Instance instanceOf( final Method method )
	{
		final AbcContext context = contextOf( method );

		if( null == context )
		{
			return null;
		}

		final Abc abc = context.getAbc();

		for( final Instance instance : abc.instances )
		{
			if( instance.instanceInitializer == method
					|| traitsContain( instance.traits, method ) )
			{
				return instance;
			}
		}

		return null;
	}

	private boolean isTraitFinal( final AbstractTrait trait )
	{
		if( trait.kind == TraitKind.Method )
		{
			return ( (TraitMethod)trait ).isFinal;
		}
		else if( trait.kind == TraitKind.Getter )
		{
			return ( (TraitGetter)trait ).isFinal;
		}
		else if( trait.kind == TraitKind.Setter )
		{
			return ( (TraitSetter)trait ).isFinal;
		}
		else
		{
			return false;
		}
	}

	public AbstractMultiname propertyType( final AbstractMultiname object,
			final AbstractMultiname property, final boolean strict )
			throws AbcException
	{
		AbcContext context = null;
		Instance instance = null;

		@SuppressWarnings( "unused" )
		Instance instanceRoot = null;

		search: for( final AbcContext ctx : contexts )
		{
			for( final Instance inst : ctx.getAbc().instances )
			{
				if( inst.name.equals( object ) )
				{
					context = ctx;
					instanceRoot = instance = inst;

					break search;
				}
			}

			for( final Script script : ctx.getAbc().scripts )
			{
				for( final AbstractTrait trait : script.traits )
				{
					if( trait.name.equals( object ) )
					{
						return TYPER_INSTANCE.solve( trait );
					}
				}
			}
		}

		if( ( null == context || null == instance ) && strict )
		{
			throw new AbcException( "Could not solve object "
					+ StringConverter.toString( object ) );
		}

		if( null != instance && instance.name.equals( property ) )
		{
			return instance.name;
		}

		AbstractMultiname result = null;

		while( null == result && null != instance )
		{
			result = TYPER_INSTANCE.solve( this, context, instance, property,
					false );

			instance = instanceOf( instance.base );
		}

		if( null == result && strict )
		{
			//
			// TODO add check if any of the instance of the inheritance graph
			// might be dynamic...
			//

			throw new AbcException( "Could not solve property "
					+ StringConverter.toString( property ) + " on "
					+ StringConverter.toString( instance ) );
		}

		return result;
	}

	public Object scopeOf( final Method method )
	{
		final Instance instance = instanceOf( method );

		if( null != instance )
		{
			return instance;
		}
		else
		{
			final Class klass = classOf( method );

			if( null != klass )
			{
				return klass;
			}
			else
			{
				final Script script = scriptOf( method );

				if( null != script )
				{
					return script;
				}
				else
				{
					return null;
				}
			}
		}
	}

	public Script scriptOf( final Method method )
	{
		final AbcContext context = contextOf( method );

		if( null == context )
		{
			return null;
		}

		final Abc abc = context.getAbc();

		for( final Script script : abc.scripts )
		{
			if( script.initializer == method
					|| traitsContain( script.traits, method ) )
			{
				return script;
			}
		}

		return null;
	}

	private boolean traitsContain( final List<AbstractTrait> traits,
			final Method method )
	{
		for( final AbstractTrait trait : traits )
		{
			switch( trait.kind )
			{
				case Function:
					if( ( (TraitFunction)trait ).function == method )
					{
						return true;
					}
					break;

				case Getter:
					if( ( (TraitGetter)trait ).method == method )
					{
						return true;
					}
					break;

				case Setter:
					if( ( (TraitSetter)trait ).method == method )
					{
						return true;
					}
					break;

				case Method:
					if( ( (TraitMethod)trait ).method == method )
					{
						return true;
					}
					break;
			}
		}

		return false;
	}
}
