package eu.jsparrow.core.rule.statistics;

//Tracks the number of changes within a single file
public class FileChangeCount {

	private String compilationUnitHandle;

	private int count;

	public FileChangeCount(String compilationUnitHandle) {
		this.compilationUnitHandle = compilationUnitHandle;
	}

	public String getCompilationUnitHandle() {
		return compilationUnitHandle;
	}

	public int getCount() {
		return count;
	}

	public void update() {
		count++;
	}

	public void clear() {
		count = 0;
	}

}
