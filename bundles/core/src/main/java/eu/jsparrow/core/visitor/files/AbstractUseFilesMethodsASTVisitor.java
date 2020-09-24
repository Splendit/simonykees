package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Parent class for visitors which transform code in order to use methods of the
 * class {@link java.nio.file.Files} for the creation of objects like for
 * example instances of <br>
 * {@link java.io.BufferedReader}<br>
 * or <br>
 * {@link java.io.BufferedWriter}
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

		List<Expression> newBufferedReaderArgs = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(),
				Expression.class);
		if (newBufferedReaderArgs.size() != 1) {
			return null;
		}
		Expression bufferedReaderArg = newBufferedReaderArgs.get(0);
		ITypeBinding firstArgType = bufferedReaderArg.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgType, qualifiedTypeName)) {
			return null;
		}
		return bufferedReaderArg;
	}
	
	protected Expression createDefaultCharsetExpression(AST ast) {
		MethodInvocation defaultCharset = ast.newMethodInvocation();
		defaultCharset.setExpression(ast.newName(findTypeNameForStaticMethodInvocation(CHARSET_QUALIFIED_NAME)));
		defaultCharset.setName(ast.newSimpleName("defaultCharset")); //$NON-NLS-1$
		return defaultCharset;
	}

	protected MethodInvocation createFilesNewBufferedIOMethodInvocation(AST ast, List<Expression> pathExpressions,
			Expression charset, String newBufferdIOMethodName) {
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
