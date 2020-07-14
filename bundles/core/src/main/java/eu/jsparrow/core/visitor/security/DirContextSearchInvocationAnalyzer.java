package eu.jsparrow.core.visitor.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Determines whether a method invocation references one of the following
 * methods:
 * <p>
 * {@link javax.naming.directory.DirContext#search(java.lang.String, java.lang.String, javax.naming.directory.SearchControls)}
 * <p>
 * or
 * <p>
 * {@link javax.naming.directory.DirContext#search(javax.naming.Name, java.lang.String, javax.naming.directory.SearchControls)}
 *
 * @since 3.19.0
 *
 */
@SuppressWarnings("nls")
public class DirContextSearchInvocationAnalyzer extends AbstractMethodInvocationAnalyzer {

	private static final List<String> OVERLOAD_USING_NAME_AS_NAME = Collections
		.unmodifiableList(Arrays.asList(javax.naming.Name.class.getName(), java.lang.String.class.getName(),
				javax.naming.directory.SearchControls.class.getName()));

	private static final List<String> OVERLOAD_USING_NAME_AS_STRING = Collections
		.unmodifiableList(
				Arrays.asList(java.lang.String.class.getName(), java.lang.String.class.getName(),
						javax.naming.directory.SearchControls.class.getName()));

	private static final String SEARCH = "search";

	public DirContextSearchInvocationAnalyzer(MethodInvocation methodInvocation) {
		super(methodInvocation);
	}

	public boolean analyze() {

		if (analyze(javax.naming.directory.DirContext.class.getName(), SEARCH, OVERLOAD_USING_NAME_AS_NAME)) {
			return true;
		}
		return analyze(javax.naming.directory.DirContext.class.getName(), SEARCH, OVERLOAD_USING_NAME_AS_STRING);
	}

	/**
	 * 
	 * @return If {@link #analyze()} has returned true, then it is guaranteed
	 *         that this method will return a valid filter expression which is
	 *         always the second argument in an LDAP context search invocation.
	 * 
	 */
	public Expression getFilterExpression() {
		return getArguments().get(1);
	}

}
