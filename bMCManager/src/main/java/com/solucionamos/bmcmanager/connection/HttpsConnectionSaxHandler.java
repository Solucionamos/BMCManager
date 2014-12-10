package com.solucionamos.bmcmanager.connection;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HttpsConnectionSaxHandler extends DefaultHandler {
	public static final int AUTH_OK = 0;
	
	private int authResult = -1;
	private String content = null;

	public int getAuthResult() {
		return authResult;
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("authResult")) {
			authResult = Integer.valueOf(content);
		}
	}

	public void characters(char[] ch, int start, int length) {
		content = String.copyValueOf(ch, start, length);
	}
}
