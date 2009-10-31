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

package com.joa_ebert.apparat.abc.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import com.joa_ebert.apparat.abc.ConstantPool;
import com.joa_ebert.apparat.abc.bytecode.operations.ApplyType;
import com.joa_ebert.apparat.abc.bytecode.operations.AsType;
import com.joa_ebert.apparat.abc.bytecode.operations.Call;
import com.joa_ebert.apparat.abc.bytecode.operations.CallMethod;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropLex;
import com.joa_ebert.apparat.abc.bytecode.operations.CallPropVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.CallProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.CallStatic;
import com.joa_ebert.apparat.abc.bytecode.operations.CallSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.CallSuperVoid;
import com.joa_ebert.apparat.abc.bytecode.operations.Coerce;
import com.joa_ebert.apparat.abc.bytecode.operations.Construct;
import com.joa_ebert.apparat.abc.bytecode.operations.ConstructProp;
import com.joa_ebert.apparat.abc.bytecode.operations.ConstructSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.Debug;
import com.joa_ebert.apparat.abc.bytecode.operations.DebugFile;
import com.joa_ebert.apparat.abc.bytecode.operations.DebugLine;
import com.joa_ebert.apparat.abc.bytecode.operations.DecLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.DecLocalInt;
import com.joa_ebert.apparat.abc.bytecode.operations.DefaultXmlNamespace;
import com.joa_ebert.apparat.abc.bytecode.operations.DeleteProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.FindPropStrict;
import com.joa_ebert.apparat.abc.bytecode.operations.FindProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetDescendants;
import com.joa_ebert.apparat.abc.bytecode.operations.GetGlobalSlot;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLex;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.GetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.GetScopeObject;
import com.joa_ebert.apparat.abc.bytecode.operations.GetSlot;
import com.joa_ebert.apparat.abc.bytecode.operations.GetSuper;
import com.joa_ebert.apparat.abc.bytecode.operations.HasNext2;
import com.joa_ebert.apparat.abc.bytecode.operations.IncLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.IncLocalInt;
import com.joa_ebert.apparat.abc.bytecode.operations.InitProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.IsType;
import com.joa_ebert.apparat.abc.bytecode.operations.Kill;
import com.joa_ebert.apparat.abc.bytecode.operations.LookupSwitch;
import com.joa_ebert.apparat.abc.bytecode.operations.NewArray;
import com.joa_ebert.apparat.abc.bytecode.operations.NewCatch;
import com.joa_ebert.apparat.abc.bytecode.operations.NewClass;
import com.joa_ebert.apparat.abc.bytecode.operations.NewFunction;
import com.joa_ebert.apparat.abc.bytecode.operations.NewObject;
import com.joa_ebert.apparat.abc.bytecode.operations.PushByte;
import com.joa_ebert.apparat.abc.bytecode.operations.PushDouble;
import com.joa_ebert.apparat.abc.bytecode.operations.PushInt;
import com.joa_ebert.apparat.abc.bytecode.operations.PushNamespace;
import com.joa_ebert.apparat.abc.bytecode.operations.PushShort;
import com.joa_ebert.apparat.abc.bytecode.operations.PushString;
import com.joa_ebert.apparat.abc.bytecode.operations.PushUInt;
import com.joa_ebert.apparat.abc.bytecode.operations.SetGlobalSlot;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.SetProperty;
import com.joa_ebert.apparat.abc.bytecode.operations.SetSlot;
import com.joa_ebert.apparat.abc.bytecode.operations.SetSuper;
import com.joa_ebert.apparat.abc.io.AbcOutputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class BytecodeEncoder
{
	private final ConstantPool pool;

	public BytecodeEncoder( final ConstantPool pool )
	{
		this.pool = pool;
	}

	public byte[] encode( final Bytecode bytecode ) throws IOException,
			MarkerException
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream(
				bytecode.size() );
		final AbcOutputStream output = new AbcOutputStream( buffer );
		final Iterator<AbstractOperation> iter = bytecode.listIterator();
		final MarkerManager markers = bytecode.markers;

		markers.prepareMarkers();

		while( iter.hasNext() )
		{
			final AbstractOperation operation = iter.next();
			final int code = operation.code;

			operation.position = (int)output.getPosition();

			if( markers.hasMarkerFor( operation ) )
			{
				final Marker marker = markers.getMarkerFor( operation );

				marker.position = operation.position;
			}

			output.writeU08( code );

			switch( code )
			{
				case Op.AsType:
					output
							.writeU30( pool
									.getIndex( ( (AsType)operation ).type ) );
					break;

				case Op.ApplyType:
					output.writeU30( ( (ApplyType)operation ).typeSize );
					break;

				case Op.Call:
					output.writeU30( ( (Call)operation ).numArguments );
					break;

				case Op.CallMethod:
					output.writeU30( ( (CallMethod)operation ).methodIndex );
					output.writeU30( ( (CallMethod)operation ).numArguments );
					break;

				case Op.CallProperty:
					output.writeU30( pool
							.getIndex( ( (CallProperty)operation ).property ) );
					output.writeU30( ( (CallProperty)operation ).numArguments );
					break;

				case Op.CallPropLex:
					output.writeU30( pool
							.getIndex( ( (CallPropLex)operation ).property ) );
					output.writeU30( ( (CallPropLex)operation ).numArguments );
					break;

				case Op.CallPropVoid:
					output.writeU30( pool
							.getIndex( ( (CallPropVoid)operation ).property ) );
					output.writeU30( ( (CallPropVoid)operation ).numArguments );
					break;

				case Op.CallStatic:
					output.writeU30( ( (CallStatic)operation ).methodIndex );
					output.writeU30( ( (CallStatic)operation ).numArguments );
					break;

				case Op.CallSuper:
					output.writeU30( pool
							.getIndex( ( (CallSuper)operation ).name ) );
					output.writeU30( ( (CallSuper)operation ).numArguments );
					break;

				case Op.CallSuperVoid:
					output.writeU30( pool
							.getIndex( ( (CallSuperVoid)operation ).name ) );
					output.writeU30( ( (CallSuperVoid)operation ).numArguments );
					break;

				case Op.Coerce:
					output
							.writeU30( pool
									.getIndex( ( (Coerce)operation ).type ) );
					break;

				case Op.Construct:
					output.writeU30( ( (Construct)operation ).numArguments );
					break;

				case Op.ConstructProp:
					output.writeU30( pool
							.getIndex( ( (ConstructProp)operation ).property ) );
					output.writeU30( ( (ConstructProp)operation ).numArguments );
					break;

				case Op.ConstructSuper:
					output
							.writeU30( ( (ConstructSuper)operation ).numArguments );
					break;

				case Op.Debug:
					final Debug debugOperation = (Debug)operation;
					output.writeU08( debugOperation.type );
					output.writeU30( pool.getIndex( debugOperation.name ) );
					output.writeU08( debugOperation.register );
					output.writeU30( 0x00 );// unused
					break;

				case Op.DebugFile:
					output.writeU30( pool
							.getIndex( ( (DebugFile)operation ).fileName ) );
					break;

				case Op.DebugLine:
					output.writeU30( ( (DebugLine)operation ).lineNumber );
					break;

				case Op.DecLocal:
					output.writeU30( ( (DecLocal)operation ).register );
					break;

				case Op.DecLocalInt:
					output.writeU30( ( (DecLocalInt)operation ).register );
					break;

				case Op.DeleteProperty:
					output
							.writeU30( pool
									.getIndex( ( (DeleteProperty)operation ).property ) );
					break;

				case Op.DefaultXmlNamespace:
					output
							.writeU30( pool
									.getIndex( ( (DefaultXmlNamespace)operation ).uri ) );
					break;

				case Op.FindProperty:
					output.writeU30( pool
							.getIndex( ( (FindProperty)operation ).property ) );
					break;

				case Op.FindPropStrict:
					output
							.writeU30( pool
									.getIndex( ( (FindPropStrict)operation ).property ) );
					break;

				case Op.GetDescendants:
					output.writeU30( pool
							.getIndex( ( (GetDescendants)operation ).name ) );
					break;

				case Op.GetGlobalSlot:
					output.writeU30( ( (GetGlobalSlot)operation ).slotIndex );
					break;

				case Op.GetLex:
					output.writeU30( pool
							.getIndex( ( (GetLex)operation ).property ) );
					break;

				case Op.GetLocal:
					output.writeU30( ( (GetLocal)operation ).register );
					break;

				case Op.GetProperty:
					output.writeU30( pool
							.getIndex( ( (GetProperty)operation ).property ) );
					break;

				case Op.GetScopeObject:
					output.writeU08( ( (GetScopeObject)operation ).scopeIndex );
					break;

				case Op.GetSlot:
					output.writeU30( ( (GetSlot)operation ).slotIndex );
					break;

				case Op.GetSuper:
					output.writeU30( pool
							.getIndex( ( (GetSuper)operation ).property ) );
					break;

				case Op.HasNext2:
					output.writeU32( ( (HasNext2)operation ).objectRegister );
					output.writeU32( ( (HasNext2)operation ).indexRegister );
					break;

				case Op.IfEqual:
				case Op.IfFalse:
				case Op.IfGreaterEqual:
				case Op.IfGreaterThan:
				case Op.IfLessEqual:
				case Op.IfLessThan:
				case Op.IfNotEqual:
				case Op.IfNotGreaterEqual:
				case Op.IfNotGreaterThan:
				case Op.IfNotLessEqual:
				case Op.IfNotLessThan:
				case Op.IfStrictEqual:
				case Op.IfStrictNotEqual:
				case Op.IfTrue:
				case Op.Jump:
					//
					// Gets Patched.
					//
					output.writeS24( 0 );
					markers.patch( operation );
					break;

				case Op.IncLocal:
					output.writeU30( ( (IncLocal)operation ).register );
					break;

				case Op.IncLocalInt:
					output.writeU30( ( (IncLocalInt)operation ).register );
					break;

				case Op.InitProperty:
					output.writeU30( pool
							.getIndex( ( (InitProperty)operation ).property ) );
					break;

				case Op.IsType:
					output
							.writeU30( pool
									.getIndex( ( (IsType)operation ).type ) );
					break;

				case Op.Kill:
					output.writeU30( ( (Kill)operation ).register );
					break;

				case Op.LookupSwitch:
					final LookupSwitch lookupSwitch = (LookupSwitch)operation;

					//
					// Gets Patched.
					//
					output.writeS24( 0 );

					final int caseCount = lookupSwitch.caseMarkers.size();

					output.writeU30( caseCount - 1 );

					for( int i = 0; i < caseCount; ++i )
					{
						//
						// Gets Patched.
						//
						output.writeS24( 0 );
					}

					markers.patch( operation );
					break;

				case Op.NewArray:
					output.writeU30( ( (NewArray)operation ).numArguments );
					break;

				case Op.NewCatch:
					output
							.writeU30( ( (NewCatch)operation ).exceptionHandlerIndex );
					break;

				case Op.NewClass:
					output.writeU30( ( (NewClass)operation ).classIndex );
					break;

				case Op.NewFunction:
					output.writeU30( ( (NewFunction)operation ).functionIndex );
					break;

				case Op.NewObject:
					output.writeU30( ( (NewObject)operation ).numProperties );
					break;

				case Op.PushByte:
					output.writeU08( ( (PushByte)operation ).value );
					break;

				case Op.PushDouble:
					output.writeU30( pool
							.getIndex( ( (PushDouble)operation ).value ) );
					break;

				case Op.PushInt:
					output.writeU30( pool
							.getNonZeroIndex( ( (PushInt)operation ).value ) );
					break;

				case Op.PushNamespace:
					output.writeU30( pool
							.getIndex( ( (PushNamespace)operation ).value ) );
					break;

				case Op.PushShort:
					output.writeU30( ( (PushShort)operation ).value );
					break;

				case Op.PushString:
					output.writeU30( pool
							.getIndex( ( (PushString)operation ).value ) );
					break;

				case Op.PushUInt:
					output.writeU30( pool
							.getNonZeroIndex( ( (PushUInt)operation ).value ) );
					break;

				case Op.SetLocal:
					output.writeU30( ( (SetLocal)operation ).register );
					break;

				case Op.SetGlobalSlot:
					output.writeU30( ( (SetGlobalSlot)operation ).slotIndex );
					break;

				case Op.SetProperty:
					output.writeU30( pool
							.getIndex( ( (SetProperty)operation ).property ) );
					break;

				case Op.SetSlot:
					output.writeU30( ( (SetSlot)operation ).slotIndex );
					break;

				case Op.SetSuper:
					output.writeU30( pool
							.getIndex( ( (SetSuper)operation ).property ) );
					break;
			}
		}

		output.flush();
		output.close();

		final byte[] bytes = buffer.toByteArray();

		markers.applyPatches( bytes );

		return bytes;
	}
}
