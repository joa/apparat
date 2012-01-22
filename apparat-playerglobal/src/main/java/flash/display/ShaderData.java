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

import apparat.pbj.Pbj;
import flash.utils.ByteArray;
import jitb.errors.ErrorUtil;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;
import jitb.util.ShaderUtil;

import java.util.HashMap;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public final class ShaderData extends jitb.lang.Object {
	public static ShaderData JITB$fromParameters(final ShaderParameter[] parameters, final ShaderInput[] inputs) {
		return new ShaderData(parameters, inputs);
	}

	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();

	private final ShaderParameter[] _parameters;
	private final ShaderInput[] _inputs;

	public ShaderData(ByteArray byteCode) {
		this(ShaderUtil.getPbj(byteCode));
	}

	ShaderData(Pbj pbj) {
		_parameters = ShaderUtil.getShaderParameters(pbj);
		_inputs = ShaderUtil.getShaderTextures(pbj);
	}

	private ShaderData(final ShaderParameter[] parameters, final ShaderInput[] inputs) {
		_parameters = parameters;
		_inputs = inputs;
	}

	@Override
	public Object JITB$getProperty(final String property) {
		for(final ShaderParameter parameter : _parameters) {
			if(null != parameter && parameter.name().equals(property)) {
				return parameter;
			}
		}

		for(final ShaderInput input : _inputs) {
			if(null != input && input.name().equals(property)) {
				return input;
			}
		}

		System.out.println("Warning: ShaderData property \""+property+"\" is not known.");
		return _dynamic.get(property);
	}

	@Override
	public void JITB$setProperty(String property, Object value) {
		int n;

		n= _parameters.length;
		while(--n > -1) {
			if(null != _parameters[n] && _parameters[n].name().equals(property)) {
				if(value instanceof ShaderParameter) {
					_parameters[n] = (ShaderParameter)value;
				} else {
					ErrorUtil.flashThrow(ErrorUtil.error1034(value, ShaderParameter.class));
				}

				return;
			}
		}

		n = _inputs.length;
		while(--n > -1) {
			if(null != _inputs[n] && _inputs[n].name().equals(property)) {
				if(value instanceof ShaderInput) {
					_inputs[n] = (ShaderInput)value;
				} else {
					ErrorUtil.flashThrow(ErrorUtil.error1034(value, ShaderInput.class));
				}

				return;
			}
		}

		System.out.println("Warning: ShaderData property \""+property+"\" is not known.");
		_dynamic.put(property, value);
	}

	public void JITB$applyParameters(final int programId) {
		for(final ShaderParameter parameter : _parameters) {
			if(null != parameter) {
				parameter.JITB$applyParameter(programId);
			}
		}

		for(final ShaderInput input : _inputs) {
			if(null != input) {
				input.JITB$applyInput(programId);
			}
		}
	}

	public void JITB$unapplyParameters() {
		for(final ShaderInput input : _inputs) {
			if(null != input) {
				input.JITB$unapplyInput();
			}
		}
	}

	public ShaderInput[] JITB$inputs() {
		return _inputs;
	}

	public ShaderParameter[] JITB$parameters() {
		return _parameters;
	}
}
