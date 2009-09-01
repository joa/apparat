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
		if( null == marker )
		{
			return "L?";
		}

		return "L" + Integer.toString( marker.key );
	}

	private final PrintWriter output;
	private boolean printName = true;

	public BytecodePrinter( final OutputStream output )
	{
		this( new PrintWriter( output ) );
	}

	public BytecodePrinter( final PrintWriter output )
	{
		this.output = output;
	}

	public void interpret( final AbcEnvironment environment,
			final Bytecode bytecode )
	{
		if( printName && null != bytecode && null != bytecode.methodBody
				&& null != bytecode.methodBody.method )
		{
			final Instance instance = environment
					.instanceOf( bytecode.methodBody.method );

			if( null != instance )
			{
				output.println( StringConverter.toString( instance ) + ":" );
			}
			else
			{
				final Class klass = environment
						.classOf( bytecode.methodBody.method );

				if( null != klass )
				{
					output.println( StringConverter.toString( klass ) + ":" );
				}
				else
				{
					final Script script = environment
							.scriptOf( bytecode.methodBody.method );

					if( null != script )
					{
						output.println( StringConverter.toString( script )
								+ ":" );
					}
					else
					{
						output.println( "Unknown Method:" );
					}
				}
			}
		}

		for( final AbstractOperation operation : bytecode )
		{
			final int code = operation.code;

			final StringBuilder line = new StringBuilder();
			final StringBuilder positionString = new StringBuilder( Integer
					.toHexString( operation.getPosition() ) );

			while( positionString.length() < 6 )
			{
				positionString.insert( 0, '0' );
			}

			positionString.insert( 0, "0x" );
			positionString.append( ' ' );

			line.append( positionString.toString() );

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

				line.append( markerString.toString() );
			}
			else
			{
				line.append( "      " );
			}

			line.append( printNames.get( operation.code ) );

			switch( code )
			{
				case Op.AsType:
					line.append( StringConverter
							.toString( ( (AsType)operation ).type ) );
					break;

				case Op.ApplyType:
					line.append( Integer
							.toString( ( (ApplyType)operation ).typeSize ) );
					break;

				case Op.Call:
					line.append( Integer
							.toString( ( (Call)operation ).numArguments ) );
					break;

				case Op.CallMethod:
					line
							.append( StringConverter
									.toString( ( (CallMethod)operation ).method )
									+ ", "
									+ Integer
											.toString( ( (CallMethod)operation ).numArguments ) );
					break;

				case Op.CallProperty:
					line
							.append( StringConverter
									.toString( ( (CallProperty)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (CallProperty)operation ).numArguments ) );
					break;

				case Op.CallPropLex:
					line
							.append( StringConverter
									.toString( ( (CallPropLex)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (CallPropLex)operation ).numArguments ) );
					break;

				case Op.CallPropVoid:
					line
							.append( StringConverter
									.toString( ( (CallPropVoid)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (CallPropVoid)operation ).numArguments ) );
					break;

				case Op.CallStatic:
					line
							.append( StringConverter
									.toString( ( (CallStatic)operation ).method )
									+ ", "
									+ Integer
											.toString( ( (CallStatic)operation ).numArguments ) );
					break;

				case Op.CallSuper:
					line
							.append( StringConverter
									.toString( ( (CallSuper)operation ).name )
									+ Integer
											.toString( ( (CallSuper)operation ).numArguments ) );
					break;

				case Op.CallSuperVoid:
					line
							.append( StringConverter
									.toString( ( (CallSuperVoid)operation ).name )
									+ Integer
											.toString( ( (CallSuperVoid)operation ).numArguments ) );
					break;

				case Op.Coerce:
					line.append( StringConverter
							.toString( ( (Coerce)operation ).type ) );
					break;

				case Op.Construct:
					line.append( Integer
							.toString( ( (Construct)operation ).numArguments ) );
					break;

				case Op.ConstructProp:
					line
							.append( StringConverter
									.toString( ( (ConstructProp)operation ).property )
									+ ", "
									+ Integer
											.toString( ( (ConstructProp)operation ).numArguments ) );
					break;

				case Op.ConstructSuper:
					line
							.append( Integer
									.toString( ( (ConstructSuper)operation ).numArguments ) );
					break;

				case Op.Debug:
					final Debug debugOperation = (Debug)operation;
					line.append( Integer.toString( debugOperation.type ) + ", "
							+ escape( debugOperation.name ) + ", "
							+ Integer.toString( debugOperation.register ) );
					break;

				case Op.DebugFile:
					line.append( escape( ( (DebugFile)operation ).fileName ) );
					break;

				case Op.DebugLine:
					line.append( Integer
							.toString( ( (DebugLine)operation ).lineNumber ) );
					break;

				case Op.DecLocal:
					line.append( Integer
							.toString( ( (DecLocal)operation ).register ) );
					break;

				case Op.DecLocalInt:
					line.append( Integer
							.toString( ( (DecLocalInt)operation ).register ) );
					break;

				case Op.DeleteProperty:
					line
							.append( StringConverter
									.toString( ( (DeleteProperty)operation ).property ) );
					break;

				case Op.DefaultXmlNamespace:
					line
							.append( escape( ( (DefaultXmlNamespace)operation ).uri ) );
					break;

				case Op.FindProperty:
					line.append( StringConverter
							.toString( ( (FindProperty)operation ).property ) );
					break;

				case Op.FindPropStrict:
					line
							.append( StringConverter
									.toString( ( (FindPropStrict)operation ).property ) );
					break;

				case Op.GetDescendants:
					line.append( StringConverter
							.toString( ( (GetDescendants)operation ).name ) );
					break;

				case Op.GetGlobalSlot:
					line
							.append( Integer
									.toString( ( (GetGlobalSlot)operation ).slotIndex ) );
					break;

				case Op.GetLex:
					line.append( StringConverter
							.toString( ( (GetLex)operation ).property ) );
					break;

				case Op.GetLocal:
					line.append( Integer
							.toString( ( (GetLocal)operation ).register ) );
					break;

				case Op.GetProperty:
					line.append( StringConverter
							.toString( ( (GetProperty)operation ).property ) );
					break;

				case Op.GetScopeObject:
					line
							.append( Integer
									.toString( ( (GetScopeObject)operation ).scopeIndex ) );
					break;

				case Op.GetSlot:
					line.append( Integer
							.toString( ( (GetSlot)operation ).slotIndex ) );
					break;

				case Op.GetSuper:
					line.append( StringConverter
							.toString( ( (GetSuper)operation ).property ) );
					break;

				case Op.HasNext2:
					line
							.append( Long
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
					line.append( markerToString( ( (Jump)operation ).marker ) );
					line.append( "\n" );
					break;

				case Op.IncLocal:
					line.append( Integer
							.toString( ( (IncLocal)operation ).register ) );
					break;

				case Op.IncLocalInt:
					line.append( Integer
							.toString( ( (IncLocalInt)operation ).register ) );
					break;

				case Op.InitProperty:
					line.append( StringConverter
							.toString( ( (InitProperty)operation ).property ) );
					break;

				case Op.IsType:
					line.append( StringConverter
							.toString( ( (IsType)operation ).type ) );
					break;

				case Op.Kill:
					line.append( Integer
							.toString( ( (Kill)operation ).register ) );
					break;

				case Op.LookupSwitch:
					final LookupSwitch lookupSwitch = (LookupSwitch)operation;

					line.append( markerToString( lookupSwitch.defaultMarker )
							+ "," );

					for( final Marker m : lookupSwitch.caseMarkers )
					{
						line.append( " " + markerToString( m ) );
					}

					line.append( "\n" );
					break;

				case Op.NewArray:
					line.append( Integer
							.toString( ( (NewArray)operation ).numArguments ) );
					break;

				case Op.NewCatch:
					line
							.append( StringConverter
									.toString( ( (NewCatch)operation ).exceptionHandler ) );
					break;

				case Op.NewClass:
					line.append( StringConverter
							.toString( ( (NewClass)operation ).klass ) );
					break;

				case Op.NewFunction:
					line.append( StringConverter
							.toString( ( (NewFunction)operation ).function ) );
					break;

				case Op.NewObject:
					line
							.append( Integer
									.toString( ( (NewObject)operation ).numProperties ) );
					break;

				case Op.PushByte:
					line
							.append( ( (PushByte)operation ).value >= 0 ? "0x"
									+ Integer
											.toHexString( ( (PushByte)operation ).value )
									: ( (PushByte)operation ).value );
					break;

				case Op.PushDouble:
					line.append( Double
							.toString( ( (PushDouble)operation ).value ) );
					break;

				case Op.PushInt:
					line.append( ( (PushInt)operation ).value >= 0 ? "0x"
							+ Integer
									.toHexString( ( (PushInt)operation ).value )
							: ( (PushInt)operation ).value );
					break;

				case Op.PushNamespace:
					line.append( StringConverter
							.toString( ( (PushNamespace)operation ).value ) );
					break;

				case Op.PushShort:
					line
							.append( ( (PushShort)operation ).value >= 0 ? "0x"
									+ Integer
											.toHexString( ( (PushShort)operation ).value )
									: ( (PushShort)operation ).value );
					break;

				case Op.PushString:
					line.append( "\""
							+ escape( ( (PushString)operation ).value ) + "\"" );
					break;

				case Op.PushUInt:
					line.append( "0x"
							+ Long.toHexString( ( (PushUInt)operation ).value )
							+ "L" );
					break;

				case Op.SetLocal:
					line.append( Integer
							.toString( ( (SetLocal)operation ).register ) );
					break;

				case Op.SetGlobalSlot:
					line
							.append( Integer
									.toString( ( (SetGlobalSlot)operation ).slotIndex ) );
					break;

				case Op.SetProperty:
					line.append( StringConverter
							.toString( ( (SetProperty)operation ).property ) );
					break;

				case Op.SetSlot:
					line.append( Integer
							.toString( ( (SetSlot)operation ).slotIndex ) );
					break;

				case Op.SetSuper:
					line.append( StringConverter
							.toString( ( (SetSuper)operation ).property ) );
					break;
			}

			output.println( line.toString() );
		}

		output.flush();
	}

	public void setPrintName( final boolean value )
	{
		printName = value;
	}
}
