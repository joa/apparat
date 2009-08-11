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

import com.joa_ebert.apparat.abc.controlflow.Edge;
import com.joa_ebert.apparat.abc.controlflow.EdgeKind;
import com.joa_ebert.apparat.abc.controlflow.export.AbstractEdgeLabelProvider;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasEdge extends Edge<TaasVertex>
{
	public static final class LabelProvider extends
			AbstractEdgeLabelProvider<TaasVertex, TaasEdge>
	{
		@Override
		public String toString( final TaasEdge edge )
		{
			switch( edge.kind )
			{
				case Case:
					return "case";
				case Default:
					return null;
				case DefaultCase:
					return "default";
				case False:
					return "false";
				case Throw:
					return "throw";
				case Jump:
					return "jump";
				case True:
					return "true";
			}

			// Unreachable.
			return null;
		}
	}

	public TaasEdge( final TaasVertex startVertex, final TaasVertex endVertex )
	{
		this( startVertex, endVertex, EdgeKind.Default );
	}

	public TaasEdge( final TaasVertex startVertex, final TaasVertex endVertex,
			final EdgeKind kind )
	{
		super( startVertex, endVertex, kind );
	}

	@Override
	public String toString()
	{
		return "[TaasEdge " + startVertex.toString() + " -> "
				+ endVertex.toString() + "]";
	}
}
