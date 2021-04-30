package eu.jsparrow.core.markers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.MarkerEvent;

public class RefactorEvent implements MarkerEvent {

	private int index;
	private int offset;
	private int length;
	private String message;
	private IJavaElement iJavaElement;
	private Map<ASTNode, ASTNode> replacements;
	private List<ASTNode> imports;

	public RefactorEvent(int index, int offset, int length, String message, IJavaElement iJavaElement,
			List<ASTNode> imports, Map<ASTNode, ASTNode> replacements) {
		this.index = index;
		this.offset = offset;
		this.length = length;
		this.message = message;
		this.iJavaElement = iJavaElement;
		this.replacements = replacements;
		this.imports = imports;
	}
	
	public RefactorEvent(int index, int offset, int length, String message, IJavaElement iJavaElement,
			Map<ASTNode, ASTNode> replacements) {
		this(index, offset, length, message, iJavaElement, Collections.emptyList(), replacements);
	}

	@Override
	public int getIndex() {
		return index;
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
	public IJavaElement getJavaElement() {
		return iJavaElement;
	}

	public Map<ASTNode, ASTNode> getReplacements() {
		return replacements;
	}
	
	public List<ASTNode> getImports() {
		return this.imports;
	}
}
