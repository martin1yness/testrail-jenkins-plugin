package testrail.testrail.testng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An unmarshalled Test NG Suite object
 */
public class TestNGSuite {
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

	private String name;
	private int duration;
	private Date started, finished;

	public TestNGSuite() { }

	public TestNGSuite(String name, int duration, Date started, Date finished) {
		this.name = name;
		this.duration = duration;
		this.started = started;
		this.finished = finished;
	}

	public String getName() {
		return name;
	}

	public TestNGSuite setName(String name) {
		this.name = name;
		return this;
	}

	public int getDuration() {
		return duration;
	}

	public TestNGSuite setDuration(String duration) {
		if(duration == null || duration.isEmpty())
			throw new IllegalArgumentException("Empty duration given: " + duration);

		setDuration(Integer.parseInt(duration));

		return this;
	}

	public TestNGSuite setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	public Date getStarted() {
		return started;
	}

	public TestNGSuite setStarted(String started) {
		try {
			return setStarted(sdf.parse(started));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Malformed date: " + started);
		}
	}

	public TestNGSuite setStarted(Date started) {
		this.started = started;
		return this;
	}

	public Date getFinished() {
		return finished;
	}

	public TestNGSuite setFinished(String started) {
		try {
			return setFinished(sdf.parse(started));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Malformed date: " + started);
		}
	}

	public TestNGSuite setFinished(Date finished) {
		this.finished = finished;
		return this;
	}
}
