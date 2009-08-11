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

package com.joa_ebert.apparat.abc.multinames;

import java.util.List;

import com.joa_ebert.apparat.abc.AbcContext;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.IAbcVisitor;
import com.joa_ebert.apparat.abc.MultinameKind;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class Typename extends QName
{
	public QName name;

	public List<AbstractMultiname> parameters;// typename or qname

	public Typename()
	{
		super( MultinameKind.Typename );
	}

	@Override
	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );

		if( null != name )
		{
			name.accept( context, visitor );
		}

		if( null != parameters )
		{
			for( final AbstractMultiname multiname : parameters )
			{
				multiname.accept( context, visitor );
			}
		}
	}

	@Override
	public boolean equals( final AbstractMultiname other )
	{
		if( other instanceof Typename )
		{
			return equals( (Typename)other );
		}

		return false;
	}

	public boolean equals( final Typename other )
	{
		//
		// We are screwed if the name is the same but the parameters not.
		//

		return name.equals( other.name );
	}
}
