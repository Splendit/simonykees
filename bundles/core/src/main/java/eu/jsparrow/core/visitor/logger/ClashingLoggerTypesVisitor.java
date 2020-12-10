package eu.jsparrow.core.visitor.logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Checks for occurrences of {@link SimpleType}s with name
 * {@value #LOGGER_CLASS_NAME}, {@value #SLF4J_LOGGER_FACTORY} or
 * {@value #SLF4J_LOGGER_FACTORY}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class ClashingLoggerTypesVisitor extends ASTVisitor {

	private final StandardLoggerASTVisitor standardLoggerASTVisitor;
	boolean clashingFound = false;

	/**
	 * @param standardLoggerASTVisitor
	 *            the parent ASTVisitor
	 */
	ClashingLoggerTypesVisitor(StandardLoggerASTVisitor standardLoggerASTVisitor) {
		this.standardLoggerASTVisitor = standardLoggerASTVisitor;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !clashingFound;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		String typeIdentifier = typeDeclaration.getName()
			.getIdentifier();
		if (isClashingLoggerName(typeIdentifier)) {
			clashingFound = true;
		}
		return true;
	}

	@Override
	public boolean visit(SimpleType simpleType) {
		Name typeName = simpleType.getName();

		if (typeName.isSimpleName() && isClashingLoggerName(((SimpleName) typeName).getIdentifier(), simpleType)) {
			clashingFound = true;
		}
		return true;
	}

	private boolean isClashingLoggerName(String typeIdentifier) {
		return StandardLoggerASTVisitor.LOGGER_CLASS_NAME.equals(typeIdentifier)
				|| StandardLoggerASTVisitor.LOG4J_LOGGER_MANAGER.equals(typeIdentifier)
				|| StandardLoggerASTVisitor.SLF4J_LOGGER_FACTORY.equals(typeIdentifier);
	}

	private boolean isClashingLoggerName(String typeIdentifier, SimpleType simpleType) {
		if (isClashingLoggerName(typeIdentifier)) {
			ITypeBinding typeBinding = simpleType.resolveBinding();
			if (typeBinding != null) {
				String bindingQualifiedName = typeBinding.getQualifiedName();
				return !this.standardLoggerASTVisitor.newImports.get(this.standardLoggerASTVisitor.loggerQualifiedName)
					.contains(bindingQualifiedName);
			}
		}
		return false;
	}

	public boolean isLoggerFree() {
		return !clashingFound;
	}
}