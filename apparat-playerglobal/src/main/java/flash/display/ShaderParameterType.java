/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package flash.display;

import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public class ShaderParameterType {
	public static final String INT2 = "int2";
	public static final String INT3 = "int3";
	public static final String INT4 = "int4";
	public static final String BOOL2 = "bool2";
	public static final String BOOL3 = "bool3";
	public static final String BOOL4 = "bool4";
	public static final String INT = "int";
	public static final String BOOL = "bool";
	public static final String MATRIX2X2 = "matrix2x2";
	public static final String MATRIX3X3 = "matrix3x3";
	public static final String MATRIX4X4 = "matrix4x4";
	public static final String FLOAT2 = "float2";
	public static final String FLOAT3 = "float3";
	public static final String FLOAT = "float";
	public static final String FLOAT4 = "float4";
}
