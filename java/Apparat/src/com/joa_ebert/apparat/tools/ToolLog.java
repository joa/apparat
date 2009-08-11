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
public class ToolLog
{
	private static final boolean DEBUG = true;

	private static PrintWriter writer;

	public static void close()
	{
		writer.flush();
		writer.close();

		writer = null;
	}

	public static void error( final String message )
	{
		write( "[-] " + message );
	}

	public static void error( final Throwable throwable )
	{
		error( throwable.getMessage() );

		if( DEBUG )
		{
			throwable.printStackTrace( writer );
		}
	}

	public static void help( final String message )
	{
		write( "[?] Help:\n" + message );
	}

	public static void info( final String message )
	{
		write( "[i] " + message );
	}

	public static void initialize( final PrintWriter writer )
	{
		ToolLog.writer = writer;
	}

	public static void success( final String message )
	{
		write( "[+] " + message );
	}

	public static void warn( final String message )
	{
		write( "[!] " + message );
	}

	private static void write( final String message )
	{
		writer.write( message + "\n" );
		writer.flush();
	}
}
