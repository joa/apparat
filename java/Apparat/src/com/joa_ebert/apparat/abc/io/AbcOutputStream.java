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

package com.joa_ebert.apparat.abc.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class AbcOutputStream extends OutputStream
{
	public static int calcU30Length( final int value )
	{
		if( value < 128 && value > -1 )
		{
			return 1;
		}
		else if( value < 16384 && value > -1 )
		{
			return 2;
		}
		else if( value < 2097152 && value > -1 )
		{
			return 3;
		}
		else if( value < 268435456 && value > -1 )
		{
			return 4;
		}
		else
		{
			return 5;
		}
	}

	public static void writeS24( final byte[] output, final int offset,
			final int value )
	{
		final int byte0 = value & 0xff;
		final int byte1 = ( value & 0xff00 ) >> 0x08;
		final int byte2 = ( value & 0xff0000 ) >> 0x10;

		output[ offset ] = (byte)byte0;
		output[ offset + 1 ] = (byte)byte1;
		output[ offset + 2 ] = (byte)byte2;
	}

	private final OutputStream output;

	private long position = 0L;

	private static final boolean DEBUG = false;
	private static final int BREAK_POSITION = 0x0;
	private int debugPosition = 0;

	public AbcOutputStream( final OutputStream output )
	{
		this.output = output;
	}

	@Override
	public void close() throws IOException
	{
		super.close();

		output.close();
	}

	@Override
	public void flush() throws IOException
	{
		output.flush();
	}

	public long getPosition()
	{
		return position;
	}

	@Override
	public void write( final byte[] value ) throws IOException
	{
		position += value.length;

		if( DEBUG )
		{
			debugPosition += value.length;

			if( debugPosition >= BREAK_POSITION )
			{
				throw new RuntimeException( "Debug breakpoint reached." );
			}
		}

		output.write( value );
	}

	@Override
	public void write( final byte[] value, final int offset, final int length )
			throws IOException
	{
		position += length;

		if( DEBUG )
		{
			debugPosition += length;

			if( debugPosition >= BREAK_POSITION )
			{
				throw new RuntimeException( "Debug breakpoint reached." );
			}
		}

		output.write( value, offset, length );
	}

	@Override
	public void write( final int value ) throws IOException
	{
		++position;

		if( DEBUG )
		{
			if( debugPosition == BREAK_POSITION )
			{
				throw new RuntimeException( "Debug breakpoint reached." );
			}

			debugPosition++;
		}

		output.write( value );
	}

	public void writeD64( final double value ) throws IOException
	{
		final long bits = Double.doubleToRawLongBits( value );

		write( (byte)bits );
		write( (byte)( bits >> 8 ) );
		write( (byte)( bits >> 16 ) );
		write( (byte)( bits >> 24 ) );
		write( (byte)( bits >> 32 ) );
		write( (byte)( bits >> 40 ) );
		write( (byte)( bits >> 48 ) );
		write( (byte)( bits >> 56 ) );
	}

	public void writeS24( final int value ) throws IOException
	{
		final int byte0 = value & 0xff;
		final int byte1 = ( value & 0xff00 ) >> 0x08;
		final int byte2 = ( value & 0xff0000 ) >> 0x10;

		write( byte0 );
		write( byte1 );
		write( byte2 );
	}

	public void writeS32( final int value ) throws IOException
	{
		writeVariableLength( value );
	}

	public void writeString( final String value ) throws IOException
	{
		final byte[] bytes = value.getBytes( "UTF8" );

		writeU30( bytes.length );
		write( bytes );
	}

	public void writeU08( final int value ) throws IOException
	{
		write( value & 0xff );
	}

	public void writeU16( final int value ) throws IOException
	{
		final int byte0 = value & 0xff;
		final int byte1 = ( value & 0xff00 ) >> 0x08;

		write( byte0 );
		write( byte1 );
	}

	public void writeU30( final int value ) throws IOException
	{
		writeVariableLength( value & 0x3fffffff );
	}

	public void writeU32( final long value ) throws IOException
	{
		writeVariableLength( value & 0xffffffffL );
	}

	private void writeVariableLength( final long value ) throws IOException
	{
		if( value < 128 && value > -1 )
		{
			write( (byte)value );
		}
		else if( value < 16384 && value > -1 )
		{
			write( (byte)( ( value & 0x7F ) | 0x80 ) );
			write( (byte)( ( ( value >> 7 ) & 0x7F ) ) );
		}
		else if( value < 2097152 && value > -1 )
		{
			write( (byte)( ( value & 0x7F ) | 0x80 ) );
			write( (byte)( value >> 7 | 0x80 ) );
			write( (byte)( ( ( value >> 14 ) ) & 0x7F ) );
		}
		else if( value < 268435456 && value > -1 )
		{
			write( (byte)( ( value & 0x7F ) | 0x80 ) );
			write( (byte)( value >> 7 | 0x80 ) );
			write( (byte)( value >> 14 | 0x80 ) );
			write( (byte)( ( value >> 21 ) & 0x7F ) );
		}
		else
		{
			write( (byte)( ( value & 0x7F ) | 0x80 ) );
			write( (byte)( value >> 7 | 0x80 ) );
			write( (byte)( value >> 14 | 0x80 ) );
			write( (byte)( value >> 21 | 0x80 ) );
			write( (byte)( ( value >> 28 ) & 0x0F ) );
		}
	}
}
