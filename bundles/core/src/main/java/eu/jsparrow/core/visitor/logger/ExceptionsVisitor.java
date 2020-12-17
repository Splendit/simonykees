package eu.jsparrow.core.visitor.logger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

class ExceptionsVisitor extends ASTVisitor {
	private List<Expression> foundExceptions = new ArrayList<>();

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding != null && IBinding.VARIABLE == binding.getKind()) {
			ITypeBinding typeBinding = simpleName.resolveTypeBinding();
			storeIfExceptionType(typeBinding, simpleName);
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation classCreation) {
		ITypeBinding typeBinding = classCreation.resolveTypeBinding();
		storeIfExceptionType(typeBinding, classCreation);
		return true;
	}

	private void storeIfExceptionType(ITypeBinding typeBinding, Expression node) {
		if (typeBinding != null
				&& (ClassRelationUtil.isContentOfTypes(typeBinding, StandardLoggerASTVisitor.exceptionQualifiedName)
						|| ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
								StandardLoggerASTVisitor.exceptionQualifiedName))) {
			foundExceptions.add(node);
		}
	}

	public List<Expression> getExceptions() {
		return this.foundExceptions;
	}
}