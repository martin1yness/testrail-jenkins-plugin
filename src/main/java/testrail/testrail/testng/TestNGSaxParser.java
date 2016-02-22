package testrail.testrail.testng;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to parse the `testng-results.xml` to produce {@link TestNGSuite} and {@link TestNGCase} objects.
 * @author Martin Dale Lyness
 * @since 2016-02-18
 */
public class TestNGSaxParser extends DefaultHandler {
	private PrintStream logger;

	enum KNOWN_ELEMENTS {
		SUITE("suite"), TEST("test"), TEST_METHOD("test-method"),
		REPORTER_OUT("reporter-output"), EXCEPTION("exception"), STACKTRACE("full-stacktrace"),
		UNKNOWN("");
		private String xmlElemName;

		KNOWN_ELEMENTS(String xmlElemName) {
			this.xmlElemName = xmlElemName;
		}

		public String getXmlElemName() {
			return xmlElemName;
		}
		public static KNOWN_ELEMENTS byXmlElemName(String xmlElemName) {
			for(KNOWN_ELEMENTS e : KNOWN_ELEMENTS.values()) {
				if(e.getXmlElemName().equalsIgnoreCase(xmlElemName))
					return e;
			}
			return UNKNOWN;
		}
	}

	interface ATTRS {
		String NAME = "name";
		String duration = "duration-ms";
		String started = "started-at";
		String finished = "finished-at";
		String status = "status";
	}

	private List<TestNGSuite> suites;
	/**
	 * The current parent suite in the XML tree during processing. Used to map a newly found test case to
	 * its parent test suite.
	 */
	private TestNGSuite currentSuite;
	/**
	 * Tracks the current test case to mapping test-method statuses to the current test case
	 */
	private TestNGCase currentTestCase;
	/**
	 * The current element being processed
	 */
	private KNOWN_ELEMENTS currentElement;

	public TestNGSaxParser(PrintStream logger) {
		this.logger = logger;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		suites = new ArrayList<TestNGSuite>();
		currentSuite = null;
		currentTestCase = null;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		currentSuite = null;
		currentTestCase = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		currentElement = KNOWN_ELEMENTS.byXmlElemName(localName);
		switch(currentElement) {
			case SUITE:
				this.currentSuite = new TestNGSuite()
						.setName(attributes.getValue(ATTRS.NAME))
						.setDuration(attributes.getValue(ATTRS.duration))
						.setStarted(attributes.getValue(ATTRS.started))
						.setFinished(attributes.getValue(ATTRS.finished))
						.setCases(new ArrayList<TestNGCase>());
				break;
			case TEST:
				this.currentTestCase = new TestNGCase()
						.setName(attributes.getValue(ATTRS.NAME))
						.setDuration(attributes.getValue(ATTRS.duration))
						.setStarted(attributes.getValue(ATTRS.started))
						.setFinished(attributes.getValue(ATTRS.finished))
						.setSuccessful(true);
				break;
			case TEST_METHOD:
				if(attributes.getValue(ATTRS.status).equalsIgnoreCase("FAIL"))
					this.currentTestCase.setSuccessful(false);
				break;
			default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		switch(KNOWN_ELEMENTS.byXmlElemName(localName)) {
			case SUITE:
				suites.add(this.currentSuite);
				this.currentSuite = null;
				break;
			case TEST:
				this.currentSuite.getCases().add(this.currentTestCase);
				this.currentTestCase = null;
				break;
			case TEST_METHOD:
				break;
			default:
		}
		currentElement = null;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);

		if(currentElement != null) {
			switch (currentElement) {
				case EXCEPTION:
				case STACKTRACE:
				case REPORTER_OUT:
					if (this.currentTestCase != null) {
						this.currentTestCase.setResultDescription(""
								+ this.currentTestCase.getResultDescription()
								+ new String(ch)
								+ "\r\n\r\n"
						);
					}
					break;
				default:
			}
		}
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
		logger.println("[WARN]\t"+e.getMessage());
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		super.error(e);
		e.printStackTrace(logger);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		super.fatalError(e);
		e.printStackTrace(logger);
	}

	public List<TestNGSuite> getSuites() {
		return suites;
	}
}
