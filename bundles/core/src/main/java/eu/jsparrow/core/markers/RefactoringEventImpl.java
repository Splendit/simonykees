package eu.jsparrow.core.markers;

import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A data holder for the {@link RefactoringMarkerEvent}s generated in this
 * module.
 * 
 * @since 4.0.0
 *
 */
public class RefactoringEventImpl implements RefactoringMarkerEvent {

	private int offset;
	private int length;
	private int highlightLength;
	private String name;
	private String message;
	private IJavaElement iJavaElement;
	private String codePreview;
	private String resolver;
	private int weightValue = 1;

	public RefactoringEventImpl(String resolver, String name, String message, IJavaElement iJavaElement,
			int highlightLenght,
			ASTNode original, ASTNode replacement, int weightValue) {
		this.resolver = resolver;
		this.name = name;
		this.offset = original.getStartPosition();
		this.length = original.getLength();
		this.highlightLength = highlightLenght;
		this.codePreview = replacement.toString();
		this.message = message;
		this.iJavaElement = iJavaElement;
		this.weightValue = weightValue;
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
	public String getCodePreview() {
		return this.codePreview;
	}

	@Override
	public String getResolver() {
		return this.resolver;
	}

	@Override
	public int getWeightValue() {
		return this.weightValue;
	}

	@Override
	public int hashCode() {
		String elementName = iJavaElement == null ? "" : iJavaElement.getElementName(); //$NON-NLS-1$
		return Objects.hash(codePreview, highlightLength, elementName, length, message, name, offset, resolver,
				weightValue);
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
		String otherElementName = other.iJavaElement == null ? "" : other.iJavaElement.getElementName(); //$NON-NLS-1$
		return Objects.equals(codePreview, other.codePreview) && highlightLength == other.highlightLength
				&& Objects.equals(elementName, otherElementName) && length == other.length
				&& Objects.equals(message, other.message) && Objects.equals(name, other.name) && offset == other.offset
				&& Objects.equals(resolver, other.resolver) && weightValue == other.weightValue;
	}

	@Override
	public String toString() {
		String elementName = iJavaElement == null ? "" : iJavaElement.getElementName(); //$NON-NLS-1$
		return String.format(
				"RefactoringEventImpl [offset=%s, length=%s, highlightLength=%s, name=%s, message=%s, iJavaElement=%s, codePreview=%s, resolver=%s, weightValue=%s]", //$NON-NLS-1$
				offset, length, highlightLength, name, message, elementName, codePreview, resolver, weightValue);
	}

}
