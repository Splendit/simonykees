package eu.jsparrow.core.visitor.junit.junit3;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class MainTopLevelJavaClass {

	private MainTopLevelJavaClass() {
		// private constructor to hide implicit public one
	}

	public static boolean isMainTopLevelClass(TypeDeclaration typeDeclaration) {
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(typeDeclaration, CompilationUnit.class);
		if (compilationUnit == null) {
			return false;
		}
		String javaElementName = compilationUnit.getJavaElement()
			.getElementName();

		int indexOfFileExtension = javaElementName.lastIndexOf(".java"); //$NON-NLS-1$
		if (indexOfFileExtension > 0) {
			javaElementName = javaElementName.substring(0, indexOfFileExtension);
			String fullyQualifiedCompilationUnitPackageName = compilationUnit.getPackage()
				.getName()
				.getFullyQualifiedName();
			String typeDeclarationQualifiedName = typeDeclaration.resolveBinding()
				.getQualifiedName();
			return typeDeclarationQualifiedName
				.equals(fullyQualifiedCompilationUnitPackageName + '.' + javaElementName);
		}

		return false;
	}
}
