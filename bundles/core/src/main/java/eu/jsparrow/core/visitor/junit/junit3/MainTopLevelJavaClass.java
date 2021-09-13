package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Utility class determining whether a given Java type is the top level type
 * which has the same name as the file storing the given compilation unit, for
 * example:<br>
 * In a Java file "HelloWorld.java" the class "HelloWorld" will be the main top
 * level class
 *
 * @since 4.1.0
 */
public class MainTopLevelJavaClass {

	private MainTopLevelJavaClass() {
		/*
		 * private constructor to hide implicit public one
		 */
	}

	public static boolean isMainTopLevelClass(TypeDeclaration typeDeclaration) {
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(typeDeclaration, CompilationUnit.class);
		if (compilationUnit == null) {
			return false;
		}
		String javaElementName = compilationUnit.getJavaElement()
			.getElementName();
		List<String> rootTypeName = new ArrayList<>();

		/*
		 * This is a workaround of using compilationUnit .getTypeRoot()
		 * .findPrimaryType() .getFullyQualifiedName() The JdtUnitFixture is not
		 * able to provide the primary type of the compilation unit.
		 */
		int indexOfFileExtension = javaElementName.lastIndexOf(".java"); //$NON-NLS-1$
		if (indexOfFileExtension > 0) {
			javaElementName = javaElementName.substring(0, indexOfFileExtension);
			PackageDeclaration packageDeclaration = compilationUnit.getPackage();
			if (packageDeclaration != null) {
				String fullyQualifiedCompilationUnitPackageName = packageDeclaration
					.getName()
					.getFullyQualifiedName();
				rootTypeName.add(fullyQualifiedCompilationUnitPackageName);
			}
			String typeDeclarationQualifiedName = typeDeclaration.resolveBinding()
				.getQualifiedName();
			rootTypeName.add(javaElementName);
			return typeDeclarationQualifiedName
				.equals(String.join(".", rootTypeName)); //$NON-NLS-1$
		}

		return false;
	}
}
