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
class FilesConstants {
	static final String STRING_QUALIFIED_NAME = java.lang.String.class.getName();
	static final String PATH_QUALIFIED_NAME = java.nio.file.Path.class.getName();
	static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();
	static final String FILE_QUALIFIED_NAME = java.io.File.class.getName();
	static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	static final String FILE_WRITER_QUALIFIED_NAME = java.io.FileWriter.class.getName();
	static final String BUFFERED_WRITER_QUALIFIED_NAME = java.io.BufferedWriter.class.getName();
	static final String GET = "get"; //$NON-NLS-1$
	static final String DEFAULT_CHARSET = "defaultCharset"; //$NON-NLS-1$

	private FilesConstants() {
		/*
		 * Hide default constructor.
		 */
	}
}
