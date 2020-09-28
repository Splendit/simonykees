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
 * Parent class for visitors which replace the initializations of
 * {@link java.io.BufferedReader}-objects or
 * {@link java.io.BufferedWriter}-objects by the corresponding methods of
 * {@link java.nio.file.Files}.
 * 
 * @since 3.22.0
 *
 */
abstract class AbstractUseFilesMethodsASTVisitor extends AbstractAddImportASTVisitor {
	protected static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	protected static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	protected static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();

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

	/**
	 * 
	 * @return If the variable declared by the given
	 *         {@link VariableDeclarationFragment} has the specified type and is
	 *         also initialized with a constructor of the specified type, then
	 *         the corresponding {@link ClassInstanceCreation} is returned.
	 *         Otherwise, null is returned.
	 */
	protected ClassInstanceCreation findClassInstanceCreationAsInitializer(VariableDeclarationFragment fragment,
			String qualifiedClassName) {
		SimpleName name = fragment.getName();
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, qualifiedClassName)) {
			return null;
		}

		Expression initializer = fragment.getInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(initializer, qualifiedClassName)) {
			return null;
		}
		return (ClassInstanceCreation) initializer;
	}

	protected Expression findFirstArgumentOfType(ClassInstanceCreation classInstanceCreation,
			String qualifiedTypeName) {

		List<Expression> newBufferedIOArgs = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(),
				Expression.class);
		if (newBufferedIOArgs.size() != 1) {
			return null;
		}
		Expression bufferedIOArg = newBufferedIOArgs.get(0);
		ITypeBinding firstArgType = bufferedIOArg.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgType, qualifiedTypeName)) {
			return null;
		}
		return bufferedIOArg;
	}

	protected boolean isDeclarationInTWRHeader(VariableDeclarationFragment fragment, Expression bufferedIOArg) {
		ASTNode fragmentParent = fragment.getParent();
		return bufferedIOArg.getNodeType() == ASTNode.SIMPLE_NAME
				&& fragment.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentParent.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY;
	}

	protected TransformationData createAnalysisDataUsingFileIOResource(VariableDeclarationFragment fragment,
			ClassInstanceCreation newBufferedIO, Expression bufferedIOArg, FileIOAnalyzer fileIOAnalyzer) {
		VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) fragment
				.getParent();
		TryStatement tryStatement = (TryStatement) declarationExpression.getParent();

		VariableDeclarationFragment fileIOResource = findFileIOResource(bufferedIOArg,
				tryStatement).orElse(null);
		if (fileIOResource == null) {
			return null;
		}

		if (!fileIOAnalyzer.analyzeFileIO((VariableDeclarationExpression) fileIOResource.getParent())) {
			return null;
		}

		boolean isUsedInTryBody = hasUsagesOn(tryStatement.getBody(), fileIOResource.getName());
		if (isUsedInTryBody) {
			return null;
		}

		// Now the transformation happens
		List<Expression> pathExpressions = fileIOAnalyzer.getPathExpressions();
		Optional<Expression> optionalCharSet = fileIOAnalyzer.getCharset();
		return new TransformationData(newBufferedIO, pathExpressions, optionalCharSet,
				fileIOResource);
	}

	protected Optional<VariableDeclarationFragment> findFileIOResource(Expression bufferedIOArg,
			TryStatement tryStatement) {
		List<VariableDeclarationExpression> resources = ASTNodeUtil
			.convertToTypedList(tryStatement.resources(), VariableDeclarationExpression.class);
		return resources.stream()
			.flatMap(resource -> ASTNodeUtil
				.convertToTypedList(resource.fragments(), VariableDeclarationFragment.class)
				.stream())
			.filter(resource -> resource.getName()
				.getIdentifier()
				.equals(((SimpleName) bufferedIOArg).getIdentifier()))
			.findFirst();
	}

	protected boolean hasUsagesOn(Block body, SimpleName fileIOName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileIOName);
		body.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}

	protected void transform(TransformationData transformationData, String newBufferedIOMethodName) {
		Optional<VariableDeclarationFragment> optionalFileIOResource = transformationData.getFileIOResource();
		List<Expression> pathExpressions = transformationData.getPathExpressions();
		Optional<Expression> optionalCharSet = transformationData.getCharSet();
		ClassInstanceCreation bufferedIOInstanceCreation = transformationData.getBufferedIOInstanceCreation();
		if (optionalFileIOResource.isPresent()) {
			VariableDeclarationFragment fileIOResource = optionalFileIOResource.get();
			astRewrite.remove(fileIOResource.getParent(), null);
		}
		MethodInvocation filesNewBufferedIO = createFilesNewBufferedIOMethodInvocation(pathExpressions,
				optionalCharSet, newBufferedIOMethodName);
		astRewrite.replace(bufferedIOInstanceCreation, filesNewBufferedIO, null);
		onRewrite();
	}

	private Expression createDefaultCharSetExpression(AST ast) {
		MethodInvocation defaultCharset = ast.newMethodInvocation();
		defaultCharset.setExpression(ast.newName(findTypeNameForStaticMethodInvocation(CHARSET_QUALIFIED_NAME)));
		defaultCharset.setName(ast.newSimpleName("defaultCharset")); //$NON-NLS-1$
		return defaultCharset;
	}

	protected MethodInvocation createFilesNewBufferedIOMethodInvocation(List<Expression> pathExpressions,
			Optional<Expression> optionalCharSet, String newBufferdIOMethodName) {
		AST ast = astRewrite.getAST();
		Expression charset = optionalCharSet
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(createDefaultCharSetExpression(ast));
		MethodInvocation pathsGet = ast.newMethodInvocation();
		String pathsIdentifier = findTypeNameForStaticMethodInvocation(PATHS_QUALIFIED_NAME);
		pathsGet.setExpression(ast.newName(pathsIdentifier));
		pathsGet.setName(ast.newSimpleName("get")); //$NON-NLS-1$
		@SuppressWarnings("unchecked")
		List<Expression> pathsGetParameters = pathsGet.arguments();
		pathExpressions
			.forEach(pathArgument -> pathsGetParameters.add((Expression) astRewrite.createCopyTarget(pathArgument)));

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(charset);
		Expression filesExpression = ast.newName(findTypeNameForStaticMethodInvocation(FILES_QUALIFIED_NAME));
		return NodeBuilder.newMethodInvocation(ast, filesExpression,
				ast.newSimpleName(newBufferdIOMethodName), arguments);
	}
}
