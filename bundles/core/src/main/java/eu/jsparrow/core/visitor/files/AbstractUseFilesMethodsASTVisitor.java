package eu.jsparrow.core.visitor.files;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Extended by visitors which carry out simplifications of code in connection
 * with file manipulations by introducing invocations of static file
 * manipulation methods declared in the class {@link java.nio.file.Files}.
 * 
 *
 */
abstract class AbstractUseFilesMethodsASTVisitor extends AbstractAddImportASTVisitor {

}
