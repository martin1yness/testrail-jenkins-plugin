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
import java.nio.file.Files;
import java.security.MessageDigest;
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
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(resultFile);
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] buff = new byte[1024];
			int c = 0, totalSize = 0;
			while((c = fis.read(buff)) != -1) {
				md.digest(buff, 0, c);
				totalSize += c;
			}
			String md5 = toHex(md.digest());
			logger.println("[TestNG Parser] Found potential TestNG results xml: " + resultFile.toURI() + ", (" + totalSize + "B)" +", md5sum: " + md5);
			fis.close();
		} catch(Exception e) {
			try { fis.close(); } catch(Exception e2) {}
			throw new RuntimeException(e);
		}
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
			logger.println("[TestNG Parser] Suites found: " + testNGSuites.size());
			for (TestNGSuite testNGSuite : testNGSuites) {
				logger.println("[TestNG Parser] Suite `"+testNGSuite.getName()+"`, cases found: " + testNGSuite.getCases().size());
				for (TestNGCase testNGCase : testNGSuite.getCases()) {
					logger.println("[TestNG Parser] Case `"+testNGCase.getName()+"`, looking up in TestRail...");
					for (Case testRailCase : existingTestCases.getCasesInSuite(testNGSuite.getName())) {
						if (testNGCase.getName().equalsIgnoreCase(testRailCase.getTitle())) {
							Result.STATUS status = Result.STATUS.PASSED;
							String comment = "Jenkins Build";
							if (!testNGCase.isSuccessful()) {
								status = Result.STATUS.FAILED;
								comment = testNGCase.getResultDescription();
							}
							resultContainer.addResult(new Result(testRailCase, status, comment));
							logger.println("[TestNG Parser] Case `"+testNGCase.getName()+"` FOUND!, status: " + status.name());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(logger);
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String toHex(byte[] bytes) {
		char[] hex = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hex[j * 2] = hexArray[v >>> 4];
			hex[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hex);
	}
}
