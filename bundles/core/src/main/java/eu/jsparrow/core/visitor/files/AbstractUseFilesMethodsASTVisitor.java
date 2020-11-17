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
import org.eclipse.jdt.core.dom.Name;
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
	private static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	private static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	private static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();

	private final String bufferedIOQualifiedTypeName;
	private final String fileIOQualifiedTypeName;
	private final String newBufferedIOMethodName;

	public AbstractUseFilesMethodsASTVisitor(String bufferedIOQualifiedTypeName, String fileIOQualifiedTypeName,
			String newBufferedIOMethodName) {
		super();
		this.bufferedIOQualifiedTypeName = bufferedIOQualifiedTypeName;
		this.fileIOQualifiedTypeName = fileIOQualifiedTypeName;
		this.newBufferedIOMethodName = newBufferedIOMethodName;
	}

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

		ClassInstanceCreation newBufferedIO = findBufferIOInstanceCreationAsInitializer(fragment);
		if (newBufferedIO == null) {
			return true;
		}
		Expression bufferedIOArgument = findBufferedIOArgument(newBufferedIO);
		if (bufferedIOArgument == null) {
			return true;
		}

		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (bufferedIOArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& newBufferedIOArgumentsAnalyzer.analyzeInitializer((ClassInstanceCreation) bufferedIOArgument)) {

			List<Expression> pathExpressions = newBufferedIOArgumentsAnalyzer.getPathExpressions();
			TransformationData transformationData = newBufferedIOArgumentsAnalyzer.getCharset()
				.map(charSet -> new TransformationData(newBufferedIO, pathExpressions, charSet))
				.orElse(new TransformationData(newBufferedIO, pathExpressions));
			transform(transformationData);

		} else if (isDeclarationInTWRHeader(fragment, bufferedIOArgument)) {
			createTransformationDataUsingFileIOResource(fragment, newBufferedIO,
					(SimpleName) bufferedIOArgument).ifPresent(this::transform);
		}

		return true;
	}

	/**
	 * 
	 * @return If the variable declared by the given
	 *         {@link VariableDeclarationFragment} has the specified type and is
	 *         also initialized with a constructor of the specified type, then
	 *         the corresponding {@link ClassInstanceCreation} is returned.
	 *         Otherwise, null is returned.
	 */
	private ClassInstanceCreation findBufferIOInstanceCreationAsInitializer(VariableDeclarationFragment fragment) {
		SimpleName name = fragment.getName();
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, bufferedIOQualifiedTypeName)) {
			return null;
		}

		Expression initializer = fragment.getInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(initializer, bufferedIOQualifiedTypeName)) {
			return null;
		}
		return (ClassInstanceCreation) initializer;
	}

	private Expression findBufferedIOArgument(ClassInstanceCreation classInstanceCreation) {

		List<Expression> newBufferedIOArgs = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(),
				Expression.class);
		if (newBufferedIOArgs.size() != 1) {
			return null;
		}
		Expression bufferedIOArg = newBufferedIOArgs.get(0);
		ITypeBinding firstArgType = bufferedIOArg.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgType, fileIOQualifiedTypeName)) {
			return null;
		}
		return bufferedIOArg;
	}

	private boolean isDeclarationInTWRHeader(VariableDeclarationFragment fragment, Expression bufferedIOArg) {
		ASTNode fragmentParent = fragment.getParent();
		return bufferedIOArg.getNodeType() == ASTNode.SIMPLE_NAME
				&& fragment.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentParent.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY;
	}

	private Optional<TransformationData> createTransformationDataUsingFileIOResource(
			VariableDeclarationFragment fragment,
			ClassInstanceCreation newBufferedIO, SimpleName bufferedIOArg) {
		VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) fragment
			.getParent();
		TryStatement tryStatement = (TryStatement) declarationExpression.getParent();

		VariableDeclarationFragment fileIOResource = FilesUtils.findVariableDeclarationFragmentAsResource(bufferedIOArg,
				tryStatement).orElse(null);
		if (fileIOResource == null) {
			return Optional.empty();
		}

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(fileIOQualifiedTypeName);
		if (!fileIOAnalyzer.analyzeFileIO((VariableDeclarationExpression) fileIOResource.getParent())) {
			return Optional.empty();
		}

		boolean isUsedInTryBody = hasUsagesOn(tryStatement.getBody(), fileIOResource.getName());
		if (isUsedInTryBody) {
			return Optional.empty();
		}

		// Now the transformation happens
		List<Expression> pathExpressions = fileIOAnalyzer.getPathExpressions();
		TransformationData transformationData = fileIOAnalyzer.getCharset()
			.map(charSet -> new TransformationData(newBufferedIO, pathExpressions, charSet, fileIOResource))
			.orElse(new TransformationData(newBufferedIO, pathExpressions, fileIOResource));
		return Optional.of(transformationData);
	}

	private boolean hasUsagesOn(Block body, SimpleName fileIOName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileIOName);
		body.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}

	private void transform(TransformationData transformationData) {
		Optional<VariableDeclarationFragment> optionalFileIOResource = transformationData.getFileIOResource();
		ClassInstanceCreation bufferedIOInstanceCreation = transformationData.getBufferedIOInstanceCreation();
		optionalFileIOResource.ifPresent(resource -> astRewrite.remove(resource.getParent(), null));
		MethodInvocation filesNewBufferedIO = createFilesNewBufferedIOMethodInvocation(transformationData);
		astRewrite.replace(bufferedIOInstanceCreation, filesNewBufferedIO, null);
		onRewrite();
	}

	private Expression createDefaultCharSetExpression(ASTNode context) {
		AST ast = context.getAST();
		MethodInvocation defaultCharset = ast.newMethodInvocation();
		Name charsetTypeName = addImport(CHARSET_QUALIFIED_NAME, context);
		defaultCharset.setExpression(charsetTypeName);
		defaultCharset.setName(ast.newSimpleName("defaultCharset")); //$NON-NLS-1$
		return defaultCharset;
	}

	private MethodInvocation createFilesNewBufferedIOMethodInvocation(TransformationData transformationData) {
		AST ast = astRewrite.getAST();
		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(createDefaultCharSetExpression(transformationData.getBufferedIOInstanceCreation()));
		MethodInvocation pathsGet = ast.newMethodInvocation();
		Name pathsTypeName = addImport(PATHS_QUALIFIED_NAME, transformationData.getBufferedIOInstanceCreation());
		pathsGet.setExpression(pathsTypeName);
		pathsGet.setName(ast.newSimpleName("get")); //$NON-NLS-1$
		@SuppressWarnings("unchecked")
		List<Expression> pathsGetParameters = pathsGet.arguments();
		List<Expression> pathExpressions = transformationData.getPathExpressions();
		pathExpressions
			.forEach(pathArgument -> pathsGetParameters.add((Expression) astRewrite.createCopyTarget(pathArgument)));

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(charset);
		Name filesTypeName = addImport(FILES_QUALIFIED_NAME, transformationData.getBufferedIOInstanceCreation());
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName(newBufferedIOMethodName), arguments);
	}
}
