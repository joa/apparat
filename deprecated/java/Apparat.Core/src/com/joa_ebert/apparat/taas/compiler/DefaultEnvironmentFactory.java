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

package com.joa_ebert.apparat.taas.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbcException;

/**
 * @author Joa Ebert
 * 
 */
public final class DefaultEnvironmentFactory
{
	public static AbcEnvironment create() throws IOException
	{
		final Abc builtinABC = new Abc();
		final Abc toplevelABC = new Abc();

		final URL builtinURL = DefaultEnvironmentFactory.class
				.getResource( "/com/joa_ebert/apparat/taas/compiler/builtin/builtin.abc" );

		final URL toplevelURL = DefaultEnvironmentFactory.class
				.getResource( "/com/joa_ebert/apparat/taas/compiler/builtin/toplevel.abc" );

		InputStream stream = null;

		try
		{
			stream = builtinURL.openStream();
			builtinABC.read( stream );
			stream.close();
			stream = toplevelURL.openStream();
			toplevelABC.read( stream );
		}
		catch( final IOException exception )
		{
			exception.printStackTrace();
		}
		catch( final AbcException exception )
		{
			exception.printStackTrace();
		}
		finally
		{
			if( null != stream )
			{
				stream.close();
			}
		}

		return new AbcEnvironment( new Abc[] {
				builtinABC, toplevelABC
		} );
	}

	private DefaultEnvironmentFactory()
	{

	}
}
