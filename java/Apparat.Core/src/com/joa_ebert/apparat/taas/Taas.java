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

package com.joa_ebert.apparat.taas;

import com.joa_ebert.apparat.taas.constants.TaasInt;
import com.joa_ebert.apparat.taas.constants.TaasMultiname;
import com.joa_ebert.apparat.taas.expr.TAdd;
import com.joa_ebert.apparat.taas.expr.TApplyType;
import com.joa_ebert.apparat.taas.expr.TAsType;
import com.joa_ebert.apparat.taas.expr.TBitAnd;
import com.joa_ebert.apparat.taas.expr.TBitNot;
import com.joa_ebert.apparat.taas.expr.TBitOr;
import com.joa_ebert.apparat.taas.expr.TBitXor;
import com.joa_ebert.apparat.taas.expr.TCallProperty;
import com.joa_ebert.apparat.taas.expr.TCheckFilter;
import com.joa_ebert.apparat.taas.expr.TCoerce;
import com.joa_ebert.apparat.taas.expr.TConstruct;
import com.joa_ebert.apparat.taas.expr.TConstructProperty;
import com.joa_ebert.apparat.taas.expr.TConstructSuper;
import com.joa_ebert.apparat.taas.expr.TConvert;
import com.joa_ebert.apparat.taas.expr.TDecLocal;
import com.joa_ebert.apparat.taas.expr.TDecrement;
import com.joa_ebert.apparat.taas.expr.TDefaultXmlNamespace;
import com.joa_ebert.apparat.taas.expr.TDeleteProperty;
import com.joa_ebert.apparat.taas.expr.TDivide;
import com.joa_ebert.apparat.taas.expr.TEnterScope;
import com.joa_ebert.apparat.taas.expr.TEquals;
import com.joa_ebert.apparat.taas.expr.TEscapeXmlAttribute;
import com.joa_ebert.apparat.taas.expr.TEscapeXmlElement;
import com.joa_ebert.apparat.taas.expr.TFindProperty;
import com.joa_ebert.apparat.taas.expr.TFindPropertyStrict;
import com.joa_ebert.apparat.taas.expr.TGetLex;
import com.joa_ebert.apparat.taas.expr.TGetProperty;
import com.joa_ebert.apparat.taas.expr.TGetScopeObject;
import com.joa_ebert.apparat.taas.expr.TGreaterEquals;
import com.joa_ebert.apparat.taas.expr.TGreaterThan;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.expr.TIncLocal;
import com.joa_ebert.apparat.taas.expr.TIncrement;
import com.joa_ebert.apparat.taas.expr.TInitProperty;
import com.joa_ebert.apparat.taas.expr.TJump;
import com.joa_ebert.apparat.taas.expr.TKill;
import com.joa_ebert.apparat.taas.expr.TLeaveScope;
import com.joa_ebert.apparat.taas.expr.TLessEquals;
import com.joa_ebert.apparat.taas.expr.TLessThan;
import com.joa_ebert.apparat.taas.expr.TLookupSwitch;
import com.joa_ebert.apparat.taas.expr.TModulo;
import com.joa_ebert.apparat.taas.expr.TMultiply;
import com.joa_ebert.apparat.taas.expr.TNegate;
import com.joa_ebert.apparat.taas.expr.TNewClass;
import com.joa_ebert.apparat.taas.expr.TNot;
import com.joa_ebert.apparat.taas.expr.TReturn;
import com.joa_ebert.apparat.taas.expr.TReturnVoid;
import com.joa_ebert.apparat.taas.expr.TSetLocal;
import com.joa_ebert.apparat.taas.expr.TSetProperty;
import com.joa_ebert.apparat.taas.expr.TShiftLeft;
import com.joa_ebert.apparat.taas.expr.TShiftRight;
import com.joa_ebert.apparat.taas.expr.TShiftRightUnsigned;
import com.joa_ebert.apparat.taas.expr.TStrictEquals;
import com.joa_ebert.apparat.taas.expr.TSubtract;
import com.joa_ebert.apparat.taas.expr.TThrow;
import com.joa_ebert.apparat.taas.expr.TTypeOf;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Taas
{
	public TAdd add( final TaasValue lhs, final TaasValue rhs )
	{
		return new TAdd( lhs, rhs );
	}

	public TApplyType applyType( final TaasValue object,
			final TaasValue[] parameters )
	{
		return new TApplyType( object, parameters );
	}

	public TAsType asType( final TaasValue lhs, final TaasType rhs )
	{
		return new TAsType( lhs, rhs );
	}

	public TBitAnd bitAnd( final TaasValue lhs, final TaasValue rhs )
	{
		return new TBitAnd( lhs, rhs );
	}

	public TBitNot bitNot( final TaasValue rhs )
	{
		return new TBitNot( rhs );
	}

	public TBitOr bitOr( final TaasValue lhs, final TaasValue rhs )
	{
		return new TBitOr( lhs, rhs );
	}

	public TBitXor bitXor( final TaasValue lhs, final TaasValue rhs )
	{
		return new TBitXor( lhs, rhs );
	}

	public TCallProperty callProperty( final TaasValue object,
			final TaasMultiname property, final TaasValue[] parameters )
	{
		return new TCallProperty( object, property, parameters );
	}

	public TCallProperty callProperty( final TaasValue object,
			final TaasMultiname property, final TaasValue[] parameters,
			final TaasType returnType )
	{
		return new TCallProperty( object, property, parameters, returnType );
	}

	public TCheckFilter checkFilter( final TaasValue value )
	{
		return new TCheckFilter( value );
	}

	public TCoerce coerce( final TaasValue lhs, final TaasType rhs )
	{
		return new TCoerce( lhs, rhs );
	}

	public TConstruct construct( final TaasValue object,
			final TaasValue[] parameters )
	{
		return new TConstruct( object, parameters );
	}

	public TConstruct constructProperty( final TaasValue object,
			final TaasMultiname property, final TaasValue[] parameters )
	{
		return new TConstructProperty( object, property, parameters );
	}

	public TConstructSuper constructSuper( final TaasValue object,
			final TaasValue[] parameters )
	{
		return new TConstructSuper( object, parameters );
	}

	public TConvert convert( final TaasValue lhs, final TaasType rhs )
	{
		return new TConvert( lhs, rhs );
	}

	public TDecLocal decLocal( final TaasLocal local )
	{
		return new TDecLocal( local );
	}

	public TDecrement decrement( final TaasValue rhs )
	{
		return new TDecrement( rhs );
	}

	public TDefaultXmlNamespace defaultXmlNamespace( final TaasValue uri )
	{
		return new TDefaultXmlNamespace( uri );
	}

	public TDeleteProperty deleteProperty( final TaasValue object,
			final TaasMultiname property )
	{
		return new TDeleteProperty( object, property );
	}

	public TDivide divide( final TaasValue lhs, final TaasValue rhs )
	{
		return new TDivide( lhs, rhs );
	}

	public TEnterScope enterScope( final TaasValue scope )
	{
		return new TEnterScope( scope );
	}

	public TEquals equals( final TaasValue lhs, final TaasValue rhs )
	{
		return new TEquals( lhs, rhs );
	}

	public TEscapeXmlAttribute escapeXmlAttribute( final TaasValue value )
	{
		return new TEscapeXmlAttribute( value );
	}

	public TEscapeXmlElement escapeXmlElement( final TaasValue value )
	{
		return new TEscapeXmlElement( value );
	}

	public TFindProperty findProperty( final TaasMultiname property )
	{
		return new TFindProperty( property );
	}

	public TFindPropertyStrict findPropertyStrict( final TaasMultiname property )
	{
		return new TFindPropertyStrict( property );
	}

	public TGetLex getLex( final TaasMultiname property,
			final TaasType returnType )
	{
		return new TGetLex( property, returnType );
	}

	public TGetProperty getProperty( final TaasValue object,
			final TaasMultiname property, final TaasType returnType )
	{
		return new TGetProperty( object, property, returnType );
	}

	// public TConstructSuper constructSuper( final )

	public TGetScopeObject getScopeObject( final TaasValue scopeObject )
	{
		return new TGetScopeObject( scopeObject );
	}

	public TGreaterEquals greaterEquals( final TaasValue lhs,
			final TaasValue rhs )
	{
		return new TGreaterEquals( lhs, rhs );
	}

	public TGreaterThan greaterThan( final TaasValue lhs, final TaasValue rhs )
	{
		return new TGreaterThan( lhs, rhs );
	}

	public TIf if$( final TaasValue lhs, final TaasValue rhs,
			final TIf.Operator operator )
	{
		return new TIf( lhs, rhs, operator );
	}

	public TIf if$( final TaasValue lhs, final TIf.Operator operator )
	{
		return new TIf( lhs, operator );
	}

	public TIncLocal incLocal( final TaasLocal local )
	{
		return new TIncLocal( local );
	}

	public TIncrement increment( final TaasValue rhs )
	{
		return new TIncrement( rhs );
	}

	public TInitProperty initProperty( final TaasValue object,
			final TaasMultiname property, final TaasValue value )
	{
		return new TInitProperty( object, property, value );
	}

	public TJump jump()
	{
		return new TJump();
	}

	public TKill kill( final TaasLocal local )
	{
		return new TKill( local );
	}

	public TLeaveScope leaveScope()
	{
		return new TLeaveScope();
	}

	public TLessEquals lessEquals( final TaasValue lhs, final TaasValue rhs )
	{
		return new TLessEquals( lhs, rhs );
	}

	public TLessThan lessThan( final TaasValue lhs, final TaasValue rhs )
	{
		return new TLessThan( lhs, rhs );
	}

	public TLookupSwitch lookupSwitch( final TaasValue index )
	{
		return new TLookupSwitch( index );
	}

	public TModulo modulo( final TaasValue lhs, final TaasValue rhs )
	{
		return new TModulo( lhs, rhs );
	}

	public TMultiply multiply( final TaasValue lhs, final TaasValue rhs )
	{
		return new TMultiply( lhs, rhs );
	}

	public TNegate negate( final TaasValue rhs )
	{
		return new TNegate( rhs );
	}

	public TNewClass newClass( final TaasValue base, final TaasInt index )
	{
		return new TNewClass( base, index );
	}

	public TNot not( final TaasValue rhs )
	{
		return new TNot( rhs );
	}

	public TReturnVoid return$()
	{
		return new TReturnVoid();
	}

	public TReturn return$( final TaasValue value )
	{
		return new TReturn( value );
	}

	public TSetLocal setLocal( final TaasLocal local, final TaasValue value )
	{
		return new TSetLocal( local, value );
	}

	public TSetProperty setProperty( final TaasValue object,
			final TaasMultiname property, final TaasValue value )
	{
		return new TSetProperty( object, property, value );
	}

	public TShiftLeft shiftLeft( final TaasValue lhs, final TaasValue rhs )
	{
		return new TShiftLeft( lhs, rhs );
	}

	public TShiftRight shiftRight( final TaasValue lhs, final TaasValue rhs )
	{
		return new TShiftRight( lhs, rhs );
	}

	public TShiftRightUnsigned shiftRightUnsigned( final TaasValue lhs,
			final TaasValue rhs )
	{
		return new TShiftRightUnsigned( lhs, rhs );
	}

	public TStrictEquals strictEquals( final TaasValue lhs, final TaasValue rhs )
	{
		return new TStrictEquals( lhs, rhs );
	}

	public TSubtract subtract( final TaasValue lhs, final TaasValue rhs )
	{
		return new TSubtract( lhs, rhs );
	}

	public TThrow throw$( final TaasValue value )
	{
		return new TThrow( value );
	}

	public TTypeOf typeOf( final TaasValue value )
	{
		return new TTypeOf( value );
	}
}
