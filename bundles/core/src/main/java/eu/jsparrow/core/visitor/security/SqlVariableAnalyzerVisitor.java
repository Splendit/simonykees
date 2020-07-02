package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * This visitor is intended to be used by visitors which transform dynamic
 * queries in order to prevent injections.
 * <p>
 * It analyzes the declaration and references on a variable which may represent
 * for example:
 * <ul>
 * <li>a dynamic SQL query</li>
 * <li>an LDAP filter expression</li>
 * </ul>
 * 
 * @since 3.16.0
 *
 */
public class SqlVariableAnalyzerVisitor extends ASTVisitor {

	private CompilationUnit compilationUnit;
	private SimpleName variableName;
	private ASTNode declarationFragment;
	private final DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
	private boolean beforeDeclaration = true;
	private boolean beforeUsage = true;
	private boolean unsafe = false;

	public SqlVariableAnalyzerVisitor(SimpleName variableName, ASTNode declaration, CompilationUnit compilationUnit) {
		this.variableName = variableName;
		this.declarationFragment = declaration;
		this.compilationUnit = compilationUnit;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (this.declarationFragment == fragment) {
			beforeDeclaration = false;
			Expression initializer = fragment.getInitializer();
			componentStore.storeComponents(initializer);
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (beforeDeclaration) {
			return false;
		}

		if (simpleName == variableName) {
			beforeUsage = false;
			return false;
		}

		if (!variableName.getIdentifier()
			.equals(simpleName.getIdentifier())) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}

		if (((IVariableBinding) binding).isField()) {
			return false;
		}

		ASTNode declaringNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
		if (declaringNode != declarationFragment) {
			return false;
		}

		if (!beforeUsage) {
			unsafe = true;
			return false;
		}

		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if (structuralDescriptor == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) simpleName.getParent();
			if (assignment.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				componentStore.storeComponents(assignment.getRightHandSide());
			} else {
				unsafe = true;
			}

		} else {
			unsafe = true;
		}

		return true;
	}

	public boolean isUnsafe() {
		return unsafe;
	}

	public List<Expression> getDynamicQueryComponents() {
		return componentStore.getComponents();
	}
}
