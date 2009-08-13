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

package com.joa_ebert.apparat.abc.bytecode.analysis;

import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.controlflow.Vertex;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.controlflow.export.AbstractVertexLabelProvider;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class BytecodeVertex extends Vertex
{
	public static final class LabelProvider extends
			AbstractVertexLabelProvider<BytecodeVertex>
	{
		@Override
		public String toString( final BytecodeVertex vertex )
		{
			if( vertex.kind == VertexKind.Entry )
			{
				return "[[Entry]]";
			}
			else if( vertex.kind == VertexKind.Exit )
			{
				return "[[Exit]]";
			}
			else
			{
				return Op.codeToString( vertex.getOperation().code );
			}
		}
	}

	private final AbstractOperation operation;

	public BytecodeVertex( final AbstractOperation operation )
	{
		super( VertexKind.Default );

		this.operation = operation;
	}

	BytecodeVertex( final VertexKind kind )
	{
		super( kind );

		this.operation = null;
	}

	public boolean equals( final BytecodeVertex other )
	{
		if( null == other )
		{
			return false;
		}

		if( kind == other.kind )
		{
			if( kind == VertexKind.Default )
			{
				// if( null == operation && other.operation != null )
				// {
				// return false;
				// }
				// else if( null == operation && null == other.operation )
				// {
				// return true;
				// }

				return operation.equals( other.operation );
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean equals( final Object other )
	{
		if( other instanceof BytecodeVertex )
		{
			return equals( (BytecodeVertex)other );
		}

		return false;
	}

	public AbstractOperation getOperation()
	{
		return operation;
	}

	@Override
	public String toString()
	{
		final String opString = ( VertexKind.Default == kind ) ? getOperation()
				.toString() : kind.toString();

		return "[BytecodeVertex " + opString + "]";
	}
}
