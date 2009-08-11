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

import com.joa_ebert.apparat.abc.bytecode.operations.*;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class OperationFactory
{
	public OperationFactory()
	{
	}

	public AbstractOperation create( final int code )
	{
		switch( code )
		{
			case Op.Breakpoint:
				return new Breakpoint();
			case Op.Nop:
				return new Nop();
			case Op.Throw:
				return new Throw();
			case Op.GetSuper:
				return new GetSuper();
			case Op.SetSuper:
				return new SetSuper();
			case Op.DefaultXmlNamespace:
				return new DefaultXmlNamespace();
			case Op.DefaultXmlNamespaceL:
				return new DefaultXmlNamespaceL();
			case Op.Kill:
				return new Kill();
			case Op.Label:
				return new Label();
			case Op.IfNotLessThan:
				return new IfNotLessThan();
			case Op.IfNotLessEqual:
				return new IfNotLessEqual();
			case Op.IfNotGreaterThan:
				return new IfNotGreaterThan();
			case Op.IfNotGreaterEqual:
				return new IfNotGreaterEqual();
			case Op.Jump:
				return new Jump();
			case Op.IfTrue:
				return new IfTrue();
			case Op.IfFalse:
				return new IfFalse();
			case Op.IfEqual:
				return new IfEqual();
			case Op.IfNotEqual:
				return new IfNotEqual();
			case Op.IfLessThan:
				return new IfLessThan();
			case Op.IfLessEqual:
				return new IfLessEqual();
			case Op.IfGreaterThan:
				return new IfGreaterThan();
			case Op.IfGreaterEqual:
				return new IfGreaterEqual();
			case Op.IfStrictEqual:
				return new IfStrictEqual();
			case Op.IfStrictNotEqual:
				return new IfStrictNotEqual();
			case Op.LookupSwitch:
				return new LookupSwitch();
			case Op.PushWith:
				return new PushWith();
			case Op.PopScope:
				return new PopScope();
			case Op.NextName:
				return new NextName();
			case Op.HasNext:
				return new HasNext();
			case Op.PushNull:
				return new PushNull();
			case Op.PushUndefined:
				return new PushUndefined();
			case Op.NextValue:
				return new NextValue();
			case Op.PushByte:
				return new PushByte();
			case Op.PushShort:
				return new PushShort();
			case Op.PushTrue:
				return new PushTrue();
			case Op.PushFalse:
				return new PushFalse();
			case Op.PushNaN:
				return new PushNaN();
			case Op.Pop:
				return new Pop();
			case Op.Dup:
				return new Dup();
			case Op.Swap:
				return new Swap();
			case Op.PushString:
				return new PushString();
			case Op.PushInt:
				return new PushInt();
			case Op.PushUInt:
				return new PushUInt();
			case Op.PushDouble:
				return new PushDouble();
			case Op.PushScope:
				return new PushScope();
			case Op.PushNamespace:
				return new PushNamespace();
			case Op.HasNext2:
				return new HasNext2();
			case Op.NewFunction:
				return new NewFunction();
			case Op.Call:
				return new Call();
			case Op.Construct:
				return new Construct();
			case Op.CallMethod:
				return new CallMethod();
			case Op.CallStatic:
				return new CallStatic();
			case Op.CallSuper:
				return new CallSuper();
			case Op.CallProperty:
				return new CallProperty();
			case Op.ReturnVoid:
				return new ReturnVoid();
			case Op.ReturnValue:
				return new ReturnValue();
			case Op.ConstructSuper:
				return new ConstructSuper();
			case Op.ConstructProp:
				return new ConstructProp();
				// case Op.CallSuperId:
				// return null;// new CallSuperId();//{MISSING}
			case Op.CallPropLex:
				return new CallPropLex();
				// case Op.CallInterface:
				// return null;// new CallInterface();//{MISSING}
			case Op.CallSuperVoid:
				return new CallSuperVoid();
			case Op.CallPropVoid:
				return new CallPropVoid();
			case Op.ApplyType:
				return new ApplyType();
			case Op.NewObject:
				return new NewObject();
			case Op.NewArray:
				return new NewArray();
			case Op.NewActivation:
				return new NewActivation();
			case Op.NewClass:
				return new NewClass();
			case Op.GetDescendants:
				return new GetDescendants();
			case Op.NewCatch:
				return new NewCatch();
			case Op.FindPropStrict:
				return new FindPropStrict();
			case Op.FindProperty:
				return new FindProperty();
				// case Op.FindDef:
				// return null;// new FindDef();//{MISSING}
			case Op.GetLex:
				return new GetLex();
			case Op.SetProperty:
				return new SetProperty();
			case Op.GetLocal:
				return new GetLocal();
			case Op.SetLocal:
				return new SetLocal();
			case Op.GetGlobalScope:
				return new GetGlobalScope();
			case Op.GetScopeObject:
				return new GetScopeObject();
			case Op.GetProperty:
				return new GetProperty();
			case Op.GetPropertyLate:
				return new GetPropertyLate();
			case Op.InitProperty:
				return new InitProperty();
			case Op.SetPropertyLate:
				return new SetPropertyLate();
			case Op.DeleteProperty:
				return new DeleteProperty();
			case Op.DeletePropertyLate:
				return new DeletePropertyLate();
			case Op.GetSlot:
				return new GetSlot();
			case Op.SetSlot:
				return new SetSlot();
			case Op.GetGlobalSlot:
				return new GetGlobalSlot();
			case Op.SetGlobalSlot:
				return new SetGlobalSlot();
			case Op.ConvertString:
				return new ConvertString();
			case Op.EscXmlElem:
				return new EscXmlElem();
			case Op.EscXmlAttr:
				return new EscXmlAttr();
			case Op.ConvertInt:
				return new ConvertInt();
			case Op.ConvertUInt:
				return new ConvertUInt();
			case Op.ConvertDouble:
				return new ConvertDouble();
			case Op.ConvertBoolean:
				return new ConvertBoolean();
			case Op.ConvertObject:
				return new ConvertObject();
			case Op.CheckFilter:
				return new CheckFilter();
			case Op.Coerce:
				return new Coerce();
			case Op.CoerceBoolean:
				return new CoerceBoolean();
			case Op.CoerceAny:
				return new CoerceAny();
			case Op.CoerceInt:
				return new CoerceInt();
			case Op.CoerceDouble:
				return new CoerceDouble();
			case Op.CoerceString:
				return new CoerceString();
			case Op.AsType:
				return new AsType();
			case Op.AsTypeLate:
				return new AsTypeLate();
			case Op.CoerceUInt:
				return new CoerceUInt();
			case Op.CoerceObject:
				return new CoerceObject();
			case Op.Negate:
				return new Negate();
			case Op.Increment:
				return new Increment();
			case Op.IncLocal:
				return new IncLocal();
			case Op.Decrement:
				return new Decrement();
			case Op.DecLocal:
				return new DecLocal();
			case Op.TypeOf:
				return new TypeOf();
			case Op.Not:
				return new Not();
			case Op.BitNot:
				return new BitNot();
			case Op.Concat:
				return new Concat();
			case Op.AddDouble:
				return new AddDouble();
			case Op.Add:
				return new Add();
			case Op.Subtract:
				return new Subtract();
			case Op.Multiply:
				return new Multiply();
			case Op.Divide:
				return new Divide();
			case Op.Modulo:
				return new Modulo();
			case Op.ShiftLeft:
				return new ShiftLeft();
			case Op.ShiftRight:
				return new ShiftRight();
			case Op.ShiftRightUnsigned:
				return new ShiftRightUnsigned();
			case Op.BitAnd:
				return new BitAnd();
			case Op.BitOr:
				return new BitOr();
			case Op.BitXor:
				return new BitXor();
			case Op.Equals:
				return new Equals();
			case Op.StrictEquals:
				return new StrictEquals();
			case Op.LessThan:
				return new LessThan();
			case Op.LessEquals:
				return new LessEquals();
			case Op.GreaterThan:
				return new GreaterThan();
			case Op.GreaterEquals:
				return new GreaterEquals();
			case Op.InstanceOf:
				return new InstanceOf();
			case Op.IsType:
				return new IsType();
			case Op.IsTypeLate:
				return new IsTypeLate();
			case Op.In:
				return new In();
			case Op.IncrementInt:
				return new IncrementInt();
			case Op.DecrementInt:
				return new DecrementInt();
			case Op.IncLocalInt:
				return new IncLocalInt();
			case Op.DecLocalInt:
				return new DecLocalInt();
			case Op.NegateInt:
				return new NegateInt();
			case Op.AddInt:
				return new AddInt();
			case Op.SubtractInt:
				return new SubtractInt();
			case Op.MultiplyInt:
				return new MultiplyInt();
			case Op.GetLocal0:
				return new GetLocal0();
			case Op.GetLocal1:
				return new GetLocal1();
			case Op.GetLocal2:
				return new GetLocal2();
			case Op.GetLocal3:
				return new GetLocal3();
			case Op.SetLocal0:
				return new SetLocal0();
			case Op.SetLocal1:
				return new SetLocal1();
			case Op.SetLocal2:
				return new SetLocal2();
			case Op.SetLocal3:
				return new SetLocal3();
			case Op.Debug:
				return new Debug();
			case Op.DebugLine:
				return new DebugLine();
			case Op.DebugFile:
				return new DebugFile();
			case Op.BreakpointLine:
				return new BreakpointLine();

			case Op.SetByte:
				return new SetByte();
			case Op.SetShort:
				return new SetShort();
			case Op.SetInt:
				return new SetInt();
			case Op.SetFloat:
				return new SetFloat();
			case Op.SetDouble:
				return new SetDouble();
			case Op.GetByte:
				return new GetByte();
			case Op.GetShort:
				return new GetShort();
			case Op.GetInt:
				return new GetInt();
			case Op.GetFloat:
				return new GetFloat();
			case Op.GetDouble:
				return new GetDouble();
			case Op.Sign1:
				return new Sign1();
			case Op.Sign8:
				return new Sign8();
			case Op.Sign16:
				return new Sign16();

			default:
				Logger.getLogger( OperationFactory.class.getName() ).severe(
						"Unkown op 0x" + Integer.toHexString( code )
								+ " detected." );
				return null;
		}
	}

	public AbstractOperation getLocal( final int register )
	{
		if( 0 == register )
		{
			return new GetLocal0();
		}
		else if( 1 == register )
		{
			return new GetLocal1();
		}
		else if( 2 == register )
		{
			return new GetLocal2();
		}
		else if( 3 == register )
		{
			return new GetLocal3();
		}
		else
		{
			final GetLocal getLocal = new GetLocal();

			getLocal.register = register;

			return getLocal;
		}
	}
}
