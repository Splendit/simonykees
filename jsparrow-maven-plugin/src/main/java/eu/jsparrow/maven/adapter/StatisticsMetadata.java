package eu.jsparrow.maven.adapter;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsMetadata {

	private static final Logger logger = LoggerFactory.getLogger(StatisticsMetadata.class);

	private Instant startTime;
	private String repoOwner;
	private String repoName;

	public StatisticsMetadata() {

	}

	public StatisticsMetadata(Instant startTime, String repoOwner, String repoName) {
		this.startTime = startTime;
		this.repoOwner = repoOwner;
		this.repoName = repoName;
	}

	public StatisticsMetadata(String startTime, String repoOwner, String repoName) {
		try {
			if (startTime != null) {
				this.startTime = Instant.parse(startTime);
			} else {
				this.startTime = null;
			}
		} catch (DateTimeParseException e) {
			this.startTime = null;
			logger.debug(e.getMessage(), e);
			logger.warn(e.getMessage());
		}

		this.repoOwner = repoOwner;
		this.repoName = repoName;
	}

	public boolean isValid() {
		return startTime != null && repoOwner != null && !repoOwner.isEmpty() && repoName != null
				&& !repoName.isEmpty();
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public String getRepoOwner() {
		return repoOwner;
	}

	public void setRepoOwner(String repoOwner) {
		this.repoOwner = repoOwner;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

}
