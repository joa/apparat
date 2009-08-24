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

package com.joa_ebert.apparat.taas.types;

/**
 * The UnknownType represents a type which is unknown at compile time.
 * 
 * Imagine this case:
 * 
 * var ns: Namespace = getNamespace(Math.random()); var name: String =
 * getName(Math.random()); trace( this.ns::name );
 * 
 * But the value of this.ns::name is not known at all. Even worse: It might
 * change all the time.
 * 
 * @author Joa Ebert
 * 
 */
public final class UnknownType extends TaasType
{
	public static final UnknownType INSTANCE = new UnknownType();

	private UnknownType()
	{
	}

	@Override
	public boolean equals( final Object other )
	{
		return false;
	}

	public boolean equals( final UnknownType other )
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "[UnknownType]";
	}
}
