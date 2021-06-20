package eu.jsparrow.core.visitor.junit.junit3;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class MainMethodDeclaration {

	boolean isMainMethodDeclaration(MethodDeclaration methodDeclaration) {

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(methodDeclaration, CompilationUnit.class);

		ASTNode parent = methodDeclaration.getParent();
		if (parent.getLocationInParent() != CompilationUnit.TYPES_PROPERTY) {
			return false;
		}

		ITypeRoot typeRoot = compilationUnit.getTypeRoot();

		IType primaryType = typeRoot.findPrimaryType();

		String primaryTypeFullyQualifiedName = primaryType.getFullyQualifiedName();

		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		if (!"main".equals(methodBinding.getName())) { //$NON-NLS-1$
			return false;
		}
		if (!methodBinding.getDeclaringClass()
			.getQualifiedName()
			.equals(primaryTypeFullyQualifiedName)) {
			return false;
		}

		int modifiers = methodBinding.getModifiers();
		if (!Modifier.isStatic(modifiers)) {
			return false;
		}
		if (!Modifier.isPublic(modifiers)) {
			return false;
		}

		ITypeBinding[] mainParameterTypes = methodBinding.getParameterTypes();
		if (mainParameterTypes.length != 1) {
			return false;
		}
		ITypeBinding mainParameterType = mainParameterTypes[0];

		ITypeBinding componentType = mainParameterType.getComponentType();
		if (componentType == null) {
			return false;
		}
		return ClassRelationUtil.isContentOfType(componentType, java.lang.String.class.getName());
	}

}
