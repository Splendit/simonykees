package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.utils.MainMethodMatches.findMainMethodMatches;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;

/**
 * Visitor collecting all type declarations, method declarations and method
 * invocations which will have to be analyzed. Additionally, this visitor
 * determines whether there is a main method which can be removed.
 *
 */
public class JUnit3DataCollectorVisitor extends ASTVisitor {

	private boolean transformationPossible = true;
	private MethodDeclaration mainMethodToRemove;
	private final List<TypeDeclaration> typeDeclarationsToAnalyze = new ArrayList<>();
	private final List<SimpleType> superClassSimpleTypes = new ArrayList<>();
	private final List<MethodDeclaration> methodDeclarationsToAnalyze = new ArrayList<>();
	private final List<MethodInvocation> methodInvocationsToAnalyze = new ArrayList<>();

	static Optional<MethodDeclaration> findMainMethodToRemove(CompilationUnit compilationUnit) {
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		List<MethodDeclaration> allMethodDeclarations = methodDeclarationsCollectorVisitor.getMethodDeclarations();
		MethodDeclaration mainMethodDeclaration = allMethodDeclarations
			.stream()
			.filter(methodDeclaration -> MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit,
					methodDeclaration))
			.findFirst()
			.orElse(null);

		if (mainMethodDeclaration != null) {
			ITypeBinding declaringClass = mainMethodDeclaration.resolveBinding()
				.getDeclaringClass();
			try {
				if (findMainMethodMatches(declaringClass).isEmpty()) {
					return Optional.of(mainMethodDeclaration);
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		mainMethodToRemove = findMainMethodToRemove(node).orElse(null);
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !UnexpectedJunit3References.isUnexpectedJUnitQualifiedName(packageName);
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		typeDeclarationsToAnalyze.add(node);
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		if (node.getLocationInParent() == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY) {
			superClassSimpleTypes.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (mainMethodToRemove != null && mainMethodToRemove == node) {
			return false;
		}
		methodDeclarationsToAnalyze.add(node);
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		methodInvocationsToAnalyze.add(node);
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	private boolean analyzeName(Name name) {
		if (name.getLocationInParent() == PackageDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == ImportDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == TypeDeclaration.NAME_PROPERTY
				|| (name.getLocationInParent() == SimpleType.NAME_PROPERTY && name.getParent()
					.getLocationInParent() == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY)
				|| name.getLocationInParent() == MethodDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == MethodInvocation.NAME_PROPERTY
				|| name.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY
				|| name.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == BreakStatement.LABEL_PROPERTY) {
			return true;
		}

		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		ITypeBinding typeBinding = null;
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			typeBinding = methodBinding.getDeclaringClass();
		}

		if (binding.getKind() == IBinding.TYPE) {
			typeBinding = (ITypeBinding) binding;
		}

		if (binding.getKind() == IBinding.ANNOTATION) {
			IAnnotationBinding annotationBinding = (IAnnotationBinding) binding;
			typeBinding = annotationBinding.getAnnotationType();
		}

		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			IMemberValuePairBinding memberValuePairBinding = (IMemberValuePairBinding) binding;
			IMethodBinding methodBinding = memberValuePairBinding.getMethodBinding();
			typeBinding = methodBinding.getDeclaringClass();
		}

		if (typeBinding != null) {
			return !UnexpectedJunit3References.hasUnexpectedJUnitReference(typeBinding);
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding variableTypeBinding = variableBinding.getVariableDeclaration()
				.getType();
			if (UnexpectedJunit3References.hasUnexpectedJUnitReference(variableTypeBinding)) {
				return false;
			}
			if (variableBinding.isField()) {
				ITypeBinding fieldDeclaringClass = variableBinding.getDeclaringClass();
				if (fieldDeclaringClass != null
						&& UnexpectedJunit3References.hasUnexpectedJUnitReference(fieldDeclaringClass)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}

	public List<TypeDeclaration> getTypeDeclarationsToAnalyze() {
		return typeDeclarationsToAnalyze;
	}

	public List<SimpleType> getSuperClassSimpleTypesToAnalyze() {
		return superClassSimpleTypes;
	}

	public List<MethodDeclaration> getMethodDeclarationsToAnalyze() {
		return methodDeclarationsToAnalyze;
	}

	public List<MethodInvocation> getMethodInvocationsToAnalyze() {
		return methodInvocationsToAnalyze;
	}

	public boolean isTransformationPossible() {
		return transformationPossible;
	}
}
