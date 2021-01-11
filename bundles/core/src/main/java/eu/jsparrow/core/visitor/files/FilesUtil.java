package eu.jsparrow.core.visitor.files;

/**
 * Declares constants referenced by multiple classes, for example:
 * 
 * <ul>
 * <li>{@link eu.jsparrow.core.visitor.files.AbstractUseFilesBufferedIOMethodsASTVisitor}</li>
 * <li>{@link eu.jsparrow.core.visitor.files.UseFilesBufferedWriterASTVisitor}</li>
 * <li>{@link eu.jsparrow.core.visitor.files.UseFilesWriteStringASTVisitor}</li>
 * </ul>
 *
 */
class FilesUtil {
	static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();
	static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	static final String GET = "get"; //$NON-NLS-1$
	static final String DEFAULT_CHARSET = "defaultCharset"; //$NON-NLS-1$

	private FilesUtil() {
		/*
		 * Hide default constructor.
		 */
	}
}
