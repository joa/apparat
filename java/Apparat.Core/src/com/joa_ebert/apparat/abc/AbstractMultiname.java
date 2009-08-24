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

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class AbstractMultiname
{
	public final MultinameKind kind;

	protected AbstractMultiname( final MultinameKind kind )
	{
		this.kind = kind;
	}

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );
	}

	public boolean equals( final AbstractMultiname other )
	{
		return kind.equals( other.kind );
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof AbstractMultiname )
		{
			return equals( (AbstractMultiname)other );
		}

		return false;
	}
}
