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

import java.util.Iterator;

import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class LocalCountAnalyzer
{
	public int getLocalCount( final Bytecode bytecode )
	{
		int localCount = 0;

		final Iterator<AbstractOperation> iter = bytecode.listIterator();

		while( iter.hasNext() )
		{
			final AbstractOperation op = iter.next();
			final int code = op.code;

			switch( code )
			{
				case Op.GetLocal:
					final GetLocal getLocal = (GetLocal)op;
					if( ( getLocal.register + 1 ) > localCount )
					{
						localCount = getLocal.register + 1;
					}
					break;

				case Op.SetLocal:
					final SetLocal setLocal = (SetLocal)op;
					if( ( setLocal.register + 1 ) > localCount )
					{
						localCount = setLocal.register + 1;
					}
					break;

				case Op.GetLocal0:
				case Op.SetLocal0:
					if( 1 > localCount )
					{
						localCount = 1;
					}
					break;

				case Op.GetLocal1:
				case Op.SetLocal1:
					if( 2 > localCount )
					{
						localCount = 2;
					}
					break;

				case Op.GetLocal2:
				case Op.SetLocal2:
					if( 3 > localCount )
					{
						localCount = 3;
					}
					break;

				case Op.GetLocal3:
				case Op.SetLocal3:
					if( 4 > localCount )
					{
						localCount = 4;
					}
					break;
			}
		}

		return localCount;
	}
}
