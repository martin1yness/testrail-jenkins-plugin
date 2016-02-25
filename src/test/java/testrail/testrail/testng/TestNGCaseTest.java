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
public class TestNGCaseTest {
	@Test(expectedExceptions = NumberFormatException.class)
	public void testSetDuration_givenInvalidInteger_throw() throws Exception {
		TestNGCase testCase = new TestNGCase();
		testCase.setDuration("1asdf");
		assertThat(testCase.getDuration(), is(not(1)));
	}
	@Test
	public void testSetDuration_givenValidInteger_setValue() throws Exception {
		TestNGCase testCase = new TestNGCase();
		testCase.setDuration("1");
		assertThat(testCase.getDuration(), is(1));
	}

	@Test
	public void testSetStarted() throws Exception {
		String date = "2016-02-18T15:41:01Z";
		TestNGCase testCase = new TestNGCase();
		testCase.setStarted(date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertThat(sdf.format(testCase.getStarted()), is(date));
	}

	@Test
	public void testSetFinished() throws Exception {
		String date = "2016-02-18T15:41:01Z";
		TestNGCase testCase = new TestNGCase();
		testCase.setFinished(date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertThat(sdf.format(testCase.getFinished()), is(date));
	}
}