package eu.jsparrow.core.refactorer;

/**
 * Model for representing metadata for project's statistics.
 * 
 * @since 2.7.0
 *
 */
public class StandaloneStatisticsMetadata {

	private long startTime;
	private String repoOwner;
	private String repoName;

	public StandaloneStatisticsMetadata() {

	}

	public StandaloneStatisticsMetadata(long startTime, String repoOwner, String repoName) {
		this.startTime = startTime;
		this.repoOwner = repoOwner;
		this.repoName = repoName;
	}

	public boolean isValid() {
		return startTime > 0 && repoOwner != null && !repoOwner.isEmpty() && repoName != null && !repoName.isEmpty();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
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
