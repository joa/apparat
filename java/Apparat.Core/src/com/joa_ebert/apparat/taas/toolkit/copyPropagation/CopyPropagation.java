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

package com.joa_ebert.apparat.taas.toolkit.copyPropagation;

import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.utils.DepthFirstIterator;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.expr.TJump;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class CopyPropagation implements ITaasTool
{
	private boolean changed;

	public boolean manipulate( final TaasMethod method )
	{
		changed = false;

		try
		{
			final DepthFirstIterator<TaasVertex, TaasEdge> iter = new DepthFirstIterator<TaasVertex, TaasEdge>(
					method.code );

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( vertex.value instanceof TaasConstant
						&& vertex.value.isConstant() )
				{
					TaasToolkit.remove( method, vertex );

					changed = true;
				}
				else if( vertex.value instanceof TaasLocal )
				{
					TaasToolkit.remove( method, vertex );

					changed = true;
				}
				else if( vertex.value instanceof TJump )
				{
					TaasToolkit.remove( method, vertex );
				}
			}
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}

		return changed;
	}
}
