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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.BytecodeDecoder;
import com.joa_ebert.apparat.abc.bytecode.MarkerException;
import com.joa_ebert.apparat.abc.bytecode.Op;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodeAnalysis;
import com.joa_ebert.apparat.abc.bytecode.analysis.DeadCodeElimination;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.FindPropStrict;
import com.joa_ebert.apparat.abc.bytecode.operations.PushByte;
import com.joa_ebert.apparat.abc.bytecode.operations.PushShort;
import com.joa_ebert.apparat.abc.io.AbcInputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class BytecodeInlineJob implements IActionScriptBridge
{
	private static final class BytecodeCall
	{
		private FindPropStrict from;
		private CallPropVoid to;

		public BytecodeCall()
		{
		}

		public FindPropStrict getFrom()
		{
			return from;
		}

		public CallPropVoid getTo()
		{
			return to;
		}

		public void setFrom( final FindPropStrict from )
		{
			this.from = from;
		}

		public void setTo( final CallPropVoid to )
		{
			this.to = to;
		}

	}

	private static final int M_FINDPROP = 0;
	private static final int M_CALLPROP = 1;

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		final Iterator<AbstractOperation> iter = bytecode.listIterator();
		final LinkedList<BytecodeCall> calls = new LinkedList<BytecodeCall>();

		int mode = M_FINDPROP;

		while( iter.hasNext() )
		{
			final AbstractOperation op = iter.next();
			final int code = op.code;

			if( M_FINDPROP == mode && code == Op.FindPropStrict )
			{
				final FindPropStrict findPropStrict = (FindPropStrict)op;

				if( findPropStrict.property.equals( Settings.BYTECODE_QNAME ) )
				{
					final BytecodeCall bytecodeCall = new BytecodeCall();

					bytecodeCall.setFrom( findPropStrict );

					calls.add( bytecodeCall );

					mode = M_CALLPROP;
				}
			}
			else if( M_CALLPROP == mode && code == Op.CallPropVoid )
			{
				final CallPropVoid callPropVoid = (CallPropVoid)op;

				if( callPropVoid.property.equals( Settings.BYTECODE_QNAME ) )
				{
					calls.getLast().setTo( callPropVoid );

					mode = M_FINDPROP;
				}
			}
		}

		boolean modified = false;

		for( final BytecodeCall call : calls )
		{
			if( call.getFrom() == null || call.getTo() == null )
			{
				// This is actually an error!
				continue;
			}

			final Iterator<AbstractOperation> searchIter = bytecode
					.listIterator( call.getFrom() );

			final List<AbstractOperation> opsToRemove = new LinkedList<AbstractOperation>();
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			while( searchIter.hasNext() )
			{
				final AbstractOperation op = searchIter.next();
				final int code = op.code;

				if( op == call.getTo() )
				{
					break;
				}

				if( code == Op.PushByte )
				{
					outputStream.write( ( (PushByte)op ).value );
					opsToRemove.add( op );
				}
				else if( code == Op.PushShort )
				{
					outputStream.write( ( (PushShort)op ).value );
					opsToRemove.add( op );
				}
				else if( code == Op.Debug || code == Op.DebugFile
						|| code == Op.DebugLine )
				{
					opsToRemove.add( op );
				}
				else
				{
					// This is an error!
				}
			}

			bytecode.remove( call.getTo() );

			for( final AbstractOperation op : opsToRemove )
			{
				bytecode.remove( op );
			}

			final BytecodeDecoder decoder = new BytecodeDecoder( environment
					.contextOf( bytecode.method ).getConstantPool() );

			final byte[] inlinedBytes = outputStream.toByteArray();

			try
			{
				outputStream.close();
			}
			catch( final IOException e )
			{
				e.printStackTrace();
				return;
			}

			try
			{
				final Bytecode newBytecode = decoder
						.decode( new AbcInputStream( new ByteArrayInputStream(
								inlinedBytes ) ) );

				bytecode.replace( call.getFrom(), newBytecode.getFirst() );

				int addIndex = bytecode.indexOf( newBytecode.getFirst() );

				final Iterator<AbstractOperation> newBytecodeIter = newBytecode
						.listIterator( 1 );

				while( newBytecodeIter.hasNext() )
				{
					bytecode.add( ++addIndex, newBytecodeIter.next() );
				}

				modified = true;
			}
			catch( final MarkerException e )
			{
				e.printStackTrace();
				return;
			}
			catch( final IOException e )
			{
				e.printStackTrace();
				return;
			}
		}

		if( modified )
		{
			new DeadCodeElimination().interpret( environment, bytecode );

			final BytecodeAnalysis analysis = new BytecodeAnalysis(
					environment, bytecode );

			analysis.updateAll();
		}
	}
}
