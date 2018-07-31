package eu.jsparrow.core.visitor.renaming;

/**
 * Keys for each option of {@link FieldDeclarationASTVisitor}
 * 
 * @author Ardit Ymeri
 * @see 2.3.1
 *
 */
public final class FieldDeclarationOptionKeys {
	
	public static final String RENAME_PUBLIC_FIELDS = "public"; //$NON-NLS-1$
	public static final String RENAME_PRIVATE_FIELDS = "private"; //$NON-NLS-1$
	public static final String RENAME_PROTECTED_FIELDS = "protected"; //$NON-NLS-1$
	public static final String RENAME_PACKAGE_PROTECTED_FIELDS = "package-protected"; //$NON-NLS-1$
	public static final String UPPER_CASE_FOLLOWING_DOLLAR_SIGN = "uppercase-after-dollar"; //$NON-NLS-1$
	public static final String UPPER_CASE_FOLLOWING_UNDERSCORE = "uppercase-after-underscore"; //$NON-NLS-1$
	public static final String ADD_COMMENT = "add-todo"; //$NON-NLS-1$
	private FieldDeclarationOptionKeys() {
		/*
		 * Hiding default constructor
		 */
	}
}
