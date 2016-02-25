package testrail.testrail.testng;

import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.*;

/**
 * Description
 */
@Test(groups = {"TESTRAIL-123"})
public class TestNGSuiteTest {

	@Test(expectedExceptions = NumberFormatException.class)
	public void testSetDuration_givenInvalidInteger_throw() throws Exception {
		TestNGSuite suite = new TestNGSuite();
		suite.setDuration("1asdf");
		assertThat(suite.getDuration(), is(not(1)));
	}
	@Test
	public void testSetDuration_givenValidInteger_setValue() throws Exception {
		TestNGSuite suite = new TestNGSuite();
		suite.setDuration("1");
		assertThat(suite.getDuration(), is(1));
	}

	@Test
	public void testSetStarted() throws Exception {
		String date = "2016-02-18T15:41:01Z";
		TestNGSuite suite = new TestNGSuite();
		suite.setStarted(date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertThat(sdf.format(suite.getStarted()), is(date));
	}

	@Test
	public void testSetFinished() throws Exception {
		String date = "2016-02-18T15:41:01Z";
		TestNGSuite suite = new TestNGSuite();
		suite.setFinished(date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertThat(sdf.format(suite.getFinished()), is(date));
	}
}