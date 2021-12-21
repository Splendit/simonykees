package eu.jsparrow.rules.api.test.dummies;

import org.eclipse.jdt.core.IJavaElement;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

public class DummyRefactoringEvent implements RefactoringMarkerEvent {
	
	private String name;
	
	public DummyRefactoringEvent(String name) {
		this.name = name;
	}

	@Override
	public String getResolver() {
		return "DummyVisitor";
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getMessage() {
		return "A refactoring event for testing purposes";
	}

	@Override
	public String getCodePreview() {
		return "Do not change anything";
	}

	@Override
	public IJavaElement getJavaElement() {
		return null;
	}

	@Override
	public int getHighlightLength() {
		return 0;
	}

	@Override
	public int getWeightValue() {
		return 0;
	}

	@Override 
	public int getLineNumber() {
		return 0;
	}
}
