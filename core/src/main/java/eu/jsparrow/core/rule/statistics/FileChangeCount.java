package eu.jsparrow.core.rule.statistics;

/**
 * This class represents the number of changes for given file. 
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.3.0
 */
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
