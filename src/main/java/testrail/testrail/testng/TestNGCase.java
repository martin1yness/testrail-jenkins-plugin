package testrail.testrail.testng;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Description
 */
public class TestNGCase implements Serializable {
	private static final long serialVersionUID = 1;
	protected transient static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

	private String name, resultDescription;
	private int duration;
	private Date started, finished;
	private boolean successful = false;

	public TestNGCase() { }

	public TestNGCase(String name, String resultDescription, int duration,
					  Date started, Date finished, boolean successful) {
		this.name = name;
		this.resultDescription = resultDescription;
		this.duration = duration;
		this.started = started;
		this.finished = finished;
		this.successful = successful;
	}

	public String getName() {
		return name;
	}

	public TestNGCase setName(String name) {
		this.name = name;
		return this;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	public TestNGCase setResultDescription(String resultDescription) {
		this.resultDescription = resultDescription;
		return this;
	}

	public int getDuration() {
		return duration;
	}

	public TestNGCase setDuration(String duration) {
		if(duration == null || duration.isEmpty())
			throw new IllegalArgumentException("Empty duration given: " + duration);

		setDuration(Integer.parseInt(duration));

		return this;
	}

	public TestNGCase setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	public Date getStarted() {
		return started;
	}

	public TestNGCase setStarted(String started) {
		try {
			return setStarted(sdf.parse(started));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Malformed date: " + started);
		}
	}

	public TestNGCase setStarted(Date started) {
		this.started = started;
		return this;
	}

	public Date getFinished() {
		return finished;
	}

	public TestNGCase setFinished(String started) {
		try {
			return setFinished(sdf.parse(started));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Malformed date: " + started);
		}
	}

	public TestNGCase setFinished(Date finished) {
		this.finished = finished;
		return this;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public TestNGCase setSuccessful(boolean successful) {
		this.successful = successful;
		return this;
	}
}