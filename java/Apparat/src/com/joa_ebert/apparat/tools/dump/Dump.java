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

package com.joa_ebert.apparat.tools.dump;

import java.io.File;
import java.io.FileOutputStream;

import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.define.DefineBitsJPEG2Tag;
import com.joa_ebert.apparat.tools.ITool;
import com.joa_ebert.apparat.tools.IToolConfiguration;
import com.joa_ebert.apparat.tools.ToolLog;
import com.joa_ebert.apparat.tools.ToolRunner;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Dump implements ITool
{
	public static void main( final String[] arguments )
	{
		final ToolRunner toolRunner = new ToolRunner( new Dump(), arguments );

		toolRunner.run();
	}

	private IToolConfiguration config;

	public String getHelp()
	{
		return "-input [file]\tThe input file\n"
				+ "-bytecode\tWill dump bytecode.\n"
				+ "-tags\t\tWill dump known tags.\n"
				+ "-images\tWill dump images.";
	}

	public String getName()
	{
		return "Dump";
	}

	public boolean needsOutput()
	{
		return false;
	}

	public void run() throws Exception
	{
		final TagIO tagIO = new TagIO( config.getInput() );

		tagIO.read();

		if( config.hasOption( "tags" ) )
		{
			for( final ITag tag : tagIO.getTags() )
			{
				final String tagString = Tags.typeToString( tag.getType() );
				String output = tagString;

				if( tagString.length() < 5 )
				{
					output += "\t\t\t";
				}
				else if( tagString.length() < 12 )
				{
					output += "\t\t";
				}
				else
				{
					output += "\t";
				}

				ToolLog.success( output + tag.toString() );
			}
		}

		if( config.hasOption( "images" ) )
		{
			for( final ITag tag : tagIO.getTags() )
			{
				if( tag.getType() == Tags.DefineBitsJPEG2 )
				{
					final DefineBitsJPEG2Tag defineBitsJPEG2 = (DefineBitsJPEG2Tag)tag;

					String extension = "";

					if( defineBitsJPEG2.imageData[ 0 ] == (byte)0xff )
					{
						extension = ".jpg";
					}
					else if( defineBitsJPEG2.imageData[ 0 ] == (byte)0x89 )
					{
						extension = ".png";
					}
					else if( defineBitsJPEG2.imageData[ 0 ] == (byte)0x47 )
					{
						extension = ".gif";
					}

					new File( "images" ).mkdirs();

					final File file = new File( "images" + File.separator
							+ defineBitsJPEG2.characterId + extension );

					final FileOutputStream fileOutputStream = new FileOutputStream(
							file );

					fileOutputStream.write( defineBitsJPEG2.imageData );

					fileOutputStream.flush();

					fileOutputStream.close();
				}
			}
		}

		if( config.hasOption( "bytecode" ) )
		{
			ToolLog.warn( "Not implemented..." );
		}

		tagIO.close();
	}

	public void setConfiguration( final IToolConfiguration configuration )
	{
		config = configuration;
	}
}
