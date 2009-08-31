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

package com.joa_ebert.apparat.taas.expr;

import java.util.LinkedList;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.Namespace;
import com.joa_ebert.apparat.abc.NamespaceKind;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.operations.GetLex;
import com.joa_ebert.apparat.abc.bytecode.operations.NewClass;
import com.joa_ebert.apparat.abc.bytecode.operations.PopScope;
import com.joa_ebert.apparat.abc.bytecode.operations.PushScope;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.taas.TaasExpression;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasTyper;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.types.ClassType;
import com.joa_ebert.apparat.taas.types.MultinameType;
import com.joa_ebert.apparat.taas.types.ObjectType;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TNewClass extends TaasExpression
{
	private static final QName OBJECT_QNAME = new QName( new Namespace(
			NamespaceKind.PackageNamespace, "" ), "Object" );

	@TaasReference
	public TaasValue base;

	@TaasReference
	public Class klass;

	public TNewClass( final TaasValue base, final Class klass )
	{
		super( ClassType.INSTANCE );

		this.base = base;
		this.klass = klass;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		//
		// NewClass works only in the correct scope. We have to establish a
		// scope stack that represents the inheritance.
		//
		// Imagine A extends B then we have to have a scope stack of:
		//
		// 0: Object
		// 1: B
		//

		final TaasTyper typer = new TaasTyper( environment );
		final LinkedList<AbstractMultiname> scopes = new LinkedList<AbstractMultiname>();

		TaasType baseType = typer.baseOf( base.getType() );

		//
		// Get all scopes up to the ObjectType
		//

		scopes.addFirst( ( (MultinameType)( base.getType() ) ).multiname );

		while( baseType != ObjectType.INSTANCE )
		{
			if( baseType instanceof MultinameType )
			{
				final MultinameType multinameType = (MultinameType)baseType;
				scopes.addFirst( multinameType.multiname );
			}

			baseType = typer.baseOf( baseType );
		}

		scopes.addFirst( OBJECT_QNAME );

		//
		// Construct scope stack:
		//

		for( final AbstractMultiname multiname : scopes )
		{
			code.add( new GetLex( multiname ) );
			code.add( new PushScope() );
		}

		//
		// NewClass
		//

		base.emit( environment, body, code );
		code.add( new NewClass( klass ) );

		//
		// Leave scope stack:
		//

		for( int i = 0, n = scopes.size(); i < n; ++i )
		{
			code.add( new PopScope() );
		}
	}

	@Override
	public String toString()
	{
		return "[TNewClass " + base.toString() + ", " + klass.toString() + "]";
	}
}
