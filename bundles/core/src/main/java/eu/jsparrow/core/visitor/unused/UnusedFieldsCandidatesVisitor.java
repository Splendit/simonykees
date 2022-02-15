package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class UnusedFieldsCandidatesVisitor extends ASTVisitor {

	private CompilationUnit compilationUnit;
	private Map<String, Boolean> options;

	private List<UnusedFieldWrapper> unusedPrivateFields = new ArrayList<>();
	private List<NonPrivateUnusedFieldCandidate> nonPrivateCandidates = new ArrayList<>();

	public UnusedFieldsCandidatesVisitor(Map<String, Boolean> options) {
		this.options = options;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {

		if (!hasSelectedAccessModifier(fieldDeclaration)) {
			return true;
		}

		boolean hasAnnotations = hasUsefulAnnotations(fieldDeclaration);
		if (hasAnnotations) {
			return true;
		}

		if(isSerialVersionUIDDeclaration(fieldDeclaration)) {
			return false;
		}
		
		AbstractTypeDeclaration typeDeclaration = ASTNodeUtil.getSpecificAncestor(fieldDeclaration, AbstractTypeDeclaration.class);
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class);
		int modifierFlags = fieldDeclaration.getModifiers();
		for (VariableDeclarationFragment fragment : fragments) {
			boolean removeSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
			if (removeSideEffects || ExpressionWithoutSideEffectRecursive.hasNoInitializerWithSideEffect(fragment)) {
				ReferencesVisitor referencesVisitor = new ReferencesVisitor(fragment, typeDeclaration, options);
				this.compilationUnit.accept(referencesVisitor);
				if (!referencesVisitor.hasActiveReference()) {
					List<ExpressionStatement> reassignments = referencesVisitor.getReassignments();
					if (Modifier.isPrivate(modifierFlags)) {
						UnusedFieldWrapper unusedField = new UnusedFieldWrapper(compilationUnit,
								JavaAccessModifier.PRIVATE, fragment, reassignments, Collections.emptyList());
						unusedPrivateFields.add(unusedField);
					} else {
						JavaAccessModifier accessModifier = findAccessModifier(fieldDeclaration);
						NonPrivateUnusedFieldCandidate candidate = new NonPrivateUnusedFieldCandidate(fragment,
								compilationUnit, typeDeclaration, accessModifier, reassignments);
						nonPrivateCandidates.add(candidate);
					}
					/*
					 * removing multiple fragments from the same field
					 * declaration may result to incorrect changes.
					 */
					return false;

				}
			}
		}
		return true;
	}
	
	private boolean isSerialVersionUIDDeclaration(FieldDeclaration fieldDeclaration) {
		int modifierFlags = fieldDeclaration.getModifiers();
		if(Modifier.isStatic(modifierFlags) && Modifier.isFinal(modifierFlags)) {

			List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class);
			final String versionUIDName = "serialVersionUID"; //$NON-NLS-1$
			boolean matchesName = fragments.stream()
				.map(VariableDeclarationFragment::getName)
				.map(SimpleName::getIdentifier)
				.anyMatch(versionUIDName::equals); 
			
			if(matchesName) {
				Type type = fieldDeclaration.getType();
				if(type.isPrimitiveType()) {
					PrimitiveType primitiveType = (PrimitiveType)type;
					Code code = primitiveType.getPrimitiveTypeCode();
					return "long".equals(code.toString()); //$NON-NLS-1$
				}
			}
		}
		return false;
	}

	private boolean hasUsefulAnnotations(FieldDeclaration fieldDeclaration) {
		List<Annotation> annotations = ASTNodeUtil.convertToTypedList(fieldDeclaration.modifiers(), Annotation.class);
		for (Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfTypes(typeBinding,
					Arrays.asList(java.lang.Deprecated.class.getName(), java.lang.SuppressWarnings.class.getName()))) {
				return true;
			}
		}

		return false;
	}

	private boolean hasSelectedAccessModifier(FieldDeclaration fieldDeclaration) {
		int modifierFlags = fieldDeclaration.getModifiers();
		if (Modifier.isPublic(modifierFlags)) {
			return options.getOrDefault(Constants.PUBLIC_FIELDS, false);
		} else if (Modifier.isProtected(modifierFlags)) {
			return options.getOrDefault(Constants.PROTECTED_FIELDS, false);
		} else if (Modifier.isPrivate(modifierFlags)) {
			return options.getOrDefault(Constants.PRIVATE_FIELDS, false);
		} else {
			return options.getOrDefault(Constants.PACKAGE_PRIVATE_FIELDS, false);
		}
	}

	private JavaAccessModifier findAccessModifier(FieldDeclaration fieldDeclaration) {
		int modifierFlags = fieldDeclaration.getModifiers();
		if (Modifier.isPrivate(modifierFlags)) {
			return JavaAccessModifier.PRIVATE;
		} else if (Modifier.isProtected(modifierFlags)) {
			return JavaAccessModifier.PROTECTED;
		} else if (Modifier.isPublic(modifierFlags)) {
			return JavaAccessModifier.PUBLIC;
		}
		return JavaAccessModifier.PACKAGE_PRIVATE;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public List<UnusedFieldWrapper> getUnusedPrivateFields() {
		return unusedPrivateFields;
	}

	public List<NonPrivateUnusedFieldCandidate> getNonPrivateCandidates() {
		return nonPrivateCandidates;
	}

}
