package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.4.0
 */
public class UseJavaRecordsASTVisitor extends AbstractASTRewriteASTVisitor {

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
		if (Modifier.isAbstract(modifiers)) {
			return false;
		}
		
		
		if (typeDeclaration.getParent() == getCompilationUnit() && Modifier.isFinal(modifiers)) {
			return true;
		}

		TypeDeclarationStatement typeDeclarationStatement = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				TypeDeclarationStatement.class);
		if (typeDeclarationStatement != null) {
			if (typeDeclaration.isLocalTypeDeclaration()) {
				if (Modifier.isFinal(modifiers) ||
						isEffectivelyFinal(typeDeclaration, typeDeclarationStatement.getParent())) {
					NonStaticReferencesVisitor nonStaticReferencesVisitor = new NonStaticReferencesVisitor(
							typeDeclaration);
					typeDeclaration.accept(nonStaticReferencesVisitor);
					return !nonStaticReferencesVisitor.isUnsupportedReferenceExisting();
				}
				return false;
			}
			if (Modifier.isStatic(modifiers)) {
				if (Modifier.isFinal(modifiers)) {
					return true;
				}
				return isEffectivelyFinal(typeDeclaration, typeDeclarationStatement.getParent());
			}
			return false;
		}
		
		AnonymousClassDeclaration anonymousClassDeclaration = ASTNodeUtil.getSpecificAncestor(typeDeclaration, AnonymousClassDeclaration.class);
		if(anonymousClassDeclaration != null) {
			if (Modifier.isStatic(modifiers)) {
				if (Modifier.isFinal(modifiers)) {
					return true;
				}
				return isEffectivelyFinal(typeDeclaration, anonymousClassDeclaration);
			}
			return false;
		}

		if (typeDeclaration.getLocationInParent() == TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
			if (!Modifier.isStatic(modifiers)) {
				return false;
			}
			if (Modifier.isFinal(modifiers)) {
				return true;
			}
			if (!Modifier.isPrivate(modifiers)) {
				return false;
			}
			return isEffectivelyFinal(typeDeclaration, getCompilationUnit());
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
