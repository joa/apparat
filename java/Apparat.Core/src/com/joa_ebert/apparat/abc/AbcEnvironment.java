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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;

import com.joa_ebert.apparat.abc.analysis.TypeSolver;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.traits.TraitFunction;
import com.joa_ebert.apparat.abc.traits.TraitGetter;
import com.joa_ebert.apparat.abc.traits.TraitMethod;
import com.joa_ebert.apparat.abc.traits.TraitSetter;
import com.joa_ebert.apparat.abc.utils.StringConverter;
import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.SwfFormatException;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class AbcEnvironment
{
	/**
	 * The PropertyInfo class is a descriptor for a method.
	 * 
	 * @author Joa Ebert
	 */
	public static final class PropertyInfo
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
		 * Creates and returns a new PropertyInfo instance.
		 */
		public PropertyInfo()
		{
		}

		@Override
		public boolean equals( final Object other )
		{
			if( other instanceof PropertyInfo )
			{
				return equals( (PropertyInfo)other );
			}

			return false;
		}

		public boolean equals( final PropertyInfo other )
		{
			if( null == method )
			{
				if( other.method != null )
				{
					return false;
				}

				return true;
			}
			else if( method.equals( other.method ) )
			{
				return true;
			}

			return false;
		}

		@Override
		public String toString()
		{
			return "[PropertyInfo method: "
					+ StringConverter.toString( method )
					+ ( ( null != instance ) ? ", instance: "
							+ StringConverter.toString( instance )
							: ", script: " + StringConverter.toString( script ) )
					+ ", isFinal: " + isFinal + ", trait: "
					+ StringConverter.toString( trait ) + "]";
		}
	}

	private static final TypeSolver TYPER_INSTANCE = new TypeSolver();

	private final List<AbcContext> contexts = new LinkedList<AbcContext>();

	public AbcEnvironment()
	{
	}

	public AbcEnvironment( final Abc abc )
	{
		add( abc );
	}

	public AbcEnvironment( final Abc[] abcs )
	{
		for( final Abc abc : abcs )
		{
			add( abc );
		}
	}

	public AbcEnvironment( final AbcContext context )
	{
		contexts.add( context );
	}

	public void add( final Abc abc )
	{
		contexts.add( new AbcContext( abc ) );
	}

	public void add( final AbcContext context )
	{
		contexts.add( context );
	}

	public void add( final List<ITag> tags ) throws IOException, AbcException
	{
		for( final ITag tag : tags )
		{
			if( Tags.DoABC == tag.getType() )
			{
				final DoABCTag doABC = (DoABCTag)tag;
				final Abc abc = new Abc();

				abc.read( doABC );

				add( abc );
			}
		}
	}

	public void add( final Swc swc ) throws SwfFormatException, IOException,
			DataFormatException, AbcException
	{
		final Swf swf = new Swf();
		ByteArrayInputStream input = null;

		try
		{
			input = new ByteArrayInputStream( swc.library );

			swf.read( input, swc.library.length );
		}
		finally
		{
			if( null != input )
			{
				input.close();
				input = null;
			}
		}

		add( swf );
	}

	public void add( final Swf swf ) throws IOException, AbcException
	{
		add( swf.tags );
	}

	public void add( final TagIO tagIO ) throws IOException, AbcException
	{
		add( tagIO.getTags() );
	}

	public void addAll( final Collection<? extends Abc> collection )
	{
		for( final Abc abc : collection )
		{
			add( abc );
		}
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

	public PropertyInfo findProperty( final AbstractMultiname object,
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
				final List<AbstractTrait> traits = script.traits;

				for( final AbstractTrait trait : traits )
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

						final PropertyInfo propertyInfo = new PropertyInfo();

						propertyInfo.instance = null;
						propertyInfo.isFinal = true;
						propertyInfo.method = result;
						propertyInfo.script = script;
						propertyInfo.trait = trait;

						return propertyInfo;
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

					final PropertyInfo propertyInfo = new PropertyInfo();

					propertyInfo.instance = instance;
					propertyInfo.isFinal = instance.isFinal
							|| isTraitFinal( trait );
					propertyInfo.method = result;
					propertyInfo.script = null;
					propertyInfo.trait = trait;

					return propertyInfo;
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

					final PropertyInfo propertyInfo = new PropertyInfo();

					propertyInfo.instance = instance;
					propertyInfo.isFinal = true;
					propertyInfo.method = result;
					propertyInfo.script = null;
					propertyInfo.trait = trait;

					return propertyInfo;
				}
			}

			instance = instanceOf( instance.base );
		}

		return null;
	}

	public PropertyInfo findProperty( final Bytecode bytecode )
	{
		if( null == bytecode.methodBody || null == bytecode.methodBody.method )
		{
			return null;
		}

		final Method method = bytecode.methodBody.method;
		final Object scope = scopeOf( method );

		if( scope instanceof Instance )
		{
			final Instance instance = (Instance)scope;
			final List<AbstractTrait> traits = instance.traits;

			for( final AbstractTrait trait : traits )
			{
				final Method search = findMethod( trait );

				if( null != search && search == method )
				{
					final PropertyInfo propertyInfo = new PropertyInfo();

					propertyInfo.instance = instance;
					propertyInfo.isFinal = instance.isFinal
							|| isTraitFinal( trait );
					propertyInfo.method = method;
					propertyInfo.script = null;
					propertyInfo.trait = trait;

					return propertyInfo;
				}
			}
		}
		else if( scope instanceof Class )
		{
			final Class klass = (Class)scope;
			final List<AbstractTrait> traits = klass.traits;

			for( final AbstractTrait trait : traits )
			{
				final Method search = findMethod( trait );

				if( null != search && search == method )
				{
					final PropertyInfo propertyInfo = new PropertyInfo();

					propertyInfo.instance = klass.instance;
					propertyInfo.isFinal = true;
					propertyInfo.method = method;
					propertyInfo.script = null;
					propertyInfo.trait = trait;

					return propertyInfo;
				}
			}
		}
		else if( scope instanceof Script )
		{
			final Script script = (Script)scope;
			final List<AbstractTrait> traits = script.traits;

			for( final AbstractTrait trait : traits )
			{
				final Method search = findMethod( trait );

				if( null != search && search == method )
				{
					final PropertyInfo propertyInfo = new PropertyInfo();

					propertyInfo.instance = null;
					propertyInfo.isFinal = true;
					propertyInfo.method = method;
					propertyInfo.script = script;
					propertyInfo.trait = trait;

					return propertyInfo;
				}
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
		switch( property.kind )
		{
			case MultinameL:
			case MultinameLA:
			case RTQNameLA:
			case RTQNameL:
				return null;
		}

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
					+ StringConverter.toString( object ) + " = "
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
