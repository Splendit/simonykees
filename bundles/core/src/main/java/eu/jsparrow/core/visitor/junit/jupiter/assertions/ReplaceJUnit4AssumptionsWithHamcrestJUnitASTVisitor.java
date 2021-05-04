package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Replaces the JUnit 4 method invocations
 * {@code org.junit.Assume.assumeNoException},
 * {@code org.junit.Assume.assumeNotNull} and
 * {@code org.junit.Assume.assumeThat} by invocations of the corresponding
 * methods of {@code org.hamcrest.junit.MatcherAssume.assumeThat}.
 * 
 * @since 3.31.0
 * 
 */
public class ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor
		extends AbstractReplaceJUnit4MethodInvocationsASTVisitor {
	
	public ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor() {
		super("org.junit.jupiter.api.Assumptions"); //$NON-NLS-1$
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);

		JUnit4MethodInvocationAnalysisResultStore transformationDataStore = createTransformationDataStore(compilationUnit);
		List<ImportDeclaration> staticMethodImportsToRemove = collectStaticMethodImportsToRemove(compilationUnit,
				transformationDataStore);
		
		Set<String> supportedNewStaticMethodImports = findSupportedStaticImports(staticMethodImportsToRemove,
				transformationDataStore);

		return true;
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assume")) {//$NON-NLS-1$
			String methodName = methodBinding.getName();
			return methodName.equals("assumeNoException") || //$NON-NLS-1$
					methodName.equals("assumeNotNull") || //$NON-NLS-1$
					methodName.equals("assumeThat"); //$NON-NLS-1$
		}
		return false;
	}
}