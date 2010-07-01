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

import com.joa_ebert.apparat.controlflow.Vertex;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.controlflow.export.AbstractVertexLabelProvider;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasVertex extends Vertex
{
	public static final class LabelProvider extends
			AbstractVertexLabelProvider<TaasVertex>
	{
		@Override
		public String toString( final TaasVertex vertex )
		{
			switch( vertex.kind )
			{
				case Default:
					return vertex.value.toString();
				case Entry:
					return "[[Entry]]";
				case Exit:
					return "[[Exit]]";
			}

			// Unreachable:
			return "";
		}
	}

	public static final TaasVertex ENTRY_POINT = new TaasVertex(
			VertexKind.Entry );

	public static final TaasVertex EXIT_POINT = new TaasVertex( VertexKind.Exit );

	public TaasValue value;

	public TaasVertex( final TaasValue value )
	{
		super( VertexKind.Default );
		this.value = value;
	}

	private TaasVertex( final VertexKind kind )
	{
		super( kind );
		value = null;
	}

	@Override
	public String toString()
	{
		return "[TaasVertex value: "
				+ ( ( null != value ) ? value.toString() : "(null)" ) + "]";
	}
}
