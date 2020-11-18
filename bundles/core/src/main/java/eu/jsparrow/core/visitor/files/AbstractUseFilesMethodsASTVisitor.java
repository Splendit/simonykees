package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Extended by visitors which carry out simplifications of code in connection
 * with file manipulations by introducing invocations of static file
 * manipulation methods declared in the class {@link java.nio.file.Files}.
 * 
 *
 */
abstract class AbstractUseFilesMethodsASTVisitor extends AbstractAddImportASTVisitor {
	private static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}
		verifyImport(compilationUnit, PATHS_QUALIFIED_NAME);
		return continueVisiting;
	}

	protected MethodInvocation createPathsGetInvocation(TransformationData transformationData, AST ast) {
		MethodInvocation pathsGet = ast.newMethodInvocation();
		Name pathsTypeName = addImport(PATHS_QUALIFIED_NAME, transformationData.getBufferedIOInstanceCreation());
		pathsGet.setExpression(pathsTypeName);
		pathsGet.setName(ast.newSimpleName("get")); //$NON-NLS-1$
		@SuppressWarnings("unchecked")
		List<Expression> pathsGetParameters = pathsGet.arguments();
		List<Expression> pathExpressions = transformationData.getPathExpressions();
		pathExpressions
			.forEach(pathArgument -> pathsGetParameters.add((Expression) astRewrite.createCopyTarget(pathArgument)));
		return pathsGet;
	}

}
