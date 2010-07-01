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

package com.joa_ebert.apparat.tools;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Joa Ebert
 * 
 */
final class DefaultConfiguration implements IToolConfiguration
{
	private String input;
	private String output;
	private final List<String> libraries;
	private final Map<String, String> options;

	public DefaultConfiguration()
	{
		options = new HashMap<String, String>();
		libraries = new LinkedList<String>();
	}

	public void close()
	{
	}

	public String getInput()
	{
		return input;
	}

	public List<String> getLibraries()
	{
		return libraries;
	}

	public String getOption( final String name )
	{
		return options.get( name );
	}

	public String getOutput()
	{
		return output;
	}

	public boolean hasOption( final String name )
	{
		return options.containsKey( name );
	}

	public void needsOutput() throws ToolConfigurationException
	{
		if( null == output )
		{
			throw new ToolConfigurationException( "No output specified." );
		}
	}

	public void parse( final String[] arguments )
			throws ToolConfigurationException
	{
		final int n = arguments.length;
		final int m = n - 1;

		final List<String> libraries = new LinkedList<String>();

		String input = null;
		String output = null;

		for( int i = 0; i < n; ++i )
		{
			final String argument = arguments[ i ].toLowerCase();

			if( argument.equalsIgnoreCase( "-input" ) )
			{
				input = arguments[ ++i ];
			}
			else if( argument.equalsIgnoreCase( "-output" ) )
			{
				output = arguments[ ++i ];
			}
			else if( argument.equalsIgnoreCase( "-library" ) )
			{
				libraries.add( arguments[ ++i ] );
			}
			else if( argument.charAt( 0 ) == '-' )
			{
				if( m != i )
				{
					if( arguments[ i + 1 ].charAt( 0 ) == '-' )
					{
						options.put( argument.substring( 1 ), "true" );
					}
					else
					{
						options.put( argument.substring( 1 ), arguments[ ++i ] );
					}
				}
				else
				{
					options.put( argument.substring( 1 ), "true" );
				}
			}
			else
			{
				throw new ToolConfigurationException( "Unrecognized option \""
						+ argument + "\"." );
			}
		}

		if( hasOption( "help" ) )
		{
			return;
		}

		if( null == input )
		{
			throw new ToolConfigurationException( "No input specified." );
		}

		final File inputFile = new File( input );

		if( !inputFile.exists() )
		{
			throw new ToolConfigurationException( "Input file does not exist." );
		}

		if( !inputFile.canRead() )
		{
			throw new ToolConfigurationException(
					"Can not read from input file." );
		}

		this.input = input;
		this.output = output;

		for( final String library : libraries )
		{
			final File libraryFile = new File( library );

			if( !libraryFile.exists() )
			{
				throw new ToolConfigurationException( "Library \"" + library
						+ "\" does not exist." );
			}

			if( !libraryFile.canRead() )
			{
				throw new ToolConfigurationException( "Can not read from \""
						+ library + "\"." );
			}

			this.libraries.add( library );
		}
	}
}
