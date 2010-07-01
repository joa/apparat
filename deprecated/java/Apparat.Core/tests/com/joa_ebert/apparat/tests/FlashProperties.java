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

package com.joa_ebert.apparat.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Joa Ebert
 * 
 */
public class FlashProperties
{
	private static final Object lock = new Object();
	private static Properties properties = null;

	private static String player;
	private static String flashlog;

	public static String getLog() throws IOException
	{
		synchronized( lock )
		{
			if( null == properties )
			{
				initialize();
			}
		}

		return flashlog;
	}

	public static String getPlayer() throws IOException
	{
		synchronized( lock )
		{
			if( null == properties )
			{
				initialize();
			}
		}

		return player;
	}

	private static void initialize() throws IOException
	{
		synchronized( lock )
		{
			if( null != properties )
			{
				return;
			}

			final URL propertiesURL = FlashProperties.class
					.getResource( "/com/joa_ebert/apparat/tests/player.properties" );

			properties = new Properties();

			InputStream stream = null;

			try
			{
				stream = propertiesURL.openStream();
				properties.load( stream );
			}
			finally
			{
				if( null != stream )
				{
					stream.close();
				}
			}

			if( !properties.containsKey( "player.log" )
					|| !properties.containsKey( "player.exe" ) )
			{
				throw new IOException(
						"Properties player.log and player.exe have to be set." );
			}

			player = properties.getProperty( "player.exe" );
			flashlog = properties.getProperty( "player.log" );

			final File playerFile = new File( player );

			if( !playerFile.exists() )
			{
				throw new FileNotFoundException(
						"FlashPlayer can not be found at \"" + player + "\"" );
			}
		}
	}
}
