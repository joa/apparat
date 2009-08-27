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

import com.joa_ebert.apparat.abc.MethodBody;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasEmitter
{
	public MethodBody emit( final TaasMethod method )
	{
		//
		// Assumptions for now:
		//
		// 1) We assume that we are not in SSA form.
		// 2) We assume all TaasPhi vertices have been resolved for us.
		// 

		//
		// Strategy:
		// 1) Convert CFG into a linked list of instructions
		// 2) Insert TaasJump for non-reachable edges
		// 3) Convert linked list to bytecode
		// 4) Solve backward jumps
		// 5) Add labels
		// 6) Done?
		//

		return null;
	}
}
