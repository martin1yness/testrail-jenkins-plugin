package testrail.testrail.testng;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An unmarshalled Test NG Suite object
 */
public class TestNGSuite implements Serializable {
	private static final long serialVersionUID = 1;
	protected transient static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

	private String name;
	private int duration;
	private Date started, finished;
	private List<TestNGCase> cases;

	public TestNGSuite() { }

	public TestNGSuite(String name, int duration, Date started, Date finished, List<TestNGCase> cases) {
		this.name = name;
		this.duration = duration;
		this.started = started;
		this.finished = finished;
		this.cases = cases;
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

	public List<TestNGCase> getCases() {
		return cases;
	}

	public TestNGSuite setCases(List<TestNGCase> cases) {
		this.cases = cases;
		return this;
	}
}
