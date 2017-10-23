package eu.jsparrow.core.visitor.logger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.util.ClassRelationUtil;

class ExceptionsASTVisitor extends ASTVisitor {
	private List<ASTNode> foundExceptions = new ArrayList<>();

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

	private void storeIfExceptionType(ITypeBinding typeBinding, ASTNode node) {
		if (typeBinding != null
				&& (ClassRelationUtil.isContentOfTypes(typeBinding, StandardLoggerASTVisitor.exceptionQualifiedName)
						|| ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
								StandardLoggerASTVisitor.exceptionQualifiedName))) {
			foundExceptions.add(node);
		}
	}

	public List<ASTNode> getExceptions() {
		return this.foundExceptions;
	}
}