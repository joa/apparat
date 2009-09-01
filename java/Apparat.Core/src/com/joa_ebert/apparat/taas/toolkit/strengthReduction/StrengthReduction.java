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

package com.joa_ebert.apparat.taas.toolkit.strengthReduction;

import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.Taas;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.constants.TaasInt;
import com.joa_ebert.apparat.taas.constants.TaasNumber;
import com.joa_ebert.apparat.taas.constants.TaasUInt;
import com.joa_ebert.apparat.taas.expr.AbstractBinaryExpr;
import com.joa_ebert.apparat.taas.expr.TAdd;
import com.joa_ebert.apparat.taas.expr.TDivide;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.expr.TMultiply;
import com.joa_ebert.apparat.taas.expr.TSubtract;
import com.joa_ebert.apparat.taas.expr.TIf.Operator;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.types.IntType;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.UIntType;

/**
 * @author Joa Ebert
 * 
 */
public class StrengthReduction implements ITaasTool
{
	private static final Taas TAAS = new Taas();

	private int getMSB( final int value )
	{
		int result;

		result = ( value & 0xaaaaaaaa ) != 0 ? 1 : 0;
		result |= ( ( value & 0xffff0000 ) != 0 ? 1 : 0 ) << 4;
		result |= ( ( value & 0xff00ff00 ) != 0 ? 1 : 0 ) << 3;
		result |= ( ( value & 0xf0f0f0f0 ) != 0 ? 1 : 0 ) << 2;
		result |= ( ( value & 0xcccccccc ) != 0 ? 1 : 0 ) << 1;

		return result;
	}

	private boolean isPow2( final int value )
	{
		if( value < 2 )
		{
			return false;
		}
		else if( ( value & ( value - 1 ) ) != 0 )
		{
			return false;
		}
		return true;
	}

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = false;

		final List<TaasVertex> vertices = method.code.vertexList();

		for( final TaasVertex vertex : vertices )
		{
			if( VertexKind.Default != vertex.kind )
			{
				continue;
			}

			final TaasValue value = vertex.value;

			if( value instanceof TIf )
			{
				final TIf ifExpr = (TIf)value;

				changed = reduce( ifExpr ) || changed;
			}
			else
			{
				final AbstractBinaryExpr binExpr = TaasToolkit.search( value,
						AbstractBinaryExpr.class );

				if( null != binExpr )
				{
					changed = reduce( value, binExpr ) || changed;
				}
			}
		}
		return changed;
	}

	private boolean reduce( final TaasValue value,
			final AbstractBinaryExpr binExpr )
	{
		boolean change0 = false;

		if( binExpr instanceof TMultiply )
		{
			change0 = reduce( value, (TMultiply)binExpr );
		}
		else if( binExpr instanceof TDivide )
		{
			change0 = reduce( value, (TDivide)binExpr );
		}

		final boolean change1 = binExpr.lhs instanceof AbstractBinaryExpr ? reduce(
				binExpr, (AbstractBinaryExpr)binExpr.lhs )
				: false;

		final boolean change2 = binExpr.rhs instanceof AbstractBinaryExpr ? reduce(
				binExpr, (AbstractBinaryExpr)binExpr.rhs )
				: false;

		return change0 || change1 || change2;
	}

	private boolean reduce( final TaasValue value, final TDivide divExpr )
	{
		if( divExpr.getType() == IntType.INSTANCE )
		{
			if( divExpr.lhs.isConstant() )
			{
				final TaasValue lhs = divExpr.lhs;

				if( lhs instanceof TaasInt )
				{
					final TaasInt lhsInt = (TaasInt)lhs;

					if( 0 == lhsInt.value )
					{
						//
						// 0 / x -> 0
						//

						TaasToolkit.replace( value, divExpr, new TaasInt( 0 ) );
						return true;
					}
				}
			}
			else if( divExpr.rhs.isConstant() )
			{
				final TaasValue rhs = divExpr.rhs;

				if( rhs instanceof TaasInt )
				{
					final TaasInt rhsInt = (TaasInt)rhs;

					if( 0 == rhsInt.value )
					{
						//
						// x / 0 -> 0
						//

						TaasToolkit.replace( value, divExpr, new TaasInt( 0 ) );
						return true;
					}
					else if( isPow2( rhsInt.value ) )
					{
						//
						// x / 2^y -> x >> y
						//

						TaasToolkit.replace( value, divExpr, TAAS.shiftRight(
								divExpr.lhs,
								new TaasInt( getMSB( rhsInt.value ) ) ) );

						return true;
					}
				}
			}
		}
		else if( divExpr.getType() == UIntType.INSTANCE )
		{
			if( divExpr.lhs.isConstant() )
			{
				final TaasValue lhs = divExpr.lhs;

				if( lhs instanceof TaasUInt )
				{
					final TaasUInt lhsUInt = (TaasUInt)lhs;

					if( 0L == lhsUInt.value )
					{
						//
						// 0 / x -> 0
						//

						TaasToolkit
								.replace( value, divExpr, new TaasUInt( 0L ) );
						return true;
					}
				}
			}
			else if( divExpr.rhs.isConstant() )
			{
				final TaasValue rhs = divExpr.rhs;

				if( rhs instanceof TaasUInt )
				{
					final TaasUInt rhsUInt = (TaasUInt)rhs;

					if( 0L == rhsUInt.value )
					{
						//
						// x / 0 -> 0
						//

						TaasToolkit
								.replace( value, divExpr, new TaasUInt( 0L ) );
						return true;
					}
				}
			}
		}
		else if( divExpr.getType() == NumberType.INSTANCE )
		{
			if( divExpr.lhs.isConstant() )
			{
				final TaasValue lhs = divExpr.lhs;

				if( lhs instanceof TaasNumber )
				{
					final TaasNumber lhsDouble = (TaasNumber)lhs;

					if( 0.0 == lhsDouble.value )
					{
						//
						// 0.0 / x -> 0.0
						//

						TaasToolkit.replace( value, divExpr, new TaasNumber(
								0.0 ) );
						return true;
					}
					else if( Double.isNaN( lhsDouble.value ) )
					{
						//
						// NaN / x -> NaN
						//

						TaasToolkit.replace( value, divExpr, new TaasNumber(
								Double.NaN ) );
						return true;
					}
				}
			}
			else if( divExpr.rhs.isConstant() )
			{
				final TaasValue rhs = divExpr.rhs;

				if( rhs instanceof TaasNumber )
				{
					final TaasNumber rhsDouble = (TaasNumber)rhs;

					//
					// x / 0.0 -> NaN
					// x / NaN -> NaN
					//

					if( 0.0 == rhsDouble.value
							|| Double.isNaN( rhsDouble.value ) )
					{
						TaasToolkit.replace( value, divExpr, new TaasNumber(
								Double.NaN ) );
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean reduce( final TaasValue value, final TMultiply mulExpr )
	{
		if( mulExpr.getType() == IntType.INSTANCE )
		{
			if( mulExpr.lhs.isConstant() )
			{
				final TaasValue lhs = mulExpr.lhs;

				if( lhs instanceof TaasInt )
				{
					final TaasInt lhsInt = (TaasInt)lhs;

					if( 0 == lhsInt.value )
					{
						//
						// 0 * x -> 0
						//

						TaasToolkit.replace( value, mulExpr, new TaasInt( 0 ) );
					}
					else if( isPow2( lhsInt.value ) )
					{
						//
						// 2^y * x -> x << y
						//

						TaasToolkit.replace( value, mulExpr, TAAS.shiftLeft(
								mulExpr.rhs,
								new TaasInt( getMSB( lhsInt.value ) ) ) );

						return true;
					}
				}
			}
			else if( mulExpr.rhs.isConstant() )
			{
				final TaasValue rhs = mulExpr.rhs;

				if( rhs instanceof TaasInt )
				{
					final TaasInt rhsInt = (TaasInt)rhs;

					if( 0 == rhsInt.value )
					{
						//
						// x * 0 -> 0
						//

						TaasToolkit.replace( value, mulExpr, new TaasInt( 0 ) );
					}
					else if( isPow2( rhsInt.value ) )
					{
						//
						// x * 2^y -> x << y
						//

						TaasToolkit.replace( value, mulExpr, TAAS.shiftLeft(
								mulExpr.lhs,
								new TaasInt( getMSB( rhsInt.value ) ) ) );

						return true;
					}
				}
			}
		}
		else if( mulExpr.getType() == UIntType.INSTANCE )
		{
			if( mulExpr.lhs.isConstant() )
			{
				final TaasValue lhs = mulExpr.lhs;

				if( lhs instanceof TaasUInt )
				{
					final TaasUInt lhsUInt = (TaasUInt)lhs;

					if( 0L == lhsUInt.value )
					{
						//
						// 0 * x -> 0
						//

						TaasToolkit
								.replace( value, mulExpr, new TaasUInt( 0L ) );
					}
					else if( 2L == lhsUInt.value
							&& mulExpr.rhs instanceof TaasLocal )
					{
						//
						// 2 * x -> x + x
						//

						TaasToolkit.replace( value, mulExpr, TAAS.add(
								mulExpr.rhs, mulExpr.rhs.dup() ) );

						return true;
					}
				}
			}
			else if( mulExpr.rhs.isConstant() )
			{
				final TaasValue rhs = mulExpr.rhs;

				if( rhs instanceof TaasUInt )
				{
					final TaasUInt rhsUInt = (TaasUInt)rhs;

					if( 0L == rhsUInt.value )
					{
						//
						// x * 0 -> 0
						//

						TaasToolkit
								.replace( value, mulExpr, new TaasUInt( 0L ) );
					}
					else if( 2L == rhsUInt.value
							&& mulExpr.lhs instanceof TaasLocal )
					{
						//
						// x * 2 -> x + x
						//

						TaasToolkit.replace( value, mulExpr, TAAS.add(
								mulExpr.lhs, mulExpr.lhs.dup() ) );

						return true;
					}
				}
			}
		}
		else if( mulExpr.getType() == NumberType.INSTANCE )
		{
			if( mulExpr.lhs.isConstant() )
			{
				final TaasValue lhs = mulExpr.lhs;

				if( lhs instanceof TaasNumber )
				{
					final TaasNumber lhsDouble = (TaasNumber)lhs;

					if( 0.0 == lhsDouble.value )
					{
						//
						// 0.0 * x -> 0.0
						//

						TaasToolkit.replace( value, mulExpr, new TaasNumber(
								0.0 ) );
					}
					else if( Double.isNaN( lhsDouble.value ) )
					{
						//
						// NaN * x -> NaN
						//

						TaasToolkit.replace( value, mulExpr, new TaasNumber(
								Double.NaN ) );
					}
					else if( 2 == lhsDouble.value
							&& mulExpr.rhs instanceof TaasLocal )
					{
						//
						// 2.0 * x -> x + x
						//

						TaasToolkit.replace( value, mulExpr, TAAS.add(
								mulExpr.rhs, mulExpr.rhs.dup() ) );

						return true;
					}
				}
			}
			else if( mulExpr.rhs.isConstant() )
			{
				final TaasValue rhs = mulExpr.rhs;

				if( rhs instanceof TaasNumber )
				{
					final TaasNumber rhsDouble = (TaasNumber)rhs;

					if( 0.0 == rhsDouble.value )
					{
						//
						// x * 0.0 -> 0.0
						//

						TaasToolkit.replace( value, mulExpr, new TaasNumber(
								0.0 ) );
					}
					else if( Double.isNaN( rhsDouble.value ) )
					{
						//
						// x * NaN -> NaN
						//

						TaasToolkit.replace( value, mulExpr, new TaasNumber(
								Double.NaN ) );
					}
					else if( 2 == rhsDouble.value
							&& mulExpr.lhs instanceof TaasLocal )
					{
						//
						// x * 2.0 -> x + x
						//

						TaasToolkit.replace( value, mulExpr, TAAS.add(
								mulExpr.lhs, mulExpr.lhs.dup() ) );

						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean reduce( final TIf ifExpr )
	{
		if( ifExpr.operator == Operator.Equal )
		{
			if( ifExpr.lhs.getType() != ifExpr.rhs.getType()
					|| !ifExpr.rhs.isConstant() )
			{
				return false;
			}

			final TaasType type = ifExpr.lhs.getType();

			if( type == IntType.INSTANCE && ifExpr.rhs instanceof TaasInt )
			{
				final TaasInt rhs = (TaasInt)ifExpr.rhs;

				if( rhs.value == 0 )
				{
					if( ifExpr.lhs instanceof TAdd )
					{
						final TAdd addExpr = (TAdd)ifExpr.lhs;

						if( addExpr.lhs.isConstant() )
						{
							if( addExpr.lhs instanceof TaasInt )
							{
								//
								// x + y == 0 -> y == -x
								//

								final TaasInt intValue = (TaasInt)addExpr.lhs;

								ifExpr.lhs = addExpr.rhs;
								ifExpr.rhs = new TaasInt( -intValue.value );

								return true;
							}
						}
						else if( addExpr.rhs.isConstant() )
						{
							if( addExpr.rhs instanceof TaasInt )
							{
								//
								// x + y == 0 -> x == -y
								//

								final TaasInt intValue = (TaasInt)addExpr.rhs;

								ifExpr.lhs = addExpr.lhs;
								ifExpr.rhs = new TaasInt( -intValue.value );

								return true;
							}
						}
					}
					else if( ifExpr.lhs instanceof TSubtract )
					{
						//
						// x - y == 0 -> x == y
						//

						final TSubtract subExpr = (TSubtract)ifExpr.lhs;

						ifExpr.lhs = subExpr.lhs;
						ifExpr.rhs = subExpr.rhs;

						return true;
					}
				}
			}
			else if( type == NumberType.INSTANCE
					&& ifExpr.rhs instanceof TaasNumber )
			{
				final TaasNumber rhs = (TaasNumber)ifExpr.rhs;

				if( rhs.value == 0.0 )
				{
					if( ifExpr.lhs instanceof TAdd )
					{
						final TAdd addExpr = (TAdd)ifExpr.lhs;

						if( addExpr.lhs.isConstant() )
						{
							if( addExpr.lhs instanceof TaasNumber )
							{
								//
								// x + y == 0 -> y == -x
								//

								final TaasNumber numberValue = (TaasNumber)addExpr.lhs;

								ifExpr.lhs = addExpr.rhs;
								ifExpr.rhs = new TaasNumber( -numberValue.value );

								return true;
							}
						}
						else if( addExpr.rhs.isConstant() )
						{
							if( addExpr.rhs instanceof TaasNumber )
							{
								//
								// x + y == 0 -> x == -y
								//

								final TaasNumber numberValue = (TaasNumber)addExpr.rhs;

								ifExpr.lhs = addExpr.lhs;
								ifExpr.rhs = new TaasNumber( -numberValue.value );

								return true;
							}
						}
					}
					else if( ifExpr.lhs instanceof TSubtract )
					{
						//
						// x - y == 0 -> x == y
						//

						final TSubtract subExpr = (TSubtract)ifExpr.lhs;

						ifExpr.lhs = subExpr.lhs;
						ifExpr.rhs = subExpr.rhs;

						return true;
					}
				}
			}
		}

		return false;
	}
}
