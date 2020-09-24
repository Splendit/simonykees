package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

/**
 * Replaces the initializations of {@link java.io.BufferedReader} objects with
 * the non-blocking alternative
 * {@link java.nio.file.Files#newBufferedReader(java.nio.file.Path, java.nio.charset.Charset)}.
 * 
 * For example, the following code:
 * <p>
 * {@code BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("path/to/file")));}
 * <p>
 * is transformed to:
 * <p>
 * {@code BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("path/to/file"), Charset.defaultCharset());}
 * <p>
 * 
 * @since 3.21.0
 *
 */
public class UseFilesBufferedReaderASTVisitor extends AbstractUseFilesMethodsASTVisitor {

	private static final String BUFFERED_READER_QUALIFIED_NAME = java.io.BufferedReader.class.getName();
	private static final String NEW_BUFFERED_READER = "newBufferedReader"; //$NON-NLS-1$

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		ClassInstanceCreation newBufferedReader = findClassInstanceCreationAsInitializer(fragment,
				BUFFERED_READER_QUALIFIED_NAME);
		if (newBufferedReader == null) {
			return true;
		}
		Expression bufferedReaderArg = findFirstArgumentOfType(newBufferedReader, java.io.FileReader.class.getName());
		if (bufferedReaderArg == null) {
			return true;
		}

		NewBufferedIOArgumentsAnalyzer analyzer = new NewBufferedIOArgumentsAnalyzer();
		if (bufferedReaderArg.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& analyzer.analyzeInitializer((ClassInstanceCreation) bufferedReaderArg)) {

			AST ast = fragment.getAST();
			List<Expression> pathExpressions = analyzer.getPathExpressions();
			Expression charset = analyzer.getCharset()
				.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
				.orElse(createDefaultCharsetExpression(ast));

			MethodInvocation filesNewBufferedReader = createFilesNewBufferedIOMethodInvocation(ast, pathExpressions,
					charset, NEW_BUFFERED_READER);

			astRewrite.replace(newBufferedReader, filesNewBufferedReader, null);
			onRewrite();
		} else if (isDeclarationInTWRHeader(fragment, bufferedReaderArg)) {
			VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) fragment
				.getParent();
			TryStatement tryStatement = (TryStatement) declarationExpression.getParent();
			VariableDeclarationFragment fileReaderResource = findFileReaderResource(bufferedReaderArg,
					tryStatement).orElse(null);
			if (fileReaderResource == null) {
				return true;
			}

			FileReaderAnalyzer fileReaderAnalyzer = new FileReaderAnalyzer();
			if (!fileReaderAnalyzer.analyzeFileReader((VariableDeclarationExpression) fileReaderResource.getParent())) {
				return true;
			}

			boolean isUsedInTryBody = hasUsagesOn(tryStatement.getBody(), fileReaderResource.getName());
			if (isUsedInTryBody) {
				return true;
			}

			// Now the transformation happens
			AST ast = tryStatement.getAST();
			Expression charset = fileReaderAnalyzer.getCharset()
				.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
				.orElse(createDefaultCharsetExpression(ast));
			List<Expression> pathArguments = fileReaderAnalyzer.getPathExpressions();

			MethodInvocation filesNewBufferedReader = createFilesNewBufferedIOMethodInvocation(ast,
					pathArguments, charset, NEW_BUFFERED_READER);
			astRewrite.remove(fileReaderResource.getParent(), null);
			astRewrite.replace(newBufferedReader, filesNewBufferedReader, null);
			onRewrite();
		}
		return true;
	}

	protected boolean isDeclarationInTWRHeader(VariableDeclarationFragment fragment, Expression bufferedReaderArg) {
		ASTNode fragmentParent = fragment.getParent();
		return bufferedReaderArg.getNodeType() == ASTNode.SIMPLE_NAME
				&& fragment.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentParent.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY;
	}

	private Optional<VariableDeclarationFragment> findFileReaderResource(Expression bufferedReaderArg,
			TryStatement tryStatement) {
		List<VariableDeclarationExpression> resources = ASTNodeUtil
			.convertToTypedList(tryStatement.resources(), VariableDeclarationExpression.class);
		return resources.stream()
			.flatMap(resource -> ASTNodeUtil
				.convertToTypedList(resource.fragments(), VariableDeclarationFragment.class)
				.stream())
			.filter(resource -> resource.getName()
				.getIdentifier()
				.equals(((SimpleName) bufferedReaderArg).getIdentifier()))
			.findFirst();
	}




	private boolean hasUsagesOn(Block body, SimpleName fileReaderName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileReaderName);
		body.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}

}
