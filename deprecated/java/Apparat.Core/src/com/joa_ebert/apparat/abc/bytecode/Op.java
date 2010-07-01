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

import java.util.logging.Logger;

import com.joa_ebert.apparat.abc.bytecode.operations.GetLocal;
import com.joa_ebert.apparat.abc.bytecode.operations.SetLocal;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Op
{
	public static final int MIN_VALUE = 0x00;
	public static final int MAX_VALUE = 0xff;

	public static final int Breakpoint = 0x01;
	public static final int Nop = 0x02;
	public static final int Throw = 0x03;
	public static final int GetSuper = 0x04;
	public static final int SetSuper = 0x05;
	public static final int DefaultXmlNamespace = 0x06;
	public static final int DefaultXmlNamespaceL = 0x07;
	public static final int Kill = 0x08;
	public static final int Label = 0x09;
	public static final int IfNotLessThan = 0x0C;
	public static final int IfNotLessEqual = 0x0D;
	public static final int IfNotGreaterThan = 0x0E;
	public static final int IfNotGreaterEqual = 0x0F;
	public static final int Jump = 0x10;
	public static final int IfTrue = 0x11;
	public static final int IfFalse = 0x12;
	public static final int IfEqual = 0x13;
	public static final int IfNotEqual = 0x14;
	public static final int IfLessThan = 0x15;
	public static final int IfLessEqual = 0x16;
	public static final int IfGreaterThan = 0x17;
	public static final int IfGreaterEqual = 0x18;
	public static final int IfStrictEqual = 0x19;
	public static final int IfStrictNotEqual = 0x1A;
	public static final int LookupSwitch = 0x1B;
	public static final int PushWith = 0x1C;
	public static final int PopScope = 0x1D;
	public static final int NextName = 0x1E;
	public static final int HasNext = 0x1F;
	public static final int PushNull = 0x20;
	public static final int PushUndefined = 0x21;
	public static final int NextValue = 0x23;
	public static final int PushByte = 0x24;
	public static final int PushShort = 0x25;
	public static final int PushTrue = 0x26;
	public static final int PushFalse = 0x27;
	public static final int PushNaN = 0x28;
	public static final int Pop = 0x29;
	public static final int Dup = 0x2A;
	public static final int Swap = 0x2B;
	public static final int PushString = 0x2C;
	public static final int PushInt = 0x2D;
	public static final int PushUInt = 0x2E;
	public static final int PushDouble = 0x2F;
	public static final int PushScope = 0x30;
	public static final int PushNamespace = 0x31;
	public static final int HasNext2 = 0x32;
	public static final int NewFunction = 0x40;
	public static final int Call = 0x41;
	public static final int Construct = 0x42;
	public static final int CallMethod = 0x43;
	public static final int CallStatic = 0x44;
	public static final int CallSuper = 0x45;
	public static final int CallProperty = 0x46;
	public static final int ReturnVoid = 0x47;
	public static final int ReturnValue = 0x48;
	public static final int ConstructSuper = 0x49;
	public static final int ConstructProp = 0x4A;
	public static final int CallSuperId = 0x4B;
	public static final int CallPropLex = 0x4C;
	public static final int CallInterface = 0x4D;
	public static final int CallSuperVoid = 0x4E;
	public static final int CallPropVoid = 0x4F;
	public static final int ApplyType = 0x53;
	public static final int NewObject = 0x55;
	public static final int NewArray = 0x56;
	public static final int NewActivation = 0x57;
	public static final int NewClass = 0x58;
	public static final int GetDescendants = 0x59;
	public static final int NewCatch = 0x5A;
	public static final int FindPropStrict = 0x5D;
	public static final int FindProperty = 0x5E;
	public static final int FindDef = 0x5F;
	public static final int GetLex = 0x60;
	public static final int SetProperty = 0x61;
	public static final int GetLocal = 0x62;
	public static final int SetLocal = 0x63;
	public static final int GetGlobalScope = 0x64;
	public static final int GetScopeObject = 0x65;
	public static final int GetProperty = 0x66;
	public static final int GetPropertyLate = 0x67;
	public static final int InitProperty = 0x68;
	public static final int SetPropertyLate = 0x69;
	public static final int DeleteProperty = 0x6A;
	public static final int DeletePropertyLate = 0x6B;
	public static final int GetSlot = 0x6C;
	public static final int SetSlot = 0x6D;
	public static final int GetGlobalSlot = 0x6E;
	public static final int SetGlobalSlot = 0x6F;
	public static final int ConvertString = 0x70;
	public static final int EscXmlElem = 0x71;
	public static final int EscXmlAttr = 0x72;
	public static final int ConvertInt = 0x73;
	public static final int ConvertUInt = 0x74;
	public static final int ConvertDouble = 0x75;
	public static final int ConvertBoolean = 0x76;
	public static final int ConvertObject = 0x77;
	public static final int CheckFilter = 0x78;
	public static final int Coerce = 0x80;
	public static final int CoerceBoolean = 0x81;
	public static final int CoerceAny = 0x82;
	public static final int CoerceInt = 0x83;
	public static final int CoerceDouble = 0x84;
	public static final int CoerceString = 0x85;
	public static final int AsType = 0x86;
	public static final int AsTypeLate = 0x87;
	public static final int CoerceUInt = 0x88;
	public static final int CoerceObject = 0x89;
	public static final int Negate = 0x90;
	public static final int Increment = 0x91;
	public static final int IncLocal = 0x92;
	public static final int Decrement = 0x93;
	public static final int DecLocal = 0x94;
	public static final int TypeOf = 0x95;
	public static final int Not = 0x96;
	public static final int BitNot = 0x97;
	public static final int Concat = 0x9A;
	public static final int AddDouble = 0x9B;
	public static final int Add = 0xA0;
	public static final int Subtract = 0xA1;
	public static final int Multiply = 0xA2;
	public static final int Divide = 0xA3;
	public static final int Modulo = 0xA4;
	public static final int ShiftLeft = 0xA5;
	public static final int ShiftRight = 0xA6;
	public static final int ShiftRightUnsigned = 0xA7;
	public static final int BitAnd = 0xA8;
	public static final int BitOr = 0xA9;
	public static final int BitXor = 0xAA;
	public static final int Equals = 0xAB;
	public static final int StrictEquals = 0xAC;
	public static final int LessThan = 0xAD;
	public static final int LessEquals = 0xAE;
	public static final int GreaterThan = 0xAF;
	public static final int GreaterEquals = 0xB0;
	public static final int InstanceOf = 0xB1;
	public static final int IsType = 0xB2;
	public static final int IsTypeLate = 0xB3;
	public static final int In = 0xB4;
	public static final int IncrementInt = 0xC0;
	public static final int DecrementInt = 0xC1;
	public static final int IncLocalInt = 0xC2;
	public static final int DecLocalInt = 0xC3;
	public static final int NegateInt = 0xC4;
	public static final int AddInt = 0xC5;
	public static final int SubtractInt = 0xC6;
	public static final int MultiplyInt = 0xC7;
	public static final int GetLocal0 = 0xD0;
	public static final int GetLocal1 = 0xD1;
	public static final int GetLocal2 = 0xD2;
	public static final int GetLocal3 = 0xD3;
	public static final int SetLocal0 = 0xD4;
	public static final int SetLocal1 = 0xD5;
	public static final int SetLocal2 = 0xD6;
	public static final int SetLocal3 = 0xD7;
	public static final int Debug = 0xEF;
	public static final int DebugLine = 0xF0;
	public static final int DebugFile = 0xF1;
	public static final int BreakpointLine = 0xF2;

	// Alchemy Opcodes
	public static final int SetByte = 0x3A;
	public static final int SetShort = 0x3B;
	public static final int SetInt = 0x3C;
	public static final int SetFloat = 0x3D;
	public static final int SetDouble = 0x3E;
	public static final int GetByte = 0x35;
	public static final int GetShort = 0x36;
	public static final int GetInt = 0x37;
	public static final int GetFloat = 0x38;
	public static final int GetDouble = 0x39;
	public static final int Sign1 = 0x50;
	public static final int Sign8 = 0x51;
	public static final int Sign16 = 0x52;

	private static final String[] opNames = new String[ MAX_VALUE + 1 ];
	private static final boolean[] opCanThrow = new boolean[] {
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			false,
			false,
			false,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			true,
			true,
			false,
			true,
			false,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			true,
			false,
			true,
			false,
			true,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false
	};

	static
	{
		for( int i = MIN_VALUE; i <= MAX_VALUE; ++i )
		{
			opNames[ i ] = "Unknown_" + Integer.toHexString( i );
		}

		opNames[ Breakpoint ] = "Breakpoint";
		opNames[ Nop ] = "Nop";
		opNames[ Throw ] = "Throw";
		opNames[ GetSuper ] = "GetSuper";
		opNames[ SetSuper ] = "SetSuper";
		opNames[ DefaultXmlNamespace ] = "DefaultXmlNamespace";
		opNames[ DefaultXmlNamespaceL ] = "DefaultXmlNamespaceL";
		opNames[ Kill ] = "Kill";
		opNames[ Label ] = "Label";
		opNames[ IfNotLessThan ] = "IfNotLessThan";
		opNames[ IfNotLessEqual ] = "IfNotLessEqual";
		opNames[ IfNotGreaterThan ] = "IfNotGreaterThan";
		opNames[ IfNotGreaterEqual ] = "IfNotGreaterEqual";
		opNames[ Jump ] = "Jump";
		opNames[ IfTrue ] = "IfTrue";
		opNames[ IfFalse ] = "IfFalse";
		opNames[ IfEqual ] = "IfEqual";
		opNames[ IfNotEqual ] = "IfNotEqual";
		opNames[ IfLessThan ] = "IfLessThan";
		opNames[ IfLessEqual ] = "IfLessEqual";
		opNames[ IfGreaterThan ] = "IfGreaterThan";
		opNames[ IfGreaterEqual ] = "IfGreaterEqual";
		opNames[ IfStrictEqual ] = "IfStrictEqual";
		opNames[ IfStrictNotEqual ] = "IfStrictNotEqual";
		opNames[ LookupSwitch ] = "LookupSwitch";
		opNames[ PushWith ] = "PushWith";
		opNames[ PopScope ] = "PopScope";
		opNames[ NextName ] = "NextName";
		opNames[ HasNext ] = "HasNext";
		opNames[ PushNull ] = "PushNull";
		opNames[ PushUndefined ] = "PushUndefined";
		opNames[ NextValue ] = "NextValue";
		opNames[ PushByte ] = "PushByte";
		opNames[ PushShort ] = "PushShort";
		opNames[ PushTrue ] = "PushTrue";
		opNames[ PushFalse ] = "PushFalse";
		opNames[ PushNaN ] = "PushNaN";
		opNames[ Pop ] = "Pop";
		opNames[ Dup ] = "Dup";
		opNames[ Swap ] = "Swap";
		opNames[ PushString ] = "PushString";
		opNames[ PushInt ] = "PushInt";
		opNames[ PushUInt ] = "PushUInt";
		opNames[ PushDouble ] = "PushDouble";
		opNames[ PushScope ] = "PushScope";
		opNames[ PushNamespace ] = "PushNamespace";
		opNames[ HasNext2 ] = "HasNext2";
		opNames[ NewFunction ] = "NewFunction";
		opNames[ Call ] = "Call";
		opNames[ Construct ] = "Construct";
		opNames[ CallMethod ] = "CallMethod";
		opNames[ CallStatic ] = "CallStatic";
		opNames[ CallSuper ] = "CallSuper";
		opNames[ CallProperty ] = "CallProperty";
		opNames[ ReturnVoid ] = "ReturnVoid";
		opNames[ ReturnValue ] = "ReturnValue";
		opNames[ ConstructSuper ] = "ConstructSuper";
		opNames[ ConstructProp ] = "ConstructProp";
		opNames[ CallSuperId ] = "CallSuperId";
		opNames[ CallPropLex ] = "CallPropLex";
		opNames[ CallInterface ] = "CallInterface";
		opNames[ CallSuperVoid ] = "CallSuperVoid";
		opNames[ CallPropVoid ] = "CallPropVoid";
		opNames[ ApplyType ] = "ApplyType";
		opNames[ NewObject ] = "NewObject";
		opNames[ NewArray ] = "NewArray";
		opNames[ NewActivation ] = "NewActivation";
		opNames[ NewClass ] = "NewClass";
		opNames[ GetDescendants ] = "GetDescendants";
		opNames[ NewCatch ] = "NewCatch";
		opNames[ FindPropStrict ] = "FindPropStrict";
		opNames[ FindProperty ] = "FindProperty";
		opNames[ FindDef ] = "FindDef";
		opNames[ GetLex ] = "GetLex";
		opNames[ SetProperty ] = "SetProperty";
		opNames[ GetLocal ] = "GetLocal";
		opNames[ SetLocal ] = "SetLocal";
		opNames[ GetGlobalScope ] = "GetGlobalScope";
		opNames[ GetScopeObject ] = "GetScopeObject";
		opNames[ GetProperty ] = "GetProperty";
		opNames[ GetPropertyLate ] = "GetPropertyLate";
		opNames[ InitProperty ] = "InitProperty";
		opNames[ SetPropertyLate ] = "SetPropertyLate";
		opNames[ DeleteProperty ] = "DeleteProperty";
		opNames[ DeletePropertyLate ] = "DeletePropertyLate";
		opNames[ GetSlot ] = "GetSlot";
		opNames[ SetSlot ] = "SetSlot";
		opNames[ GetGlobalSlot ] = "GetGlobalSlot";
		opNames[ SetGlobalSlot ] = "SetGlobalSlot";
		opNames[ ConvertString ] = "ConvertString";
		opNames[ EscXmlElem ] = "EscXmlElem";
		opNames[ EscXmlAttr ] = "EscXmlAttr";
		opNames[ ConvertInt ] = "ConvertInt";
		opNames[ ConvertUInt ] = "ConvertUInt";
		opNames[ ConvertDouble ] = "ConvertDouble";
		opNames[ ConvertBoolean ] = "ConvertBoolean";
		opNames[ ConvertObject ] = "ConvertObject";
		opNames[ CheckFilter ] = "CheckFilter";
		opNames[ Coerce ] = "Coerce";
		opNames[ CoerceBoolean ] = "CoerceBoolean";
		opNames[ CoerceAny ] = "CoerceAny";
		opNames[ CoerceInt ] = "CoerceInt";
		opNames[ CoerceDouble ] = "CoerceDouble";
		opNames[ CoerceString ] = "CoerceString";
		opNames[ AsType ] = "AsType";
		opNames[ AsTypeLate ] = "AsTypeLate";
		opNames[ CoerceUInt ] = "CoerceUInt";
		opNames[ CoerceObject ] = "CoerceObject";
		opNames[ Negate ] = "Negate";
		opNames[ Increment ] = "Increment";
		opNames[ IncLocal ] = "IncLocal";
		opNames[ Decrement ] = "Decrement";
		opNames[ DecLocal ] = "DecLocal";
		opNames[ TypeOf ] = "TypeOf";
		opNames[ Not ] = "Not";
		opNames[ BitNot ] = "BitNot";
		opNames[ Concat ] = "Concat";
		opNames[ AddDouble ] = "AddDouble";
		opNames[ Add ] = "Add";
		opNames[ Subtract ] = "Subtract";
		opNames[ Multiply ] = "Multiply";
		opNames[ Divide ] = "Divide";
		opNames[ Modulo ] = "Modulo";
		opNames[ ShiftLeft ] = "ShiftLeft";
		opNames[ ShiftRight ] = "ShiftRight";
		opNames[ ShiftRightUnsigned ] = "ShiftRightUnsigned";
		opNames[ BitAnd ] = "BitAnd";
		opNames[ BitOr ] = "BitOr";
		opNames[ BitXor ] = "BitXor";
		opNames[ Equals ] = "Equals";
		opNames[ StrictEquals ] = "StrictEquals";
		opNames[ LessThan ] = "LessThan";
		opNames[ LessEquals ] = "LessEquals";
		opNames[ GreaterThan ] = "GreaterThan";
		opNames[ GreaterEquals ] = "GreaterEquals";
		opNames[ InstanceOf ] = "InstanceOf";
		opNames[ IsType ] = "IsType";
		opNames[ IsTypeLate ] = "IsTypeLate";
		opNames[ In ] = "In";
		opNames[ IncrementInt ] = "IncrementInt";
		opNames[ DecrementInt ] = "DecrementInt";
		opNames[ IncLocalInt ] = "IncLocalInt";
		opNames[ DecLocalInt ] = "DecLocalInt";
		opNames[ NegateInt ] = "NegateInt";
		opNames[ AddInt ] = "AddInt";
		opNames[ SubtractInt ] = "SubtractInt";
		opNames[ MultiplyInt ] = "MultiplyInt";
		opNames[ GetLocal0 ] = "GetLocal0";
		opNames[ GetLocal1 ] = "GetLocal1";
		opNames[ GetLocal2 ] = "GetLocal2";
		opNames[ GetLocal3 ] = "GetLocal3";
		opNames[ SetLocal0 ] = "SetLocal0";
		opNames[ SetLocal1 ] = "SetLocal1";
		opNames[ SetLocal2 ] = "SetLocal2";
		opNames[ SetLocal3 ] = "SetLocal3";
		opNames[ Debug ] = "Debug";
		opNames[ DebugLine ] = "DebugLine";
		opNames[ DebugFile ] = "DebugFile";
		opNames[ BreakpointLine ] = "BreakpointLine";

		opNames[ SetByte ] = "SetByte";
		opNames[ SetShort ] = "SetShort";
		opNames[ SetInt ] = "SetInt";
		opNames[ SetFloat ] = "SetFloat";
		opNames[ SetDouble ] = "SetDouble";
		opNames[ GetByte ] = "GetByte";
		opNames[ GetShort ] = "GetShort";
		opNames[ GetInt ] = "GetInt";
		opNames[ GetFloat ] = "GetFloat";
		opNames[ GetDouble ] = "GetDouble";
		opNames[ Sign1 ] = "Sign1";
		opNames[ Sign8 ] = "Sign8";
		opNames[ Sign16 ] = "Sign16";
	}

	public static boolean canThrow( final AbstractOperation operation )
	{
		return canThrow( operation.code );
	}

	public static boolean canThrow( final byte code )
	{
		return canThrow( code & 0xff );
	}

	public static boolean canThrow( final int code )
	{
		return opCanThrow[ code ];
	}

	public static String codeToString( final AbstractOperation operation )
	{
		return codeToString( operation.code );
	}

	public static String codeToString( final byte code )
	{
		return codeToString( code & 0xff );
	}

	public static String codeToString( final int code )
	{
		if( code < MIN_VALUE || code > MAX_VALUE )
		{
			return null;
		}

		return opNames[ code ];
	}

	public static int getLocalRegister( final AbstractOperation operation )
	{
		if( operation instanceof GetLocal )
		{
			return ( (GetLocal)operation ).register;
		}
		else if( operation instanceof SetLocal )
		{
			return ( (SetLocal)operation ).register;
		}
		else
		{
			switch( operation.code )
			{
				case Op.GetLocal0:
					return 0;
				case Op.GetLocal1:
					return 1;
				case Op.GetLocal2:
					return 2;
				case Op.GetLocal3:
					return 3;

				default:
					Logger.getLogger( Op.class.getName() ).severe(
							"Not implemented local register operation." );
					return -1;
			}
		}
	}

	private Op()
	{

	}
}
