package eu.jsparrow.core.visitor.junit.junit3;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class JUnit3TestMethodsStore {

	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	private final List<MethodDeclaration> jUnit3TestMethods;

	JUnit3TestMethodsStore(CompilationUnit compilationUnit,
			UnreferencedMainMethodStore unreferencedMainMethodStore) {
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		List<MethodDeclaration> allMethodDeclarations = methodDeclarationsCollectorVisitor.getMethodDeclarations();
		jUnit3TestMethods = allMethodDeclarations
			.stream()
			.filter(methodDeclaration -> !unreferencedMainMethodStore
				.isSurroundedByMethodDeclaration(methodDeclaration))
			.filter(JUnit3TestMethodsStore::isJUnit3TestMethod)
			.collect(Collectors.toList());
	}

	private static boolean isJUnit3TestMethod(MethodDeclaration methodDeclaration) {

		IMethodBinding methodBinding = methodDeclaration.resolveBinding();

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();

		if (declaringClass.isLocal() || declaringClass.isAnonymous()) {
			return false;
		}

		ITypeBinding declaringClassSuperType = declaringClass.getSuperclass();
		if (declaringClassSuperType == null) {
			return false;
		}

		if (!ClassRelationUtil.isContentOfType(declaringClassSuperType, "junit.framework.TestCase")) { //$NON-NLS-1$
			return false;
		}

		if (methodBinding.getParameterTypes().length != 0) {
			return false;
		}

		String methodName = methodBinding.getName();
		return methodName.startsWith(TEST) || methodName.equals(SET_UP) || methodName.equals(TEAR_DOWN);
	}

	boolean isSurroundedWithJUnit3Test(MethodInvocation methodInvocation) {
		BodyDeclaration bodyDeclarationAncestor = ASTNodeUtil.getSpecificAncestor(methodInvocation,
				BodyDeclaration.class);
		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent == bodyDeclarationAncestor) {
				return parent.getNodeType() == ASTNode.METHOD_DECLARATION && jUnit3TestMethods.contains(parent);
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return false;
			}
			parent = parent.getParent();
		}
		return false;
	}

	List<MethodDeclaration> getJUnit3TestMethods() {
		return jUnit3TestMethods;
	}
}