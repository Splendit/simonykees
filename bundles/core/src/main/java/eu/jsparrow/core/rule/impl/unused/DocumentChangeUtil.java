package eu.jsparrow.core.rule.impl.unused;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;

/**
 * 
 * @since 4.9.0
 *
 */
public class DocumentChangeUtil {

	private DocumentChangeUtil() {
		/*
		 * Hide the default constructor
		 */
	}

	public static String computeDocumentChangeName(ICompilationUnit targetICU) {
		String packagePath = targetICU.getParent()
			.getPath()
			.toString();
		String normalised = StringUtils.removeStart(packagePath, "/"); //$NON-NLS-1$
		String targetICUName = targetICU.getElementName();
		return String.format("%s - %s", targetICUName, normalised); //$NON-NLS-1$
	}

	public static DocumentChange createDocumentChange(ICompilationUnit iCompilationUnit) throws JavaModelException {
		ICompilationUnit primary = iCompilationUnit.getPrimary();
		String source = primary.getSource();
		Document document = new Document(source);
		String changeName = DocumentChangeUtil.computeDocumentChangeName(iCompilationUnit);
		return new DocumentChange(changeName, document);
	}
}
