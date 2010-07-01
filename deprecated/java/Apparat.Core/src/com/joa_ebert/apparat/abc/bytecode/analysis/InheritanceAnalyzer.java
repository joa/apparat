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

package com.joa_ebert.apparat.abc.bytecode.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.Instance;

/**
 * @author Joa Ebert
 * 
 */
public final class InheritanceAnalyzer
{
	public void finalize( final AbcEnvironment abcEnvironment )
	{
		final Set<AbstractMultiname> set = new HashSet<AbstractMultiname>();

		for( final AbcContext abcContext : abcEnvironment.getContexts() )
		{
			final Abc abc = abcContext.getAbc();

			for( final Instance instance : abc.instances )
			{
				if( null != instance.base )
				{
					set.add( instance.base );
				}
			}
		}

		for( final AbcContext abcContext : abcEnvironment.getContexts() )
		{
			final Abc abc = abcContext.getAbc();

			for( final Instance instance : abc.instances )
			{
				if( !instance.isFinal && !set.contains( instance.name ) )
				{
					instance.isFinal = true;
				}
			}
		}
	}

	public void finalize( final List<Instance> instances )
	{
		final List<AbstractMultiname> map = new LinkedList<AbstractMultiname>();

		for( final Instance instance : instances )
		{
			if( null != instance.base )
			{
				map.add( instance.base );
			}
		}

		for( final Instance instance : instances )
		{
			if( !instance.isFinal && instance.isSealed
					&& !map.contains( instance.name ) )
			{
				instance.isFinal = true;
			}
		}
	}
}
