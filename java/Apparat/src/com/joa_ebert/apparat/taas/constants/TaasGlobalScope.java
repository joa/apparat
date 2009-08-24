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

package com.joa_ebert.apparat.taas.constants;

import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceKind;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.types.MultinameType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasGlobalScope extends TaasConstant
{
	public static final TaasGlobalScope INSTANCE = new TaasGlobalScope();

	private TaasGlobalScope()
	{
		super( new MultinameType( new QName( new Namespace(
				NamespaceKind.PackageNamespace, "" ), "" ) ) );
	}

	@Override
	public String toString()
	{
		return "[TaasGlobalScope]";
	}
}
