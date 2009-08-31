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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.ConstantPool;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
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
import com.joa_ebert.apparat.abc.bytecode.operations.Jump;
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
import com.joa_ebert.apparat.abc.io.AbcInputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class BytecodeDecoder
{
	private static final OperationFactory factory = new OperationFactory();

	private final ConstantPool pool;

	public BytecodeDecoder( final ConstantPool constantPool )
	{
		this.pool = constantPool;
	}

	public Bytecode decode( final AbcInputStream input ) throws IOException,
			MarkerException
	{
		final Bytecode result = new Bytecode();
		final MarkerManager markers = result.markers;

		while( input.available() > 0 )
		{
			final int position = (int)input.getPosition();
			final int code = input.read();
			final AbstractOperation operation = factory.create( code );

			if( null == operation )
			{
				continue;
			}

			operation.position = position;

			switch( code )
			{
				case Op.AsType:
					( (AsType)operation ).type = pool.getMultiname( input
							.readU30() );
					break;

				case Op.ApplyType:
					( (ApplyType)operation ).typeSize = input.readU30();
					break;

				case Op.Call:
					( (Call)operation ).numArguments = input.readU30();
					break;

				case Op.CallMethod:
					( (CallMethod)operation ).methodIndex = input.readU30();
					( (CallMethod)operation ).numArguments = input.readU30();
					break;

				case Op.CallProperty:
					( (CallProperty)operation ).property = pool
							.getMultiname( input.readU30() );
					( (CallProperty)operation ).numArguments = input.readU30();
					break;

				case Op.CallPropLex:
					( (CallPropLex)operation ).property = pool
							.getMultiname( input.readU30() );
					( (CallPropLex)operation ).numArguments = input.readU30();
					break;

				case Op.CallPropVoid:
					( (CallPropVoid)operation ).property = pool
							.getMultiname( input.readU30() );
					( (CallPropVoid)operation ).numArguments = input.readU30();
					break;

				case Op.CallStatic:
					( (CallStatic)operation ).methodIndex = input.readU30();
					( (CallStatic)operation ).numArguments = input.readU30();
					break;

				case Op.CallSuper:
					( (CallSuper)operation ).name = pool.getMultiname( input
							.readU30() );
					( (CallSuper)operation ).numArguments = input.readU30();
					break;

				case Op.CallSuperVoid:
					( (CallSuperVoid)operation ).name = pool
							.getMultiname( input.readU30() );
					( (CallSuperVoid)operation ).numArguments = input.readU30();
					break;

				case Op.Coerce:
					( (Coerce)operation ).type = pool.getMultiname( input
							.readU30() );
					break;

				case Op.Construct:
					( (Construct)operation ).numArguments = input.readU30();
					break;

				case Op.ConstructProp:
					( (ConstructProp)operation ).property = pool
							.getMultiname( input.readU30() );
					( (ConstructProp)operation ).numArguments = input.readU30();
					break;

				case Op.ConstructSuper:
					( (ConstructSuper)operation ).numArguments = input
							.readU30();
					break;

				case Op.Debug:
					final Debug debugOperation = (Debug)operation;
					debugOperation.type = input.readU08();
					debugOperation.name = pool.getString( input.readU30() );
					debugOperation.register = input.readU08();
					input.readU30();// unused
					break;

				case Op.DebugFile:
					( (DebugFile)operation ).fileName = pool.getString( input
							.readU30() );
					break;

				case Op.DebugLine:
					( (DebugLine)operation ).lineNumber = input.readU30();
					break;

				case Op.DecLocal:
					( (DecLocal)operation ).register = input.readU30();
					break;

				case Op.DecLocalInt:
					( (DecLocalInt)operation ).register = input.readU30();
					break;

				case Op.DeleteProperty:
					( (DeleteProperty)operation ).property = pool
							.getMultiname( input.readU30() );
					break;

				case Op.DefaultXmlNamespace:
					( (DefaultXmlNamespace)operation ).uri = pool
							.getString( input.readU30() );
					break;

				case Op.FindProperty:
					( (FindProperty)operation ).property = pool
							.getMultiname( input.readU30() );
					break;

				case Op.FindPropStrict:
					( (FindPropStrict)operation ).property = pool
							.getMultiname( input.readU30() );
					break;

				case Op.GetDescendants:
					( (GetDescendants)operation ).name = pool
							.getMultiname( input.readU30() );
					break;

				case Op.GetGlobalSlot:
					( (GetGlobalSlot)operation ).slotIndex = input.readU30();
					break;

				case Op.GetLex:
					( (GetLex)operation ).property = pool.getMultiname( input
							.readU30() );
					break;

				case Op.GetLocal:
					( (GetLocal)operation ).register = input.readU30();
					break;

				case Op.GetProperty:
					( (GetProperty)operation ).property = pool
							.getMultiname( input.readU30() );
					break;

				case Op.GetScopeObject:
					( (GetScopeObject)operation ).scopeIndex = input.readU08();
					break;

				case Op.GetSlot:
					( (GetSlot)operation ).slotIndex = input.readU30();
					break;

				case Op.GetSuper:
					( (GetSuper)operation ).property = pool.getMultiname( input
							.readU30() );
					break;

				case Op.HasNext2:
					( (HasNext2)operation ).objectRegister = input.readU32();
					( (HasNext2)operation ).indexRegister = input.readU32();
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
					// The position advanced 4 bytes.
					//
					// The opcode is 1 byte, the S24 is 3 bytes.
					// Therefore we have to add 4 to the current position in
					// order to get the correct marker AFTER those bytes have
					// been consumed.
					//
					( (Jump)operation ).marker = markers.putMarkerAt( position
							+ 0x04 + input.readS24() );
					break;

				case Op.IncLocal:
					( (IncLocal)operation ).register = input.readU30();
					break;

				case Op.IncLocalInt:
					( (IncLocalInt)operation ).register = input.readU30();
					break;

				case Op.InitProperty:
					( (InitProperty)operation ).property = pool
							.getMultiname( input.readU30() );
					break;

				case Op.IsType:
					( (IsType)operation ).type = pool.getMultiname( input
							.readU30() );
					break;

				case Op.Kill:
					( (Kill)operation ).register = input.readU30();
					break;

				case Op.Label:
					markers.putMarkerAt( position );
					break;

				case Op.LookupSwitch:
					final LookupSwitch lookupSwitch = (LookupSwitch)operation;

					lookupSwitch.defaultMarker = markers.putMarkerAt( position
							+ input.readS24() );

					final int caseCount = input.readU30() + 1;

					lookupSwitch.caseMarkers = new ArrayList<Marker>( caseCount );

					for( int i = 0; i < caseCount; ++i )
					{
						lookupSwitch.caseMarkers.add( markers
								.putMarkerAt( position + input.readS24() ) );
					}
					break;

				case Op.NewArray:
					( (NewArray)operation ).numArguments = input.readU30();
					break;

				case Op.NewCatch:
					( (NewCatch)operation ).exceptionHandlerIndex = input
							.readU30();
					break;

				case Op.NewClass:
					( (NewClass)operation ).classIndex = input.readU30();
					break;

				case Op.NewFunction:
					( (NewFunction)operation ).functionIndex = input.readU30();
					break;

				case Op.NewObject:
					( (NewObject)operation ).numProperties = input.readU30();
					break;

				case Op.PushByte:
					( (PushByte)operation ).value = input.readU08();
					break;

				case Op.PushDouble:
					( (PushDouble)operation ).value = pool.getDouble( input
							.readU30() );
					break;

				case Op.PushInt:
					( (PushInt)operation ).value = pool
							.getInt( input.readU30() );
					break;

				case Op.PushNamespace:
					( (PushNamespace)operation ).value = pool
							.getNamespace( input.readU30() );
					break;

				case Op.PushShort:
					( (PushShort)operation ).value = input.readU30();
					break;

				case Op.PushString:
					( (PushString)operation ).value = pool.getString( input
							.readU30() );
					break;

				case Op.PushUInt:
					( (PushUInt)operation ).value = pool.getUInt( input
							.readU30() );
					break;

				case Op.SetLocal:
					( (SetLocal)operation ).register = input.readU30();
					break;

				case Op.SetGlobalSlot:
					( (SetGlobalSlot)operation ).slotIndex = input.readU30();
					break;

				case Op.SetProperty:
					( (SetProperty)operation ).property = pool
							.getMultiname( input.readU30() );
					break;

				case Op.SetSlot:
					( (SetSlot)operation ).slotIndex = input.readU30();
					break;

				case Op.SetSuper:
					( (SetSuper)operation ).property = pool.getMultiname( input
							.readU30() );
					break;
			}

			result.add( operation );
		}

		try
		{
			markers.solve();
		}
		catch( final MarkerException ex )
		{
			new BytecodePrinter( System.out ).interpret( new AbcEnvironment(),
					result );

			throw ex;
		}

		return result;
	}

	public Bytecode decode( final byte[] code ) throws IOException,
			MarkerException
	{
		return decode( new ByteArrayInputStream( code ) );
	}

	public Bytecode decode( final InputStream input ) throws IOException,
			MarkerException
	{
		return decode( new AbcInputStream( input ) );
	}
}
