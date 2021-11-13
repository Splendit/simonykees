package eu.jsparrow.rules.java16.javarecords;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeParameter;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.4.0
 */
public class UseJavaRecordsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		TypeVisibilityAnalyzer visibilityAnalyzer = new TypeVisibilityAnalyzer();

		if (visibilityAnalyzer.analyzeEffectiveVisibility(typeDeclaration)) {

			List<Modifier> classModifiers = ASTNodeUtil
				.convertToTypedList(typeDeclaration.modifiers(), Modifier.class);
			boolean allClassModifiersSupported = classModifiers
				.stream()
				.allMatch(this::isSupportedClassModifier);

			if (allClassModifiersSupported && isSupportedClassDeclaration(typeDeclaration)) {

				BodyDeclarationsAnalyzer bodyDeclarationsAnalyzer = new BodyDeclarationsAnalyzer();
				bodyDeclarationsAnalyzer.analyzeBodyDeclarations(typeDeclaration)
					.ifPresent(bodyDeclarationAnalysisResult -> {
						List<Annotation> annotations = ASTNodeUtil.convertToTypedList(typeDeclaration.modifiers(),
								Annotation.class);
						List<Modifier> recordModifiers = classModifiers
							.stream()
							.filter(modifier -> !modifier.isStatic())
							.filter(modifier -> !modifier.isFinal())
							.collect(Collectors.toList());

						transform(annotations, recordModifiers, bodyDeclarationAnalysisResult);
					});
			}
		}
		return true;
	}

	private boolean isSupportedClassModifier(Modifier modifier) {
		return modifier.isPrivate() || modifier.isStatic() || modifier.isFinal() || modifier.isStrictfp();
	}

	private boolean isSupportedClassDeclaration(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isInterface()) {
			return false;
		}

		if (typeDeclaration.getSuperclassType() != null) {
			return false;
		}

		int modifiers = typeDeclaration.getModifiers();

		if (typeDeclaration.getParent() != getCompilationUnit() && !Modifier.isStatic(modifiers)) {
			NonStaticReferencesVisitor nonStaticReferencesVisitor = new NonStaticReferencesVisitor(
					getCompilationUnit(), typeDeclaration);
			typeDeclaration.accept(nonStaticReferencesVisitor);
			if (nonStaticReferencesVisitor.isUnsupportedReferenceExisting()) {
				return false;
			}
		}

		if (Modifier.isFinal(modifiers)) {
			return true;
		}

		ASTNode scopeHidingTypeDeclaration = findScopeHidingTypeDeclaration(typeDeclaration).orElse(null);
		if (scopeHidingTypeDeclaration != null) {
			return isEffectivelyFinal(typeDeclaration, scopeHidingTypeDeclaration);
		}

		if (Modifier.isPrivate(modifiers)) {
			return isEffectivelyFinal(typeDeclaration, getCompilationUnit());
		}

		return false;
	}

	private Optional<ASTNode> findScopeHidingTypeDeclaration(TypeDeclaration typeDeclaration) {
		TypeDeclarationStatement typeDeclarationStatement = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				TypeDeclarationStatement.class);
		if (typeDeclarationStatement != null) {
			return Optional.of(typeDeclarationStatement.getParent());
		}

		AnonymousClassDeclaration anonymousClassDeclaration = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				AnonymousClassDeclaration.class);
		if (anonymousClassDeclaration != null) {
			return Optional.of(anonymousClassDeclaration);
		}

		return Optional.empty();
	}

	private boolean isEffectivelyFinal(TypeDeclaration typeDeclaration, ASTNode scope) {
		SubclassesVisitor subclassesVisitor = new SubclassesVisitor(typeDeclaration);
		scope.accept(subclassesVisitor);
		return !subclassesVisitor.isSubclassExisting();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void transform(List<Annotation> annotations, List<Modifier> modifiers,
			BodyDeclarationsAnalysisResult analysisResult) {
		TypeDeclaration typeDeclarationToReplace = analysisResult.getTypeDeclarationToReplace();
		AST ast = astRewrite.getAST();
		RecordDeclaration recordDeclaration = ast.newRecordDeclaration();
		SimpleName recordName = (SimpleName) astRewrite.createCopyTarget(typeDeclarationToReplace.getName());
		recordDeclaration.setName(recordName);

		List<TypeParameter> classTypeParameters = ASTNodeUtil
			.convertToTypedList(typeDeclarationToReplace.typeParameters(), TypeParameter.class);

		List recordTypeParameters = recordDeclaration.typeParameters();
		classTypeParameters.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(recordTypeParameters::add);

		List<Type> classSuperInterfaces = ASTNodeUtil.convertToTypedList(typeDeclarationToReplace.superInterfaceTypes(),
				Type.class);

		List recordSuperInterfaces = recordDeclaration.superInterfaceTypes();
		classSuperInterfaces.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(recordSuperInterfaces::add);

		List recordModifiers = recordDeclaration.modifiers();
		annotations.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(recordModifiers::add);

		modifiers.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(recordModifiers::add);

		List recordComponents = recordDeclaration.recordComponents();
		analysisResult.getCanonicalConstructorParameters()
			.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(recordComponents::add);

		List recordBodyDeclarations = recordDeclaration.bodyDeclarations();
		analysisResult.getRecordBodyDeclarations()
			.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(recordBodyDeclarations::add);

		astRewrite.replace(typeDeclarationToReplace, recordDeclaration, null);
		onRewrite();
	}
}
