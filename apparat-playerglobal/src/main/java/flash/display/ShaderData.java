package flash.display;

import apparat.pbj.Pbj;
import flash.utils.ByteArray;
import jitb.errors.ErrorUtil;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

import java.util.HashMap;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public final class ShaderData extends jitb.lang.Object {
	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();

	private final Pbj _pbj;
	private final ShaderParameter[] _parameters;

	public ShaderData(ByteArray byteCode) {
		this(ShaderUtil.getPbj(byteCode));
	}

	ShaderData(Pbj pbj) {
		_pbj = pbj;
		_parameters = ShaderUtil.getShaderParameters(_pbj);
	}

	@Override
	public Object JITB$getProperty(final String property) {
		for(final ShaderParameter parameter : _parameters) {
			if(null != parameter && parameter.name().equals(property)) {
				return parameter;
			}
		}

		System.out.println("Warning: ShaderData property \""+property+"\" is not a parameter.");
		return _dynamic.get(property);
	}

	@Override
	public void JITB$setProperty(String property, Object value) {
		int n = _parameters.length;
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

		System.out.println("Warning: ShaderData property \""+property+"\" is not a parameter.");
		_dynamic.put(property, value);
	}

	public void JITB$applyParameters(final int programId) {
		for(final ShaderParameter parameter : _parameters) {
			if(null != parameter) {
				parameter.JITB$applyParameter(programId);
			}
		}
	}
}
