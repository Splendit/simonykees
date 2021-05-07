package eu.jsparrow.core.markers;

import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.markers.MarkerEvent;

public class RefactorEvent implements MarkerEvent {

	private int offset;
	private int length;
	private String name;
	private String message;
	private IJavaElement iJavaElement;
	private Map<ASTNode, ASTNode> replacements;
	private String description;

	public RefactorEvent(String name, String message, IJavaElement iJavaElement,
			ASTNode original, ASTNode replacement) {
		this.name = name;
		this.offset = original.getStartPosition();
		this.length = original.getLength();
		this.description = replacement.toString();
		this.message = message;
		this.iJavaElement = iJavaElement;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IJavaElement getJavaElement() {
		return iJavaElement;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	public Map<ASTNode, ASTNode> getReplacements() {
		return replacements;
	}
}
