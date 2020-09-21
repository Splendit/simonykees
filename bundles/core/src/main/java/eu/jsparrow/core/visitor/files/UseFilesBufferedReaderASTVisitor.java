package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

/**
 * Replaces the initializations of {@link java.io.BufferedReader} objects with
 * the non-blocking alternative
 * {@link java.nio.file.Files#newBufferedReader(java.nio.file.Path, java.nio.charset.Charset)}.
 * 
 * For example, the following code:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("path/to/file")));
 * }
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("path/to/file"), Charset.defaultCharset());
 * }
 * </pre>
 * 
 * @since 3.21.0
 *
 */
public class UseFilesBufferedReaderASTVisitor extends AbstractAddImportASTVisitor {

	private static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	private static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	private static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();
	private static final String BUFFERED_READER_QUALIFIED_NAME = java.io.BufferedReader.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}
		verifyImport(compilationUnit, PATHS_QUALIFIED_NAME);
		verifyImport(compilationUnit, FILES_QUALIFIED_NAME);
		verifyImport(compilationUnit, CHARSET_QUALIFIED_NAME);
		return continueVisiting;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		SimpleName name = fragment.getName();
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, BUFFERED_READER_QUALIFIED_NAME)) {
			return true;
		}

		Expression initializer = fragment.getInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(initializer, BUFFERED_READER_QUALIFIED_NAME)) {
			return true;
		}
		ClassInstanceCreation newBufferedReader = (ClassInstanceCreation) initializer;

		List<Expression> newBufferedReaderArgs = ASTNodeUtil.convertToTypedList(newBufferedReader.arguments(),
				Expression.class);
		if (newBufferedReaderArgs.size() != 1) {
			return true;
		}

		Expression bufferedReaderArg = newBufferedReaderArgs.get(0);
		ITypeBinding firstArgType = bufferedReaderArg.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgType, java.io.FileReader.class.getName())) {
			return true;
		}

		NewBufferedReaderAnalyzer analyzer = new NewBufferedReaderAnalyzer();
		if (bufferedReaderArg.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& analyzer.isInitializedWithNewReader((ClassInstanceCreation) bufferedReaderArg)) {

			AST ast = fragment.getAST();
			List<Expression> pathExpressions = analyzer.getPathExpressions();
			Expression charset = analyzer.getCharset()
				.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
				.orElse(createDefaultCharsetExpression(ast));

			MethodInvocation filesNewBufferedReader = createFilesNewBufferedReaderExpression(ast, pathExpressions,
					charset);

			astRewrite.replace(initializer, filesNewBufferedReader, null);
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

			MethodInvocation filesNewBufferedReader = createFilesNewBufferedReaderExpression(ast,
					pathArguments, charset);
			astRewrite.remove(fileReaderResource.getParent(), null);
			astRewrite.replace(initializer, filesNewBufferedReader, null);
			onRewrite();
		}
		return true;
	}

	private boolean isDeclarationInTWRHeader(VariableDeclarationFragment fragment, Expression bufferedReaderArg) {
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

	private MethodInvocation createFilesNewBufferedReaderExpression(AST ast, List<Expression> pathExpressions,
			Expression charset) {
		MethodInvocation pathsGet = ast.newMethodInvocation();
		addImport(PATHS_QUALIFIED_NAME);
		pathsGet.setExpression(findTypeName(PATHS_QUALIFIED_NAME));
		pathsGet.setName(ast.newSimpleName("get")); //$NON-NLS-1$
		@SuppressWarnings("unchecked")
		List<Expression> pathsGetParameters = pathsGet.arguments();
		pathExpressions
			.forEach(pathArgument -> pathsGetParameters.add((Expression) astRewrite.createCopyTarget(pathArgument)));

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(charset);
		addImport(FILES_QUALIFIED_NAME);
		Expression filesExpression = findTypeName(FILES_QUALIFIED_NAME);
		return NodeBuilder.newMethodInvocation(ast, filesExpression,
				ast.newSimpleName("newBufferedReader"), arguments); //$NON-NLS-1$
	}

	private Expression createDefaultCharsetExpression(AST ast) {
		MethodInvocation defaultCharset = ast.newMethodInvocation();
		addImport(CHARSET_QUALIFIED_NAME);
		defaultCharset.setExpression(findTypeName(CHARSET_QUALIFIED_NAME));
		defaultCharset.setName(ast.newSimpleName("defaultCharset")); //$NON-NLS-1$
		return defaultCharset;
	}

	private boolean hasUsagesOn(Block body, SimpleName fileReaderName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileReaderName);
		body.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}

}
