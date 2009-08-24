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

package com.joa_ebert.apparat.utils;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author Joa Ebert
 * 
 */
public class IndentingPrintWriter extends PrintWriter
{
	private boolean useTabs = false;
	private int indent = 0;
	private char[] indentBuffer;

	private final boolean isChained;
	private final IndentingPrintWriter parent;

	public IndentingPrintWriter( final Writer writer )
	{
		super( writer );

		isChained = writer instanceof IndentingPrintWriter;

		if( isChained )
		{
			parent = (IndentingPrintWriter)writer;
		}
		else
		{
			parent = null;
		}

		updateIndent();
	}

	public void popIndent()
	{
		if( --indent < 0 )
		{
			indent = 0;
		}

		updateIndent();
	}

	private void printIndent()
	{
		print( indentBuffer );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println()
	{
		printIndent();

		if( isChained )
		{
			parent.println();
		}
		else
		{
			super.println();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final boolean x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final char x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final char[] x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final double x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final float x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final int x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final long x )

	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final Object x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println( final String x )
	{
		printIndent();

		if( isChained )
		{
			parent.println( x );
		}
		else
		{
			super.println( x );
		}
	}

	public void pushIndent()
	{
		indent++;
		updateIndent();
	}

	public void setIndent( final int value )
	{
		indent = value;
		updateIndent();
	}

	public void setUseTabs( final boolean value )
	{
		useTabs = value;
		updateIndent();
	}

	private void updateIndent()
	{
		if( useTabs )
		{
			indentBuffer = new char[ indent ];

			for( int i = 0; i < indent; ++i )
			{
				indentBuffer[ i ] = '\t';
			}
		}
		else
		{
			int j = 0;

			indentBuffer = new char[ indent << 2 ];

			for( int i = 0; i < indent; ++i )
			{
				indentBuffer[ j++ ] = ' ';
				indentBuffer[ j++ ] = ' ';
				indentBuffer[ j++ ] = ' ';
				indentBuffer[ j++ ] = ' ';
			}
		}
	}
}
