package eu.jsparrow.core.visitor.functionalinterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.sub.SimpleNameQualifier;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds unqualified references to class fields which may be problematic in
 * connection with the transformation of anonymous classes to lambda
 * expressions.
 * 
 * @since 3.16
 */
class UnqualifiedFieldNamesVisitor extends ASTVisitor {

	private final Map<SimpleName, QualifiedName> simpleNameReplacementsMap = new HashMap<>();
	
	private final Type instanceCreationType;
	
	private final ITypeBinding instanceCreationResolvedTypeBinding;

	UnqualifiedFieldNamesVisitor(ClassInstanceCreation instanceCreation) {
		instanceCreationType = instanceCreation.getType();
		instanceCreationResolvedTypeBinding = instanceCreationType.resolveBinding();
	}

	private void addToReplacementsMap(SimpleName simpleNameToReplace) {
		QualifiedName qualifiedName = SimpleNameQualifier.qualifyByType(instanceCreationType, simpleNameToReplace);
		simpleNameReplacementsMap.put(simpleNameToReplace, qualifiedName);
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		ASTNode simpleNameParent = simpleName.getParent();
		if (simpleNameParent instanceof QualifiedName) {
			return true;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return true;
		}

		if (binding.getKind() != IBinding.VARIABLE) {
			return true;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (!variableBinding.isField()) {
			return true;
		}

		ITypeBinding declaringClass = variableBinding.getDeclaringClass();
		boolean isInheritingDeclaringClass = ClassRelationUtil.isInheritingContentOfTypes(
				instanceCreationResolvedTypeBinding,
				Collections.singletonList(declaringClass.getQualifiedName()));
		
		boolean isDeclaringClass = ClassRelationUtil.isContentOfType(instanceCreationResolvedTypeBinding, declaringClass.getQualifiedName());

		if (isInheritingDeclaringClass || isDeclaringClass) {
			addToReplacementsMap(simpleName);
		}

		return true;
	}

	public Stream<Entry<SimpleName, QualifiedName>> getSimpleNameReplacements() {
		return simpleNameReplacementsMap.entrySet()
			.stream();
	}

}