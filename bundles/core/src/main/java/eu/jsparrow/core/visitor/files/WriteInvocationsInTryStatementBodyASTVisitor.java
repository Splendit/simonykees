package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Needed for temporary workaround where try statements like the following are
 * not transformed:
 * 
 * <pre>
 * try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs);
 * 		BufferedWriter bufferedWriter2 = Files.newBufferedWriter(path, cs)) {
 * 	bufferedWriter.write(value);
 * 	bufferedWriter2.write(value);
 * } catch (Exception exception) {
 * }
 * </pre>
 * 
 *
 */
public class WriteInvocationsInTryStatementBodyASTVisitor extends ASTVisitor {
	private final List<MethodInvocation> writeMethodInvocations = new ArrayList<>();

	@Override
	public boolean visit(MethodInvocation invocation) {

		if (invocation.getName()
			.getIdentifier()
			.equals("write")) { //$NON-NLS-1$
			ITypeBinding declaringClass = invocation.resolveMethodBinding()
				.getDeclaringClass();
			boolean isType = false;
			boolean isInheritingContentOfTypes = false;
			if (ClassRelationUtil.isContentOfType(declaringClass, java.io.Writer.class.getName())) {
				isType = true;
			}
			if (ClassRelationUtil.isInheritingContentOfTypes(declaringClass,
					Collections.singletonList(java.io.Writer.class.getName()))) {
				isInheritingContentOfTypes = true;
			}
			if (isType || isInheritingContentOfTypes) {
				writeMethodInvocations.add(invocation);
			}

		}
		return true;
	}

	List<MethodInvocation> getWriteMethodInvocations() {
		return writeMethodInvocations;
	}
}
