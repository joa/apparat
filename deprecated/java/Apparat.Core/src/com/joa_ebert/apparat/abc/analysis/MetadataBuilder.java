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
 * @author Joa Ebert
 * 
 */
public final class MetadataBuilder implements IAbcVisitor
{

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Abc abc )
	{
		abc.metadata.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context,
			final AbstractMultiname multiname )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final AbstractTrait trait )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Class klass )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final ConstantPool constantPool )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context,
			final ExceptionHandler exceptionHandler )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Instance instance )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Metadata metadata )
	{
		if( !context.getAbc().metadata.contains( metadata ) )
		{
			context.getAbc().metadata.add( metadata );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Method method )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final MethodBody methodBody )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Multiname multiname )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final MultinameA multinameA )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final MultinameL multinameL )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final MultinameLA multinameLA )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Namespace namespace )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final NamespaceSet namespaceSet )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Parameter parameter )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final QName name )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final QNameA nameA )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final RTQName rtqName )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final RTQNameA rtqNameA )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final RTQNameL rtqNameL )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final RTQNameLA rtqNameLA )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Script script )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitClass klass )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitConst konst )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitFunction function )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitGetter getter )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitMethod method )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitSetter setter )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final TraitSlot slot )
	{

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit( final AbcContext context, final Typename typename )
	{

	}
}
