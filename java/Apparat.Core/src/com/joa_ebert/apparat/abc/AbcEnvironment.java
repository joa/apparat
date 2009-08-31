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

	public QName baseType( final QName name )
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

		return null;
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
