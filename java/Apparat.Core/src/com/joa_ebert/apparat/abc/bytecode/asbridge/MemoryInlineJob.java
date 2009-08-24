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

package com.joa_ebert.apparat.abc.bytecode.asbridge;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodeAnalysis;
import com.joa_ebert.apparat.abc.bytecode.analysis.DeadCodeElimination;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.CallProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetByte;
import com.joa_ebert.apparat.abc.bytecode.operations.GetDouble;
import com.joa_ebert.apparat.abc.bytecode.operations.GetFloat;
import com.joa_ebert.apparat.abc.bytecode.operations.GetInt;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLex;
import com.joa_ebert.apparat.abc.bytecode.operations.GetShort;
import com.joa_ebert.apparat.abc.bytecode.operations.SetByte;
import com.joa_ebert.apparat.abc.bytecode.operations.SetDouble;
import com.joa_ebert.apparat.abc.bytecode.operations.SetFloat;
import com.joa_ebert.apparat.abc.bytecode.operations.SetInt;
import com.joa_ebert.apparat.abc.bytecode.operations.SetShort;
import com.joa_ebert.apparat.abc.bytecode.operations.Sign1;
import com.joa_ebert.apparat.abc.bytecode.operations.Sign16;
import com.joa_ebert.apparat.abc.bytecode.operations.Sign8;
import com.joa_ebert.apparat.abc.multinames.QName;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class MemoryInlineJob implements IActionScriptBridge
{
	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		final Iterator<AbstractOperation> iter = bytecode.listIterator();
		final LinkedList<AbstractOperation> removes = new LinkedList<AbstractOperation>();
		final Map<AbstractOperation, AbstractOperation> replacements = new LinkedHashMap<AbstractOperation, AbstractOperation>();

		boolean modified = false;
		int balance = 0;

		while( iter.hasNext() )
		{
			final AbstractOperation op = iter.next();
			final int code = op.code;

			if( code == Op.GetLex )
			{
				final GetLex getLex = (GetLex)op;

				if( getLex.property.equals( Settings.MEMORY_QNAME ) )
				{
					removes.add( getLex );
					++balance;
				}
			}
			else if( code == Op.CallPropVoid && balance > 0 )
			{
				final CallPropVoid callPropVoid = (CallPropVoid)op;
				final QName qname = (QName)callPropVoid.property;
				final String property = qname.name;

				AbstractOperation replacement = null;

				if( property.equals( "writeByte" ) )
				{
					replacement = new SetByte();
				}
				else if( property.equals( "writeShort" ) )
				{
					replacement = new SetShort();
				}
				else if( property.equals( "writeInt" ) )
				{
					replacement = new SetInt();
				}
				else if( property.equals( "writeFloat" ) )
				{
					replacement = new SetFloat();
				}
				else if( property.equals( "writeDouble" ) )
				{
					replacement = new SetDouble();
				}
				else if( property.equals( "select" ) )
				{
					removes.removeLast();
				}

				if( null != replacement )
				{
					--balance;
					replacements.put( callPropVoid, replacement );
					modified = true;
				}
			}
			else if( code == Op.CallProperty && balance > 0 )
			{
				final CallProperty callProperty = (CallProperty)op;
				final QName qname = (QName)callProperty.property;
				final String property = qname.name;

				AbstractOperation replacement = null;

				if( property.equals( "readUnsignedByte" ) )
				{
					replacement = new GetByte();
				}
				else if( property.equals( "readUnsignedShort" ) )
				{
					replacement = new GetShort();
				}
				else if( property.equals( "readInt" ) )
				{
					replacement = new GetInt();
				}
				else if( property.equals( "readFloat" ) )
				{
					replacement = new GetFloat();
				}
				else if( property.equals( "readDouble" ) )
				{
					replacement = new GetDouble();
				}
				else if( property.equals( "signExtend1" ) )
				{
					replacement = new Sign1();
				}
				else if( property.equals( "signExtend8" ) )
				{
					replacement = new Sign8();
				}
				else if( property.equals( "signExtend16" ) )
				{
					replacement = new Sign16();
				}

				if( null != replacement )
				{
					--balance;
					replacements.put( callProperty, replacement );
					modified = true;
				}
			}
		}

		if( modified )
		{
			for( final AbstractOperation opToRemove : removes )
			{
				bytecode.remove( opToRemove );
			}

			for( final Entry<AbstractOperation, AbstractOperation> entry : replacements
					.entrySet() )
			{
				bytecode.replace( entry.getKey(), entry.getValue() );
			}

			new DeadCodeElimination().interpret( environment, bytecode );

			final BytecodeAnalysis analysis = new BytecodeAnalysis(
					environment, bytecode );

			analysis.updateAll();
		}
	}
}
