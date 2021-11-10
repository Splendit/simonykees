package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class NonStaticReferencesVisitor extends ASTVisitor {
	private final CompilationUnit compilationUnit;
	private final ArrayList<AbstractTypeDeclaration> surroundingTypeDeclarations;
	private final String typeDeclarationQualifiedName;
	private boolean unsupportedReferenceExisting;

	NonStaticReferencesVisitor(CompilationUnit compilationUnit, TypeDeclaration typeDeclaration) {
		this.compilationUnit = compilationUnit;
		this.typeDeclarationQualifiedName = typeDeclaration.resolveBinding()
			.getErasure()
			.getQualifiedName();
		this.surroundingTypeDeclarations = collectSurroundingTypeDeclarations(typeDeclaration);
	}

	private static ArrayList<AbstractTypeDeclaration> collectSurroundingTypeDeclarations(
			TypeDeclaration typeDeclaration) {
		ArrayList<AbstractTypeDeclaration> surroundingTypeDeclarations = new ArrayList<>();
		AbstractTypeDeclaration surroundingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				AbstractTypeDeclaration.class);
		while (surroundingTypeDeclaration != null) {
			surroundingTypeDeclarations.add(surroundingTypeDeclaration);
			surroundingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(surroundingTypeDeclaration,
					AbstractTypeDeclaration.class);
		}
		return surroundingTypeDeclarations;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unsupportedReferenceExisting;
	}

	@Override
	public boolean visit(ThisExpression node) {
		Name thisQualifier = node.getQualifier();
		if (thisQualifier != null) {
			unsupportedReferenceExisting = !ClassRelationUtil.isContentOfType(thisQualifier.resolveTypeBinding(),
					typeDeclarationQualifiedName);
		}
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		unsupportedReferenceExisting = true;
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		unsupportedReferenceExisting = !Modifier.isStatic(typeBinding.getModifiers()) && !typeBinding.isTopLevel();
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		unsupportedReferenceExisting = !analyzeNameBinding(node) || !analyzeRootQualifier(node.getQualifier());
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		unsupportedReferenceExisting = !analyzeSimpleName(node);
		return false;
	}

	private boolean analyzeRootQualifier(Name qualifier) {
		if (qualifier.getNodeType() == ASTNode.QUALIFIED_NAME) {
			return analyzeRootQualifier(((QualifiedName) qualifier).getQualifier());
		}
		return analyzeNameBinding(qualifier);
	}

	private boolean analyzeSimpleName(SimpleName node) {
		if (node.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
				node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
				node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
				node.getLocationInParent() == LabeledStatement.LABEL_PROPERTY ||
				node.getLocationInParent() == ContinueStatement.LABEL_PROPERTY ||
				node.getLocationInParent() == BreakStatement.LABEL_PROPERTY) {
			return true;
		}
		return analyzeNameBinding(node);
	}

	private boolean analyzeNameBinding(Name name) {
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		if (Modifier.isStatic(binding.getModifiers())) {
			return true;
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;

			if (variableBinding.isField()) {
				return !isSurroundingClass(variableBinding.getDeclaringClass());
			}
			ASTNode declaringNode = compilationUnit.findDeclaringNode(variableBinding);
			AbstractTypeDeclaration surroundingType = ASTNodeUtil.getSpecificAncestor(declaringNode,
					AbstractTypeDeclaration.class);
			return !surroundingTypeDeclarations.contains(surroundingType);
		}

		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			return !isSurroundingClass(methodBinding.getDeclaringClass());
		}

		return true;
	}

	private boolean isSurroundingClass(ITypeBinding typeBinding) {
		ASTNode declaringNode = compilationUnit.findDeclaringNode(typeBinding);
		return surroundingTypeDeclarations.contains(declaringNode);
	}

	boolean isUnsupportedReferenceExisting() {
		return unsupportedReferenceExisting;
	}
}
