package testrail.testrail.JunitResults;

import hudson.util.FileVisitor;
import testrail.testrail.ResultFileProcessor;
import testrail.testrail.TestRailObjects.Case;
import testrail.testrail.TestRailObjects.ExistingTestCases;
import testrail.testrail.TestRailObjects.Result;
import testrail.testrail.TestRailObjects.Results;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Description
 */
public class JUnitResultFileProcessor implements ResultFileProcessor {
	private final PrintStream logger;
	private final ExistingTestCases testCases;
	private final JAXBContext jaxbContext;
	private final Unmarshaller jaxbUnmarshaller;

	public JUnitResultFileProcessor(PrintStream logger, ExistingTestCases testCases) {
		this.logger = logger;
		this.testCases = testCases;
		try {
			this.jaxbContext = JAXBContext.newInstance(Testsuites.class);
			this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public void handleResult(File resultFile, Results resultContainer) {
		ArrayList<Testsuite> Suites = new ArrayList<Testsuite>();
		logger.println("processing " + resultFile.toURI());
		Testsuites suites = null;
		logger.println("Processing potential JUnit result xml: " + resultFile.getName());
		try {
			Object obj = jaxbUnmarshaller.unmarshal(resultFile);
			if(Testsuites.class.isAssignableFrom(obj.getClass())) {
				suites = (Testsuites) obj;
				if (suites.hasSuites()) {
					for (Testsuite suite : suites.getSuites()) {
						Suites.add(suite);
					}
				}
			} else if(Testsuite.class.isAssignableFrom(obj.getClass())) {
				Suites.add((Testsuite)obj);
			} else {
				throw new IllegalStateException("Unexpected unmarshalled type: " + obj.getClass());
			}
		} catch (JAXBException e) {
			logger.println("[JUnit Parser] Ignoring file `"+resultFile.getName()+"`: " + e.getMessage());
		}

		for (Testsuite suite: Suites) {
			for(Testcase testCase: suite.getCases()) {
				for(Case testRailCase: testCases.getCasesInSuite(suite.getName())) {
					if(testCase.getName().equalsIgnoreCase(testRailCase.getTitle())) {
						Result.STATUS status = Result.STATUS.PASSED;
						String comment = "";
						if(testCase.getFailure() != null) {
							status = Result.STATUS.FAILED;
							comment += testCase.getFailure().getMessage() + "\r\n\r\n"
									+ testCase.getFailure().getText();
						}
						resultContainer.addResult(new Result(testRailCase, status, comment));
					}
				}
			}
		}
	}
}
