package testrail.testrail.testng;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import testrail.testrail.ResultFileProcessor;
import testrail.testrail.TestRailObjects.Case;
import testrail.testrail.TestRailObjects.ExistingTestCases;
import testrail.testrail.TestRailObjects.Result;
import testrail.testrail.TestRailObjects.Results;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Description
 */
public class TestNGResultFileProcessor implements ResultFileProcessor {
	private final PrintStream logger;
	private final ExistingTestCases existingTestCases;

	public TestNGResultFileProcessor(PrintStream logger, ExistingTestCases existingTestCases) {
		this.logger = logger;
		this.existingTestCases = existingTestCases;
	}

	public void handleResult(File resultFile, Results resultContainer) {
		logger.println("[TestNG Parser] Found potential TestNG results xml: " + resultFile.toURI());
		try {
			TestNGSaxParser parser = new TestNGSaxParser(System.out);

			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = null;
			saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(parser);
			InputSource isource = new InputSource(new FileInputStream(resultFile));
			xmlReader.parse(isource);

			List<TestNGSuite> testNGSuites = parser.getSuites();
			for (TestNGSuite testNGSuite : testNGSuites) {
				for (TestNGCase testNGCase : testNGSuite.getCases()) {
					for (Case testRailCase : existingTestCases.getCasesInSuite(testNGSuite.getName())) {
						if (testNGCase.getName().equalsIgnoreCase(testRailCase.getTitle())) {
							Result.STATUS status = Result.STATUS.PASSED;
							String comment = "";
							if (!testNGCase.isSuccessful()) {
								status = Result.STATUS.FAILED;
								comment = testNGCase.getResultDescription();
							}
							resultContainer.addResult(new Result(testRailCase, status, comment));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(logger);
		}
	}
}
