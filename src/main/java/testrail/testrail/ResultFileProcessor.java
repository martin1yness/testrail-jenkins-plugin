package testrail.testrail;

import testrail.testrail.TestRailObjects.Results;

import java.io.File;
import java.io.InputStream;

/**
 * Description
 */
public interface ResultFileProcessor {

	void handleResult(File resultFile, Results resultContainer);

}
