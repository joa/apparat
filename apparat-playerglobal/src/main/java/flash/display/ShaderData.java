package flash.display;

import apparat.pbj.Pbj;
import apparat.pbj.pbjdata;
import flash.utils.ByteArray;
import jitb.lang.Array;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import scala.Tuple2;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author Joa Ebert
 */
public final class ShaderData extends jitb.lang.Object {
	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();

	private Pbj _pbj;

	public ShaderData(ByteArray byteCode) {
		this(Pbj.fromByteArray(byteCode.JITB$toByteArray()));
	}

	ShaderData(Pbj pbj) {
		_pbj = pbj;
	}

	@Override
	public Object JITB$getProperty(final String property) {
		return _dynamic.get(property);
	}

	@Override
	public void JITB$setProperty(String property, Object value) {
		_dynamic.put(property, value);
	}

	public void JITB$applyParameters(final int programId) {
		final Tuple2<pbjdata.PParam, scala.collection.immutable.List<pbjdata.PMeta>> list[] = _pbj.parametersAsArray();

		for(Tuple2<pbjdata.PParam, scala.collection.immutable.List<pbjdata.PMeta>> tuple : list) {
			final pbjdata.PParam param = (pbjdata.PParam)tuple._1();
			final scala.collection.immutable.List<pbjdata.PMeta> meta =
					(scala.collection.immutable.List<pbjdata.PMeta>)tuple._2();

			//TODO read default, min, max and the other crap from meta

			//
			// Please ignore IDE errors due to Scala bytecode:
			//
			
			final String name = param.name();
			final Object property = JITB$getProperty(name);

			if(property instanceof Array) {
				final Array array = (Array)property;
				final ByteBuffer buffer = BufferUtils.createByteBuffer(name.length()+1);

				buffer.put(name.getBytes()).put((byte)0).flip();

				final int location = ARBShaderObjects.glGetUniformLocationARB(programId, buffer);
				final pbjdata.PType type = param.type();

				if(type == apparat.pbj.pbjdata$PFloatType$.MODULE$) {
					final Object value = array.JITB$getIndex(0);

					float floatValue = Float.NaN;

					if(value instanceof Integer) {
						floatValue = ((Integer)value).floatValue();
					} else if(value instanceof Double) {
						floatValue = ((Double)value).floatValue();
					} else if(value instanceof Long) {
						floatValue = ((Long)value).floatValue();
					} else {
						//use default value?
					}

					System.out.println("Setting parameter \""+name+"\" to "+floatValue+" at location "+location+".");
					ARBShaderObjects.glUniform1fARB(location, floatValue);
				}
			}
		}
	}
}
