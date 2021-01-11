package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
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
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Parent class for visitors which replace the initializations of
 * {@link java.io.BufferedReader}-objects or
 * {@link java.io.BufferedWriter}-objects by the corresponding methods of
 * {@link java.nio.file.Files}.
 * 
 * @since 3.22.0
 *
 */
abstract class AbstractUseFilesBufferedIOMethodsASTVisitor extends AbstractAddImportASTVisitor {
	private final String bufferedIOQualifiedTypeName;
	private final String fileIOQualifiedTypeName;
	private final String newBufferedIOMethodName;

	public AbstractUseFilesBufferedIOMethodsASTVisitor(String bufferedIOQualifiedTypeName,
			String fileIOQualifiedTypeName,
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
		verifyImport(compilationUnit, FilesUtil.PATHS_QUALIFIED_NAME);
		verifyImport(compilationUnit, FilesUtil.CHARSET_QUALIFIED_NAME);
		verifyImport(compilationUnit, FilesUtil.FILES_QUALIFIED_NAME);
		return continueVisiting;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		ClassInstanceCreation newBufferedIO = findBufferIOInstanceCreationAsInitializer(fragment).orElse(null);
		if (newBufferedIO == null) {
			return true;
		}

		Expression bufferedIOArgument = FilesUtil.findBufferedIOArgument(newBufferedIO, fileIOQualifiedTypeName)
			.orElse(null);
		if (bufferedIOArgument == null) {
			return true;
		}

		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (bufferedIOArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& newBufferedIOArgumentsAnalyzer.analyzeInitializer((ClassInstanceCreation) bufferedIOArgument)) {

			transform(createTransformationData(newBufferedIOArgumentsAnalyzer, newBufferedIO));

		} else if (bufferedIOArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			createTransformationDataUsingFileIOResource(fragment, newBufferedIO, (SimpleName) bufferedIOArgument)
				.ifPresent(this::transform);
		}
		return true;
	}

	TransformationData createTransformationData(NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer,
			ClassInstanceCreation newBufferedIO) {
		Expression charsetExpression = newBufferedIOArgumentsAnalyzer.getCharsetExpression()
			.orElse(null);
		if (charsetExpression != null) {
			return new TransformationData(newBufferedIO, newBufferedIOArgumentsAnalyzer.getPathExpressions(),
					charsetExpression);
		}
		return new TransformationData(newBufferedIO, newBufferedIOArgumentsAnalyzer.getPathExpressions());
	}

	private Optional<TransformationData> createTransformationDataUsingFileIOResource(
			VariableDeclarationFragment fragment,
			ClassInstanceCreation newBufferedIO, SimpleName bufferedIOArgSimpleName) {

		TryStatement tryStatement;
		ASTNode fragmentParent = fragment.getParent();
		if (fragment.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentParent.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY) {
			tryStatement = (TryStatement) fragmentParent.getParent();
		} else {
			return Optional.empty();
		}

		TryResourceAnalyzer fileIOResourceAnalyzer = new TryResourceAnalyzer();
		if (!fileIOResourceAnalyzer.analyzeResourceUsedOnce(tryStatement, bufferedIOArgSimpleName)) {
			return Optional.empty();
		}

		VariableDeclarationFragment fileIOResourceDeclarationFragment = fileIOResourceAnalyzer.getResourceFragment();

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(fileIOQualifiedTypeName);
		if (!fileIOAnalyzer.analyzeFileIO(fileIOResourceDeclarationFragment)) {
			return Optional.empty();
		}

		List<Expression> pathExpressions = fileIOAnalyzer.getPathExpressions();
		TransformationData transformationData = fileIOAnalyzer.getCharset()
			.map(charSet -> new TransformationData(newBufferedIO, pathExpressions, charSet, fileIOResourceDeclarationFragment))
			.orElse(new TransformationData(newBufferedIO, pathExpressions, fileIOResourceDeclarationFragment));
		return Optional.of(transformationData);
	}

	private void transform(TransformationData transformationData) {
		Optional<VariableDeclarationFragment> optionalFileIOResource = transformationData.getFileIOResource();
		ClassInstanceCreation bufferedIOInstanceCreation = transformationData.getBufferedIOInstanceCreation();
		optionalFileIOResource.ifPresent(resource -> astRewrite.remove(resource.getParent(), null));
		MethodInvocation filesNewBufferedIO = createFilesNewBufferedIOMethodInvocation(transformationData);
		astRewrite.replace(bufferedIOInstanceCreation, filesNewBufferedIO, null);
		onRewrite();
	}

	private MethodInvocation createFilesNewBufferedIOMethodInvocation(TransformationData transformationData) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME,
				transformationData.getBufferedIOInstanceCreation());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		MethodInvocation pathsGet = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName(FilesUtil.GET), pathsGetArguments);

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(FilesUtil.CHARSET_QUALIFIED_NAME,
					transformationData.getBufferedIOInstanceCreation());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, FilesUtil.DEFAULT_CHARSET);
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(charset);
		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getBufferedIOInstanceCreation());
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName(newBufferedIOMethodName), arguments);
	}

	private Optional<ClassInstanceCreation> findBufferIOInstanceCreationAsInitializer(
			VariableDeclarationFragment fragment) {
		SimpleName name = fragment.getName();
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, bufferedIOQualifiedTypeName)) {
			return Optional.empty();
		}
		return FilesUtil.findClassInstanceCreationAsInitializer(fragment, bufferedIOQualifiedTypeName);
	}
}
