package eu.jsparrow.core.markers;

import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A data holder for the {@link RefactoringMarkerEvent}s generated in this module. 
 * 
 * @since 3.31.0
 *
 */
public class RefactoringEventImpl implements RefactoringMarkerEvent {

	private int offset;
	private int length;
	private int highlightLength;
	private String name;
	private String message;
	private IJavaElement iJavaElement;
	private String description;
	private String resolver;

	public RefactoringEventImpl(String resolver, String name, String message, IJavaElement iJavaElement, int highlightLenght,
			ASTNode original, ASTNode replacement) {
		this.resolver = resolver;
		this.name = name;
		this.offset = original.getStartPosition();
		this.length = original.getLength();
		this.highlightLength = highlightLenght;
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
	public int getHighlightLength() {
		return highlightLength;
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

	@Override
	public String getResolver() {
		return this.resolver;
	}

	@Override
	public int hashCode() {
		String elementName = iJavaElement == null ? "" : iJavaElement.getElementName(); //$NON-NLS-1$
		return Objects.hash(description, elementName, length, message, name, offset, resolver);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RefactoringEventImpl)) {
			return false;
		}
		RefactoringEventImpl other = (RefactoringEventImpl) obj;
		String elementName = iJavaElement == null ? "" : iJavaElement.getElementName(); //$NON-NLS-1$
		String otherElString = other.iJavaElement == null ? "" : other.iJavaElement.getElementName(); //$NON-NLS-1$
		return Objects.equals(description, other.description)
				&& Objects.equals(elementName, otherElString)
				&& length == other.length
				&& Objects.equals(message, other.message) && Objects.equals(name, other.name) && offset == other.offset
				&& Objects.equals(resolver, other.resolver);
	}

	@Override
	public String toString() {
		return String.format(
				"RefactoringEventImpl [offset=%s, length=%s, name=%s, message=%s, iJavaElement=%s, description=%s, resolver=%s]", //$NON-NLS-1$
				offset, length, name, message, iJavaElement.getElementName(), description, resolver);
	}

}
