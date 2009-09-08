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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Joa Ebert
 * 
 */
public class FlashPlayerTest
{
	public void assertNoError() throws FileNotFoundException, IOException
	{
		final FileReader fileReader = new FileReader( getFlashLog() );
		final BufferedReader bufferedReader = new BufferedReader( fileReader );

		while( bufferedReader.ready() )
		{
			final String line = bufferedReader.readLine();

			if( line.startsWith( "VerifyError:" )
					|| line.startsWith( "ReferenceError:" )
					|| line.startsWith( "TypeError:" ) )
			{
				bufferedReader.close();
				fileReader.close();

				Assert.fail( line );
			}
		}

		bufferedReader.close();
		fileReader.close();
	}

	public boolean containsError() throws IOException
	{
		final FileReader fileReader = new FileReader( getFlashLog() );
		final BufferedReader bufferedReader = new BufferedReader( fileReader );

		while( bufferedReader.ready() )
		{
			final String line = bufferedReader.readLine();

			if( line.startsWith( "VerifyError:" ) )
			{
				bufferedReader.close();
				fileReader.close();

				return true;
			}
		}

		bufferedReader.close();
		fileReader.close();

		return false;
	}

	private File getFlashLog() throws IOException
	{
		final File result = new File( FlashProperties.getLog() );

		if( !result.exists() )
		{
			throw new FileNotFoundException(
					"flashlog.txt can not be found at \""
							+ FlashProperties.getLog() + "\"." );
		}

		return result;
	}

	public String[] getLog() throws IOException
	{
		final List<String> list = new LinkedList<String>();
		final FileReader fileReader = new FileReader( getFlashLog() );
		final BufferedReader bufferedReader = new BufferedReader( fileReader );

		while( bufferedReader.ready() )
		{
			list.add( bufferedReader.readLine() );
		}

		bufferedReader.close();
		fileReader.close();

		final String[] result = new String[ list.size() ];

		list.toArray( result );

		return result;
	}

	public void printLog( final OutputStream output ) throws IOException
	{
		printLog( new PrintWriter( output ) );
	}

	public void printLog( final PrintWriter output )
			throws FileNotFoundException, IOException
	{
		final FileReader fileReader = new FileReader( getFlashLog() );
		final BufferedReader bufferedReader = new BufferedReader( fileReader );

		while( bufferedReader.ready() )
		{
			output.println( bufferedReader.readLine() );
		}

		output.flush();

		bufferedReader.close();
		fileReader.close();
	}

	public void spawn( final File swf ) throws IOException,
			InterruptedException
	{
		spawn( swf, 0x200 );
	}

	public void spawn( final File swf, final long timeout ) throws IOException,
			InterruptedException
	{
		final ProcessBuilder builder = new ProcessBuilder( FlashProperties
				.getPlayer(), swf.getAbsolutePath() );

		Process process;

		process = builder.start();

		Thread.sleep( timeout );

		process.destroy();
	}

	public void spawn( final String swf ) throws IOException,
			InterruptedException
	{
		spawn( swf, 0x3e8 );
	}

	public void spawn( final String swf, final long timeout )
			throws IOException, InterruptedException
	{
		spawn( new File( swf ), timeout );
	}

	@Test
	public void testPlayer() throws IOException, InterruptedException
	{
		final Process process = new ProcessBuilder( FlashProperties.getPlayer() )
				.start();

		Thread.sleep( 0x10 );

		process.destroy();
	}
}
