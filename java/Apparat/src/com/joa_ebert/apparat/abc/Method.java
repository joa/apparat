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

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Method
{
	public List<Parameter> parameters;

	public AbstractMultiname returnType;

	public String name;

	public boolean needsArguments;

	public boolean needsActivation;

	public boolean needsRest;

	public boolean hasOptionalParameters;

	public boolean setsDXNS;

	public boolean hasParameterNames;

	public MethodBody body;

	public Method()
	{
		parameters = new LinkedList<Parameter>();
	}

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );

		if( null != parameters )
		{
			for( final Parameter parameter : parameters )
			{
				parameter.accept( context, visitor );
			}
		}

		if( null != returnType )
		{
			returnType.accept( context, visitor );
		}

		if( null != body )
		{
			body.accept( context, visitor );
		}

	}

	public void accept( final AbcContext context, final IMethodVisitor visitor )
	{
		visitor.visit( context, this );
	}
}
