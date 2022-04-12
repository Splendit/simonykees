package eu.jsparrow.core.visitor.unused.type;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;

import eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName;

/**
 * Visitor to verify whether there is any reference to JUnit3 or JUnit4 or JUnit
 * Jupiter packages, regardless whether or not this compilation unit is used for
 * running JUnit tests.
 * <p>
 * Any reference found by this visitor may be interpreted as an indicator that
 * the given compilation unit is either used for running tests or is containing
 * utility class functionalities used by JUnit tests, which may also prohibit
 * removing type declarations declared in the given compilation unit.
 * 
 *
 */
public class JUnitNamesVisitor extends ASTVisitor {
	private boolean jUnitReferenceFound = false;

	@Override
	public boolean visit(ImportDeclaration node) {
		jUnitReferenceFound = RegexJUnitQualifiedName.isJUnitName(node.getName()
			.getFullyQualifiedName());
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		jUnitReferenceFound = RegexJUnitQualifiedName.isJUnitName(node.getFullyQualifiedName());
		return false;
	}

	public boolean isJUnitReferenceFound() {
		return jUnitReferenceFound;
	}

}
