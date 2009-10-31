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

import java.io.PrintWriter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class ToolRunner
{
	private final ITool tool;
	private final String[] arguments;

	public ToolRunner( final ITool tool, final String arguments[] )
	{
		this.tool = tool;
		this.arguments = arguments;
	}

	public int run()
	{
		long t0, t1;
		ToolLog.initialize( new PrintWriter( System.out ) );
		ToolLog.info( "Running tool \"" + tool.getName() + "\" ..." );
		t0 = System.currentTimeMillis();

		final DefaultConfiguration defaultConfiguration = new DefaultConfiguration();

		boolean hasError = false;
		try
		{
			defaultConfiguration.parse( arguments );

			if( tool.needsOutput() )
			{
				defaultConfiguration.needsOutput();
			}
		}
		catch( final ToolConfigurationException e )
		{
			ToolLog.error( e );
			hasError = true;
		}

		if( defaultConfiguration.hasOption( "help" ) )
		{
			ToolLog.help( tool.getHelp() );
		}
		else
		{
			if( !hasError )
			{
				try
				{
					tool.setConfiguration( defaultConfiguration );
					tool.run();
				}
				catch( final Exception e )
				{
					ToolLog.error( e );
					hasError = true;
				}
			}
			else
			{
				ToolLog.help( tool.getHelp() );
			}
		}

		t1 = System.currentTimeMillis();

		ToolLog
				.info( "Completed in " + Long.toString( t1 - t0 )
						+ " milliseconds." );

		ToolLog.info( "http://www.joa-ebert.com/" );

		return ( hasError ) ? -1 : 0;
	}
}
