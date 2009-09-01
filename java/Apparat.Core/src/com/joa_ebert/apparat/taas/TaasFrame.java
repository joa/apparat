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

package com.joa_ebert.apparat.taas;

/**
 * 
 * @author Joa Ebert
 * 
 */
final class TaasFrame
{
	public final TaasStack operandStack;
	public final TaasStack scopeStack;
	public final TaasRegisters localRegisters;

	public TaasFrame( final TaasFrame original )
	{
		this( original.operandStack, original.scopeStack,
				original.localRegisters );
	}

	public TaasFrame( final TaasStack operandStack, final TaasStack scopeStack,
			final TaasRegisters localRegisters )
	{
		this.operandStack = operandStack.clone();
		this.scopeStack = scopeStack.clone();
		this.localRegisters = localRegisters.clone();
	}

	@Override
	public TaasFrame clone()
	{
		return new TaasFrame( operandStack, scopeStack, localRegisters );
	}
}
