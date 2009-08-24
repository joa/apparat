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

package com.joa_ebert.apparat.tests.taas;

import org.junit.Assert;
import org.junit.Test;

import com.joa_ebert.apparat.taas.constants.TaasInt;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasIntTests
{
	@Test
	public void testAdd()
	{
		final TaasInt a = new TaasInt( 1 );
		final TaasInt b = new TaasInt( 2 );

		final TaasInt result = (TaasInt)a.add( b );

		Assert.assertEquals( 3, result.value );
	}

	@Test
	public void testDiv()
	{
		final TaasInt a = new TaasInt( 4 );
		final TaasInt b = new TaasInt( 2 );

		final TaasInt result = (TaasInt)a.divide( b );

		Assert.assertEquals( 2, result.value );
	}

	@Test
	public void testMul()
	{
		final TaasInt a = new TaasInt( 1 );
		final TaasInt b = new TaasInt( 2 );

		final TaasInt result = (TaasInt)a.multiply( b );

		Assert.assertEquals( 2, result.value );
	}

	@Test
	public void testSub()
	{
		final TaasInt a = new TaasInt( 1 );
		final TaasInt b = new TaasInt( 2 );

		final TaasInt result = (TaasInt)a.subtract( b );

		Assert.assertEquals( -1, result.value );
	}
}
