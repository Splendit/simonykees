package eu.jsparrow.core.visitor.security;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

/**
 * This visitor looks for invocations of
 * <ul>
 * <li>{@link java.io.File#createTempFile(String, String)} and</li>
 * <li>{@link java.io.File#createTempFile(String, String, java.io.File)}</li>
 * </ul>
 * and replaces them by invocations of the corresponding methods of the class
 * {@link java.nio.file.Files}.
 * 
 * @since 3.21.0
 *
 */
public class CreateTempFilesUsingJavaNIOASTVisitor extends AbstractAddImportASTVisitor {
	private static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	private static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	private static final Class<File> FILE = java.io.File.class;
	private static final Class<?> STRING = java.lang.String.class;
	private static final String CREATE_TEMP_FILE = "createTempFile"; //$NON-NLS-1$
	private final SignatureData createTempFile = new SignatureData(FILE, CREATE_TEMP_FILE, STRING, STRING);
	private final SignatureData createTempFileWithDirectory = new SignatureData(FILE, CREATE_TEMP_FILE, STRING, STRING,
			FILE);
	private final SignatureData newFileFromPath = new SignatureData(FILE, FILE.getSimpleName(), STRING);
	private final Set<String> safeTypeImports = new HashSet<>();
	private final Set<String> typesImportedOnDemand = new HashSet<>();

	@Override
	public boolean visit(CompilationUnit node) {
		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(node.imports(),
				ImportDeclaration.class);

		if (isSafeToAddImport(node, PATHS_QUALIFIED_NAME)) {
			safeTypeImports.add(PATHS_QUALIFIED_NAME);
			if (matchesTypeImportOnDemand(importDeclarations, PATHS_QUALIFIED_NAME)) {
				typesImportedOnDemand.add(PATHS_QUALIFIED_NAME);
			}
		}
		if (isSafeToAddImport(node, FILES_QUALIFIED_NAME)) {
			safeTypeImports.add(FILES_QUALIFIED_NAME);
			if (matchesTypeImportOnDemand(importDeclarations, FILES_QUALIFIED_NAME)) {
				typesImportedOnDemand.add(FILES_QUALIFIED_NAME);
			}
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		safeTypeImports.clear();
		typesImportedOnDemand.clear();
	}

	@Override
	public boolean visit(MethodInvocation node) {

		IMethodBinding methodBinding = node.resolveMethodBinding();
		List<Expression> createTempFileArguments = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class);
		TransformationData data = null;

		if (createTempFile.isEquivalentTo(methodBinding)) {
			Expression filePrefix = createTempFileArguments.get(0);
			Expression fileSuffix = createTempFileArguments.get(1);
			data = new TransformationData(filePrefix, fileSuffix);

		} else if (createTempFileWithDirectory.isEquivalentTo(methodBinding)) {
			data = createTransformationDataUsingDirectory(createTempFileArguments);
		}

		if (data != null) {
			transform(node, data);
		}
		return true;
	}

	private TransformationData createTransformationDataUsingDirectory(List<Expression> createTempFileArguments) {

		Expression filePrefix = createTempFileArguments.get(0);
		Expression fileSuffix = createTempFileArguments.get(1);
		Expression directory = createTempFileArguments.get(2);

		if (directory.getNodeType() == ASTNode.NULL_LITERAL) {
			return new TransformationData(filePrefix, fileSuffix);
		}

		if (directory.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation directoryFileInstanceCreation = (ClassInstanceCreation) directory;
			IMethodBinding constructorBinding = directoryFileInstanceCreation.resolveConstructorBinding();

			if (newFileFromPath.isEquivalentTo(constructorBinding)) {
				Expression directoryPath = ASTNodeUtil
					.convertToTypedList(directoryFileInstanceCreation.arguments(), Expression.class)
					.get(0);
				return new TransformationData(directoryPath, filePrefix, fileSuffix);
			}
		}

		if (directory.getNodeType() == ASTNode.SIMPLE_NAME && checkDirectoryVariableUsage((SimpleName) directory)) {
			return new TransformationData(directory, filePrefix, fileSuffix);
		}

		return null;
	}

	private boolean checkDirectoryVariableUsage(SimpleName directoryName) {
		VariableDeclarationFragment localVariableDeclarationFragment = findLocalVariableDeclarationFragment(
				directoryName);

		if (localVariableDeclarationFragment == null) {
			return false;
		}
		Expression initializer = localVariableDeclarationFragment.getInitializer();
		if (initializer == null) {
			return false;
		}
		if (initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}
		LocalVariableUsagesASTVisitor usagesVisitor = new LocalVariableUsagesASTVisitor(directoryName);
		Block block = ASTNodeUtil.getSpecificAncestor(localVariableDeclarationFragment, Block.class);
		block.accept(usagesVisitor);

		List<SimpleName> usages = usagesVisitor.getUsages();
		for (SimpleName usage : usages) {
			if (usage == directoryName) {
				break;
			}
			if (usage.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
				return false;
			}
		}
		return true;
	}

	private VariableDeclarationFragment findLocalVariableDeclarationFragment(SimpleName directoryName) {
		IBinding fileVariableBinding = directoryName.resolveBinding();
		if (fileVariableBinding.getKind() != IBinding.VARIABLE) {
			return null;
		}
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(directoryName, CompilationUnit.class);
		ASTNode declarationNode = compilationUnit.findDeclaringNode(fileVariableBinding);
		if (declarationNode == null) {
			return null;
		}
		if (declarationNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return null;
		}
		if (declarationNode.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return null;
		}
		return (VariableDeclarationFragment) declarationNode;
	}

	private void transform(MethodInvocation replacedCreateTempFileInvocation, TransformationData data) {

		AST ast = replacedCreateTempFileInvocation.getAST();
		MethodInvocation createTempFileInvocation = ast.newMethodInvocation();
		createTempFileInvocation.setName(ast.newSimpleName(CREATE_TEMP_FILE));
		String typeNameFiles = findTypeNameForStaticMethodInvocation(FILES_QUALIFIED_NAME);
		createTempFileInvocation.setExpression(ast.newName(typeNameFiles));

		@SuppressWarnings("unchecked")
		List<Expression> createTempFileArguments = createTempFileInvocation.arguments();
		Optional<Expression> optionalDirectory = data.getDirectory();
		if (optionalDirectory.isPresent()) {
			Expression directory = optionalDirectory.get();
			Expression directoryCopyTarget = (Expression) astRewrite.createCopyTarget(directory);
			MethodInvocation pathExpression = ast.newMethodInvocation();
			boolean isStringPath = directory.resolveTypeBinding()
				.getQualifiedName()
				.equals(STRING.getName());
			if (isStringPath) {
				pathExpression.setName(ast.newSimpleName("get")); //$NON-NLS-1$
				String typeNamePaths = findTypeNameForStaticMethodInvocation(PATHS_QUALIFIED_NAME);
				pathExpression.setExpression(ast.newName(typeNamePaths));
				@SuppressWarnings("unchecked")
				List<Expression> pathsGetterInvocationArguments = pathExpression.arguments();
				pathsGetterInvocationArguments.add(directoryCopyTarget);
			} else {
				// assumed that directoryPath is an instance of java.io.File
				pathExpression.setName(ast.newSimpleName("toPath")); //$NON-NLS-1$
				pathExpression.setExpression(directoryCopyTarget);
			}
			createTempFileArguments.add(pathExpression);
		}
		createTempFileArguments.add((Expression) astRewrite.createCopyTarget(data.getFilePrefix()));
		createTempFileArguments.add((Expression) astRewrite.createCopyTarget(data.getFileSuffix()));

		MethodInvocation toFileInvocation = ast.newMethodInvocation();
		toFileInvocation.setName(ast.newSimpleName("toFile")); //$NON-NLS-1$
		toFileInvocation.setExpression(createTempFileInvocation);

		astRewrite.replace(replacedCreateTempFileInvocation, toFileInvocation, null);
		onRewrite();
	}

	private String findTypeNameForStaticMethodInvocation(String qualifiedName) {
		if (!safeTypeImports.contains(qualifiedName)) {
			return qualifiedName;
		}
		if (!typesImportedOnDemand.contains(qualifiedName)) {
			addImports.add(qualifiedName);
		}
		return getSimpleName(qualifiedName);
	}

	private class TransformationData {
		private final Expression filePrefix;
		private final Expression fileSuffix;
		private final Optional<Expression> directory;

		private TransformationData(Expression directoryPath, Expression filePrefix, Expression fileSuffix) {
			this.filePrefix = filePrefix;
			this.fileSuffix = fileSuffix;
			this.directory = Optional.of(directoryPath);
		}

		private TransformationData(Expression filePrefix, Expression fileSuffix) {
			this.filePrefix = filePrefix;
			this.fileSuffix = fileSuffix;
			this.directory = Optional.empty();
		}

		public Optional<Expression> getDirectory() {
			return directory;
		}

		public Expression getFilePrefix() {
			return filePrefix;
		}

		public Expression getFileSuffix() {
			return fileSuffix;
		}
	}

}