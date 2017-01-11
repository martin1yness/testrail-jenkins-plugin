package testrail.testrail;

import org.junit.Ignore;
import org.testng.annotations.Test;
import testrail.testrail.TestRailObjects.Project;
import testrail.testrail.TestRailObjects.Suite;

import static org.testng.Assert.*;

/**
 *
 */
@Ignore
public class TestRailClientTest {
	@Test
	public void testGetCases() throws Exception {
		TestRailClient client = new TestRailClient("http://edc-testrail.dev.datacard.com/testrail",
				"**", "**");

		Project[] projects = client.getProjects();
		for(int i=0; i<projects.length; i++) {
			Suite[] suites = client.getSuits(projects[i].getId());
			if(suites.length > 0)
				client.getCases(1);
		}
	}

}