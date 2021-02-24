package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;

/**
 * 
 * @since 3.28.0
 *
 */
public class MethodReferenceCollectorVisitor extends ASTVisitor {

	private final List<MethodReference> methodReferences = new ArrayList<>();

	@Override
	public boolean visit(ExpressionMethodReference node) {
		methodReferences.add(node);
		return true;
	}
	
	@Override
	public boolean visit(TypeMethodReference node) {
		methodReferences.add(node);
		return true;
	}

	public List<MethodReference> getMethodReferences() {		
		return methodReferences;
	}

}