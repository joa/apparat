package jitb.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * @author Joa Ebert
 */
public final class XMLUtil {
	public static String toXMLString(final Node value) {
		return toXMLString(new DOMSource(value));
	}

	public static String toXMLString(final Document value) {
		return toXMLString(new DOMSource(value));
	}

	public static String toXMLString(final DOMSource value) {
		final StringWriter writer = new StringWriter();
		try {
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD,"xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(value, new StreamResult(writer));
		} catch(final Throwable t) {
			//todo throw flash error
			throw new RuntimeException(t);
		}

		final String result = writer.toString();

		return result.substring(0, result.length() - 1);
	}

	private XMLUtil() {}
}
