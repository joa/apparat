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

package com.joa_ebert.apparat.taas.expr;

import com.joa_ebert.apparat.taas.TaasExpression;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.constants.TaasInt;
import com.joa_ebert.apparat.taas.types.ClassType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TNewClass extends TaasExpression
{
	@TaasReference
	public TaasValue base;

	@TaasReference
	public TaasInt index;

	public TNewClass( final TaasValue base, final TaasInt index )
	{
		super( ClassType.INSTANCE );

		this.base = base;
		this.index = index;
	}

	@Override
	public String toString()
	{
		return "[TNewClass " + base.toString() + ", " + index.toString() + "]";
	}
}
