package eu.jsparrow.core.markers;

import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;

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
	private int lineNumber;

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
	public int getLineNumber() {
		return this.lineNumber;
	}

	@Override
	public int hashCode() {
		String elementName = iJavaElement == null ? "" : iJavaElement.getElementName(); //$NON-NLS-1$
		return Objects.hash(codePreview, highlightLength, elementName, length, message, name, offset, resolver,
				lineNumber, weightValue);
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
				&& lineNumber == other.lineNumber
				&& Objects.equals(message, other.message) && Objects.equals(name, other.name) && offset == other.offset
				&& Objects.equals(resolver, other.resolver) && weightValue == other.weightValue;
	}

	@Override
	public String toString() {
		String elementName = iJavaElement == null ? "" : iJavaElement.getElementName(); //$NON-NLS-1$
		return String.format(
				"RefactoringEventImpl [offset=%s, length=%s, highlightLength=%s, name=%s, message=%s, iJavaElement=%s, codePreview=%s, resolver=%s, lineNumber=%s, weightValue=%s]", //$NON-NLS-1$
				offset, length, highlightLength, name, message, elementName, codePreview, resolver, lineNumber,
				weightValue);
	}

	public static class Builder {
		private int offset;
		private int length;
		private int highlightLength;
		private String name;
		private String message;
		private IJavaElement iJavaElement;
		private String codePreview;
		private String resolver;
		private int weightValue = 1;
		private int lineNumber;

		public Builder withOffset(int offset) {
			this.offset = offset;
			return this;
		}

		public Builder withLength(int length) {
			this.length = length;
			return this;
		}

		public Builder withHighlightLength(int highlightLength) {
			this.highlightLength = highlightLength;
			return this;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder withIJavaElement(IJavaElement iJavaElement) {
			this.iJavaElement = iJavaElement;
			return this;
		}

		public Builder withCodePreview(String codePreview) {
			this.codePreview = codePreview;
			return this;
		}

		public Builder withResolver(String resolver) {
			this.resolver = resolver;
			return this;
		}

		public Builder withWeightValue(int weightValue) {
			this.weightValue = weightValue;
			return this;
		}

		public Builder withLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
			return this;
		}

		public RefactoringEventImpl build() {
			RefactoringEventImpl result = new RefactoringEventImpl();
			result.offset = offset;
			result.length = length;
			result.highlightLength = highlightLength;
			result.name = name;
			result.message = message;
			result.iJavaElement = iJavaElement;
			result.codePreview = codePreview;
			result.resolver = resolver;
			result.weightValue = weightValue;
			result.lineNumber = lineNumber;
			return result;
		}
	}
}
