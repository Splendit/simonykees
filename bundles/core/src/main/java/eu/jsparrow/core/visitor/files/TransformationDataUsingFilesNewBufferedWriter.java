package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Stores all Data needed necessary for a transformation of code in connection
 * with invocations of <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset, java.nio.file.OpenOption...)}
 * or <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)}.
 *
 */
class TransformationDataUsingFilesNewBufferedWriter {
	private final VariableDeclarationExpression resourceToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final List<Expression> argumentsToCopy;

	TransformationDataUsingFilesNewBufferedWriter(
			VariableDeclarationExpression resourceToRemove,
			ExpressionStatement writeInvocationStatementToReplace,
			List<Expression> argumentsToCopy) {
		this.resourceToRemove = resourceToRemove;
		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.argumentsToCopy = argumentsToCopy;
	}
	
	static Optional<TransformationDataUsingFilesNewBufferedWriter> findTransformationData(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			MethodInvocation bufferedIOInitializerMethodInvocation,
			VariableDeclarationExpression resourceToRemove) {

		IMethodBinding methodBinding = bufferedIOInitializerMethodInvocation.resolveMethodBinding();

		if (!ClassRelationUtil.isContentOfType(methodBinding
			.getDeclaringClass(), java.nio.file.Files.class.getName())) {
			return Optional.empty();
		}
		if (!Modifier.isStatic(methodBinding.getModifiers())) {
			return Optional.empty();
		}
		if (!methodBinding.getName()
			.equals("newBufferedWriter")) { //$NON-NLS-1$
			return Optional.empty();
		}
		if (!checkFilesNewBufferedWriterParameterTypes(methodBinding)) {
			return Optional.empty();
		}

		List<Expression> argumentsToCopy = ASTNodeUtil.convertToTypedList(
				bufferedIOInitializerMethodInvocation.arguments(),
				Expression.class);

		argumentsToCopy.add(1, charSequenceArgument);

		return Optional.of(
				new TransformationDataUsingFilesNewBufferedWriter(resourceToRemove,
						writeInvocationStatementToReplace, argumentsToCopy));

	}

	private static boolean checkFilesNewBufferedWriterParameterTypes(IMethodBinding methodBinding) {
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		if (parameterTypes.length == 2) {
			if (!ClassRelationUtil.isContentOfType(parameterTypes[0], java.nio.file.Path.class.getName())) {
				return false;
			}
			return ClassRelationUtil.isContentOfType(parameterTypes[1].getElementType(),
					java.nio.file.OpenOption.class.getName());
		} else if (parameterTypes.length == 3) {
			if (!ClassRelationUtil.isContentOfType(parameterTypes[0], java.nio.file.Path.class.getName())) {
				return false;
			}
			if (!ClassRelationUtil.isContentOfType(parameterTypes[1], java.nio.charset.Charset.class.getName())) {
				return false;
			}
			return ClassRelationUtil.isContentOfType(parameterTypes[2].getElementType(),
					java.nio.file.OpenOption.class.getName());
		}
		return false;
	}

	VariableDeclarationExpression getResourceToRemove() {
		return resourceToRemove;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	List<Expression> getArgumentsToCopy() {
		return argumentsToCopy;
	}
}
