package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.4.0
 */
public class UseJavaRecordsASTVisitor extends AbstractASTRewriteASTVisitor {

	/**
	 * Prototype with incomplete validation
	 */
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		if (isSupportedClassDeclaration(typeDeclaration)) {

			BodyDeclarationsAnalyzer bodyDeclarationsAnalyzer = new BodyDeclarationsAnalyzer();
			bodyDeclarationsAnalyzer.analyzeBodyDeclarations(typeDeclaration)
				.ifPresent(this::transform);

		}
		return true;
	}

	private boolean isSupportedClassDeclaration(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isInterface()) {
			return false;
		}

		if (typeDeclaration.getSuperclassType() != null) {
			return false;
		}

		int modifiers = typeDeclaration.getModifiers();
		if (typeDeclaration.getParent() == getCompilationUnit()) {
			return Modifier.isFinal(modifiers);
		}

		if (typeDeclaration.isLocalTypeDeclaration()) {
			if (Modifier.isFinal(modifiers)) {
				return true;
			}
			return isEffectivelyFinal(typeDeclaration, typeDeclaration.getParent()
				.getParent());
		}

		if (typeDeclaration.getLocationInParent() == TypeDeclaration.BODY_DECLARATIONS_PROPERTY
				&& Modifier.isStatic(modifiers)) {
			if (Modifier.isFinal(modifiers)) {
				return true;
			}
			return Modifier.isPrivate(modifiers) && isEffectivelyFinal(typeDeclaration, getCompilationUnit());
		}

		return false;

	}

	private boolean isEffectivelyFinal(TypeDeclaration typeDeclaration, ASTNode scope) {
		SubclassesVisitor subclassesVisitor = new SubclassesVisitor(typeDeclaration);
		scope.accept(subclassesVisitor);
		return !subclassesVisitor.isSubclassExisting();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void transform(BodyDeclarationsAnalysisResult analysisResult) {
		TypeDeclaration typeDeclarationToReplace = analysisResult.getTypeDeclarationToReplace();
		AST ast = astRewrite.getAST();
		RecordDeclaration recordDeclaration = ast.newRecordDeclaration();
		SimpleName recordName = (SimpleName) astRewrite.createCopyTarget(typeDeclarationToReplace.getName());
		recordDeclaration.setName(recordName);
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
