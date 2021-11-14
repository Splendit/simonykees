package org.eu.jsparrow.rules.java16.javarecords;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class AbstractUseJavaRecordsTest extends UsesJDTUnitFixture {

	@Override
	protected void assertChange(String actual, String expected)
			throws JdtUnitException, JavaModelException, BadLocationException {
		List<String> modifiers = Arrays.asList("public"); //$NON-NLS-1$
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual, modifiers);
		AbstractASTRewriteASTVisitor defaultVisitor = getDefaultVisitor();
		defaultVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(defaultVisitor);

		TypeDeclaration expectedNode = ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				expected, modifiers);
		TypeDeclaration actualNode = defaultFixture.getTypeDeclaration();
		assertEquals(expectedNode.toString(), actualNode.toString());
	}
}
