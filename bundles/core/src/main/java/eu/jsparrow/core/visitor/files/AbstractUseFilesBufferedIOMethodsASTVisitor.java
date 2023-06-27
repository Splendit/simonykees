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

import eu.jsparrow.core.markers.common.UseFilesBufferedIOMethodsEvent;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
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
public abstract class AbstractUseFilesBufferedIOMethodsASTVisitor extends AbstractAddImportASTVisitor
		implements UseFilesBufferedIOMethodsEvent {
	private final String bufferedIOQualifiedTypeName;
	private final String fileIOQualifiedTypeName;
	private final String newBufferedIOMethodName;

	protected AbstractUseFilesBufferedIOMethodsASTVisitor(String bufferedIOQualifiedTypeName,
			String fileIOQualifiedTypeName,
			String newBufferedIOMethodName) {
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
		verifyImport(compilationUnit, java.nio.file.Paths.class.getName());
		verifyImport(compilationUnit, java.nio.charset.Charset.class.getName());
		verifyImport(compilationUnit, java.nio.file.Files.class.getName());
		return continueVisiting;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		ITypeBinding typeBinding = fragment.getName()
			.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, bufferedIOQualifiedTypeName)) {
			return true;
		}

		Expression initializer = fragment.getInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(initializer, bufferedIOQualifiedTypeName)) {
			return true;
		}

		ClassInstanceCreation newBufferedIO = (ClassInstanceCreation) initializer;

		Expression bufferedIOArgument = findBufferedIOArgument(newBufferedIO).orElse(null);
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

	private Optional<Expression> findBufferedIOArgument(ClassInstanceCreation classInstanceCreation) {

		return ASTNodeUtil
			.findSingletonListElement(classInstanceCreation.arguments(), Expression.class)
			.filter(bufferedIOArg -> ClassRelationUtil.isContentOfType(bufferedIOArg.resolveTypeBinding(),
					fileIOQualifiedTypeName));
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
			.map(charSet -> new TransformationData(newBufferedIO, pathExpressions, charSet,
					fileIOResourceDeclarationFragment))
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
		addMarkerEvent(bufferedIOInstanceCreation);

	}

	private MethodInvocation createFilesNewBufferedIOMethodInvocation(TransformationData transformationData) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(java.nio.file.Paths.class.getName(),
				transformationData.getBufferedIOInstanceCreation());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		MethodInvocation pathsGet = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName("get"), pathsGetArguments); //$NON-NLS-1$

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(java.nio.charset.Charset.class.getName(),
					transformationData.getBufferedIOInstanceCreation());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, "defaultCharset"); //$NON-NLS-1$
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(charset);
		Name filesTypeName = addImport(java.nio.file.Files.class.getName(),
				transformationData.getBufferedIOInstanceCreation());
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName(newBufferedIOMethodName), arguments);
	}
}
