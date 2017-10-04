package eu.jsparrow.core.refactorer;

/**
 * Wrapper class for displaying proper error messages if a refactoring state
 * could not be committed
 * 
 * @author Matthias Webhofer
 * @since 2.0.3
 */
public class RefactoringStateNotCommited {
	private String filePath;
	private Exception exception;

	public RefactoringStateNotCommited() {
	}

	public RefactoringStateNotCommited(String filePath, Exception exception) {
		this.filePath = filePath;
		this.exception = exception;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("["); //$NON-NLS-1$
		sb.append(exception.getMessage());
		sb.append("]"); //$NON-NLS-1$

		return sb.toString();
	}
}
