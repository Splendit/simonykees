package eu.jsparrow.core.visitor.security;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor looks for invocations of
 * <ul>
 * <li>{@link java.io.File#createTempFile(String, String)} and</li>
 * <li>{@link java.io.File#createTempFile(String, String, java.io.File)}</li>
 * </ul>
 * and replaces them by invocations of the corresponding methods of the class
 * {@link java.nio.file.Files}.
 * 
 * @since 3.22.0
 *
 */
public class CreateTempFilesUsingJavaNioASTVisitor extends AbstractAddImportASTVisitor {
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

	private class TransformationData {
		private final Expression filePrefix;
		private final Expression fileSuffix;
		private final Expression directoryPath;

		private TransformationData(Expression directoryPath, Expression filePrefix, Expression fileSuffix) {
			super();
			this.filePrefix = filePrefix;
			this.fileSuffix = fileSuffix;
			this.directoryPath = directoryPath;
		}
	}

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);
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
		return true;
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		safeTypeImports.clear();
		typesImportedOnDemand.clear();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		TransformationData data = createTransformationData(node);
		if (data != null) {
			transform(node, data);
		}
		return true;
	}

	private TransformationData createTransformationData(MethodInvocation node) {
		SignatureData signature;
		IMethodBinding methodBinding = node.resolveMethodBinding();

		if (createTempFile.isEquivalentTo(methodBinding)) {
			signature = createTempFile;
		} else if (createTempFileWithDirectory.isEquivalentTo(methodBinding)) {
			signature = createTempFileWithDirectory;
		} else {
			return null;
		}

		List<Expression> createTempFileArguments = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class);
		Expression filePrefix = createTempFileArguments.get(0);
		Expression fileSuffix = createTempFileArguments.get(1);
		if (signature == createTempFile) {
			return new TransformationData(null, filePrefix, fileSuffix);
		}

		Expression directory = createTempFileArguments
			.get(2);

		if (directory.getNodeType() == ASTNode.NULL_LITERAL) {
			return new TransformationData(null, filePrefix, fileSuffix);
		}

		if (directory.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return null;
		}
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) directory;
		IMethodBinding constructorBinding = classInstanceCreation.resolveConstructorBinding();
		if (!newFileFromPath.isEquivalentTo(constructorBinding)) {
			return null;
		}
		Expression directoryPath = ASTNodeUtil
			.convertToTypedList(classInstanceCreation.arguments(), Expression.class)
			.get(0);

		return new TransformationData(directoryPath, filePrefix, fileSuffix);

	}

	private void transform(MethodInvocation replacedCreateTempFileInvocation, TransformationData data) {

		AST ast = replacedCreateTempFileInvocation.getAST();
		MethodInvocation createTempFileInvocation = ast.newMethodInvocation();
		createTempFileInvocation.setName(ast.newSimpleName(CREATE_TEMP_FILE));
		String typeNameFiles = findTypeNameForStaticMethodInvocation(FILES_QUALIFIED_NAME);
		createTempFileInvocation.setExpression(ast.newName(typeNameFiles));

		@SuppressWarnings("unchecked")
		List<Expression> createTempFileArguments = createTempFileInvocation.arguments();
		if (data.directoryPath != null) {

			MethodInvocation pathsGetterInvocation = ast.newMethodInvocation();
			pathsGetterInvocation.setName(ast.newSimpleName("get")); //$NON-NLS-1$
			String typeNamePaths = findTypeNameForStaticMethodInvocation(PATHS_QUALIFIED_NAME);
			pathsGetterInvocation.setExpression(ast.newName(typeNamePaths));
			@SuppressWarnings("unchecked")
			List<Expression> pathsGetterInvocationArguments = pathsGetterInvocation.arguments();
			pathsGetterInvocationArguments.add((Expression) astRewrite.createCopyTarget(data.directoryPath));
			createTempFileArguments.add((Expression) pathsGetterInvocation);
		}
		createTempFileArguments.add((Expression) astRewrite.createCopyTarget(data.filePrefix));
		createTempFileArguments.add((Expression) astRewrite.createCopyTarget(data.fileSuffix));

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

}
