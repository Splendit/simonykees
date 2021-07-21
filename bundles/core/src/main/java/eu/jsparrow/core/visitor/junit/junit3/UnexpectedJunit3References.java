package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.junit3.JUnit3DataCollectorVisitor.JUNIT_FRAMEWORK_TEST_CASE;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Utility class for detecting unexpected references to JUnit which prohibit
 * transformation.
 * 
 * @since 4.1.0
 *
 */
public class UnexpectedJunit3References {

	private UnexpectedJunit3References() {
		// private constructor of utility class in hiding implicit public one
	}

	static boolean analyzeNameBinding(CompilationUnit compilationUnit, IBinding binding) {

		if (binding.getKind() == IBinding.PACKAGE) {
			IPackageBinding packageBinding = (IPackageBinding) binding;
			return !isJUnit3Name(packageBinding.getName());
		}

		if (binding.getKind() == IBinding.TYPE) {
			return analyzeTypeBinding(compilationUnit, (ITypeBinding) binding);
		}

		if (binding.getKind() == IBinding.METHOD) {
			return analyzeMethodBinding(compilationUnit, ((IMethodBinding) binding));
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			return analyzeVariableBinding(compilationUnit, (IVariableBinding) binding);
		}
		// Not covered: any other binding which is not expected for a name in
		// connection with the migration of JUnit3
		return false;
	}

	static boolean analyzeTypeBinding(CompilationUnit compilationUnit, ITypeBinding typeBinding) {
		if (isTestCaseSubclassDeclaredInCompilationUnit(compilationUnit, typeBinding)) {
			return true;
		}
		return !isUnexpectedJUnitReference(typeBinding);
	}

	static boolean analyzeMethodBinding(CompilationUnit compilationUnit, IMethodBinding methodBinding) {
		return analyzeTypeBinding(compilationUnit, methodBinding.getDeclaringClass())
				&& analyzeTypeBinding(compilationUnit, methodBinding.getReturnType());
	}

	static boolean analyzeVariableBinding(CompilationUnit compilationUnit, IVariableBinding variableBinding) {
		if (variableBinding.isField()) {
			ITypeBinding fieldDeclaringClass = variableBinding.getDeclaringClass();
			if (fieldDeclaringClass != null && !analyzeTypeBinding(compilationUnit, fieldDeclaringClass)) {
				return false;
			}
		}
		ITypeBinding variableTypeBinding = variableBinding.getVariableDeclaration()
			.getType();

		return analyzeTypeBinding(compilationUnit, variableTypeBinding);
	}

	static boolean isTestCaseSubclassDeclaredInCompilationUnit(CompilationUnit compilationUnit,
			ITypeBinding typeBinding) {

		if (ClassRelationUtil.isContentOfType(typeBinding.getSuperclass(), JUNIT_FRAMEWORK_TEST_CASE)) {
			ASTNode declaringNode = compilationUnit.findDeclaringNode(typeBinding);
			if (declaringNode != null) {
				return true;
			}
		}
		return false;
	}

	private static boolean isUnexpectedJUnitReference(ITypeBinding typeBinding) {
		if (typeBinding.isPrimitive()) {
			return false;
		}
		if (typeBinding.isArray()) {
			return isUnexpectedJUnitReference(typeBinding.getComponentType());
		}
		if (isJUnit3Name(typeBinding.getQualifiedName())) {
			return true;
		}

		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			if (isJUnit3Name(ancestor.getQualifiedName())) {
				return true;
			}
		}
		return false;
	}

	static boolean isJUnit3Name(String fullyQualifiedName) {
		return fullyQualifiedName.equals("junit") || fullyQualifiedName.startsWith("junit."); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
