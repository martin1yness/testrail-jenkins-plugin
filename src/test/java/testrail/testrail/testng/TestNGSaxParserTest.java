package testrail.testrail.testng;

import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.*;

/**
 * Description
 */
@Test(groups = {"TESTRAIL-123"})
public class TestNGSaxParserTest {

	@Test
	public void testParseSampleTestNGResults() throws Exception {
		TestNGSaxParser parser = new TestNGSaxParser(System.out);

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser saxParser = spf.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(parser);
		InputStream istream = getClass().getResourceAsStream("testng-results.xml");
		InputSource isource = new InputSource(istream);
		xmlReader.parse(isource);

		List<TestNGSuite> suites = parser.getSuites();
		assertThat(suites.size(), is(2));
		assertThat(suites.get(0).getName(), is("Jenkins TestRail Plugin"));
		assertThat(suites.get(1).getName(), is("Widget"));
		assertThat(suites.get(0).getDuration(), is(81));
		assertThat(suites.get(1).getDuration(), is(9));
		assertThat(suites.get(0).getCases().size(), is(1));
		assertThat(suites.get(1).getCases().size(), is(2));
		assertThat(suites.get(0).getCases().get(0).getName(), is("TestNG Result Model Creation"));
		assertThat(suites.get(0).getCases().get(0).getDuration(), is(81));
		assertThat(suites.get(0).getCases().get(0).getResultDescription(), containsString("IllegalArgumentException: Malformed date"));
		assertThat(suites.get(0).getCases().get(0).isSuccessful(), is(false));
		assertThat(suites.get(1).getCases().get(0).getName(), is("Widget Works"));
		assertThat(suites.get(1).getCases().get(0).getDuration(), is(4));
		assertThat(suites.get(1).getCases().get(0).isSuccessful(), is(true));
		assertThat(suites.get(1).getCases().get(1).getName(), is("Widget Does X"));
		assertThat(suites.get(1).getCases().get(1).isSuccessful(), is(false));
		assertThat(suites.get(1).getCases().get(1).getResultDescription(), containsString("java.lang.AssertionError: expected [true] but found [false]"));
	}
}