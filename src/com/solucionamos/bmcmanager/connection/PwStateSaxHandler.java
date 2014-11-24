package com.solucionamos.bmcmanager.connection;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PwStateSaxHandler extends DefaultHandler {
	public int pwState = 666;
	String content = null;

	public int getPwState() {
		return pwState;
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		System.out.println("localName: " + localName);
		if (localName.equals("pwState")) {
			pwState = Integer.valueOf(content);
		}
	}

	public void characters(char[] ch, int start, int length) {
		content = String.copyValueOf(ch, start, length);
	}
}
