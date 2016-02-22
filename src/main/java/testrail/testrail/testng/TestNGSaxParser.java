package testrail.testrail.testng;

import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to parse the `testng-results.xml` to produce {@link TestNGSuite} and {@link TestNGCase} objects.
 * @author Martin Dale Lyness
 * @since 2016-02-18
 */
public class TestNGSaxParser extends DefaultHandler {
	private PrintStream logger;

	enum KNOWN_ELEMENTS { SUITE, TEST }
	interface ATTRS {
		String NAME = "name";
		String duration = "duration-ms";
		String started = "started-at";
		String finished = "finished-at";
	}

	private Map<String, TestNGSuite> suiteMap;
	/**
	 * The current parent suite in the XML tree during processing. Used to map a newly found test case to
	 * its parent test suite in the {@link #suiteMap}
	 */
	private String currentSuiteName;

	public TestNGSaxParser(PrintStream logger) {
		this.logger = logger;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		suiteMap = new HashMap<String, TestNGSuite>();
		currentSuiteName = null;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		switch(KNOWN_ELEMENTS.valueOf(localName.toUpperCase())) {
			case SUITE:
				this.currentSuiteName = attributes.getValue(ATTRS.NAME);
				TestNGSuite suite = new TestNGSuite()
						.setName(this.currentSuiteName)
						.setDuration(attributes.getValue(ATTRS.duration))
						.setStarted(attributes.getValue(ATTRS.started))
						.setFinished(attributes.getValue(ATTRS.finished));
				suiteMap.put(this.currentSuiteName, suite);
				break;
			case TEST:
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		super.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		super.fatalError(e);
	}
}
