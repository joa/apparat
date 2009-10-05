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

import java.util.List;

import com.joa_ebert.apparat.abc.multinames.QName;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Instance implements ITraitsOwner
{
	public Abc abc;
	public Class klass;

	public QName name;

	public AbstractMultiname base;

	public boolean isSealed;

	public boolean isFinal;

	public boolean isInterface;

	public Namespace protectedNamespace;

	public List<AbstractMultiname> interfaces;

	public int instanceInitializerIndex;

	@AbcBinding
	public Method instanceInitializer;

	public List<AbstractTrait> traits;

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );

		if( null != name )
		{
			name.accept( context, visitor );
		}

		if( null != base )
		{
			base.accept( context, visitor );
		}

		if( null != protectedNamespace )
		{
			protectedNamespace.accept( context, visitor );
		}

		if( null != interfaces )
		{
			for( final AbstractMultiname interfaceName : interfaces )
			{
				interfaceName.accept( context, visitor );
			}
		}

		if( null != traits )
		{
			for( final AbstractTrait trait : traits )
			{
				trait.accept( context, visitor );
			}
		}
	}

	@Override
	public List<AbstractTrait> getTraits()
	{
		return traits;
	}
}
