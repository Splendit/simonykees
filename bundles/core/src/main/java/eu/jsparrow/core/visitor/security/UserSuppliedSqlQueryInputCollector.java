package eu.jsparrow.core.visitor.security;

public class UserSuppliedSqlQueryInputCollector extends UserSuppliedInputCollector {

	protected static final String SIMPLE_QUOTATION_MARK = "'"; //$NON-NLS-1$

	public UserSuppliedSqlQueryInputCollector() {
		super(true, s -> s.endsWith(SIMPLE_QUOTATION_MARK), s -> s.startsWith(SIMPLE_QUOTATION_MARK));
	}

}
