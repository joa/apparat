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

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.operations.DefaultXmlNamespace;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasExpression;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.constants.TaasString;
import com.joa_ebert.apparat.taas.types.StringType;
import com.joa_ebert.apparat.taas.types.VoidType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TDefaultXmlNamespace extends TaasExpression
{
	@TaasReference
	public TaasValue uri;

	public TDefaultXmlNamespace( final TaasValue uri )
	{
		super( VoidType.INSTANCE );

		if( !uri.isType( StringType.INSTANCE ) )
		{
			throw new TaasException( "URI must be of StringType." );
		}

		this.uri = uri;
	}

	@Override
	protected void emitOps( final AbcEnvironment environment,
			final MethodBody body, final Bytecode code )
	{
		if( uri instanceof TaasString )
		{
			final TaasString uriString = (TaasString)uri;
			code.add( new DefaultXmlNamespace( uriString.value ) );
		}
		else
		{
			throw new TaasException( "TaasString expected." );
		}
	}
}
