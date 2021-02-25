package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces invocations of methods of the JUnit-4-class {@code org.junit.Assert}
 * by invocations of the corresponding methods of the JUnit-Jupiter-class
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 * 
 */
public class ReplaceJUnit4AssertWithJupiterASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}

		List<ImportDeclaration> assertMethodStaticImportsToReplace = collectAssertMethodStaticImports(compilationUnit);
		Set<String> assertMethodStaticImportsSimpleNames = assertMethodStaticImportsToReplace.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		List<AssertTransformationData> assertTransformationDataList = createAssertInvocationTransformationDataList(
				compilationUnit);

		MethodReferenceCollectorVisitor methodReferenceCollectorVisitor = new MethodReferenceCollectorVisitor();
		compilationUnit.accept(methodReferenceCollectorVisitor);
		List<MethodReference> methodReferences = methodReferenceCollectorVisitor.getMethodReferences();

		if (!assertTransformationDataList.isEmpty() || !methodReferences.isEmpty()) {
			transform(assertTransformationDataList, methodReferences);
		}
		return false;
	}

	List<AssertTransformationData> createAssertInvocationTransformationDataList(CompilationUnit compilationUnit) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		return invocationCollectorVisitor.getMethodInvocations()
			.stream()
			.map(this::findAssertTransformationData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	private Optional<AssertTransformationData> findAssertTransformationData(MethodInvocation methodInvocation) {
		IMethodBinding methodDeclaration = methodInvocation.resolveMethodBinding()
			.getMethodDeclaration();
		if (!isSupportedJUnit4Method(methodDeclaration)) {
			return Optional.empty();
		}
		String newMethodName = isAssertEqualsComparingObjectArrays(methodDeclaration) ? "assertArrayEquals" : null; //$NON-NLS-1$

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);
		Expression assertionMessage = null;
		if (invocationArguments.size() > 0 && isParameterTypeString(methodDeclaration.getParameterTypes()[0])) {
			assertionMessage = invocationArguments.remove(0);
		}

		if (assertionMessage != null) {
			if (newMethodName != null) {
				return Optional.of(new AssertTransformationData(newMethodName, invocationArguments, assertionMessage));
			}
			return Optional.of(new AssertTransformationData(invocationArguments, assertionMessage));
		}

		if (newMethodName != null) {
			return Optional.of(new AssertTransformationData(newMethodName, invocationArguments));
		}

		return Optional.of(new AssertTransformationData(invocationArguments));
	}

	private boolean isSupportedJUnit4Method(IMethodBinding methodDeclaration) {
		if (isOrgJUnitAssertClass(methodDeclaration.getDeclaringClass())) {
			String methodName = methodDeclaration.getName();
			return !methodName.equals("assertThat") //$NON-NLS-1$
					&& !methodName.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isOrgJUnitAssertClass(ITypeBinding declaringClass) {
		return declaringClass.getQualifiedName()
			.equals("org.junit.Assert");//$NON-NLS-1$
	}

	/**
	 * This applies to the following signatures:<br>
	 * {@code assertEquals(Object[], Object[])}
	 * {@code assertEquals(String, Object[], Object[])} where a corresponding
	 * method with the name "assertArrayEquals" is available
	 * 
	 * @param methodBinding
	 * @return
	 */
	private boolean isAssertEqualsComparingObjectArrays(IMethodBinding methodBinding) {
		if (!methodBinding.getName()
			.equals("assertEquals")) { //$NON-NLS-1$
			return false;
		}
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		/*
		 * applies to {@code assertEquals(Object[], Object[])}
		 */
		if (parameterTypes.length == 2) {
			return isParameterTypeObjectArray(parameterTypes[0])
					&& isParameterTypeObjectArray(parameterTypes[1]);
		}
		/*
		 * applies to {@code assertEquals(String, Object[], Object[])}
		 */
		if (parameterTypes.length == 3) {
			return isParameterTypeString(parameterTypes[0])
					&& isParameterTypeObjectArray(parameterTypes[1])
					&& isParameterTypeObjectArray(parameterTypes[2]);
		}
		return false;
	}

	private boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		return parameterType.getComponentType()
			.getQualifiedName()
			.equals("java.lang.Object") && parameterType.getDimensions() == 1; //$NON-NLS-1$
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return parameterType.getQualifiedName()
			.equals("java.lang.String"); //$NON-NLS-1$
	}

	List<ImportDeclaration> collectAssertMethodStaticImports(CompilationUnit compilationUnit) {
		return ASTNodeUtil.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.stream()
			.filter(this::isAssertMethodStaticImport)
			.collect(Collectors.toList());
	}

	private boolean isAssertMethodStaticImport(ImportDeclaration importDeclaration) {
		if (!importDeclaration.isStatic()) {
			return false;
		}

		if (!importDeclaration.isOnDemand()) {
			return false;
		}
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = ((IMethodBinding) importBinding);
			return isSupportedJUnit4Method(methodBinding);
		}

		return false;
	}

	private void transform(List<AssertTransformationData> assertTransformationDataList,
			List<MethodReference> methodReferences) {
		// ...
	}
}
