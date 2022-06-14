package eu.jsparrow.jdt.ls.core.internal;

public enum EventType {
    /**
	 * classpath updated event.
	 */
	ClasspathUpdated(100),

	/**
	 * projects imported event.
	 */
	ProjectsImported(200),

	/**
	 * Incompatible issue between Gradle and Jdk event.
	 */
	IncompatibleGradleJdkIssue(300);

	private final int value;

	EventType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
