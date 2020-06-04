package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * A helper visitor for analyzing the creation and references of a
 * {@link java.sql.Statement}.
 * 
 * @since 3.16.0
 *
 */
public class SqlStatementAnalyzerVisitor extends AbstractLocalVariableUsageASTVisitor {

	private MethodInvocation getResultSetInvocation;

	public SqlStatementAnalyzerVisitor(SimpleName sqlStatement, MethodInvocation methodInvocation) {
		super(sqlStatement, methodInvocation);
	}

	private MethodInvocation findGetResultSet(SimpleName simpleName) {
		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if (structuralDescriptor == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) simpleName.getParent();
			SimpleName methodName = methodInvocation.getName();
			if ("getResultSet".equals(methodName.getIdentifier())) { //$NON-NLS-1$
				return methodInvocation;
			}
		}
		return null;
	}
	
	@Override
	protected boolean isOtherUnsafeVariableReference(SimpleName simpleName) {
		MethodInvocation getResultSet = findGetResultSet(simpleName);
		if (getResultSet != null) {
			if (this.getResultSetInvocation == null) {
				this.getResultSetInvocation = getResultSet;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return the first invocation of {@link java.sql.Statement#getResultSet()}
	 *         after invoking {@link java.sql.Statement#execute(String)}. In
	 *         case more than one invocation of
	 *         {@link java.sql.Statement#getResultSet()} occurs, then the flag
	 *         {@link #unsafe} will be set to {@code true}.
	 */
	public MethodInvocation getGetResultSetInvocation() {
		return this.getResultSetInvocation;
	}

}
