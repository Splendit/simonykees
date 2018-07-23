package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A visitor for finding all the referenced {@link IJavaElement#FIELD}s.
 * 
 * @since 2.6.0
 */
public class ReferencedFieldsVisitor extends ASTVisitor {

	private List<SimpleName> referencedNames = new ArrayList<>();

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return false;
		}

		IJavaElement javaElement = binding.getJavaElement();
		if (javaElement == null) {
			return false;
		}

		if (IJavaElement.FIELD == javaElement.getElementType()) {
			referencedNames.add(simpleName);
		}

		return true;
	}

	public List<SimpleName> getReferencedVariables() {
		return referencedNames;
	}
}
