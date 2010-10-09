package jitb.lang;

import jitb.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.*;
import java.lang.Object;
import java.lang.String;

/**
 * @author Joa Ebert
 */
public final class XML extends jitb.lang.Object {
	public static XML JITB$fromDocument(final Document value) {
		return new XML(value);
	}

	private final Document _document;

	public XML(final jitb.lang.Object value) {
		if(value instanceof jitb.lang.String) {
			try {
				_document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(((jitb.lang.String)value).toString())));
			} catch(final Throwable t) {
				//todo throw flash error
				throw new RuntimeException(t);
			}
		} else {
			//todo other types as in as3 langref
			throw new RuntimeException("unsupported "+value);
		}
	}

	protected XML(final Document value) {
		_document = value;
	}

	@Override
	public java.lang.String toString() {
		return toXMLString();
	}

	public java.lang.String toXMLString() {
		return XMLUtil.toXMLString(_document);
	}

	@Override
	public Object JITB$getProperty(final String property) {
		return new XMLList(_document.getElementsByTagName(property));
	}
}
