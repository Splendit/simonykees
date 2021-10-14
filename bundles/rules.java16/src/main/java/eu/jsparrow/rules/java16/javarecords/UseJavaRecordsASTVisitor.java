package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
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

		BodyDeclarationsAnalyzer bodyDeclarationsAnalyzer = new BodyDeclarationsAnalyzer();
		bodyDeclarationsAnalyzer.analyzeBodyDeclarations(typeDeclaration)
			.ifPresent(this::transform);
		return true;
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
			.forEach(bodyDeclaration -> {
				recordBodyDeclarations.add(astRewrite.createCopyTarget(bodyDeclaration));
			});
		astRewrite.replace(typeDeclarationToReplace, recordDeclaration, null);
		onRewrite();
	}
}
