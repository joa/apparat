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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.SortedMap;
import java.util.TreeMap;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Script;
import com.joa_ebert.apparat.abc.bytecode.AbstractOperation;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.Marker;
import com.joa_ebert.apparat.abc.bytecode.Op;
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
import com.joa_ebert.apparat.abc.utils.StringConverter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class BytecodePrinter implements IInterpreter
{
	private static SortedMap<Integer, String> printNames;

	static
	{
		printNames = new TreeMap<Integer, String>();

		int maxLength = 0;

		for( int i = Op.MIN_VALUE; i <= Op.MAX_VALUE; ++i )
		{
			final int length = Op.codeToString( i ).length() + 1;

			if( length > maxLength )
			{
				maxLength = length;
			}
		}

		for( int i = Op.MIN_VALUE; i <= Op.MAX_VALUE; ++i )
		{
			final StringBuilder string = new StringBuilder( Op.codeToString( i ) );

			while( string.length() < maxLength )
			{
				string.append( ' ' );
			}

			printNames.put( i, string.toString() );
		}
	}

	protected static String escape( final String value )
	{
		return value.replace( "\"", "\\\"" );
	}

	protected static String markerToString( final Marker marker )
	{
		return "L" + Integer.toString( marker.key );
	}

	private final PrintWriter writer;

	public BytecodePrinter( final OutputStream output )
	{
		this( new PrintWriter( output ) );
	}

	public BytecodePrinter( final PrintWriter writer )
	{
		this.writer = writer;
	}

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		final Instance instance = environment.instanceOf( bytecode.method );

		if( null != instance )
		{
			writer.write( StringConverter.toString( instance ) + ":\n" );
		}
		else
		{
			final Class klass = environment.classOf( bytecode.method );

			if( null != klass )
			{
				writer.write( StringConverter.toString( klass ) + ":\n" );
			}
			else
			{
				final Script script = environment.scriptOf( bytecode.method );

				if( null != script )
				{
					writer.write( StringConverter.toString( script ) + ":\n" );
				}
				else
				{
					writer.write( "Unknown Method:\n" );
				}
			}
		}

		for( final AbstractOperation operation : bytecode )
		{
			final int code = operation.code;

			final StringBuilder positionString = new StringBuilder( Integer
					.toHexString( operation.getPosition() ) );

			while( positionString.length() < 6 )
			{
				positionString.insert( 0, '0' );
			}

			positionString.insert( 0, "0x" );
			positionString.append( ' ' );

			writer.write( positionString.toString() );

			if( bytecode.markers.hasMarkerFor( operation ) )
			{
				final StringBuilder markerString = new StringBuilder(
						markerToString( bytecode.markers
								.getMarkerFor( operation ) ) );

				markerString.append( ':' );

				while( markerString.length() < 6 )
				{
					markerString.append( ' ' );
				}

				writer.write( markerString.toString() );
			}
			else
			{
				writer.write( "      " );
			}

			writer.write( printNames.get( operation.code ) );

			switch( code )
			{
				case Op.AsType:
					writer.write( StringConverter
							.toString( ( (AsType)operation ).type ) );
					break;

				case Op.ApplyType:
					writer.write( Integer
							.toString( ( (ApplyType)operation ).typeSize ) );
					break;

				case Op.Call:
					writer.write( Integer
							.toString( ( (Call)operation ).numArguments ) );
					break;

				case Op.CallMethod:
					writer
							.write( StringConverter
									.toString( ( (CallMethod)operation ).method )
									+ ", "
									+ Integer
											.toString( ( (CallMethod)operation ).numArguments ) );
					break;

				case Op.CallProperty:
					writer
							.write( StringConverter
									.toString( ( (CallProperty)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (CallProperty)operation ).numArguments ) );
					break;

				case Op.CallPropLex:
					writer
							.write( StringConverter
									.toString( ( (CallPropLex)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (CallPropLex)operation ).numArguments ) );
					break;

				case Op.CallPropVoid:
					writer
							.write( StringConverter
									.toString( ( (CallPropVoid)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (CallPropVoid)operation ).numArguments ) );
					break;

				case Op.CallStatic:
					writer
							.write( StringConverter
									.toString( ( (CallStatic)operation ).method )
									+ ", "
									+ Integer
											.toString( ( (CallStatic)operation ).numArguments ) );
					break;

				case Op.CallSuper:
					writer
							.write( StringConverter
									.toString( ( (CallSuper)operation ).name )
									+ Integer
											.toString( ( (CallSuper)operation ).numArguments ) );
					break;

				case Op.CallSuperVoid:
					writer
							.write( StringConverter
									.toString( ( (CallSuperVoid)operation ).name )
									+ Integer
											.toString( ( (CallSuperVoid)operation ).numArguments ) );
					break;

				case Op.Coerce:
					writer.write( StringConverter
							.toString( ( (Coerce)operation ).type ) );
					break;

				case Op.Construct:
					writer.write( Integer
							.toString( ( (Construct)operation ).numArguments ) );
					break;

				case Op.ConstructProp:
					writer
							.write( StringConverter
									.toString( ( (ConstructProp)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (ConstructProp)operation ).numArguments ) );
					break;

				case Op.ConstructSuper:
					writer
							.write( Integer
									.toString( ( (ConstructSuper)operation ).numArguments ) );
					break;

				case Op.Debug:
					final Debug debugOperation = (Debug)operation;
					writer.write( Integer.toString( debugOperation.type )
							+ ", " + escape( debugOperation.name ) + ", "
							+ Integer.toString( debugOperation.register ) );
					break;

				case Op.DebugFile:
					writer.write( escape( ( (DebugFile)operation ).fileName ) );
					break;

				case Op.DebugLine:
					writer.write( Integer
							.toString( ( (DebugLine)operation ).lineNumber ) );
					break;

				case Op.DecLocal:
					writer.write( Integer
							.toString( ( (DecLocal)operation ).register ) );
					break;

				case Op.DecLocalInt:
					writer.write( Integer
							.toString( ( (DecLocalInt)operation ).register ) );
					break;

				case Op.DeleteProperty:
					writer
							.write( StringConverter
									.toString( ( (DeleteProperty)operation ).property ) );
					break;

				case Op.DefaultXmlNamespace:
					writer
							.write( escape( ( (DefaultXmlNamespace)operation ).uri ) );
					break;

				case Op.FindProperty:
					writer.write( StringConverter
							.toString( ( (FindProperty)operation ).property ) );
					break;

				case Op.FindPropStrict:
					writer
							.write( StringConverter
									.toString( ( (FindPropStrict)operation ).property ) );
					break;

				case Op.GetDescendants:
					writer.write( StringConverter
							.toString( ( (GetDescendants)operation ).name ) );
					break;

				case Op.GetGlobalSlot:
					writer
							.write( Integer
									.toString( ( (GetGlobalSlot)operation ).slotIndex ) );
					break;

				case Op.GetLex:
					writer.write( StringConverter
							.toString( ( (GetLex)operation ).property ) );
					break;

				case Op.GetLocal:
					writer.write( Integer
							.toString( ( (GetLocal)operation ).register ) );
					break;

				case Op.GetProperty:
					writer.write( StringConverter
							.toString( ( (GetProperty)operation ).property ) );
					break;

				case Op.GetScopeObject:
					writer
							.write( Integer
									.toString( ( (GetScopeObject)operation ).scopeIndex ) );
					break;

				case Op.GetSlot:
					writer.write( Integer
							.toString( ( (GetSlot)operation ).slotIndex ) );
					break;

				case Op.GetSuper:
					writer.write( StringConverter
							.toString( ( (GetSuper)operation ).property ) );
					break;

				case Op.HasNext2:
					writer
							.write( Long
									.toString( ( (HasNext2)operation ).objectRegister )
									+ "L, "
									+ Long
											.toString( ( (HasNext2)operation ).indexRegister )
									+ "L" );
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
					writer.write( markerToString( ( (Jump)operation ).marker ) );
					writer.write( "\n" );
					break;

				case Op.IncLocal:
					writer.write( Integer
							.toString( ( (IncLocal)operation ).register ) );
					break;

				case Op.IncLocalInt:
					writer.write( Integer
							.toString( ( (IncLocalInt)operation ).register ) );
					break;

				case Op.InitProperty:
					writer.write( StringConverter
							.toString( ( (InitProperty)operation ).property ) );
					break;

				case Op.IsType:
					writer.write( StringConverter
							.toString( ( (IsType)operation ).type ) );
					break;

				case Op.Kill:
					writer.write( Integer
							.toString( ( (Kill)operation ).register ) );
					break;

				case Op.LookupSwitch:
					final LookupSwitch lookupSwitch = (LookupSwitch)operation;

					writer.write( markerToString( lookupSwitch.defaultMarker )
							+ "," );

					for( final Marker m : lookupSwitch.caseMarkers )
					{
						writer.write( " " + markerToString( m ) );
					}

					writer.write( "\n" );
					break;

				case Op.NewArray:
					writer.write( Integer
							.toString( ( (NewArray)operation ).numArguments ) );
					break;

				case Op.NewCatch:
					writer
							.write( StringConverter
									.toString( ( (NewCatch)operation ).exceptionHandler ) );
					break;

				case Op.NewClass:
					writer.write( StringConverter
							.toString( ( (NewClass)operation ).klass ) );
					break;

				case Op.NewFunction:
					writer.write( StringConverter
							.toString( ( (NewFunction)operation ).function ) );
					break;

				case Op.NewObject:
					writer
							.write( Integer
									.toString( ( (NewObject)operation ).numProperties ) );
					break;

				case Op.PushByte:
					writer
							.write( "0x"
									+ Integer
											.toHexString( ( (PushByte)operation ).value ) );
					break;

				case Op.PushDouble:
					writer.write( Double
							.toString( ( (PushDouble)operation ).value ) );
					break;

				case Op.PushInt:
					writer
							.write( "0x"
									+ Integer
											.toHexString( ( (PushInt)operation ).value ) );
					break;

				case Op.PushNamespace:
					writer.write( StringConverter
							.toString( ( (PushNamespace)operation ).value ) );
					break;

				case Op.PushShort:
					writer
							.write( "0x"
									+ Integer
											.toHexString( ( (PushShort)operation ).value ) );
					break;

				case Op.PushString:
					writer.write( "\""
							+ escape( ( (PushString)operation ).value ) + "\"" );
					break;

				case Op.PushUInt:
					writer.write( "0x"
							+ Long.toHexString( ( (PushUInt)operation ).value )
							+ "L" );
					break;

				case Op.SetLocal:
					writer.write( Integer
							.toString( ( (SetLocal)operation ).register ) );
					break;

				case Op.SetGlobalSlot:
					writer
							.write( Integer
									.toString( ( (SetGlobalSlot)operation ).slotIndex ) );
					break;

				case Op.SetProperty:
					writer.write( StringConverter
							.toString( ( (SetProperty)operation ).property ) );
					break;

				case Op.SetSlot:
					writer.write( Integer
							.toString( ( (SetSlot)operation ).slotIndex ) );
					break;

				case Op.SetSuper:
					writer.write( StringConverter
							.toString( ( (SetSuper)operation ).property ) );
					break;
			}

			writer.write( "\n" );
		}

		writer.flush();
	}
}
