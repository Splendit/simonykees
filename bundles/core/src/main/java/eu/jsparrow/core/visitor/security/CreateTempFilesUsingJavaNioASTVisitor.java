package eu.jsparrow.core.visitor.security;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor looks for invocations of
 * <ul>
 * <li>{@link java.io.File#createTempFile(String, String)} and</li>
 * <li>{@link java.io.File#createTempFile(String, String, java.io.File)}</li>
 * </ul>
 * and replaces them by invocations of the corresponding methods of the class
 * {@link java.nio.file.Files}.
 * 
 * @since 3.22.0
 *
 */
public class CreateTempFilesUsingJavaNioASTVisitor extends AbstractAddImportASTVisitor {

}
