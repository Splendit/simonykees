package eu.jsparrow.ui.quickfix;

import org.eclipse.jdt.core.IJavaElement;

public class RefactoringEvent {

	private int index;
	private int offset;
	private int length;
	private String message;
	private IJavaElement javaElement;

	public RefactoringEvent(IJavaElement javaElement, String message, int index, int offset, int length) {
		this.index = index;
		this.offset = offset;
		this.length = length;
		this.message = message;
		this.javaElement = javaElement;
	}

	public int getIndex() {
		return index;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getMessage() {
		return message;
	}
	
	public IJavaElement getJavaElement() {
		return this.javaElement;
	}
	
}
