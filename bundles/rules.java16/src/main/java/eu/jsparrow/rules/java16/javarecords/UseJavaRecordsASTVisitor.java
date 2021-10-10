package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.4.0
 */
public class UseJavaRecordsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(TypeDeclaration instanceOf) {
		return true;
	}
}
