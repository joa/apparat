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

package com.joa_ebert.apparat.swf.tags.control;

import java.io.IOException;
import java.util.Date;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.io.RECORDHEADER;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;
import com.joa_ebert.apparat.swf.tags.ControlTag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class ProductInfoTag extends ControlTag
{
	public static final int PRODUCT_UNKNOWN = 0;
	public static final int PRODUCT_J2EE = 1;
	public static final int PRODUCT_NET = 2;
	public static final int PRODUCT_FLEX = 3;

	public static final int EDITION_DEVELOPER = 0;
	public static final int EDITION_FULL_COMMERCIAL = 1;
	public static final int EDITION_NON_COMMERCIAL = 2;
	public static final int EDITION_EDUCATIONAL = 3;
	public static final int EDITION_NFR = 4;
	public static final int EDITION_TRIAL = 5;
	public static final int EDITION_NONE = 6;

	public long product;
	public long edition;

	public int versionMajor;
	public int versionMinor;

	public int build;

	public Date compileDate;

	public void accept( final ITagVisitor visitor )
	{
		visitor.visit( this );
	}

	public int getLength()
	{
		return 26;
	}

	public int getType()
	{
		return Tags.ProductInfo;
	}

	public boolean isLengthKnown()
	{
		return true;
	}

	public void read( final RECORDHEADER header, final SwfInputStream input )
			throws IOException, SwfException
	{
		product = input.readUI32();
		edition = input.readUI32();

		versionMajor = input.readUI08();
		versionMinor = input.readUI08();

		build = input.readUI64().intValue();
		compileDate = new Date( input.readUI64().longValue() );
	}

	@Override
	public String toString()
	{
		return "[ProductInfo product: " + product + ", edition: " + edition
				+ ", version " + versionMajor + "." + versionMinor
				+ ", build: " + build + ", compileDate: " + compileDate + "]";
	}

	public void write( final SwfOutputStream output ) throws IOException
	{
		output.writeUI32( product );
		output.writeUI32( edition );

		output.writeUI08( versionMajor );
		output.writeUI08( versionMinor );

		output.writeUI64( build );
		output.writeUI64( compileDate.getTime() );
	}
}
