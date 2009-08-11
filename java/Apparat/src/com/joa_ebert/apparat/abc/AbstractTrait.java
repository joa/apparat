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

import com.joa_ebert.apparat.abc.multinames.QName;

/**
 * 
 * @author Joa Ebert
 * 
 */
public abstract class AbstractTrait
{
	public final TraitKind kind;

	public QName name;

	public List<Metadata> metadata;

	protected AbstractTrait( final TraitKind kind )
	{
		this.kind = kind;

		name = ConstantPool.EMPTY_MULTINAME;

		metadata = new LinkedList<Metadata>();
	}

	public void accept( final AbcContext context, final IAbcVisitor visitor )
	{
		visitor.visit( context, this );
	}
}
