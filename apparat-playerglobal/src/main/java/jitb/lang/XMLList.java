package jitb.lang;

import jitb.util.XMLUtil;
import org.w3c.dom.NodeList;

import java.lang.*;

/**
 * @author Joa Ebert
 */
public final class XMLList extends jitb.lang.Object {
	private final NodeList _nodeList;

	protected XMLList(final NodeList value) {
		_nodeList = value;
	}

	@Override
	public java.lang.String toString() {
		return toXMLString();
	}

	public java.lang.String toXMLString() {
		final StringBuffer buffer = new StringBuffer();

		final int n = _nodeList.getLength();
		final int m = n - 1;

		for(int i = 0; i < n; ++i) {
			buffer.append(XMLUtil.toXMLString(_nodeList.item(i)));

			if(i != m) {
				buffer.append('\n');
			}
		}

		return buffer.toString();
	}
}
