package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * A visitor for replacing invocations of {@link java.util.Arrays#asList(Object...)}  
 * with {@link java.util.Collections#emptyList()} or {@link java.util.Collections#singletonList(Object)} 
 * in case the number of arguments is respectively 0 or 1. 
 * 
 * @since 3.7.0
 */
public class UseCollectionsSingletonListASTVisitor extends AbstractAddImportASTVisitor {
	
	private static final String COLLECTIONS = "Collections"; //$NON-NLS-1$
	private static final String AS_LIST = "asList"; //$NON-NLS-1$
	private static final String SINGLETON_LIST = "singletonList"; //$NON-NLS-1$
	private static final String EMPTY_LIST = "emptyList"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String methodIdentifier = methodName.getIdentifier();
		if(!AS_LIST.equals(methodIdentifier)) {
			return true;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if(arguments.size() > 1) {
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if(methodBinding == null) {
			return true;
		}
		
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if(!ClassRelationUtil.isContentOfType(declaringClass, java.util.Arrays.class.getName())) {
			return true;
		}
		
		AST ast = astRewrite.getAST();
		String newMethodIdentifier = arguments.isEmpty() ? EMPTY_LIST : SINGLETON_LIST;
		SimpleName newMethodName = ast.newSimpleName(newMethodIdentifier);
		SimpleName newExpressionName = ast.newSimpleName(COLLECTIONS);
		
		Expression originalExpression = methodInvocation.getExpression();
		if(originalExpression != null) {
			addImports.add(java.util.Collections.class.getName());
			astRewrite.replace(originalExpression, newExpressionName, null);
		} else {
			addStaticImport(java.util.Collections.class.getName() + "." + newMethodIdentifier); //$NON-NLS-1$
		}
		astRewrite.replace(methodName, newMethodName, null);
		onRewrite();
		
		return true;
	}

}