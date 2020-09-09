package eu.jsparrow.core.visitor.impl;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor looks for invocations of
 * {@link java.nio.charset.Charset#forName(String)} and replaces them by the
 * corresponding reference to a constant in
 * {@link java.nio.charset.StandardCharsets}.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 * Charset c = Charset.forName("UTF-8");
 * </pre>
 * <p>
 * is replaced by
 * <p>
 * 
 * <pre>
 * Charset c = StandardCharsets.UTF_8;
 * </pre>
 * 
 * @since 3.21.0
 *
 */
public class UsePredefinedStandardCharsetASTVisitor extends AbstractAddImportASTVisitor {
	private static final String STANDARD_CHARSETS_QUALIFIED_NAME = java.nio.charset.StandardCharsets.class.getName();
	private static final Class<String> STRING = java.lang.String.class;
	private static final Class<Charset> CHARSET = java.nio.charset.Charset.class;
	private final SignatureData charsetForName = new SignatureData(CHARSET, "forName", STRING); //$NON-NLS-1$
	@SuppressWarnings("nls")
	private final Map<String, String> charsetConstants = Collections.unmodifiableMap(
			Stream.of(
					"UTF-8",
					"ISO-8859-1",
					"US-ASCII",
					"UTF-16",
					"UTF-16BE",
					"UTF-16LE")
				.collect(Collectors.toMap(s -> s, s -> s.replace('-', '_'))));

	private boolean safeImportStandardCharsets;

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);

		safeImportStandardCharsets = isSafeToAddImport(node, STANDARD_CHARSETS_QUALIFIED_NAME);
		return true;
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		safeImportStandardCharsets = false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (!charsetForName.isEquivalentTo(node.resolveMethodBinding())) {
			return true;
		}
		Expression charsetArgument = ASTNodeUtil.returnTypedList(node.arguments(), Expression.class)
			.get(0);
		if (charsetArgument.getNodeType() != ASTNode.STRING_LITERAL) {
			return true;
		}
		String constantKey = ((StringLiteral) charsetArgument).getLiteralValue();
		if (!charsetConstants.containsKey(constantKey)) {
			return true;
		}
		transform(node, charsetConstants.get(constantKey));
		return true;
	}

	private void transform(MethodInvocation forNameInvocation, String charsetConstantIdentifier) {
		AST ast = forNameInvocation.getAST();

		String typeNameStandardCharsets;
		if (safeImportStandardCharsets) {
			typeNameStandardCharsets = java.nio.charset.StandardCharsets.class.getSimpleName();
			addImports.add(STANDARD_CHARSETS_QUALIFIED_NAME);
		} else {
			typeNameStandardCharsets = STANDARD_CHARSETS_QUALIFIED_NAME;
		}
		SimpleName charsetConstantSimpleName = ast.newSimpleName(charsetConstantIdentifier);
		QualifiedName charsetConstantQualifiedName = ast.newQualifiedName(ast.newName(typeNameStandardCharsets),
				charsetConstantSimpleName);
		this.astRewrite.replace(forNameInvocation, charsetConstantQualifiedName, null);
		onRewrite();
	}
}
